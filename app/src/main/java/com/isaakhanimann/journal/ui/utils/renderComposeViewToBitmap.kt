package com.isaakhanimann.journal.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * 安全、不卡死主线程的 Compose 转 Bitmap 方案
 */
suspend fun renderComposeViewToBitmap(
    context: Context,
    widthPx: Int,
    lifecycleView: View,
    content: @Composable () -> Unit
): Bitmap {
    // 1. 核心修复：整个 View 的创建、配置和测量，全部包装在一个轻量级的主线程任务中，
    // 执行完立刻释放主线程，让水波纹等特效继续动，绝不卡死！
    val composeView = withContext(Dispatchers.Main) {
        val container = FrameLayout(context)
        val view = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                content()
            }
        }
        container.addView(view)

        // 拷贝宿主环境
        view.setViewTreeLifecycleOwner(lifecycleView.findViewTreeLifecycleOwner())
        view.setViewTreeViewModelStoreOwner(lifecycleView.findViewTreeViewModelStoreOwner())
        view.setViewTreeSavedStateRegistryOwner(lifecycleView.findViewTreeSavedStateRegistryOwner())
        
        view
    }

    // 2. 核心修复：放开主线程控制权，挂起协程，老老实实让出 150 毫秒的时间！
    // 这段时间内，主线程可以自由地让你的点击水波纹特效流畅地播完，同时 Compose 在后台悄悄把 Text("Try") 拼装好。
    delay(150)

    // 3. 再次回到主线程进行收网（测量、布局、截图）
    return withContext(Dispatchers.Main) {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        composeView.measure(widthSpec, heightSpec)
        
        val measuredWidth = composeView.measuredWidth
        // 保底处理：如果依然是 0，给 100 像素防止 createBitmap 崩溃闪退
        val measuredHeight = composeView.measuredHeight.coerceAtLeast(100)
        
        composeView.layout(0, 0, measuredWidth, measuredHeight)

        val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        composeView.draw(canvas)

        // 干净利落地移除
        (composeView.parent as? ViewGroup)?.removeView(composeView)
        
        bitmap
    }
}
