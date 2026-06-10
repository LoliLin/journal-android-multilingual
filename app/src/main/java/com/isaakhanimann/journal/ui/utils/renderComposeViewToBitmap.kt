package com.isaakhanimann.journal.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
 * 安全、不卡死主线程的 Compose 转 Bitmap 方案
 * * @param context 上下文
 * @param widthPx 目标图片的物理宽度（像素）
 * @param lifecycleView 当前界面上活跃的任意 View（用作生命周期和状态树的宿主参考）
 * @param content 需要被渲染的 Compose 布局
 */
suspend fun renderComposeViewToBitmap(
    context: Context,
    widthPx: Int,
    lifecycleView: View,
    content: @Composable () -> Unit
): Bitmap = withContext(Dispatchers.Main) {

    // 1. 获取宿主的顶级 DecorView，确保能真正 Attach 上去以激活 Compose 渲染流水线
    val hostActivityView = lifecycleView.rootView as? ViewGroup 
        ?: throw IllegalStateException("无法找到合法的宿主 View")

    // 2. 创建隐形容器并放入宿主树中（大小设为 0x0，用户完全不可见，但不影响其内部测绘）
    val container = FrameLayout(context).apply {
        layoutParams = ViewGroup.LayoutParams(0, 0)
    }
    
    val composeView = ComposeView(context)
    
    // 3. 核心机制：利用挂起协程，等待 Compose 框架的 LaunchedEffect 信号
    suspendCancellableCoroutine<Unit> { continuation ->
        composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                content()
                
                // 当 Compose 完成初次组合（Composition）并成功附着到上下文后触发
                LaunchedEffect(Unit) {
                    // 顺便往主线程消息队列尾部推一把，确保 Compose 内部的布局测量信号同步刷新完毕
                    handler?.post {
                        if (continuation.isActive) continuation.resume(Unit)
                    } ?: run {
                        if (continuation.isActive) continuation.resume(Unit)
                    }
                }
            }
        }
    }

    // 将 View 节点正式挂载到活跃的窗口树中
    container.addView(composeView)
    hostActivityView.addView(container)
    
    // 4. 拷贝宿主环境的生命周期、ViewModelStore 和 SavedState
    container.setViewTreeLifecycleOwner(lifecycleView.findViewTreeLifecycleOwner())
    container.setViewTreeViewModelStoreOwner(lifecycleView.findViewTreeViewModelStoreOwner())
    container.setViewTreeSavedStateRegistryOwner(lifecycleView.findViewTreeSavedStateRegistryOwner())

    // 5. 放心收网：此时数据链与 Compose 管道已就绪，手动触发精确测量
    val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
    val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    composeView.measure(widthSpec, heightSpec)

    // 严格的保底机制：确保宽高必须大于 0 像素，彻底杜绝 createBitmap 抛出异常闪退
    val measuredWidth = composeView.measuredWidth.coerceAtLeast(1)
    val measuredHeight = composeView.measuredHeight.coerceAtLeast(1)

    // 给 View 树分发最终的边界边界
    composeView.layout(0, 0, measuredWidth, measuredHeight)

    // 6. 创建 Bitmap 并绘制
    val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    composeView.draw(canvas)

    // 7. 干净利落地将临时容器从宿主中移除，防止潜在的内存泄漏
    hostActivityView.removeView(container)

    return@withContext bitmap
}
