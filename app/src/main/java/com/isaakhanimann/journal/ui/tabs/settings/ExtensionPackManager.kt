package com.isaakhanimann.journal.ui.tabs.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.isaakhanimann.journal.localization.I18n
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.localization.i18nOrDefault
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.net.URL

data class ExtensionPack(
    val registerName: String,
    val titleKey: String,
    val descriptionKey: String,
    val officalLink: String,
    val updateJsonLink: String,
    val versionName: String,
    val versionCode: Int,
    val iconPath: String? = null
)

data class ExtensionUpdateInfo(
    val versionName: String,
    val url: String
)

object ExtensionPackLoader {
    private const val EXT_DIR = "ext_packs"

    fun getInstalledPacks(context: Context): List<ExtensionPack> {
        val dir = File(context.filesDir, EXT_DIR)
        if (!dir.exists()) return emptyList()
        return dir.listFiles()?.mapNotNull { packDir ->
            val manifestFile = File(packDir, "manifest.json")
            if (!manifestFile.exists()) return@mapNotNull null
            try {
                parseManifest(manifestFile.readText(), packDir)
            } catch (_: Exception) { null }
        } ?: emptyList()
    }

    fun parseManifest(jsonText: String, packDir: File): ExtensionPack? {
        val json = JSONObject(jsonText)
        return ExtensionPack(
            registerName = json.getString("registerName"),
            titleKey = json.getString("titleTranslateable"),
            descriptionKey = json.getString("descriptionTranslateable"),
            officalLink = json.getString("officalLink"),
            updateJsonLink = json.getString("updateJsonLink"),
            versionName = json.getString("versionName"),
            versionCode = json.getInt("versionCode"),
            iconPath = json.optString("icon", null)?.let { File(packDir, it).takeIf { f -> f.exists() }?.absolutePath }
        )
    }

    suspend fun checkUpdate(updateJsonLink: String, currentVersionCode: Int): ExtensionUpdateInfo? {
        return try {
            val jsonText = URL(updateJsonLink).readText()
            val json = JSONObject(jsonText)
            val latest = json.keys().asSequence()
                .map { it.toInt() }
                .maxOrNull() ?: return null
            if (latest > currentVersionCode) {
                val info = json.getJSONObject(latest.toString())
                ExtensionUpdateInfo(
                    versionName = info.getString("versionName"),
                    url = info.getString("url")
                )
            } else null
        } catch (_: Exception) { null }
    }

    fun applyExtension(context: Context, pack: ExtensionPack) {
        val packDir = File(context.filesDir, "$EXT_DIR/${pack.registerName}")
    }

    fun deleteExtension(context: Context, registerName: String): Boolean {
        val packDir = File(context.filesDir, "$EXT_DIR/${pack.registerName}")
        return if (packDir.exists()) {
            packDir.deleteRecursively()
            //com.isaakhanimann.journal.data.substances.repositories.SubstanceRepository.triggerReload()
            //I18n.notifyOverridesChanged()
            !packDir.exists()
        } else false
    }

