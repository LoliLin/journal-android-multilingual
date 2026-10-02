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
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object AvatarUtil {

    // 存储目录名
    private const val AVATAR_DIR = "Avatars"

    // 文件后缀（统一使用 png）
    private const val EXTENSION = ".png"

    const val AVATAR_OUTPUT_SIZE = 512

    // --------------- 公开 API ---------------

    /**
     * 检查用户是否已设置头像。
     */
    fun isUserHasAvatar(context: Context, userName: String): Boolean =
        getAvatarFile(context, userName).exists()

    /**
     * 获取用户的头像文件，若文件不存在则返回 null。
     */
    fun getUserAvatar(context: Context, userName: String): File? =
        getAvatarFile(context, userName).takeIf { it.exists() }

    /**
     * 生成一个用于触发“选择并裁切头像”的 Composable 工具。
     * @return 一个 lambda，调用它会打开系统图片选择器，选中后弹出自定义裁切界面，裁切确认后自动保存为 Avatars/Username.png。
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

        if (pendingCropUri != null) {
            AvatarCropDialog(
                imageUri = pendingCropUri!!,
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

        // 返回可以触发选择器的 lambda
        return remember(userName) {
            { launcher.launch("image/*") }
        }
    }

    // --------------- 内部与保存实现 ---------------

    /**
     * 根据用户名构造目标文件路径：/data/data/.../files/Avatars/Username.png
     */
    fun getAvatarFile(context: Context, userName: String): File {
        val dir = File(context.filesDir, AVATAR_DIR)
        // Whitelist approach: keep only letters/digits (incl. Unicode) and - _ so a
        // user-supplied (or imported) name can never escape the Avatars directory.
        val safeName = userName.map { c ->
            if (c.isLetterOrDigit() || c == '-' || c == '_') c else '_'
        }.joinToString("")
        return File(dir, "$safeName$EXTENSION")
    }

    /**
     * 将裁切好的 Bitmap 保存至内部存储，固定命名为 Username.png。
     * 采用临时文件写入后重命名的方式确保原子性。
     */
    fun saveAvatarBitmap(context: Context, userName: String, bitmap: Bitmap) {
        val targetFile = getAvatarFile(context, userName)
        val parent = targetFile.parentFile
        if (parent != null && !parent.exists()) {
            parent.mkdirs()
        }
        val tempFile = File(parent ?: context.filesDir, "${targetFile.name}.tmp")
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
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                    )
                } catch (_: Exception) {
                    Files.move(
                        tempFile.toPath(),
                        targetFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                    )
                }
            }
        } finally {
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }

    /**
     * 将用户选中的原始图片从 URI 复制到内部存储，固定命名为 Username.png。
     * 如果目录不存在会自动创建，原有头像会被覆盖。
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
