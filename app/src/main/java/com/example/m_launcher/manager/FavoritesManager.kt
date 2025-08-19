package com.example.m_launcher.manager

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import com.example.m_launcher.data.FavoriteApp
import com.example.m_launcher.data.FavoritesValidation
import com.example.m_launcher.utils.ErrorHandler
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Manages favorite app storage, retrieval, and persistence using SharedPreferences
 * Handles validation, default fallbacks, and automatic cleanup of uninstalled apps
 */
class FavoritesManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val packageManager: PackageManager = context.packageManager
    private val json = Json { ignoreUnknownKeys = true }
    
    private var favoritesChangeListeners = mutableListOf<() -> Unit>()
    
    companion object {
        private const val TAG = "FavoritesManager"
        private const val PREFS_NAME = "launcher_favorites"
        private const val KEY_FAVORITE_APPS = "favorite_apps_json"
        private const val KEY_FAVORITES_COUNT = "favorites_count"
        private const val KEY_LAST_UPDATE = "last_update_timestamp"
    }
    
    /**
     * Load favorite apps from storage with validation and cleanup
     */
    fun loadFavorites(): List<FavoriteApp> {
        return try {
            val favoritesJson = prefs.getString(KEY_FAVORITE_APPS, null)
            
            if (favoritesJson.isNullOrBlank()) {
                Log.d(TAG, "No favorites found, returning defaults")
                return getDefaultFavorites()
            }
            
            val favorites = json.decodeFromString<List<FavoriteApp>>(favoritesJson)
            val cleanedFavorites = cleanupUninstalledApps(favorites)
            
            // If cleanup removed apps, save the cleaned list and notify user
            if (cleanedFavorites.size != favorites.size) {
                val removedCount = favorites.size - cleanedFavorites.size
                Log.d(TAG, "Cleaned up $removedCount uninstalled apps")
                
                // Find removed apps for user notification
                val removedApps = favorites.filter { favorite ->
                    !cleanedFavorites.any { it.packageName == favorite.packageName }
                }
                
                saveFavorites(cleanedFavorites)
                
                // Notify user about removed apps
                removedApps.forEach { app ->
                    ErrorHandler.handleAppUninstalled(context, app.displayName)
                }
            }
            
            // Validate and return
            val validationResult = FavoritesValidation.validateFavorites(cleanedFavorites)
            if (validationResult.isValid()) {
                FavoritesValidation.normalizeFavoriteOrders(cleanedFavorites)
            } else {
                Log.w(TAG, "Invalid favorites found, returning defaults")
                ErrorHandler.handleDataReset(context)
                getDefaultFavorites()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading favorites", e)
            getDefaultFavorites()
        }
    }
    
    /**
     * Save favorite apps to storage with validation
     */
    fun saveFavorites(favorites: List<FavoriteApp>): Boolean {
        return try {
            // Validate favorites
            val validationResult = FavoritesValidation.validateFavorites(favorites)
            if (!validationResult.isValid()) {
                Log.e(TAG, "Cannot save invalid favorites: ${(validationResult as FavoritesValidation.ValidationResult.Error).message}")
                return false
            }
            
            // Normalize order and serialize
            val normalizedFavorites = FavoritesValidation.normalizeFavoriteOrders(favorites)
            val favoritesJson = json.encodeToString(normalizedFavorites)
            
            // Save to SharedPreferences
            prefs.edit()
                .putString(KEY_FAVORITE_APPS, favoritesJson)
                .putInt(KEY_FAVORITES_COUNT, normalizedFavorites.size)
                .putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
                .apply()
            
            Log.d(TAG, "Saved ${normalizedFavorites.size} favorite apps")
            
            // Notify listeners
            notifyFavoritesChanged()
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving favorites", e)
            false
        }
    }
    
    /**
     * Get default favorite apps (Phone, Messages, Browser)
     */
    fun getDefaultFavorites(): List<FavoriteApp> {
        return FavoriteApp.DEFAULT_FAVORITES.filter { favorite ->
            isAppInstalled(favorite.packageName)
        }.ifEmpty {
            // If none of the default apps are installed, return at least one entry
            listOf(FavoriteApp("android", "Settings", 0))
        }
    }
    
    /**
     * Add a new favorite app
     */
    fun addFavorite(packageName: String, displayName: String): Boolean {
        val currentFavorites = loadFavorites().toMutableList()
        
        // Check if already exists
        if (currentFavorites.any { it.packageName == packageName }) {
            Log.w(TAG, "App $packageName is already in favorites")
            return false
        }
        
        // Check limit
        if (currentFavorites.size >= FavoriteApp.MAX_FAVORITES) {
            Log.w(TAG, "Cannot add favorite, maximum limit reached")
            return false
        }
        
        // Add new favorite
        val newFavorite = FavoriteApp(
            packageName = packageName,
            displayName = displayName,
            order = currentFavorites.size
        )
        
        currentFavorites.add(newFavorite)
        return saveFavorites(currentFavorites)
    }
    
    /**
     * Remove a favorite app
     */
    fun removeFavorite(packageName: String): Boolean {
        val currentFavorites = loadFavorites().toMutableList()
        val removed = currentFavorites.removeAll { it.packageName == packageName }
        
        if (removed) {
            // If we removed the last app, add defaults
            if (currentFavorites.isEmpty()) {
                return saveFavorites(getDefaultFavorites())
            }
            return saveFavorites(currentFavorites)
        }
        
        return false
    }
    
    /**
     * Reorder favorite apps
     */
    fun reorderFavorites(favorites: List<FavoriteApp>): Boolean {
        val reorderedFavorites = favorites.mapIndexed { index, favorite ->
            favorite.copy(order = index)
        }
        return saveFavorites(reorderedFavorites)
    }
    
    /**
     * Check if an app is currently in favorites
     */
    fun isFavorite(packageName: String): Boolean {
        return loadFavorites().any { it.packageName == packageName }
    }
    
    /**
     * Get the number of current favorites
     */
    fun getFavoritesCount(): Int {
        return prefs.getInt(KEY_FAVORITES_COUNT, 0)
    }
    
    /**
     * Reset favorites to default configuration
     */
    fun resetToDefaults(): Boolean {
        return saveFavorites(getDefaultFavorites())
    }
    
    /**
     * Add listener for favorites changes
     */
    fun addFavoritesChangeListener(listener: () -> Unit) {
        favoritesChangeListeners.add(listener)
    }
    
    /**
     * Remove listener for favorites changes
     */
    fun removeFavoritesChangeListener(listener: () -> Unit) {
        favoritesChangeListeners.remove(listener)
    }
    
    /**
     * Clean up uninstalled apps from favorites list
     */
    private fun cleanupUninstalledApps(favorites: List<FavoriteApp>): List<FavoriteApp> {
        return favorites.filter { favorite ->
            isAppInstalled(favorite.packageName)
        }
    }
    
    /**
     * Check if an app is installed on the device
     */
    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    /**
     * Notify all listeners that favorites have changed
     */
    private fun notifyFavoritesChanged() {
        favoritesChangeListeners.forEach { listener ->
            try {
                listener()
            } catch (e: Exception) {
                Log.e(TAG, "Error notifying favorites change listener", e)
            }
        }
    }
}