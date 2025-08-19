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
import com.example.m_launcher.adapter.FavoritesAdapter
import com.example.m_launcher.adapter.InstalledAppsAdapter
import com.example.m_launcher.data.FavoriteApp
import com.example.m_launcher.data.InstalledApp
import com.example.m_launcher.data.GestureConfig
import com.example.m_launcher.data.LayoutConfig
import com.example.m_launcher.data.HorizontalPosition
import com.example.m_launcher.data.VerticalPosition
import com.example.m_launcher.manager.FavoritesManager
import com.example.m_launcher.repository.AppRepository
import com.example.m_launcher.manager.SettingsManager
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
    private lateinit var settingsManager: SettingsManager
    private lateinit var appRepository: AppRepository
    private lateinit var favoritesAdapter: FavoritesAdapter
    private lateinit var installedAppsAdapter: InstalledAppsAdapter
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var installedAppsRecyclerView: RecyclerView
    private lateinit var saveButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    private lateinit var leftSwipePicker: MaterialButton
    private lateinit var rightSwipePicker: MaterialButton
    private lateinit var positionHorizontalPicker: MaterialButton
    private lateinit var positionVerticalPicker: MaterialButton
    private lateinit var layoutPreview: android.widget.LinearLayout
    private lateinit var previewItem1: android.widget.TextView
    private lateinit var previewItem2: android.widget.TextView
    private lateinit var previewItem3: android.widget.TextView
    
    private var currentFavorites = mutableListOf<FavoriteApp>()
    private var installedApps = listOf<InstalledApp>()
    private var gestureConfig: GestureConfig = GestureConfig()
    private var layoutConfig: LayoutConfig = LayoutConfig()
    
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
        settingsManager = SettingsManager(this)
        appRepository = AppRepository(this)
        
        // Initialize views
        toolbar = findViewById(R.id.toolbar)
        favoritesRecyclerView = findViewById(R.id.favorites_recycler_view)
        installedAppsRecyclerView = findViewById(R.id.installed_apps_recycler_view)
        saveButton = findViewById(R.id.save_button)
        cancelButton = findViewById(R.id.cancel_button)
        leftSwipePicker = findViewById(R.id.left_swipe_picker)
        rightSwipePicker = findViewById(R.id.right_swipe_picker)
        positionHorizontalPicker = findViewById(R.id.position_horizontal_picker)
        positionVerticalPicker = findViewById(R.id.position_vertical_picker)
        layoutPreview = findViewById(R.id.layout_preview)
        previewItem1 = findViewById(R.id.preview_item_1)
        previewItem2 = findViewById(R.id.preview_item_2)
        previewItem3 = findViewById(R.id.preview_item_3)
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

        // Load gesture configuration
        gestureConfig = settingsManager.loadGestureConfig()
        updateGesturePickersText()

        // Load layout configuration
        layoutConfig = settingsManager.loadLayoutConfig()
        updateLayoutPickersText()
        
        // Load installed apps asynchronously
        lifecycleScope.launch {
            try {
                installedApps = appRepository.getAllLaunchableApps()

                // Clean up gesture selections if apps are uninstalled
                val leftInstalled = gestureConfig.leftSwipePackage?.let { pkg ->
                    installedApps.any { it.packageName == pkg }
                } ?: true
                val rightInstalled = gestureConfig.rightSwipePackage?.let { pkg ->
                    installedApps.any { it.packageName == pkg }
                } ?: true

                if (!leftInstalled || !rightInstalled) {
                    if (!leftInstalled) {
                        gestureConfig = gestureConfig.copy(leftSwipePackage = null, leftSwipeName = null)
                        ErrorHandler.handleAppUninstalled(this@SettingsActivity, "Left swipe app")
                    }
                    if (!rightInstalled) {
                        gestureConfig = gestureConfig.copy(rightSwipePackage = null, rightSwipeName = null)
                        ErrorHandler.handleAppUninstalled(this@SettingsActivity, "Right swipe app")
                    }
                }
                
                // Update adapter with selected packages
                installedAppsAdapter.updateApps(
                    apps = installedApps,
                    selectedPackages = currentFavorites.map { it.packageName }.toSet()
                )
                
                updateGesturePickersText()
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

        leftSwipePicker.setOnClickListener {
            showGestureAppPicker(isLeft = true)
        }

        rightSwipePicker.setOnClickListener {
            showGestureAppPicker(isLeft = false)
        }

        positionHorizontalPicker.setOnClickListener {
            showHorizontalPositionPicker()
        }

        positionVerticalPicker.setOnClickListener {
            showVerticalPositionPicker()
        }
    }

    private fun updateGesturePickersText() {
        leftSwipePicker.text = gestureConfig.leftSwipeName ?: "Select app"
        rightSwipePicker.text = gestureConfig.rightSwipeName ?: "Select app"
    }

    private fun showGestureAppPicker(isLeft: Boolean) {
        val items = installedApps.map { it.displayName }.toTypedArray()
        val context = this
        val builder = android.app.AlertDialog.Builder(context)
            .setTitle(if (isLeft) "Choose app for Left Swipe" else "Choose app for Right Swipe")
            .setItems(items) { dialog, which ->
                val app = installedApps[which]
                if (isLeft) {
                    gestureConfig = gestureConfig.copy(
                        leftSwipePackage = app.packageName,
                        leftSwipeName = app.displayName
                    )
                } else {
                    gestureConfig = gestureConfig.copy(
                        rightSwipePackage = app.packageName,
                        rightSwipeName = app.displayName
                    )
                }
                updateGesturePickersText()
            }
            .setNegativeButton("Cancel", null)

        // Allow clearing selection if set
        val hasSelection = if (isLeft) gestureConfig.leftSwipePackage != null else gestureConfig.rightSwipePackage != null
        if (hasSelection) {
            builder.setNeutralButton("Clear") { _, _ ->
                gestureConfig = if (isLeft) {
                    gestureConfig.copy(leftSwipePackage = null, leftSwipeName = null)
                } else {
                    gestureConfig.copy(rightSwipePackage = null, rightSwipeName = null)
                }
                updateGesturePickersText()
            }
        }

        builder.show()
    }

    private fun updateLayoutPickersText() {
        positionHorizontalPicker.text = when (layoutConfig.horizontalPosition) {
            HorizontalPosition.LEFT -> "Left"
            HorizontalPosition.CENTER -> "Center"
            HorizontalPosition.RIGHT -> "Right"
        }
        positionVerticalPicker.text = when (layoutConfig.verticalPosition) {
            VerticalPosition.TOP -> "Top"
            VerticalPosition.CENTER -> "Center"
            VerticalPosition.BOTTOM -> "Bottom"
        }

        // Update preview alignment
        val parentParams = (layoutPreview.layoutParams as FrameLayout.LayoutParams?)
        parentParams?.let { params ->
            params.gravity = when (layoutConfig.horizontalPosition) {
                HorizontalPosition.LEFT -> android.view.Gravity.START
                HorizontalPosition.CENTER -> android.view.Gravity.CENTER_HORIZONTAL
                HorizontalPosition.RIGHT -> android.view.Gravity.END
            } or when (layoutConfig.verticalPosition) {
                VerticalPosition.TOP -> android.view.Gravity.TOP
                VerticalPosition.CENTER -> android.view.Gravity.CENTER_VERTICAL
                VerticalPosition.BOTTOM -> android.view.Gravity.BOTTOM
            }
            layoutPreview.layoutParams = params
        }

        layoutPreview.gravity = when (layoutConfig.horizontalPosition) {
            HorizontalPosition.LEFT -> android.view.Gravity.START
            HorizontalPosition.CENTER -> android.view.Gravity.CENTER_HORIZONTAL
            HorizontalPosition.RIGHT -> android.view.Gravity.END
        }

        val childGravity = when (layoutConfig.horizontalPosition) {
            HorizontalPosition.LEFT -> android.view.Gravity.START
            HorizontalPosition.CENTER -> android.view.Gravity.CENTER
            HorizontalPosition.RIGHT -> android.view.Gravity.END
        }
        previewItem1.gravity = childGravity
        previewItem2.gravity = childGravity
        previewItem3.gravity = childGravity
    }

    private fun showHorizontalPositionPicker() {
        val items = arrayOf("Left", "Center", "Right")
        android.app.AlertDialog.Builder(this)
            .setTitle("Horizontal Position")
            .setSingleChoiceItems(items, when (layoutConfig.horizontalPosition) {
                HorizontalPosition.LEFT -> 0
                HorizontalPosition.CENTER -> 1
                HorizontalPosition.RIGHT -> 2
            }) { dialog, which ->
                layoutConfig = layoutConfig.copy(
                    horizontalPosition = when (which) {
                        0 -> HorizontalPosition.LEFT
                        1 -> HorizontalPosition.CENTER
                        else -> HorizontalPosition.RIGHT
                    }
                )
                updateLayoutPickersText()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showVerticalPositionPicker() {
        val items = arrayOf("Top", "Center", "Bottom")
        android.app.AlertDialog.Builder(this)
            .setTitle("Vertical Position")
            .setSingleChoiceItems(items, when (layoutConfig.verticalPosition) {
                VerticalPosition.TOP -> 0
                VerticalPosition.CENTER -> 1
                VerticalPosition.BOTTOM -> 2
            }) { dialog, which ->
                layoutConfig = layoutConfig.copy(
                    verticalPosition = when (which) {
                        0 -> VerticalPosition.TOP
                        1 -> VerticalPosition.CENTER
                        else -> VerticalPosition.BOTTOM
                    }
                )
                updateLayoutPickersText()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
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
            val favoritesSaved = favoritesManager.saveFavorites(currentFavorites)
            val gestureSaved = settingsManager.saveGestureConfig(gestureConfig)
            val layoutSaved = settingsManager.saveLayoutConfig(layoutConfig)

            if (favoritesSaved && gestureSaved && layoutSaved) {
                Log.d(TAG, "Settings saved successfully")
                setResult(RESULT_OK)
                finish()
            } else {
                if (!favoritesSaved) ErrorHandler.handleFavoritesSaveError(this, RuntimeException("Save operation failed"))
                if (!gestureSaved) ErrorHandler.handleValidationError(this, "Could not save gesture configuration")
                if (!layoutSaved) ErrorHandler.handleValidationError(this, "Could not save layout configuration")
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