package com.isaakhanimann.journal.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
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
import coil.ImageLoader
import coil.compose.LocalImageLoader
import androidx.compose.runtime.CompositionLocalProvider
import coil.intercept.Interceptor
import kotlinx.coroutines.delay

/**
 * 终极完全体：安全、丝滑、绝不卡死且绝不无反应的 Compose 转 Bitmap 方案
 */
suspend fun renderComposeViewToBitmap(
    context: Context,
    widthPx: Int,
    lifecycleView: View,
    content: @Composable () -> Unit,
    postLayoutDelayMs: Long = 0L
): Bitmap = withContext(Dispatchers.Main) {

    // 1. 获取宿主的顶级 DecorView，确保能真正挂载上屏
    val hostActivityView = lifecycleView.rootView as? ViewGroup 
        ?: throw IllegalStateException("无法找到合法的宿主 View")

    // 2. 创建隐形容器，将其扔到屏幕右侧 10000 像素外的“宇宙盲区”
    // 既能骗过系统触发完整测量，又绝对不会让用户看见
    val container = FrameLayout(context).apply {
        layoutParams = ViewGroup.LayoutParams(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
        translationX = 10000f 
        translationY = 10000f 
    }
    
    val composeView = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        setContent {
            // 创建一个自动禁用硬件加速的 ImageLoader
            val safeImageLoader = ImageLoader.Builder(context)
                .components {
                    add(coil.intercept.Interceptor { chain ->
                        val newRequest = chain.request.newBuilder()
                            .allowHardware(false)   // 关键！禁用硬件位图
                            .build()
                        chain.proceed(newRequest)
                    })
                }
                .build()
            CompositionLocalProvider(
                LocalImageLoader provides safeImageLoader
            ) {
                content()
            }
        }
    }
    container.addView(composeView)
    hostActivityView.addView(container)

    // 3. 完美拷贝宿主环境生命周期与 Hilt 上下文
    container.setViewTreeLifecycleOwner(lifecycleView.findViewTreeLifecycleOwner())
    container.setViewTreeViewModelStoreOwner(lifecycleView.findViewTreeViewModelStoreOwner())
    container.setViewTreeSavedStateRegistryOwner(lifecycleView.findViewTreeSavedStateRegistryOwner())

    // 4. 双重异步唤醒锁（原生监听 + 定时器保底）
    suspendCancellableCoroutine<Unit> { continuation ->
        val mainHandler = Handler(Looper.getMainLooper())
        
        // 核心锁 A：监听系统真实的排版布局完成信号
        val layoutListener = object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (composeView.height > 0) {
                    composeView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    mainHandler.removeCallbacksAndMessages(null) // 取消保底定时器
                    if (continuation.isActive) continuation.resume(Unit)
                }
            }
        }
        composeView.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        
        // 核心锁 B：50ms 强行自我唤醒保底（防止部分魔改系统不触发 OnGlobalLayout）
        val timeoutRunnable = Runnable {
            composeView.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
            if (continuation.isActive) continuation.resume(Unit)
        }
        mainHandler.postDelayed(timeoutRunnable, 50)
        
        // 强推系统一把，激活 Layout 信号
        composeView.requestLayout()
        
        // 严谨防泄漏：如果协程中途取消，拔掉所有异步桩
        continuation.invokeOnCancellation {
            composeView.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
            mainHandler.removeCallbacksAndMessages(null)
        }
    }

    if (postLayoutDelayMs > 0) {
        delay(postLayoutDelayMs)
    }

    // 5. 放心收网：此时不管是哪个锁醒来的，宽高都已经准备就绪
    val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
    val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    composeView.measure(widthSpec, heightSpec)

    val measuredWidth = composeView.measuredWidth
    // 终极硬化保底：确保高度绝对不为 0，彻底绝育 createBitmap 的闪退风险
    val measuredHeight = composeView.measuredHeight.coerceAtLeast(100)

    composeView.layout(0, 0, measuredWidth, measuredHeight)

    // 6. 咔嚓！绘制位图
    val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    composeView.draw(canvas)

    // 7. 悄悄离场，不留一丝痕迹
    val safeBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)
    bitmap.recycle()

    hostActivityView.removeView(container)

    return@withContext safeBitmap
}
