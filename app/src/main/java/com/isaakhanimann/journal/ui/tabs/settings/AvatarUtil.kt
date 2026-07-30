package com.isaakhanimann.journal.ui.tabs.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.io.File
import java.io.FileOutputStream

object AvatarUtil {

    // 存储目录名
    private const val AVATAR_DIR = "Avatars"

    // 文件后缀（统一使用 png）
    private const val EXTENSION = ".png"

    // --------------- 公开 API ---------------

    /**
     * 检查用户是否已设置头像。
     */
    fun isUserHasAvatar(context: Context, userName: String): Boolean =
        getAvatarFile(context, userName).exists() == true

    /**
     * 获取用户的头像文件，若文件不存在则返回 null。
     */
    fun getUserAvatar(context: Context, userName: String): File? =
        getAvatarFile(context, userName).takeIf { it.exists() }

    /**
     * 生成一个用于触发“选择并保存头像”的 Composable 工具。
     * @return 一个 lambda，调用它会打开系统图片选择器，选中后自动保存为 Avatars/Username.png。
     */
    @Composable
    fun acquireUserAvatar(
        context: Context,
        userName: String,
        onAvatarSaved: () -> Unit = {}
    ): () -> Unit {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let { safeUri ->
                saveAvatarFromUri(context, userName, safeUri)
                onAvatarSaved()
            }
        }

        // 返回可以触发选择器的 lambda
        return remember(userName) {
            { launcher.launch("image/*") }
        }
    }

    // --------------- 内部实现 ---------------

    /**
     * 根据用户名构造目标文件路径：/data/data/.../files/Avatars/Username.png
     */
    fun getAvatarFile(context: Context, userName: String): File {
        val dir = File(context.filesDir, AVATAR_DIR)
        return File(dir, "$userName$EXTENSION")
    }

    /**
     * 将用户选中的图片从 URI 复制到内部存储，固定命名为 Username.png。
     * 如果目录不存在会自动创建，原有头像会被覆盖。
     */
    private fun saveAvatarFromUri(context: Context, userName: String, uri: Uri) {
        val targetFile = getAvatarFile(context, userName)
        // 确保目录存在
        targetFile.parentFile?.mkdirs()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(targetFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
}
