package com.isaakhanimann.journal.ui.tabs.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.isaakhanimann.journal.data.substances.repositories.SubstanceEvents
import com.isaakhanimann.journal.localization.I18n
import com.isaakhanimann.journal.localization.i18n
import com.isaakhanimann.journal.localization.i18nOrDefault
import java.io.File
import java.net.URL
import kotlinx.coroutines.launch
import org.json.JSONObject

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

data class ExtensionUpdateInfo(val versionName: String, val url: String, val sha256: String)

private const val MAX_TOTAL_UNCOMPRESSED_BYTES = 100L * 1024 * 1024
private const val MAX_ENTRY_COUNT = 2000
private val PACK_NAME_REGEX = Regex("^[A-Za-z0-9_-]+$")

fun isValidRegisterName(registerName: String): Boolean =
    PACK_NAME_REGEX.matches(registerName)

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
            } catch (_: Exception) {
                null
            }
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
            iconPath = json.optString("icon")
        )
    }

    suspend fun checkUpdate(updateJsonLink: String, currentVersionCode: Int): ExtensionUpdateInfo? {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val url = URL(updateJsonLink)
                if (url.protocol != "https") return@withContext null
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                // Never follow redirects: a https->http redirect would silently downgrade the channel.
                connection.instanceFollowRedirects = false
                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonText)
                val latest = json.keys().asSequence()
                    .mapNotNull { it.toIntOrNull() }
                    .maxOrNull() ?: return@withContext null
                if (latest > currentVersionCode) {
                    val info = json.getJSONObject(latest.toString())
                    // Refuse unsigned updates: the sha256 field is mandatory in the new protocol.
                    if (!info.has("sha256")) return@withContext null
                    ExtensionUpdateInfo(
                        versionName = info.getString("versionName"),
                        url = info.getString("url"),
                        sha256 = info.getString("sha256")
                    )
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    fun applyExtension(context: Context, pack: ExtensionPack) {
        val packDir = File(context.filesDir, "$EXT_DIR/${pack.registerName}")

        SubstanceEvents.notifySubstanceReload()
        I18n.markDirty()
    }

    fun deleteExtension(context: Context, registerName: String): Boolean {
        if (!isValidRegisterName(registerName)) return false
        val packDir = File(context.filesDir, "$EXT_DIR/$registerName")
        return if (packDir.exists()) {
            packDir.deleteRecursively()

            SubstanceEvents.notifySubstanceReload()
            I18n.markDirty()

            !packDir.exists()
        } else {
            false
        }
    }

    suspend fun downloadAndInstall(
        context: Context,
        pack: ExtensionPack,
        updateInfo: ExtensionUpdateInfo
    ): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val tempFile = File(context.cacheDir, "download_${pack.registerName}.zip")
        try {
            val url = URL(updateInfo.url)
            if (url.protocol != "https") return@withContext "Download failed: https required"
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            // Never follow redirects: a https->http redirect would silently downgrade the channel.
            connection.instanceFollowRedirects = false
            connection.inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    // Stream with a running size check so an oversized download aborts
                    // before the whole file is written.
                    val buffer = ByteArray(8192)
                    var total = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        total += read
                        if (total > MAX_TOTAL_UNCOMPRESSED_BYTES) {
                            throw Exception("Download too large")
                        }
                        output.write(buffer, 0, read)
                    }
                }
            }
            // Verify integrity before installing anything.
            val actualSha256 = tempFile.inputStream().use { input ->
                java.security.MessageDigest.getInstance("SHA-256").digest(input.readBytes())
                    .joinToString("") { "%02x".format(it) }
            }
            if (!actualSha256.equals(updateInfo.sha256, ignoreCase = true)) {
                return@withContext "Download failed: checksum mismatch"
            }
            installPackFromZip(context, tempFile, pack.registerName)
        } catch (e: Exception) {
            "Download failed: ${e.localizedMessage ?: "unknown error"}"
        } finally {
            tempFile.delete()
        }
    }

    private fun installPackFromZip(context: Context, zipFile: File, registerName: String): String {
        val baseDir = File(context.filesDir, EXT_DIR)
        return try {
            val targetDir = File(baseDir, registerName)
            val backupDir = File(baseDir, ".${registerName}_bak")

            // Backup existing
            if (targetDir.exists()) {
                targetDir.renameTo(backupDir)
            }

            try {
                // Refuse downgrades/reinstalls of the same version.
                val oldManifest = File(backupDir, "manifest.json")
                val newPack = java.util.zip.ZipFile(zipFile).use { zip ->
                    val manifestEntry = zip.getEntry("manifest.json")
                        ?: throw Exception("manifest.json missing")
                    parseManifest(
                        zip.getInputStream(manifestEntry).bufferedReader().readText(),
                        backupDir
                    ) ?: throw Exception("manifest.json parse failed")
                }
                if (!isValidRegisterName(newPack.registerName)) {
                    throw Exception("invalid registerName")
                }
                if (newPack.registerName != registerName) {
                    throw Exception("registerName does not match target dir")
                }
                if (oldManifest.exists()) {
                    val oldPack = parseManifest(oldManifest.readText(), backupDir)
                    if (oldPack != null && newPack.versionCode <= oldPack.versionCode) {
                        throw Exception(
                            "already installed (v${oldPack.versionName} >= v${newPack.versionName})"
                        )
                    }
                }

                // Extract (validated against path traversal and size limits)
                java.util.zip.ZipFile(zipFile).use { zip ->
                    extractZipSafely(zip, targetDir)
                }

                // Verify
                if (!File(targetDir, "manifest.json").exists()) {
                    throw Exception("manifest.json missing")
                }

                // Success - remove backup
                if (backupDir.exists()) backupDir.deleteRecursively()

                // Apply
                applyExtension(context, newPack)

                "Installed: $registerName"
            } catch (e: Exception) {
                // Rollback
                targetDir.deleteRecursively()
                if (backupDir.exists()) backupDir.renameTo(targetDir)
                "Install failed: ${e.localizedMessage ?: "unknown error"}"
            }
        } catch (e: Exception) {
            "Install failed: ${e.localizedMessage ?: "unknown error"}"
        }
    }

    /**
     * Extracts zip entries into [targetDir], rejecting path traversal, absolute
     * paths and unbounded sizes. Returns the number of files extracted.
     */
    internal fun extractZipSafely(zipFile: java.util.zip.ZipFile, targetDir: File): Int {
        val targetCanonical = targetDir.canonicalPath
        var totalBytes = 0L
        var count = 0
        val entries = zipFile.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            val entryName = entry.name
            if (entryName.contains("..") || entryName.startsWith("/") ||
                entryName.contains("\\") || entryName.contains(":")
            ) {
                throw Exception("Invalid entry name in zip: $entryName")
            }
            val outFile = File(targetDir, entryName)
            if (!outFile.canonicalPath.startsWith(targetCanonical + File.separator) &&
                outFile.canonicalPath != targetCanonical
            ) {
                throw Exception("Entry escapes pack directory: $entryName")
            }
            if (entry.isDirectory) {
                outFile.mkdirs()
            } else {
                count++
                if (count > MAX_ENTRY_COUNT) throw Exception("Too many files in zip")
                outFile.parentFile?.mkdirs()
                zipFile.getInputStream(entry).use { input ->
                    outFile.outputStream().use { output ->
                        val written = input.copyTo(output)
                        totalBytes += written
                        if (totalBytes > MAX_TOTAL_UNCOMPRESSED_BYTES) {
                            throw Exception("Zip too large")
                        }
                    }
                }
            }
        }
        return count
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
fun ExtensionPackScreen() {
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
            scope.launch {
                // Blocking zip copy/extract runs off the main thread.
                val resultMsg = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    ExtensionPackImporter.import(context, uri)
                }
                snackbarHostState.showSnackbar(resultMsg)
                packs = ExtensionPackLoader.getInstalledPacks(context)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(i18n("settings_extension_pack")) },

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
                    ExtensionPackRow(
                        pack = pack,
                        context = context,
                        scope = scope,
                        snackbarHostState = snackbarHostState
                    ) {
                        refreshKey++
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ExtensionPackRow(
    pack: ExtensionPack,
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit
) {
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
                    model = java.io.File(
                        context.filesDir,
                        "ext_packs/${pack.registerName}/${pack.iconPath}"
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(
                        40.dp
                    ).clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
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
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pack.officalLink))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Cannot open link")
                            }
                        }
                    }) {
                        Icon(
                            Icons.Outlined.Link,
                            contentDescription = i18n("extension_open_link"),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            if (!checkingUpdate) {
                                checkingUpdate = true
                                scope.launch {
                                    val info = ExtensionPackLoader.checkUpdate(
                                        pack.updateJsonLink,
                                        pack.versionCode
                                    )
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
                                if (updateInfo !=
                                    null
                                ) {
                                    Icons.Outlined.SystemUpdateAlt
                                } else {
                                    Icons.Outlined.Update
                                },
                                contentDescription = i18n("extension_check_update"),
                                modifier = Modifier.size(20.dp),
                                tint = if (updateInfo !=
                                    null
                                ) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                    if (updateInfo != null) {
                        IconButton(onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Downloading...")
                                val result = ExtensionPackLoader.downloadAndInstall(
                                    context,
                                    pack,
                                    updateInfo!!
                                )
                                snackbarHostState.showSnackbar(result)
                                onRefresh()
                            }
                        }) {
                            Icon(
                                Icons.Outlined.FileDownload,
                                contentDescription = i18n("extension_download"),
                                modifier = Modifier.size(20.dp)
                            )
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
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = i18n("common_delete"),
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
