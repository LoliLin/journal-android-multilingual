package com.isaakhanimann.journal.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 在后台动态创建 ComposeView 并将其强行渲染为本地 Bitmap 图片
 * * @param context 上下文
 * @param widthPx 期待导出的图片宽度（单位：像素，例如 1080）
 * @param lifecycleView 处于活跃状态的当前界面 View（用来为内存中的临时 View 拷贝生命周期和 Hilt 环境）
 * @param content 需要被渲染并截图的 Compose 组件闭包（例如 ShareableExperienceCard）
 */
suspend fun renderComposeViewToBitmap(
    context: Context,
    widthPx: Int,
    lifecycleView: View,
    content: @Composable () -> Unit
): Bitmap = withContext(Dispatchers.Main) { // ⚠️ 必须在 Android 的主线程（UI线程）操作 View
    
    // 1. 创建一个 FrameLayout 容器，并将新 new 出来的 ComposeView 塞进去
    val container = FrameLayout(context)
    val composeView = ComposeView(context).apply {
        setContent {
            content()
        }
    }
    container.addView(composeView)

    // 2. 【极为关键的一步】：从当前正常的界面 View 树中，把生命周期（Lifecycle）、
    // ViewModel 存储器（ViewModelStore）以及状态注册器（SavedStateRegistry）全部拷贝给这个临时的 View。
    // 如果不拷贝，ComposeView 内部在跑 hiltViewModel() 时会因为找不到宿主环境直接崩溃！
    composeView.setViewTreeLifecycleOwner(lifecycleView.findViewTreeLifecycleOwner())
    composeView.setViewTreeViewModelStoreOwner(lifecycleView.findViewTreeViewModelStoreOwner())
    composeView.setViewTreeSavedStateRegistryOwner(lifecycleView.findViewTreeSavedStateRegistryOwner())

    // 3. 手动触发测量 (Measure)
    // 宽度：严格限制为传入的像素值（EXACTLY）
    // 高度：设置为未指定（UNSPECIFIED），让卡片内部的组件根据内容自己往下无限撑开
    val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
    val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    composeView.measure(widthSpec, heightSpec)
    
    // 4. 手动触发布局 (Layout)
    // 拿到测量出的宽高后，强行让它在内存里排版分配像素坐标
    val measuredWidth = composeView.measuredWidth
    val measuredHeight = composeView.measuredHeight
    composeView.layout(0, 0, measuredWidth, measuredHeight)

    // 5. 【咔嚓！】：创建对应大小的空白 Bitmap，并扔给 Canvas 画布
    val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    // 让 ComposeView 把自己身上的每一个像素点，全画到我们的 Canvas 也就是 Bitmap 上
    composeView.draw(canvas)

    // 6. 成功返回 Bitmap
    return@withContext bitmap
}

