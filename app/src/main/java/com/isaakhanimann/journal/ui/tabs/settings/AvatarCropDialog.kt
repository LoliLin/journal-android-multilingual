package com.isaakhanimann.journal.ui.tabs.settings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.isaakhanimann.journal.localization.i18n
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Full-screen avatar cropping dialog.
 * Supports pan, pinch-to-zoom, slider zoom, 90-degree rotation, reset, and image re-selection.
 */
@Composable
fun AvatarCropDialog(
    imageUri: Uri,
    userName: String,
    onDismiss: () -> Unit,
    onAvatarCroppedAndSaved: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        var currentUri by remember { mutableStateOf(imageUri) }
        var bitmap by remember { mutableStateOf<Bitmap?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var hasError by remember { mutableStateOf(false) }

        var rotationDegrees by remember { mutableIntStateOf(0) }
        var scale by remember { mutableFloatStateOf(1f) }
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }
        var cropSizePx by remember { mutableFloatStateOf(0f) }
        var isSaving by remember { mutableStateOf(false) }
        val saveFailedMessage = i18n("crop_save_failed")

        val reselectLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { newUri ->
            if (newUri != null) {
                currentUri = newUri
            }
        }

        // Load currently selected image
        LaunchedEffect(currentUri) {
            isLoading = true
            hasError = false
            val loaded = loadBitmapSafely(context, currentUri)
            if (loaded != null) {
                bitmap = loaded
                rotationDegrees = 0
                scale = 1f
                offsetX = 0f
                offsetY = 0f
            } else {
                hasError = true
            }
            isLoading = false
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101010))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = i18n("common_cancel"),
                            tint = Color.White
                        )
                    }

                    Text(
                        text = i18n("crop_avatar"),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { reselectLauncher.launch("image/*") },
                        enabled = !isSaving
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = i18n("crop_reselect"),
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            if (bitmap != null) {
                                rotationDegrees = (rotationDegrees + 90) % 360
                            }
                        },
                        enabled = !isSaving && bitmap != null
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.RotateRight,
                            contentDescription = i18n("crop_rotate"),
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            rotationDegrees = 0
                            scale = 1f
                            offsetX = 0f
                            offsetY = 0f
                        },
                        enabled = !isSaving && bitmap != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = i18n("crop_reset"),
                            tint = Color.White
                        )
                    }
                }

                // Center crop viewport
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clipToBounds(),
                    contentAlignment = Alignment.Center
                ) {
                    val density = LocalDensity.current
                    val viewWidthPx = constraints.maxWidth.toFloat()
                    val viewHeightPx = constraints.maxHeight.toFloat()
                    val marginPx = with(density) { 32.dp.toPx() }
                    val currentCropSize = AvatarCropMath.calculateCropSize(viewWidthPx, viewHeightPx, marginPx)
                    val cropRadiusPx = currentCropSize / 2f
                    val cropCenter = Offset(viewWidthPx / 2f, viewHeightPx / 2f)

                    LaunchedEffect(currentCropSize) {
                        cropSizePx = currentCropSize
                    }

                    val currentBitmap = bitmap
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    } else if (hasError || currentBitmap == null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = i18n("crop_load_failed"),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { reselectLauncher.launch("image/*") }) {
                                Text(i18n("crop_reselect"))
                            }
                        }
                    } else {
                        val (effectiveWidth, effectiveHeight) = AvatarCropMath.calculateEffectiveDimensions(
                            currentBitmap.width,
                            currentBitmap.height,
                            rotationDegrees
                        )
                        val minScale = AvatarCropMath.calculateMinScale(effectiveWidth, effectiveHeight, currentCropSize)
                        val maxScale = AvatarCropMath.calculateMaxScale(minScale)

                        // Guarantee image covers crop circle initially and after rotation
                        LaunchedEffect(minScale) {
                            if (scale < minScale) {
                                scale = minScale
                            }
                        }

                        // Constrain pan within bounds
                        val bounds = AvatarCropMath.calculatePanBounds(effectiveWidth, effectiveHeight, scale, currentCropSize)
                        LaunchedEffect(bounds) {
                            offsetX = offsetX.coerceIn(-bounds.maxOffsetX, bounds.maxOffsetX)
                            offsetY = offsetY.coerceIn(-bounds.maxOffsetY, bounds.maxOffsetY)
                        }

                        val imageBitmap = remember(currentBitmap) { currentBitmap.asImageBitmap() }

                        val currentMinScale by rememberUpdatedState(minScale)
                        val currentMaxScale by rememberUpdatedState(maxScale)
                        val currentEffectiveWidth by rememberUpdatedState(effectiveWidth)
                        val currentEffectiveHeight by rememberUpdatedState(effectiveHeight)
                        val currentCropCenter by rememberUpdatedState(cropCenter)
                        val currentCropSizeState by rememberUpdatedState(currentCropSize)

                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    // Gesture rotation is intentionally ignored; discrete 90-degree rotation is provided via the toolbar.
                                    detectTransformGestures { centroid, pan, zoom, _ ->
                                        val oldScale = scale
                                        val newScale = (oldScale * zoom).coerceIn(currentMinScale, currentMaxScale)
                                        val zoomFactor = if (oldScale > 0f) newScale / oldScale else 1f

                                        val imageCenter = currentCropCenter + Offset(offsetX, offsetY)
                                        val newImageCenter = centroid + (imageCenter - centroid) * zoomFactor + pan
                                        val newOffset = newImageCenter - currentCropCenter

                                        val curBounds = AvatarCropMath.calculatePanBounds(
                                            effectiveWidth = currentEffectiveWidth,
                                            effectiveHeight = currentEffectiveHeight,
                                            scale = newScale,
                                            cropSizePx = currentCropSizeState
                                        )

                                        scale = newScale
                                        offsetX = newOffset.x.coerceIn(-curBounds.maxOffsetX, curBounds.maxOffsetX)
                                        offsetY = newOffset.y.coerceIn(-curBounds.maxOffsetY, curBounds.maxOffsetY)
                                    }
                                }
                        ) {
                            // 1. Draw transformed image
                            withTransform({
                                translate(cropCenter.x + offsetX, cropCenter.y + offsetY)
                                rotate(rotationDegrees.toFloat())
                                scale(scale, scale)
                                translate(-currentBitmap.width / 2f, -currentBitmap.height / 2f)
                            }) {
                                drawImage(imageBitmap, Offset.Zero)
                            }

                            // 2. Dim background outside circular crop area (EvenOdd mask)
                            val overlayPath = Path().apply {
                                fillType = PathFillType.EvenOdd
                                addRect(Rect(0f, 0f, size.width, size.height))
                                addOval(Rect(cropCenter, cropRadiusPx))
                            }
                            drawPath(overlayPath, color = Color.Black.copy(alpha = 0.72f))

                            // 3. Circular crop outline
                            drawCircle(
                                color = Color.White.copy(alpha = 0.85f),
                                radius = cropRadiusPx,
                                center = cropCenter,
                                style = Stroke(width = 2.dp.toPx())
                            )

                            // 4. Rule-of-thirds grid lines (clipped inside circular crop)
                            clipPath(Path().apply { addOval(Rect(cropCenter, cropRadiusPx)) }) {
                                val oneThird = cropRadiusPx * 2f / 3f
                                val leftX = cropCenter.x - cropRadiusPx + oneThird
                                val rightX = cropCenter.x + cropRadiusPx - oneThird
                                val topY = cropCenter.y - cropRadiusPx + oneThird
                                val bottomY = cropCenter.y + cropRadiusPx - oneThird
                                val gridColor = Color.White.copy(alpha = 0.28f)
                                val gridStroke = 1.dp.toPx()

                                drawLine(gridColor, Offset(leftX, cropCenter.y - cropRadiusPx), Offset(leftX, cropCenter.y + cropRadiusPx), strokeWidth = gridStroke)
                                drawLine(gridColor, Offset(rightX, cropCenter.y - cropRadiusPx), Offset(rightX, cropCenter.y + cropRadiusPx), strokeWidth = gridStroke)
                                drawLine(gridColor, Offset(cropCenter.x - cropRadiusPx, topY), Offset(cropCenter.x + cropRadiusPx, topY), strokeWidth = gridStroke)
                                drawLine(gridColor, Offset(cropCenter.x - cropRadiusPx, bottomY), Offset(cropCenter.x + cropRadiusPx, bottomY), strokeWidth = gridStroke)
                            }
                        }
                    }
                }

                // Bottom zoom controls
                val activeBitmap = bitmap
                val (sliderMin, sliderMax) = if (activeBitmap != null && cropSizePx > 0f) {
                    val dims = AvatarCropMath.calculateEffectiveDimensions(
                        activeBitmap.width,
                        activeBitmap.height,
                        rotationDegrees
                    )
                    val minS = AvatarCropMath.calculateMinScale(dims.width, dims.height, cropSizePx)
                    Pair(minS, AvatarCropMath.calculateMaxScale(minS))
                } else {
                    Pair(1f, 5f)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newScale = (scale / 1.15f).coerceIn(sliderMin, sliderMax)
                            scale = newScale
                        },
                        enabled = !isSaving && bitmap != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomOut,
                            contentDescription = i18n("crop_zoom_out"),
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Slider(
                        value = scale.coerceIn(sliderMin, sliderMax),
                        onValueChange = { scale = it },
                        valueRange = sliderMin..sliderMax,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        enabled = !isSaving && bitmap != null,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        )
                    )

                    IconButton(
                        onClick = {
                            val newScale = (scale * 1.15f).coerceIn(sliderMin, sliderMax)
                            scale = newScale
                        },
                        enabled = !isSaving && bitmap != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = i18n("crop_zoom_in"),
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // Bottom action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSaving
                    ) {
                        Text(
                            text = i18n("common_cancel"),
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Button(
                        onClick = {
                            val currentBm = bitmap ?: return@Button
                            if (isSaving) return@Button
                            isSaving = true
                            coroutineScope.launch {
                                try {
                                    val cropped = cropBitmap(
                                        sourceBitmap = currentBm,
                                        rotationDegrees = rotationDegrees,
                                        scale = scale,
                                        offsetX = offsetX,
                                        offsetY = offsetY,
                                        cropSizePx = cropSizePx,
                                        outputSizePx = AvatarUtil.AVATAR_OUTPUT_SIZE
                                    )
                                    AvatarUtil.saveAvatarBitmap(context, userName, cropped)
                                    onAvatarCroppedAndSaved()
                                } catch (e: Exception) {
                                    Toast.makeText(context, saveFailedMessage, Toast.LENGTH_SHORT).show()
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        enabled = !isSaving && bitmap != null
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(text = i18n("common_done"))
                    }
                }
            }
        }
    }
}

