package com.isaakhanimann.journal.ui.tabs.settings

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.zip.ZipFile

object ExtensionPackImporter {

    fun import(context: Context, uri: Uri): String {
        return try {
            val tempFile = File(context.cacheDir, "ext_pack_${System.currentTimeMillis()}.zip")
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            } ?: return "Cannot read file"

            try {
                val zipFile = ZipFile(tempFile)
                try {
                    // Find manifest.json first
                    var manifestContent: String? = null
                    val entries = zipFile.entries()
                    while (entries.hasMoreElements()) {
                        val entry = entries.nextElement()
                        if (entry.name == "manifest.json") {
                            manifestContent = zipFile.getInputStream(entry).bufferedReader().readText()
                            break
                        }
                    }

                    if (manifestContent == null) {
                        return "Invalid pack: manifest.json not found"
                    }

                    val pack = ExtensionPackLoader.parseManifest(manifestContent, context.cacheDir)
                    if (pack == null) {
                        return "Invalid pack: manifest.json parse failed"
                    }
                    if (!isValidRegisterName(pack.registerName)) {
                        return "Invalid pack: registerName contains illegal characters"
                    }

                    val packDir = File(context.filesDir, "ext_packs/${pack.registerName}")
                    // Refuse downgrades / same-version re-installs.
                    val existingManifest = File(packDir, "manifest.json")
                    if (existingManifest.exists()) {
                        val existingPack = ExtensionPackLoader.parseManifest(
                            existingManifest.readText(),
                            packDir
                        )
                        if (existingPack != null && pack.versionCode <= existingPack.versionCode) {
                            return "Already installed (v${existingPack.versionName} >= v${pack.versionName})"
                        }
                    }

                    // Replace semantics: remove previous install so stale files cannot linger.
                    if (packDir.exists()) {
                        packDir.deleteRecursively()
                    }
                    packDir.mkdirs()

                    // Extraction validates entry names (path traversal) and size limits.
                    val extracted = ExtensionPackLoader.extractZipSafely(zipFile, packDir)

                    // Apply overrides
                    ExtensionPackLoader.applyExtension(context, pack)

                    "Extension pack '${pack.registerName}' v${pack.versionName} imported ($extracted files)"
                } finally {
                    zipFile.close()
                }
            } finally {
                tempFile.delete()
            }
        } catch (e: Exception) {
            "Import failed: ${e.localizedMessage ?: "unknown error"}"
        }
    }
}
