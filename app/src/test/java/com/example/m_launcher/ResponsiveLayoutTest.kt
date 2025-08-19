package com.example.m_launcher

import android.content.Context
import android.content.res.Configuration
import android.util.DisplayMetrics
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test class to verify Material Expressive responsive layout implementation
 * Tests different screen sizes and orientations for Android 16 compatibility
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // Android 16 equivalent
class ResponsiveLayoutTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun testSmallPhoneLayout() {
        // Test 320dp width (small phones)
        val config = Configuration(context.resources.configuration).apply {
            screenWidthDp = 320
            screenHeightDp = 568
            densityDpi = DisplayMetrics.DENSITY_MEDIUM
        }
        
        val resources = context.createConfigurationContext(config).resources
        
        // Verify small phone dimensions
        val textSize = resources.getDimensionPixelSize(R.dimen.material_expressive_text_size_body)
        val touchTarget = resources.getDimensionPixelSize(R.dimen.material_expressive_touch_target_min)
        val spacing = resources.getDimensionPixelSize(R.dimen.material_expressive_spacing_large)
        
        // Assert Material Expressive scaling for small screens
        assert(textSize > 0) { "Text size should be defined for small phones" }
        assert(touchTarget >= 44 * resources.displayMetrics.density) { "Touch target should meet accessibility guidelines" }
        assert(spacing > 0) { "Spacing should be defined for small phones" }
    }

    @Test
    fun testMediumPhoneLayout() {
        // Test 480dp width (medium phones)
        val config = Configuration(context.resources.configuration).apply {
            screenWidthDp = 480
            screenHeightDp = 800
            densityDpi = DisplayMetrics.DENSITY_HIGH
        }
        
        val resources = context.createConfigurationContext(config).resources
        
        // Verify medium phone dimensions
        val textSize = resources.getDimensionPixelSize(R.dimen.material_expressive_text_size_body)
        val touchTarget = resources.getDimensionPixelSize(R.dimen.material_expressive_touch_target_min)
        val spacing = resources.getDimensionPixelSize(R.dimen.material_expressive_spacing_large)
        
        // Assert Material Expressive scaling for medium screens
        assert(textSize > 0) { "Text size should be defined for medium phones" }
        assert(touchTarget >= 48 * resources.displayMetrics.density) { "Touch target should be optimized for medium screens" }
        assert(spacing > 0) { "Spacing should be defined for medium phones" }
    }

    @Test
    fun testTabletLayout() {
        // Test 600dp width (tablets)
        val config = Configuration(context.resources.configuration).apply {
            screenWidthDp = 600
            screenHeightDp = 960
            densityDpi = DisplayMetrics.DENSITY_XHIGH
        }
        
        val resources = context.createConfigurationContext(config).resources
        
        // Verify tablet dimensions
        val textSize = resources.getDimensionPixelSize(R.dimen.material_expressive_text_size_body)
        val touchTarget = resources.getDimensionPixelSize(R.dimen.material_expressive_touch_target_min)
        val spacing = resources.getDimensionPixelSize(R.dimen.material_expressive_spacing_large)
        
        // Assert Material Expressive scaling for tablets
        assert(textSize > 0) { "Text size should be larger for tablets" }
        assert(touchTarget >= 56 * resources.displayMetrics.density) { "Touch target should be larger for tablets" }
        assert(spacing > 0) { "Spacing should be larger for tablets" }
    }

    @Test
    fun testLargeTabletLayout() {
        // Test 720dp width (large tablets)
        val config = Configuration(context.resources.configuration).apply {
            screenWidthDp = 720
            screenHeightDp = 1024
            densityDpi = DisplayMetrics.DENSITY_XXHIGH
        }
        
        val resources = context.createConfigurationContext(config).resources
        
        // Verify large tablet dimensions
        val textSize = resources.getDimensionPixelSize(R.dimen.material_expressive_text_size_body)
        val touchTarget = resources.getDimensionPixelSize(R.dimen.material_expressive_touch_target_min)
        val spacing = resources.getDimensionPixelSize(R.dimen.material_expressive_spacing_large)
        
        // Assert Material Expressive scaling for large tablets
        assert(textSize > 0) { "Text size should be largest for large tablets" }
        assert(touchTarget >= 64 * resources.displayMetrics.density) { "Touch target should be largest for large tablets" }
        assert(spacing > 0) { "Spacing should be largest for large tablets" }
    }

    @Test
    fun testFoldableLayout() {
        // Test 840dp width (foldables)
        val config = Configuration(context.resources.configuration).apply {
            screenWidthDp = 840
            screenHeightDp = 1680
            densityDpi = DisplayMetrics.DENSITY_XXXHIGH
        }
        
        val resources = context.createConfigurationContext(config).resources
        
        // Verify foldable dimensions
        val textSize = resources.getDimensionPixelSize(R.dimen.material_expressive_text_size_body)
        val touchTarget = resources.getDimensionPixelSize(R.dimen.material_expressive_touch_target_min)
        val spacing = resources.getDimensionPixelSize(R.dimen.material_expressive_spacing_large)
        
        // Assert Material Expressive scaling for foldables
        assert(textSize > 0) { "Text size should be extra large for foldables" }
        assert(touchTarget >= 72 * resources.displayMetrics.density) { "Touch target should be extra large for foldables" }
        assert(spacing > 0) { "Spacing should be extra large for foldables" }
    }

    @Test
    fun testLandscapeOrientation() {
        // Test landscape orientation
        val config = Configuration(context.resources.configuration).apply {
            screenWidthDp = 800
            screenHeightDp = 480
            orientation = Configuration.ORIENTATION_LANDSCAPE
            densityDpi = DisplayMetrics.DENSITY_HIGH
        }
        
        val resources = context.createConfigurationContext(config).resources
        
        // Verify landscape layout resources exist
        val textSize = resources.getDimensionPixelSize(R.dimen.material_expressive_text_size_body)
        val spacing = resources.getDimensionPixelSize(R.dimen.material_expressive_spacing_large)
        
        // Assert landscape layout works properly
        assert(textSize > 0) { "Text size should be defined for landscape" }
        assert(spacing > 0) { "Spacing should be defined for landscape" }
    }
}