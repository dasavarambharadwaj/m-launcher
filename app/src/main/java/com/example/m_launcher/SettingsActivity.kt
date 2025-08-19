package com.example.m_launcher

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.m_launcher.adapter.FavoritesAdapter
import com.example.m_launcher.adapter.InstalledAppsAdapter
import com.example.m_launcher.data.FavoriteApp
import com.example.m_launcher.data.InstalledApp
import com.example.m_launcher.manager.FavoritesManager
import com.example.m_launcher.repository.AppRepository
import com.example.m_launcher.utils.ErrorHandler
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

/**
 * Settings activity for configuring favorite apps
 * Maintains Material Expressive design consistency with the launcher
 */
class SettingsActivity : AppCompatActivity() {
    
    private lateinit var favoritesManager: FavoritesManager
    private lateinit var appRepository: AppRepository
    private lateinit var favoritesAdapter: FavoritesAdapter
    private lateinit var installedAppsAdapter: InstalledAppsAdapter
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var installedAppsRecyclerView: RecyclerView
    private lateinit var saveButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    
    private var currentFavorites = mutableListOf<FavoriteApp>()
    private var installedApps = listOf<InstalledApp>()
    
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
        
        // Set up RecyclerViews
        setupRecyclerViews()
        
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
        appRepository = AppRepository(this)
        
        // Initialize views
        toolbar = findViewById(R.id.toolbar)
        favoritesRecyclerView = findViewById(R.id.favorites_recycler_view)
        installedAppsRecyclerView = findViewById(R.id.installed_apps_recycler_view)
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
            title = "Favorites Settings"
        }
    }
    
    /**
     * Set up RecyclerViews for favorites and installed apps
     */
    private fun setupRecyclerViews() {
        // Set up favorites RecyclerView with drag-and-drop
        favoritesAdapter = FavoritesAdapter(
            favorites = currentFavorites,
            onRemoveFavorite = { favorite ->
                removeFavorite(favorite)
            }
        )
        
        favoritesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SettingsActivity)
            adapter = favoritesAdapter
        }
        
        // Set up drag-and-drop for favorites reordering
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                
                // Reorder favorites
                if (fromPosition < currentFavorites.size && toPosition < currentFavorites.size) {
                    val movedItem = currentFavorites.removeAt(fromPosition)
                    currentFavorites.add(toPosition, movedItem)
                    
                    // Update orders
                    currentFavorites.forEachIndexed { index, favorite ->
                        currentFavorites[index] = favorite.copy(order = index)
                    }
                    
                    favoritesAdapter.notifyItemMoved(fromPosition, toPosition)
                    return true
                }
                return false
            }
            
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Remove favorite on swipe
                val position = viewHolder.adapterPosition
                if (position < currentFavorites.size) {
                    removeFavorite(currentFavorites[position])
                }
            }
        })
        
        itemTouchHelper.attachToRecyclerView(favoritesRecyclerView)
        
        // Set up installed apps RecyclerView
        installedAppsAdapter = InstalledAppsAdapter(
            apps = installedApps,
            selectedPackages = currentFavorites.map { it.packageName }.toSet(),
            onAppSelected = { app ->
                addFavorite(app)
            },
            onAppDeselected = { app ->
                removeFavoriteByPackage(app.packageName)
            }
        )
        
        installedAppsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SettingsActivity)
            adapter = installedAppsAdapter
        }
    }
    
    /**
     * Load current favorites and installed apps
     */
    private fun loadData() {
        // Load current favorites
        currentFavorites.clear()
        currentFavorites.addAll(favoritesManager.loadFavorites())
        favoritesAdapter.notifyDataSetChanged()
        
        // Load installed apps asynchronously
        lifecycleScope.launch {
            try {
                installedApps = appRepository.getAllLaunchableApps()
                
                // Update adapter with selected packages
                installedAppsAdapter.updateApps(
                    apps = installedApps,
                    selectedPackages = currentFavorites.map { it.packageName }.toSet()
                )
                
                Log.d(TAG, "Loaded ${installedApps.size} installed apps")
            } catch (e: Exception) {
                ErrorHandler.handleAppRepositoryError(this@SettingsActivity, e)
            }
        }
    }
    
    /**
     * Set up button click listeners
     */
    private fun setupButtonListeners() {
        saveButton.setOnClickListener {
            saveFavorites()
        }
        
        cancelButton.setOnClickListener {
            finish()
        }
    }
    
    /**
     * Add an app to favorites
     */
    private fun addFavorite(app: InstalledApp) {
        if (currentFavorites.size >= FavoriteApp.MAX_FAVORITES) {
            ErrorHandler.handleMaxFavoritesReached(this)
            return
        }
        
        // Check if already in favorites
        if (currentFavorites.any { it.packageName == app.packageName }) {
            return
        }
        
        val newFavorite = app.toFavoriteApp(currentFavorites.size)
        currentFavorites.add(newFavorite)
        
        // Update adapters
        favoritesAdapter.notifyItemInserted(currentFavorites.size - 1)
        installedAppsAdapter.updateSelectedPackages(
            currentFavorites.map { it.packageName }.toSet()
        )
        
        Log.d(TAG, "Added favorite: ${app.displayName}")
    }
    
    /**
     * Remove a favorite app
     */
    private fun removeFavorite(favorite: FavoriteApp) {
        val index = currentFavorites.indexOf(favorite)
        if (index >= 0) {
            currentFavorites.removeAt(index)
            
            // Update orders for remaining favorites
            currentFavorites.forEachIndexed { newIndex, fav ->
                currentFavorites[newIndex] = fav.copy(order = newIndex)
            }
            
            // Update adapters
            favoritesAdapter.notifyItemRemoved(index)
            favoritesAdapter.notifyItemRangeChanged(index, currentFavorites.size - index)
            installedAppsAdapter.updateSelectedPackages(
                currentFavorites.map { it.packageName }.toSet()
            )
            
            Log.d(TAG, "Removed favorite: ${favorite.displayName}")
        }
    }
    
    /**
     * Remove favorite by package name
     */
    private fun removeFavoriteByPackage(packageName: String) {
        val favorite = currentFavorites.find { it.packageName == packageName }
        if (favorite != null) {
            removeFavorite(favorite)
        }
    }
    
    /**
     * Save favorites and return to home screen
     */
    private fun saveFavorites() {
        try {
            // Ensure at least one favorite
            if (currentFavorites.isEmpty()) {
                ErrorHandler.handleMinFavoritesRequired(this)
                currentFavorites.addAll(favoritesManager.getDefaultFavorites())
            }
            
            // Save favorites
            val success = favoritesManager.saveFavorites(currentFavorites)
            
            if (success) {
                Log.d(TAG, "Favorites saved successfully")
                setResult(RESULT_OK)
                finish()
            } else {
                ErrorHandler.handleFavoritesSaveError(this, RuntimeException("Save operation failed"))
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