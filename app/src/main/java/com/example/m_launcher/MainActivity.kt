package com.example.m_launcher

import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set the main launcher layout
        setContentView(R.layout.activity_main)
        
        // Apply Material Expressive LauncherTheme and configure launcher window
        setupMaterialExpressiveLauncher()
        
        // Configure full-screen immersive mode with Android 16 optimizations
        setupFullScreenImmersiveMode()
        
        // Set up transparent window background to show device wallpaper
        setupWallpaperTransparency()
        
        // Basic launcher activity setup - content will be added in later tasks
        // For now, we just set up the activity structure
    }
    
    override fun onResume() {
        super.onResume()
        // Ensure launcher behavior is maintained when returning to home screen
        setupFullScreenImmersiveMode()
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Re-apply immersive mode when window gains focus
            setupFullScreenImmersiveMode()
        }
    }
    
    /**
     * Configure Material Expressive launcher theme and basic window setup
     */
    private fun setupMaterialExpressiveLauncher() {
        // Material Expressive theme is already applied via AndroidManifest.xml
        // Configure window for launcher behavior with Material Expressive styling
        
        // Enable edge-to-edge display for Material Expressive design
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Configure window flags for launcher with wallpaper visibility
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
            WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER
        )
    }
    
    /**
     * Configure full-screen immersive mode with Android 16 optimizations
     */
    private fun setupFullScreenImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+) - Use WindowInsetsController for Android 16 optimizations
            val windowInsetsController = window.insetsController
            windowInsetsController?.let { controller ->
                // Configure system bars behavior for launcher
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                
                // Keep system bars visible but allow immersive experience
                // This provides better launcher behavior on Android 16
                controller.show(WindowInsets.Type.systemBars())
            }
        } else {
            // Fallback for older Android versions
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.systemBarsBehavior = 
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
    
    /**
     * Set up transparent window background using Material Expressive theming to show device wallpaper
     */
    private fun setupWallpaperTransparency() {
        // Configure transparent colors for system bars
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        
        // Enable drawing behind system bars for wallpaper visibility
        window.setFlags(
            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        )
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Override back button behavior for launcher
        // Launchers typically don't respond to back button or minimize other apps
        // This prevents users from accidentally leaving the launcher
        // Do nothing - don't call super.onBackPressed()
    }
}