    fun downloadAndInstall(context: Context, pack: ExtensionPack, updateUrl: String): String {
        return try {
            val tempFile = File(context.cacheDir, "download_${pack.registerName}.zip")
            URL(updateUrl).openStream().use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            }
            val result = installPackFromZip(context, tempFile, pack.registerName)
            tempFile.delete()
            result
        } catch (e: Exception) {
            "Download failed: ${e.localizedMessage ?: "unknown error"}"
        }
    }

    private fun installPackFromZip(context: Context, zipFile: File, registerName: String): String {
        val baseDir = File(context.filesDir, EXT_DIR)
        return try{
            val targetDir = File(baseDir, registerName)
            val backupDir = File(baseDir, ".${registerName}_bak")

            // Backup existing
            if (targetDir.exists()) {
                targetDir.renameTo(backupDir)
            }

            // Extract
            java.util.zip.ZipFile(zipFile).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val outFile = File(targetDir, entry.name)
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        zip.getInputStream(entry).use { input ->
                            outFile.outputStream().use { output -> input.copyTo(output) }
                        }
                    }
                }
            }

            // Verify
            if (!File(targetDir, "manifest.json").exists()) {
                throw Exception("manifest.json missing")
            }

            // Success - remove backup
            if (backupDir.exists()) backupDir.deleteRecursively()

            // Apply
            val manifestFile = File(targetDir, "manifest.json")
            val pack = parseManifest(manifestFile.readText(), targetDir)
            if (pack != null) applyExtension(context, pack)

            return "Installed: ${registerName}"
        } catch (e: Exception) {
            // Rollback
            val targetDir2 = File(baseDir, registerName)
            val backupDir2 = File(baseDir, ".${registerName}_bak")
            targetDir2.deleteRecursively()
            if (backupDir2.exists()) backupDir2.renameTo(targetDir2)
            "Install failed: ${e.localizedMessage ?: "unknown error"}"
        }
    }

    fun getExtensionSubstanceDir(context: Context): File? {
        val dir = File(context.filesDir, EXT_DIR)
        if (!dir.exists()) return null
        val substancesDir = File(dir, "substances")
        return substancesDir.takeIf { it.exists() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionPackScreen(navigateBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var refreshKey by remember { mutableStateOf(0) }
    var packs by remember { mutableStateOf(ExtensionPackLoader.getInstalledPacks(context)) }

    LaunchedEffect(refreshKey) {
        packs = ExtensionPackLoader.getInstalledPacks(context)
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val resultMsg = ExtensionPackImporter.import(context, uri)
            scope.launch {
                snackbarHostState.showSnackbar(resultMsg)
            }
            packs = ExtensionPackLoader.getInstalledPacks(context)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(i18n("settings_extension_pack")) },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },

                actions = {
                    IconButton(onClick = { importLauncher.launch(arrayOf("application/zip")) }) {
                        Icon(Icons.Outlined.FileDownload, "Import")
                    }
                }
            )
        }

    ) { padding ->
        if (packs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(i18n("extension_no_packs"), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                items(packs, key = { it.registerName }) { pack ->
                    ExtensionPackRow(pack = pack, context = context，scope = scope, snackbarHostState = snackbarHostState)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ExtensionPackRow(pack: ExtensionPack, context: Context, scope: kotlinx.coroutines.CoroutineScope, snackbarHostState: SnackbarHostState, onRefresh: () -> Unit) {
    var checkingUpdate by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<ExtensionUpdateInfo?>(null) }

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (pack.iconPath != null) {
                AsyncImage(
                    model = java.io.File(context.filesDir, "ext_packs/${pack.registerName}/${pack.iconPath}"),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Extension,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = i18nOrDefault(pack.titleKey, pack.titleKey),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = i18nOrDefault(pack.descriptionKey, pack.descriptionKey),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "v${pack.versionName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pack.officalLink))
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Outlined.OpenInNew, contentDescription = i18n("extension_open_link"), modifier = Modifier.size(20.dp))
                    }
                    IconButton(
                        onClick = {
                            if (!checkingUpdate) {
                                checkingUpdate = true
                                scope.launch {
                                    val info = ExtensionPackLoader.checkUpdate(pack.updateJsonLink, pack.versionCode)
                                    if (info != null) {
                                        updateInfo = info
                                    } else {
                                        snackbarHostState.showSnackbar("No update available")
                                    }
                                    checkingUpdate = false
                                }
                            }
                        },
                        enabled = !checkingUpdate
                    ) {
                        if (checkingUpdate) {
                            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                if (updateInfo != null) Icons.Outlined.SystemUpdateAlt else Icons.Outlined.Update,
                                contentDescription = i18n("extension_check_update"),
                                modifier = Modifier.size(20.dp),
                                tint = if (updateInfo != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (updateInfo != null) {
                        IconButton(onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Downloading...")
                                val result = ExtensionPackLoader.downloadAndInstall(context, pack, updateInfo!!.url)
                                snackbarHostState.showSnackbar(result)
                                onRefresh()
                            }
                        }) {
                            Icon(Icons.Outlined.FileDownload, contentDescription = i18n("extension_download"), modifier = Modifier.size(20.dp))
                        }
                    }
                    IconButton(onClick = {
                        scope.launch {
                            if (ExtensionPackLoader.deleteExtension(context, pack.registerName)) {
                                snackbarHostState.showSnackbar("Deleted: " + pack.registerName)
                                onRefresh()
                            } else {
                                snackbarHostState.showSnackbar("Delete failed")
                            }
                        }
                    }) {
                        Icon(Icons.Outlined.Delete, contentDescription = i18n("common_delete"), modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
