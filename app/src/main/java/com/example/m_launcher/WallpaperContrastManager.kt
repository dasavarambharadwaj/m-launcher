package com.example.m_launcher

import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlin.math.sqrt

/**
 * WallpaperContrastManager - Manages dynamic text color based on wallpaper luminance
 * Implements wallpaper sampling and contrast calculation for optimal text readability
 */
class WallpaperContrastManager(
    private val context: Context,
    private val onTextColorChanged: (Int) -> Unit
) : DefaultLifecycleObserver {

    private val wallpaperManager = WallpaperManager.getInstance(context)
    private var wallpaperChangeReceiver: BroadcastReceiver? = null
    
    // Text colors for light and dark backgrounds
    private val lightTextColor = ContextCompat.getColor(context, R.color.launcher_text_light)
    private val darkTextColor = ContextCompat.getColor(context, R.color.launcher_text_dark)
    
    // Luminance threshold for determining text color (0.5 = 50% luminance)
    private val luminanceThreshold = 0.5f
    
    // Sample area size for wallpaper analysis (in pixels)
    private val sampleSize = 100
    
    companion object {
        private const val TAG = "WallpaperContrastManager"
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        registerWallpaperChangeListener()
        updateTextColorFromWallpaper()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        unregisterWallpaperChangeListener()
    }

    /**
     * Register broadcast receiver to listen for wallpaper changes
     */
    private fun registerWallpaperChangeListener() {
        wallpaperChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_WALLPAPER_CHANGED) {
                    updateTextColorFromWallpaper()
                }
            }
        }
        
        val filter = IntentFilter(Intent.ACTION_WALLPAPER_CHANGED)
        context.registerReceiver(wallpaperChangeReceiver, filter)
    }

    /**
     * Unregister wallpaper change listener
     */
    private fun unregisterWallpaperChangeListener() {
        wallpaperChangeReceiver?.let { receiver ->
            try {
                context.unregisterReceiver(receiver)
            } catch (e: IllegalArgumentException) {
                // Receiver was not registered, ignore
            }
        }
        wallpaperChangeReceiver = null
    }

    /**
     * Update text color based on current wallpaper luminance
     */
    fun updateTextColorFromWallpaper() {
        try {
            val wallpaperDrawable = wallpaperManager.drawable
            if (wallpaperDrawable != null) {
                val luminance = calculateWallpaperLuminance(wallpaperDrawable)
                val textColor = if (luminance > luminanceThreshold) {
                    darkTextColor // Dark text on light background
                } else {
                    lightTextColor // Light text on dark background
                }
                Log.d(TAG, "Wallpaper luminance: $luminance, using ${if (luminance > luminanceThreshold) "dark" else "light"} text")
                onTextColorChanged(textColor)
            } else {
                Log.d(TAG, "No wallpaper drawable available, using light text")
                // Fallback to light text if wallpaper is not available
                onTextColorChanged(lightTextColor)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating text color from wallpaper", e)
            // Fallback to light text on error
            onTextColorChanged(lightTextColor)
        }
    }

    /**
     * Calculate the average luminance of the wallpaper
     * Samples the center area of the wallpaper for analysis
     */
    private fun calculateWallpaperLuminance(drawable: Drawable): Float {
        val bitmap = drawableToBitmap(drawable)
        return if (bitmap != null) {
            sampleWallpaperLuminance(bitmap)
        } else {
            0.0f // Default to dark background assumption
        }
    }

    /**
     * Convert drawable to bitmap for pixel analysis
     */
    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        return try {
            if (drawable is BitmapDrawable) {
                drawable.bitmap
            } else {
                // Create bitmap from drawable
                val bitmap = Bitmap.createBitmap(
                    sampleSize,
                    sampleSize,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Sample wallpaper luminance from center area where text will be displayed
     */
    private fun sampleWallpaperLuminance(bitmap: Bitmap): Float {
        val width = bitmap.width
        val height = bitmap.height
        
        // Sample from center area (where app list will be displayed)
        val centerX = width / 2
        val centerY = height / 2
        val sampleRadius = minOf(width, height) / 4
        
        var totalLuminance = 0.0
        var sampleCount = 0
        
        // Sample pixels in a grid pattern around the center
        val step = maxOf(1, sampleRadius / 10) // Sample ~100 pixels
        
        for (x in (centerX - sampleRadius) until (centerX + sampleRadius) step step) {
            for (y in (centerY - sampleRadius) until (centerY + sampleRadius) step step) {
                if (x in 0 until width && y in 0 until height) {
                    val pixel = bitmap.getPixel(x, y)
                    val luminance = calculatePixelLuminance(pixel)
                    totalLuminance += luminance
                    sampleCount++
                }
            }
        }
        
        return if (sampleCount > 0) {
            (totalLuminance / sampleCount).toFloat()
        } else {
            0.0f
        }
    }

    /**
     * Calculate luminance of a single pixel using relative luminance formula
     * Based on ITU-R BT.709 standard for RGB to luminance conversion
     */
    private fun calculatePixelLuminance(pixel: Int): Double {
        val red = Color.red(pixel) / 255.0
        val green = Color.green(pixel) / 255.0
        val blue = Color.blue(pixel) / 255.0
        
        // Apply gamma correction
        val r = if (red <= 0.03928) red / 12.92 else Math.pow((red + 0.055) / 1.055, 2.4)
        val g = if (green <= 0.03928) green / 12.92 else Math.pow((green + 0.055) / 1.055, 2.4)
        val b = if (blue <= 0.03928) blue / 12.92 else Math.pow((blue + 0.055) / 1.055, 2.4)
        
        // Calculate relative luminance
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    /**
     * Force update text color (useful for manual refresh)
     */
    fun forceUpdate() {
        Log.d(TAG, "Force updating text color from wallpaper")
        updateTextColorFromWallpaper()
    }
    
    /**
     * Get current text colors for testing
     */
    fun getCurrentTextColors(): Pair<Int, Int> {
        return Pair(lightTextColor, darkTextColor)
    }
}