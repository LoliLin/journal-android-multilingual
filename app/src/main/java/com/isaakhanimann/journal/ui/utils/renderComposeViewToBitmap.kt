package com.isaakhanimann.journal.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 无需侵入 Activity 的离线渲染方案。
 * 创建一个有独立 LifecycleOwner 的 ComposeView，attach 到 ViewTree 但不 attach 到 Window。
 */
suspend fun renderComposeViewToBitmap(
    context: Context,
    widthPx: Int,
    lifecycleView: View, // 仅用于拷贝 ViewModelStoreOwner / SavedStateRegistryOwner
    content: @Composable () -> Unit
): Bitmap = withContext(Dispatchers.Main) {

    // 独立的微生命周期，只够让 Compose 完成 composition
    val lifecycleOwner = rememberLifecycleOwner()

    val composeView = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        setViewTreeLifecycleOwner(lifecycleOwner)
        setViewTreeViewModelStoreOwner(lifecycleView.findViewTreeViewModelStoreOwner())
        setViewTreeSavedStateRegistryOwner(lifecycleView.findViewTreeSavedStateRegistryOwner())
        setContent { content() }

        // 强制触发 composition
        requestLayout()
    }

    // 给予足够时间让 Compose 完成首次 composition
    // 实际上 ComposeView.setContent 是同步的，content 会立即被调用
    // 但 measure/layout 需要等到下一帧

    // 手动 measure & layout（不需要 attach 到 window）
    val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
    val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    composeView.measure(widthSpec, heightSpec)

    val measuredWidth = composeView.measuredWidth.coerceAtLeast(1)
    val measuredHeight = composeView.measuredHeight.coerceAtLeast(100)

    composeView.layout(0, 0, measuredWidth, measuredHeight)

    // 画到 bitmap
    val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
    composeView.draw(Canvas(bitmap))

    // 清理
    lifecycleOwner.markFinished()

    bitmap
}

/**
 * 创建一个最小可用的 LifecycleOwner，用完就销毁。
 */
private fun rememberLifecycleOwner(): LifecycleOwnerWithControl {
    return LifecycleOwnerWithControl()
}

private class LifecycleOwnerWithControl : LifecycleOwner {
    private val registry = LifecycleRegistry(this)

    init {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun getLifecycle(): Lifecycle = registry

    fun markFinished() {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}