/**
 * Produces a square [outputSizePx]x[outputSizePx] bitmap whose inscribed circle matches the preview.
 */
suspend fun cropBitmap(
    sourceBitmap: Bitmap,
    rotationDegrees: Int,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    cropSizePx: Float,
    outputSizePx: Int = AvatarUtil.AVATAR_OUTPUT_SIZE
): Bitmap = withContext(Dispatchers.Default) {
    val params = AvatarCropMath.calculateOutputMatrixParams(
        cropSizePx = cropSizePx,
        outputSizePx = outputSizePx,
        scale = scale,
        offsetX = offsetX,
        offsetY = offsetY,
        rotationDegrees = rotationDegrees
    )
    val outputBitmap = Bitmap.createBitmap(outputSizePx, outputSizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(outputBitmap)
    val matrix = Matrix()

    // 1. Center the source bitmap at origin
    matrix.postTranslate(-sourceBitmap.width / 2f, -sourceBitmap.height / 2f)
    // 2. Rotate around origin
    matrix.postRotate(params.rotationDegrees.toFloat())
    // 3. Scale to output canvas ratio
    matrix.postScale(params.scaleInOutput, params.scaleInOutput)
    // 4. Translate to destination canvas center with offset
    matrix.postTranslate(params.destCenterX, params.destCenterY)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
    canvas.drawBitmap(sourceBitmap, matrix, paint)
    outputBitmap
}

/**
 * Decodes a bitmap from the given [uri], downsampling high-resolution images
 * to 1024px (2x target avatar resolution) to keep memory usage low while handling EXIF orientation.
 *
 * Coil's default decoder handles EXIF orientation automatically. The fallback decoding path
 * also explicitly handles EXIF orientation so portrait/landscape images remain consistent.
 */
private suspend fun loadBitmapSafely(context: Context, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
    try {
        val imageLoader = context.imageLoader
        val request = ImageRequest.Builder(context)
            .data(uri)
            .allowHardware(false)
            .size(1024)
            .build()
        val result = imageLoader.execute(request)
        if (result is SuccessResult) {
            val drawable = result.drawable
            if (drawable is BitmapDrawable && drawable.bitmap != null) {
                return@withContext drawable.bitmap
            }
            val w = drawable.intrinsicWidth.coerceAtLeast(1)
            val h = drawable.intrinsicHeight.coerceAtLeast(1)
            val bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bm)
            drawable.setBounds(0, 0, w, h)
            drawable.draw(canvas)
            return@withContext bm
        }
    } catch (_: Exception) {
        // Fallback to manual BitmapFactory decoding
    }

    try {
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        }
        val maxDim = max(boundsOptions.outWidth, boundsOptions.outHeight)
        var sampleSize = 1
        while (maxDim / (sampleSize * 2) >= 1024) {
            sampleSize *= 2
        }
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val rawBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: return@withContext null

        val orientation = try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (_: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }

        val rotation = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        if (rotation != 0f) {
            val matrix = Matrix().apply { postRotate(rotation) }
            Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
        } else {
            rawBitmap
        }
    } catch (_: Exception) {
        null
    }
}
