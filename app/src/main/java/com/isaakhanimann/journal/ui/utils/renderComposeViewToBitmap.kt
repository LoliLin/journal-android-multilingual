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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * 渲染请求封装
 */
private data class RenderRequest(
    val context: Context,
    val widthPx: Int,
    val lifecycleView: View,
    val content: @Composable () -> Unit,
    val result: CompletableDeferred<Bitmap>
)

/**
 * 单线程渲染协程，永远只有一个任务在执行
 */
private val renderChannel = Channel<RenderRequest>(Channel.UNLIMITED)

private val renderJob = GlobalScope.launch(Dispatchers.Main) {
    for (request in renderChannel) {
        try {
            val bitmap = doRender(
                context = request.context,
                widthPx = request.widthPx,
                lifecycleView = request.lifecycleView,
                content = request.content
            )
            request.result.complete(bitmap)
        } catch (e: Exception) {
            request.result.completeExceptionally(e)
        }
    }
}

suspend fun renderComposeViewToBitmap(
    context: Context,
    widthPx: Int,
    lifecycleView: View,
    content: @Composable () -> Unit
): Bitmap {
    val request = RenderRequest(
        context = context,
        widthPx = widthPx,
        lifecycleView = lifecycleView,
        content = content,
        result = CompletableDeferred()
    )
    renderChannel.send(request)
    return request.result.await()
}

/**
 * 你的原版渲染代码，完全不变，只抽成单独函数
 */
private suspend fun doRender(
    context: Context,
    widthPx: Int,
    lifecycleView: View,
    content: @Composable () -> Unit
): Bitmap = withContext(Dispatchers.Main) {

    val hostActivityView = lifecycleView.rootView as? ViewGroup
        ?: throw IllegalStateException("无法找到合法的宿主 View")

    val container = FrameLayout(context).apply {
        layoutParams = ViewGroup.LayoutParams(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
        translationX = 10000f
        translationY = 10000f
    }

    val composeView = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        setContent { content() }
    }
    container.addView(composeView)
    hostActivityView.addView(container)

    container.setViewTreeLifecycleOwner(lifecycleView.findViewTreeLifecycleOwner())
    container.setViewTreeViewModelStoreOwner(lifecycleView.findViewTreeViewModelStoreOwner())
    container.setViewTreeSavedStateRegistryOwner(lifecycleView.findViewTreeSavedStateRegistryOwner())

    suspendCancellableCoroutine<Unit> { continuation ->
        val mainHandler = Handler(Looper.getMainLooper())

        val layoutListener = object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (composeView.height > 0) {
                    composeView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    mainHandler.removeCallbacksAndMessages(null)
                    if (continuation.isActive) continuation.resume(Unit)
                }
            }
        }
        composeView.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)

        val timeoutRunnable = Runnable {
            composeView.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
            if (continuation.isActive) continuation.resume(Unit)
        }
        mainHandler.postDelayed(timeoutRunnable, 50)

        composeView.requestLayout()

        continuation.invokeOnCancellation {
            composeView.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
            mainHandler.removeCallbacksAndMessages(null)
        }
    }

    val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
    val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    composeView.measure(widthSpec, heightSpec)

    val measuredWidth = composeView.measuredWidth.coerceAtLeast(1)
    val measuredHeight = composeView.measuredHeight.coerceAtLeast(100)
    composeView.layout(0, 0, measuredWidth, measuredHeight)

    val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    composeView.draw(canvas)

    hostActivityView.removeView(container)

    bitmap
}
