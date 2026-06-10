package com.isaakhanimann.journal.ui.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

suspend fun shareBitmap(context: Context, bitmap: Bitmap) {
    // 1. 强行切到 IO 线程，绝对不堵塞主线程
    withContext(Dispatchers.IO) {
        try {
            val cachePath = File(context.cacheDir, "images")
            if (!cachePath.exists()) {
                cachePath.mkdirs()
            }
            val file = File(cachePath, "experience_share_${System.currentTimeMillis()}.png")
            val stream = FileOutputStream(file)
            
            // 压缩并写入磁盘
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            // 2. 使用 FileProvider 生成安全的 content:// URI
            // ⚠️ 注意：这里的 "${context.packageName}.fileprovider" 必须和你的 AndroidManifest.xml 里的配置完全一致
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            // 3. 构建分享 Intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                // 极其关键：临时授予接收方（分享去处的App）读取这个文件的权限
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) 
            }

            // 4. 切回主线程调起系统分享选择器
            withContext(Dispatchers.Main) {
                context.startActivity(Intent.createChooser(shareIntent, "分享到"))
            }

        } catch (e: Exception) {
            // 哪怕由于奇奇怪怪的原因失败了，也只是打印日志，绝对不会让 App 闪退
            e.printStackTrace() 
        }
    }
}
