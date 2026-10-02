package com.isaakhanimann.journal.ui.tabs.settings

import kotlin.math.max
import kotlin.math.min

object AvatarCropMath {

    data class PanBounds(
        val maxOffsetX: Float,
        val maxOffsetY: Float
    )

    data class EffectiveDimensions(
        val width: Float,
        val height: Float
    )

    data class CropMatrixParams(
        val scaleInOutput: Float,
        val destCenterX: Float,
        val destCenterY: Float,
        val rotationDegrees: Int
    )

    fun calculateEffectiveDimensions(
        bitmapWidth: Int,
        bitmapHeight: Int,
        rotationDegrees: Int
    ): EffectiveDimensions {
        val normalizedDegrees = ((rotationDegrees % 360) + 360) % 360
        val isSwapped = (normalizedDegrees == 90 || normalizedDegrees == 270)
        val effectiveWidth = if (isSwapped) bitmapHeight.toFloat() else bitmapWidth.toFloat()
        val effectiveHeight = if (isSwapped) bitmapWidth.toFloat() else bitmapHeight.toFloat()
        return EffectiveDimensions(effectiveWidth, effectiveHeight)
    }

    fun calculateCropSize(
        containerWidth: Float,
        containerHeight: Float,
        marginPx: Float = 64f
    ): Float {
        val minDim = min(containerWidth, containerHeight)
        if (minDim <= 0f) return 0f
        val desired = minDim - marginPx
        return desired.coerceIn(min(100f, minDim), minDim)
    }

    fun calculateMinScale(
        effectiveWidth: Float,
        effectiveHeight: Float,
        cropSizePx: Float
    ): Float {
        if (effectiveWidth <= 0f || effectiveHeight <= 0f || cropSizePx <= 0f) return 1f
        return max(cropSizePx / effectiveWidth, cropSizePx / effectiveHeight)
    }

    fun calculateMaxScale(minScale: Float, factor: Float = 5f): Float {
        return minScale * factor
    }

    fun calculatePanBounds(
        effectiveWidth: Float,
        effectiveHeight: Float,
        scale: Float,
        cropSizePx: Float
    ): PanBounds {
        val maxOffsetX = max(0f, (effectiveWidth * scale - cropSizePx) / 2f)
        val maxOffsetY = max(0f, (effectiveHeight * scale - cropSizePx) / 2f)
        return PanBounds(maxOffsetX, maxOffsetY)
    }

    fun calculateOutputMatrixParams(
        cropSizePx: Float,
        outputSizePx: Int,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        rotationDegrees: Int
    ): CropMatrixParams {
        val ratio = if (cropSizePx > 0f) outputSizePx.toFloat() / cropSizePx else 1f
        val destCenterX = outputSizePx / 2f + offsetX * ratio
        val destCenterY = outputSizePx / 2f + offsetY * ratio
        return CropMatrixParams(
            scaleInOutput = scale * ratio,
            destCenterX = destCenterX,
            destCenterY = destCenterY,
            rotationDegrees = rotationDegrees
        )
    }
}
