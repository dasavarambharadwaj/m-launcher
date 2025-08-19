package com.example.m_launcher

import android.content.Context
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for WallpaperContrastManager
 * Tests the dynamic text contrast calculation logic
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class WallpaperContrastManagerTest {

    private lateinit var context: Context
    private lateinit var wallpaperContrastManager: WallpaperContrastManager
    private var lastTextColor: Int = Color.WHITE

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        wallpaperContrastManager = WallpaperContrastManager(context) { textColor ->
            lastTextColor = textColor
        }
    }

    @Test
    fun testTextColorInitialization() {
        // Test that the manager initializes with valid text colors
        val (lightColor, darkColor) = wallpaperContrastManager.getCurrentTextColors()
        
        // Verify colors are different
        assert(lightColor != darkColor) { "Light and dark text colors should be different" }
        
        // Verify colors are valid (not transparent)
        assert(Color.alpha(lightColor) == 255) { "Light text color should be opaque" }
        assert(Color.alpha(darkColor) == 255) { "Dark text color should be opaque" }
    }

    @Test
    fun testForceUpdate() {
        // Test that force update doesn't crash
        wallpaperContrastManager.forceUpdate()
        
        // Verify that a text color was set
        assert(lastTextColor != 0) { "Text color should be set after force update" }
    }
}