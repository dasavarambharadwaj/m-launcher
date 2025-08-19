package com.example.m_launcher

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.example.m_launcher.adapter.FavoritesAdapter
import com.example.m_launcher.adapter.InstalledAppsAdapter
import com.example.m_launcher.data.FavoriteApp
import com.example.m_launcher.data.InstalledApp
import com.example.m_launcher.data.GestureConfig
import com.example.m_launcher.data.LayoutConfig
import com.example.m_launcher.data.HorizontalPosition
import com.example.m_launcher.data.VerticalPosition
import com.example.m_launcher.data.FontSize
import com.example.m_launcher.manager.FavoritesManager
import com.example.m_launcher.repository.AppRepository
import com.example.m_launcher.manager.SettingsManager
import com.example.m_launcher.utils.ErrorHandler
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import com.example.m_launcher.adapter.SettingsPagerAdapter

/**
 * Settings activity for configuring favorite apps
 * Maintains Material Expressive design consistency with the launcher
 */
class SettingsActivity : AppCompatActivity() {
    
    private lateinit var favoritesManager: FavoritesManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var appRepository: AppRepository
    private lateinit var favoritesAdapter: FavoritesAdapter
    private lateinit var installedAppsAdapter: InstalledAppsAdapter
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: SettingsPagerAdapter
    private lateinit var saveButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    
    // State loaded and saved via fragments
    
    companion object {
        private const val TAG = "SettingsActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        // Initialize components
        initializeComponents()
        
        // Apply Material Expressive theming
        setupMaterialExpressiveTheme()
        
        // Set up toolbar
        setupToolbar()
        
        // Set up tabs and pager
        setupTabs()
        
        // Load data
        loadData()
        
        // Set up button listeners
        setupButtonListeners()
    }
    
    /**
     * Initialize views and managers
     */
    private fun initializeComponents() {
        // Initialize managers
        favoritesManager = FavoritesManager(this)
        settingsManager = SettingsManager(this)
        appRepository = AppRepository(this)
        
        // Initialize views
        toolbar = findViewById(R.id.toolbar)
        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)
        saveButton = findViewById(R.id.save_button)
        cancelButton = findViewById(R.id.cancel_button)
    }
    
    /**
     * Apply Material Expressive theming consistent with launcher
     */
    private fun setupMaterialExpressiveTheme() {
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Configure window for wallpaper visibility with semi-transparent overlay
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
            WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER
        )
        
        // Configure transparent system bars
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
    }
    
    /**
     * Set up toolbar with Material Expressive styling
     */
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Launcher Settings"
        }
    }
    
    // Obsolete in tabbed UI – handled inside fragments
    private fun setupRecyclerViews() { }

    private fun setupTabs() {
        pagerAdapter = SettingsPagerAdapter(this)
        viewPager.adapter = pagerAdapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Favorites"
                1 -> "Gestures"
                else -> "Layout"
            }
        }.attach()
    }
    
    /**
     * Load current favorites and installed apps
     */
    private fun loadData() { /* Fragments load their own data */ }
    
    /**
     * Set up button click listeners
     */
    private fun setupButtonListeners() {
        saveButton.setOnClickListener { saveFavorites() }
        
        cancelButton.setOnClickListener { finish() }
    }

    private fun updateGesturePickersText() { }
    private fun showGestureAppPicker(isLeft: Boolean) { }
    private fun updateLayoutPickersText() { }
    private fun updateFontSizePickerText() { }
    private fun showHorizontalPositionPicker() { }
    private fun showVerticalPositionPicker() { }
    private fun showFontSizePicker() { }
    
    /**
     * Add an app to favorites
     */
    private fun addFavorite(app: InstalledApp) { }
    private fun removeFavorite(favorite: FavoriteApp) { }
    private fun removeFavoriteByPackage(packageName: String) { }
    
    /**
     * Save favorites and return to home screen
     */
    private fun saveFavorites() {
        try {
            // Collect state from fragments
            val favorites = pagerAdapter.getFavoritesFragment().getSelectedFavorites().toMutableList()
            val gestures = pagerAdapter.getGesturesFragment().getGestureConfig()
            val layout = pagerAdapter.getLayoutFragment().getLayoutConfig()
            val font = pagerAdapter.getLayoutFragment().getFontSize()

            // Ensure at least one favorite
            if (favorites.isEmpty()) {
                ErrorHandler.handleMinFavoritesRequired(this)
                favorites.addAll(favoritesManager.getDefaultFavorites())
            }

            // Save
            val favoritesSaved = favoritesManager.saveFavorites(favorites)
            val gestureSaved = settingsManager.saveGestureConfig(gestures)
            val layoutSaved = settingsManager.saveLayoutConfig(layout)
            val fontSaved = settingsManager.saveFontSize(font)

            if (favoritesSaved && gestureSaved && layoutSaved && fontSaved) {
                Log.d(TAG, "Settings saved successfully")
                setResult(RESULT_OK)
                finish()
            } else {
                if (!favoritesSaved) ErrorHandler.handleFavoritesSaveError(this, RuntimeException("Save operation failed"))
                if (!gestureSaved) ErrorHandler.handleValidationError(this, "Could not save gesture configuration")
                if (!layoutSaved) ErrorHandler.handleValidationError(this, "Could not save layout configuration")
                if (!fontSaved) ErrorHandler.handleValidationError(this, "Could not save font size")
            }
        } catch (e: Exception) {
            ErrorHandler.handleFavoritesSaveError(this, e)
        }
    }
    
    /**
     * Handle toolbar back button
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    /**
     * Handle back button press
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
    }
    
    override fun finish() {
        super.finish()
        // Apply smooth transition animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}