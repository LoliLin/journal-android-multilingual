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

            val zipFile = ZipFile(tempFile)
            val entries = zipFile.entries()

            // Find manifest.json first
            var manifestContent: String? = null
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.name == "manifest.json") {
                    manifestContent = zipFile.getInputStream(entry).bufferedReader().readText()
                    break
                }
            }

            if (manifestContent == null) {
                zipFile.close(); tempFile.delete()
                return "Invalid pack: manifest.json not found"
            }

            val parsed = ExtensionPackLoader.parseManifest(manifestContent, File(context.cacheDir))
            val pack = parsed
            if (pack == null) {
                zipFile.close(); tempFile.delete()
                return "Invalid pack: manifest.json parse failed"
            }

            // Extract to permanent storage
            val packDir = File(context.filesDir, "ext_packs/${pack.registerName}")
            packDir.mkdirs()

            val zipEntries = zipFile.entries()
            var extracted = 0
            while (zipEntries.hasMoreElements()) {
                val entry = zipEntries.nextElement()
                val target = File(packDir, entry.name)
                if (entry.isDirectory) {
                    target.mkdirs()
                } else {
                    target.parentFile?.mkdirs()
                    zipFile.getInputStream(entry).use { input ->
                        target.outputStream().use { output -> input.copyTo(output) }
                    }
                    extracted++
                }
            }

            zipFile.close()
            tempFile.delete()

            // Apply overrides
            ExtensionPackLoader.applyExtension(context, pack)

            return "Extension pack '${pack.titleDefault}' v${pack.versionName} imported ($extracted files)"
        } catch (e: Exception) {
            "Import failed: ${e.localizedMessage ?: "unknown error"}"
        }
    }
}
