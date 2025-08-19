package com.example.m_launcher

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.m_launcher.data.FavoriteApp
import com.example.m_launcher.manager.FavoritesManager
import com.example.m_launcher.utils.ErrorHandler

class MainActivity : AppCompatActivity() {
    
    private lateinit var wallpaperContrastManager: WallpaperContrastManager
    private lateinit var appListView: AppListView
    private lateinit var favoritesManager: FavoritesManager
    private lateinit var gestureDetector: GestureDetector
    private lateinit var vibrator: Vibrator
    private lateinit var rootView: View
    
    companion object {
        private const val TAG = "MainActivity"
        private const val SETTINGS_REQUEST_CODE = 1001
        private const val SEARCH_REQUEST_CODE = 1002
        private const val LONG_PRESS_DURATION_MS = 500L
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set the main launcher layout
        setContentView(R.layout.activity_main)
        
        // Initialize views and managers
        initializeComponents()
        
        // Apply Material Expressive LauncherTheme and configure launcher window
        setupMaterialExpressiveLauncher()
        
        // Configure full-screen immersive mode with Android 16 optimizations
        setupFullScreenImmersiveMode()
        
        // Set up transparent window background to show device wallpaper
        setupWallpaperTransparency()
        
        // Initialize dynamic text contrast system
        setupDynamicTextContrast()
        
        // Set up long press gesture detection
        setupLongPressGesture()
        
        // Load and display favorite apps
        loadFavoriteApps()
    }
    
    override fun onResume() {
        super.onResume()
        // Ensure launcher behavior is maintained when returning to home screen
        setupFullScreenImmersiveMode()
        
        // Update text contrast when returning to launcher
        if (::wallpaperContrastManager.isInitialized) {
            wallpaperContrastManager.forceUpdate()
        }
        
        // Refresh favorites in case they were changed in settings
        loadFavoriteApps()
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
    
    /**
     * Initialize components and managers
     */
    private fun initializeComponents() {
        // Initialize views
        appListView = findViewById(R.id.app_list_view)
        rootView = findViewById(android.R.id.content)
        
        // Initialize managers
        favoritesManager = FavoritesManager(this)
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        
        // Set up app click listener
        appListView.setOnAppClickListener { favorite ->
            launchApp(favorite)
        }
        
        // Listen for favorites changes
        favoritesManager.addFavoritesChangeListener {
            loadFavoriteApps()
        }
    }
    
    /**
     * Set up gesture detection for settings access and search interface
     */
    private fun setupLongPressGesture() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                Log.d(TAG, "Long press detected, opening settings")
                
                // Provide haptic feedback
                performHapticFeedback()
                
                // Navigate to settings
                openSettings()
            }
            
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                
                val deltaY = e2.y - e1.y
                val deltaX = e2.x - e1.x
                
                // Check for swipe up gesture
                if (Math.abs(deltaY) > Math.abs(deltaX) && 
                    deltaY < -100 && // Minimum swipe distance (upward)
                    Math.abs(velocityY) > 500) { // Minimum velocity threshold
                    
                    Log.d(TAG, "Swipe up detected, opening search")
                    
                    // Provide haptic feedback for swipe gesture
                    performHapticFeedback()
                    
                    // Launch search interface
                    openSearch()
                    
                    return true
                }
                
