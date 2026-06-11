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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun renderComposeViewToBitmap(
    context: Context,
    widthPx: Int,
    lifecycleView: View,
    content: @Composable () -> Unit
): Bitmap = withContext(Dispatchers.Main) {

    val lifecycleOwner = rememberLifecycleOwner()

    val composeView = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        setViewTreeLifecycleOwner(lifecycleOwner)
        // ShareableExperienceCard 没有用 ViewModel / SavedState，
        // 所以不需要 setViewTreeViewModelStoreOwner / SavedStateRegistryOwner
        setContent { content() }
    }

    // 手动测量 + 布局（不需要 attach 到 window）
    val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
    val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    composeView.measure(widthSpec, heightSpec)

    val mw = composeView.measuredWidth.coerceAtLeast(1)
    val mh = composeView.measuredHeight.coerceAtLeast(100)

    composeView.layout(0, 0, mw, mh)

    // 绘制到 bitmap
    val bitmap = Bitmap.createBitmap(mw, mh, Bitmap.Config.ARGB_8888)
    composeView.draw(Canvas(bitmap))

    // 清理 Lifecycle
    lifecycleOwner.markFinished()

    bitmap
}

private class LifecycleOwnerWithControl : LifecycleOwner {
    private val registry = LifecycleRegistry(this)

    init {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    // ⚠️ 修正：用 Kotlin property 语法，不用 getLifecycle()
    override val lifecycle: Lifecycle get() = registry

    fun markFinished() {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}

private fun rememberLifecycleOwner(): LifecycleOwnerWithControl = LifecycleOwnerWithControl()
