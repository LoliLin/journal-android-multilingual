package com.isaakhanimann.journal.ui.tabs.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TestAvatarCropMath {

    @Test
    fun effectiveDimensionsSwapOn90And270Degrees() {
        val (w0, h0) = AvatarCropMath.calculateEffectiveDimensions(1200, 800, 0)
        assertEquals(1200f, w0, 0.001f)
        assertEquals(800f, h0, 0.001f)

        val (w90, h90) = AvatarCropMath.calculateEffectiveDimensions(1200, 800, 90)
        assertEquals(800f, w90, 0.001f)
        assertEquals(1200f, h90, 0.001f)

        val (w180, h180) = AvatarCropMath.calculateEffectiveDimensions(1200, 800, 180)
        assertEquals(1200f, w180, 0.001f)
        assertEquals(800f, h180, 0.001f)

        val (w270, h270) = AvatarCropMath.calculateEffectiveDimensions(1200, 800, 270)
        assertEquals(800f, w270, 0.001f)
        assertEquals(1200f, h270, 0.001f)

        // Negative angles and wrap-around
        val (wNeg90, hNeg90) = AvatarCropMath.calculateEffectiveDimensions(1200, 800, -90)
        assertEquals(800f, wNeg90, 0.001f)
        assertEquals(1200f, hNeg90, 0.001f)
    }

    @Test
    fun minScaleEnsuresBothDimensionsCoverCropCircle() {
        // Landscape image: 1000 x 500, crop size 400
        // Width scale to cover: 400 / 1000 = 0.4
        // Height scale to cover: 400 / 500 = 0.8
        // minScale must be 0.8 so that height covers 400
        val minScaleLandscape = AvatarCropMath.calculateMinScale(1000f, 500f, 400f)
        assertEquals(0.8f, minScaleLandscape, 0.001f)
        assertTrue(1000f * minScaleLandscape >= 400f)
        assertTrue(500f * minScaleLandscape >= 400f)

        // Portrait image: 600 x 1200, crop size 400
        // Width scale to cover: 400 / 600 = 0.6667
        // Height scale to cover: 400 / 1200 = 0.3333
        // minScale must be 400 / 600
        val minScalePortrait = AvatarCropMath.calculateMinScale(600f, 1200f, 400f)
        assertEquals(400f / 600f, minScalePortrait, 0.001f)
        assertTrue(600f * minScalePortrait >= 400f)
        assertTrue(1200f * minScalePortrait >= 400f)
    }

    @Test
    fun panBoundsClampedProperly() {
        // 1000 x 500 at scale 0.8 with crop size 400
        // scaled dimensions: 800 x 400
        // X can pan by: (800 - 400) / 2 = 200
        // Y can pan by: (400 - 400) / 2 = 0
        val bounds = AvatarCropMath.calculatePanBounds(
            effectiveWidth = 1000f,
            effectiveHeight = 500f,
            scale = 0.8f,
            cropSizePx = 400f
        )
        assertEquals(200f, bounds.maxOffsetX, 0.001f)
        assertEquals(0f, bounds.maxOffsetY, 0.001f)

        // At 2x zoom: scale = 1.6
        // scaled dimensions: 1600 x 800
        // X can pan by: (1600 - 400) / 2 = 600
        // Y can pan by: (800 - 400) / 2 = 200
        val boundsZoomed = AvatarCropMath.calculatePanBounds(
            effectiveWidth = 1000f,
            effectiveHeight = 500f,
            scale = 1.6f,
            cropSizePx = 400f
        )
        assertEquals(600f, boundsZoomed.maxOffsetX, 0.001f)
        assertEquals(200f, boundsZoomed.maxOffsetY, 0.001f)
    }

    @Test
    fun outputMatrixParamsAreProportional() {
        val params = AvatarCropMath.calculateOutputMatrixParams(
            cropSizePx = 400f,
            outputSizePx = 512,
            scale = 0.8f,
            offsetX = 100f,
            offsetY = -50f,
            rotationDegrees = 90
        )
        val expectedRatio = 512f / 400f // 1.28
        assertEquals(0.8f * expectedRatio, params.scaleInOutput, 0.001f)
        assertEquals(256f + 100f * expectedRatio, params.destCenterX, 0.001f)
        assertEquals(256f - 50f * expectedRatio, params.destCenterY, 0.001f)
        assertEquals(90, params.rotationDegrees)
    }
}
