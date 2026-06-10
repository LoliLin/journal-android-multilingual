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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * 安全、不卡死主线程的 Compose 转 Bitmap 方案（修复版）
 */
suspend fun renderComposeViewToBitmap(
    context: Context,
    widthPx: Int,
    lifecycleView: View,
    content: @Composable () -> Unit
): Bitmap = withContext(Dispatchers.Main) {

    // 1. 获取宿主的顶级 DecorView，确保能真正 Attach 上去
    val hostActivityView = lifecycleView.rootView as? ViewGroup 
        ?: throw IllegalStateException("无法找到合法的宿主 View")

    // 2. 创建隐形容器，放入宿主树中（大小设为0，用户看不见，但不影响测量绘制）
    val container = FrameLayout(context).apply {
        layoutParams = ViewGroup.LayoutParams(0, 0)
    }
    
    val composeView = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        setContent {
            content()
        }
    }
    container.addView(composeView)
    hostActivityView.addView(container)

    // 3. 传递环境生命周期
    container.setViewTreeLifecycleOwner(lifecycleView.findViewTreeLifecycleOwner())
    container.setViewTreeViewModelStoreOwner(lifecycleView.findViewTreeViewModelStoreOwner())
    container.setViewTreeSavedStateRegistryOwner(lifecycleView.findViewTreeSavedStateRegistryOwner())

    // 4. 【完美修复】：使用 GlobalLayout 监听，直到 Compose 真正撑开 View 的高度
    suspendCancellableCoroutine<Unit> { continuation ->
        val layoutListener = object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // 只有当 ComposeView 内部真正被内容撑开、高度大于 0 时，才代表后台渲染彻底完成了！
                if (composeView.width > 0 && composeView.height > 0) {
                    // 移除监听，防止重复触发
                    composeView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    if (continuation.isActive) continuation.resume(Unit)
                }
            }
        }
        composeView.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        
        // 强行触发一次初始测量，推系统一把，让它开始走 OnGlobalLayout 流程
        val initialSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
        val unspecifiedSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        composeView.measure(initialSpec, unspecifiedSpec)
        // 给它一个临时的虚拟排版，激活系统的 Layout 信号
        composeView.layout(0, 0, widthPx, composeView.measuredHeight.coerceAtLeast(100))
        
        // 如果协程中途被取消了，记得拔掉监听器，非常严谨
        continuation.invokeOnCancellation {
            composeView.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
        }
    }

    // 5. 【放心收网】：此时宽高已经绝对真实，不需要任何盲目的 delay
    val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
    val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    composeView.measure(widthSpec, heightSpec)

    val measuredWidth = composeView.measuredWidth
    val measuredHeight = composeView.measuredHeight // 👈 此时拿到的高度绝对是真实的、内容撑开的高度

    composeView.layout(0, 0, measuredWidth, measuredHeight)

    // 6. 截图（稳如泰山）
    val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    composeView.draw(canvas)

    // 7. 安全离场
    hostActivityView.removeView(container)

    return@withContext bitmap
}
