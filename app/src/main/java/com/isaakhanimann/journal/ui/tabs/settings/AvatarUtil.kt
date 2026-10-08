package com.isaakhanimann.journal.ui.tabs.settings

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.io.File
import java.io.FileOutputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object AvatarUtil {

    private const val AVATAR_DIR = "Avatars"
    private const val EXTENSION = ".png"

    const val AVATAR_OUTPUT_SIZE = 512

    // --------------- Public API ---------------

    /**
     * Checks whether the user has a saved avatar file.
     */
    fun isUserHasAvatar(context: Context, userName: String): Boolean =
        getAvatarFile(context, userName).exists()

    /**
     * Returns the user's avatar file, or null if it does not exist.
     */
    fun getUserAvatar(context: Context, userName: String): File? =
        getAvatarFile(context, userName).takeIf { it.exists() }

    /**
     * Composable helper that returns a lambda to launch the image picker and opens
     * the cropping dialog before saving to Avatars/<Username>.png.
     */
    @Composable
    fun acquireUserAvatar(
        context: Context,
        userName: String,
        onAvatarSaved: () -> Unit = {}
    ): () -> Unit {
        var pendingCropUri by remember { mutableStateOf<Uri?>(null) }
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                pendingCropUri = uri
            }
        }

        val currentCropUri = pendingCropUri
        if (currentCropUri != null) {
            AvatarCropDialog(
                imageUri = currentCropUri,
                userName = userName,
                onDismiss = {
                    pendingCropUri = null
                },
                onAvatarCroppedAndSaved = {
                    pendingCropUri = null
                    onAvatarSaved()
                }
            )
        }

        return remember(userName) {
            { launcher.launch("image/*") }
        }
    }

    // --------------- Internal and Storage Implementation ---------------

    /**
     * Returns the target file for a user avatar: files/Avatars/<safeName>.png
     */
    fun getAvatarFile(context: Context, userName: String): File {
        val dir = File(context.filesDir, AVATAR_DIR)
        // Whitelist approach: keep only letters/digits (incl. Unicode) and - _ so a
        // user-supplied (or imported) name can never escape the Avatars directory.
        val safeName = buildString(userName.length) {
            for (c in userName) {
                append(if (c.isLetterOrDigit() || c == '-' || c == '_') c else '_')
            }
        }.ifEmpty { "default" }
        return File(dir, "$safeName$EXTENSION")
    }

    /**
     * Saves the cropped avatar bitmap atomically to internal storage as <Username>.png.
     */
    fun saveAvatarBitmap(context: Context, userName: String, bitmap: Bitmap) {
        val targetFile = getAvatarFile(context, userName)
        val parent = targetFile.parentFile ?: File(context.filesDir, AVATAR_DIR)
        if (!parent.exists()) {
            parent.mkdirs()
        }
        val tempFile = File(parent, "${targetFile.name}.tmp")
        var isMoveSuccessful = false
        try {
            FileOutputStream(tempFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
            }
            if (tempFile.exists()) {
                try {
                    Files.move(
                        tempFile.toPath(),
                        targetFile.toPath(),
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                    )
                } catch (_: AtomicMoveNotSupportedException) {
                    Files.move(
                        tempFile.toPath(),
                        targetFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                    )
                }
                isMoveSuccessful = true
            }
        } finally {
            if (!isMoveSuccessful && tempFile.exists()) {
                tempFile.delete()
            }
        }
    }

    /**
     * Copies selected raw image from Uri to internal storage.
     */
    private fun saveAvatarFromUri(context: Context, userName: String, uri: Uri) {
        val targetFile = getAvatarFile(context, userName)
        targetFile.parentFile?.mkdirs()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(targetFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
}
