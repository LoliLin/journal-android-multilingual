
package com.isaakhanimann.journal.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
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
import kotlinx.coroutines.android.awaitFrame // 👈 关键：引入等待下一帧的挂起函数
import kotlinx.coroutines.withContext

suspend fun renderComposeViewToBitmap(
    context: Context,
    widthPx: Int,
    lifecycleView: View,
    content: @Composable () -> Unit
): Bitmap = withContext(Dispatchers.Main) { 
    
    val container = FrameLayout(context)
    val composeView = ComposeView(context).apply {
        // 【核心修复 1】避免离屏 View 离开屏幕时直接被销毁
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        setContent {
            content()
        }
    }
    container.addView(composeView)

    composeView.setViewTreeLifecycleOwner(lifecycleView.findViewTreeLifecycleOwner())
    composeView.setViewTreeViewModelStoreOwner(lifecycleView.findViewTreeViewModelStoreOwner())
    composeView.setViewTreeSavedStateRegistryOwner(lifecycleView.findViewTreeSavedStateRegistryOwner())

    // 【核心修复 2】：强行等待 Android 的 UI 渲染时钟走 1-2 帧！
    // 这一步能让 Compose 闭包在后台悄悄完成 Recomposition，真正把卡片、文本甚至图标给“拼装”出来。
    awaitFrame()
    awaitFrame()

    // 此时再进行测量，高度就绝对不会是 0 了
    val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
    val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    composeView.measure(widthSpec, heightSpec)
    
    val measuredWidth = composeView.measuredWidth
    // 如果万一还是 0，给一个保底高度，防止 createBitmap 崩溃
    val measuredHeight = composeView.measuredHeight.coerceAtLeast(1)
    
    composeView.layout(0, 0, measuredWidth, measuredHeight)

    // 创建对应大小的画布
    val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    composeView.draw(canvas)

    // 顺手从容器移除，保持内存干净
    container.removeView(composeView)

    return@withContext bitmap
}