                return false
            }
            
            override fun onDown(e: MotionEvent): Boolean {
                // Return true to indicate we want to handle gestures
                return true
            }
        })
        
        // Set touch listener on root view to detect gestures anywhere on screen
        rootView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
        
        Log.d(TAG, "Gesture detection configured (long press and swipe up)")
    }
    
    /**
     * Load and display favorite apps
     */
    private fun loadFavoriteApps() {
        try {
            val favorites = favoritesManager.loadFavorites()
            Log.d(TAG, "Loaded ${favorites.size} favorite apps: ${favorites.map { it.displayName }}")
            
            if (favorites.isEmpty()) {
                Log.w(TAG, "No favorites loaded, this should not happen")
                // Force load defaults if somehow we get empty list
                val defaults = favoritesManager.getDefaultFavorites()
                appListView.updateFavorites(defaults)
            } else {
                appListView.updateFavorites(favorites)
            }
            
            // Update text contrast for new apps
            if (::wallpaperContrastManager.isInitialized) {
                wallpaperContrastManager.forceUpdate()
            }
            
            Log.d(TAG, "AppListView updated with ${appListView.getAppCount()} apps")
        } catch (e: Exception) {
            ErrorHandler.handleFavoritesLoadError(this, e)
            // Fallback to defaults on error
            try {
                val defaults = favoritesManager.getDefaultFavorites()
                appListView.updateFavorites(defaults)
                Log.d(TAG, "Loaded default apps as fallback")
            } catch (fallbackError: Exception) {
                Log.e(TAG, "Error loading default apps", fallbackError)
            }
        }
    }
    
    /**
     * Launch an app from favorites
     */
    private fun launchApp(favorite: FavoriteApp) {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(favorite.packageName)
            if (launchIntent != null) {
                Log.d(TAG, "Launching app: ${favorite.displayName}")
                startActivity(launchIntent)
            } else {
                Log.w(TAG, "No launch intent found for ${favorite.packageName}")
                ErrorHandler.handleAppLaunchError(this, favorite.displayName, 
                    RuntimeException("No launch intent available"))
            }
        } catch (e: Exception) {
            ErrorHandler.handleAppLaunchError(this, favorite.displayName, e)
        }
    }
    
    /**
     * Perform haptic feedback for long press gesture
     */
    private fun performHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Use VibrationEffect for Android 8.0+
                val vibrationEffect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                // Fallback for older versions
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
            Log.d(TAG, "Haptic feedback performed")
        } catch (e: Exception) {
            Log.w(TAG, "Could not perform haptic feedback", e)
        }
    }
    
    /**
     * Open settings activity with smooth transition
     */
    private fun openSettings() {
        try {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivityForResult(intent, SETTINGS_REQUEST_CODE)
            
            // Apply smooth transition animation
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            
            Log.d(TAG, "Settings activity launched")
        } catch (e: Exception) {
            ErrorHandler.handleSettingsNavigationError(this, e)
        }
    }
    
    /**
     * Open search interface with Material Expressive animations
     */
    private fun openSearch() {
        try {
            val intent = Intent(this, SearchActivity::class.java)
            startActivityForResult(intent, SEARCH_REQUEST_CODE)
            
            // Apply Material Expressive transition animation for search interface
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            
            Log.d(TAG, "Search activity launched with Material Expressive transitions")
        } catch (e: Exception) {
            Log.e(TAG, "Error launching search activity", e)
            ErrorHandler.handleSearchNavigationError(this, e)
        }
    }
    
    /**
     * Handle result from settings and search activities
     */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            SETTINGS_REQUEST_CODE -> {
                Log.d(TAG, "Returned from settings, refreshing favorites")
                // Refresh favorites when returning from settings
                loadFavoriteApps()
            }
            SEARCH_REQUEST_CODE -> {
                Log.d(TAG, "Returned from search interface")
                // Ensure home screen is properly restored after search
                setupFullScreenImmersiveMode()
                
                // Update text contrast in case wallpaper changed while in search
                if (::wallpaperContrastManager.isInitialized) {
                    wallpaperContrastManager.forceUpdate()
                }
            }
        }
    }
    
    /**
     * Initialize dynamic text contrast system based on wallpaper luminance
     */
    private fun setupDynamicTextContrast() {
        wallpaperContrastManager = WallpaperContrastManager(this) { textColor ->
            // Update AppListView text color when wallpaper changes
            appListView.updateTextColor(textColor)
        }
        
        // Register lifecycle observer to manage wallpaper change listener
        lifecycle.addObserver(wallpaperContrastManager)
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Override back button behavior for launcher
        // Launchers typically don't respond to back button or minimize other apps
        // This prevents users from accidentally leaving the launcher
        // Do nothing - don't call super.onBackPressed()
    }
}