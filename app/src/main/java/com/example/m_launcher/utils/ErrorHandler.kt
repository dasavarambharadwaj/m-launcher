package com.example.m_launcher.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.m_launcher.R

/**
 * Centralized error handling utility for the launcher
 * Provides user-friendly error messages and logging
 */
object ErrorHandler {
    
    private const val TAG = "ErrorHandler"
    
    /**
     * Handle app launch errors
     */
    fun handleAppLaunchError(context: Context, appName: String, error: Throwable) {
        Log.e(TAG, "Failed to launch app: $appName", error)
        showUserMessage(context, "Could not launch $appName")
    }
    
    /**
     * Handle favorites loading errors
     */
    fun handleFavoritesLoadError(context: Context, error: Throwable) {
        Log.e(TAG, "Failed to load favorites", error)
        showUserMessage(context, "Could not load favorite apps")
    }
    
    /**
     * Handle favorites saving errors
     */
    fun handleFavoritesSaveError(context: Context, error: Throwable) {
        Log.e(TAG, "Failed to save favorites", error)
        showUserMessage(context, "Could not save favorite apps")
    }
    
    /**
     * Handle settings navigation errors
     */
    fun handleSettingsNavigationError(context: Context, error: Throwable) {
        Log.e(TAG, "Failed to open settings", error)
        showUserMessage(context, "Could not open settings")
    }
    
    /**
     * Handle search navigation errors
     */
    fun handleSearchNavigationError(context: Context, error: Throwable) {
        Log.e(TAG, "Failed to open search interface", error)
        showUserMessage(context, "Could not open search")
    }
    
    /**
     * Handle app repository errors
     */
    fun handleAppRepositoryError(context: Context, error: Throwable) {
        Log.e(TAG, "App repository error", error)
        showUserMessage(context, "Could not load installed apps")
    }
    
    /**
     * Handle wallpaper contrast errors
     */
    fun handleWallpaperContrastError(context: Context, error: Throwable) {
        Log.e(TAG, "Wallpaper contrast error", error)
        // Don't show user message for wallpaper errors as they're not critical
    }
    
    /**
     * Handle gesture detection errors
     */
    fun handleGestureError(context: Context, error: Throwable) {
        Log.e(TAG, "Gesture detection error", error)
        // Don't show user message for gesture errors as they're not critical
    }
    
    /**
     * Handle validation errors
     */
    fun handleValidationError(context: Context, message: String) {
        Log.w(TAG, "Validation error: $message")
        showUserMessage(context, message)
    }
    
    /**
     * Handle maximum favorites limit
     */
    fun handleMaxFavoritesReached(context: Context) {
        Log.w(TAG, "Maximum favorites limit reached")
        showUserMessage(context, "Maximum 7 favorite apps allowed")
    }
    
    /**
     * Handle minimum favorites requirement
     */
    fun handleMinFavoritesRequired(context: Context) {
        Log.w(TAG, "Minimum favorites requirement not met")
        showUserMessage(context, "At least 1 favorite app is required")
    }
    
    /**
     * Handle app uninstallation cleanup
     */
    fun handleAppUninstalled(context: Context, appName: String) {
        Log.i(TAG, "App uninstalled and removed from favorites: $appName")
        showUserMessage(context, "$appName was removed from favorites")
    }
    
    /**
     * Handle corrupted data reset
     */
    fun handleDataReset(context: Context) {
        Log.w(TAG, "Corrupted data detected, resetting to defaults")
        showUserMessage(context, "Settings reset to defaults")
    }
    
    /**
     * Show user-friendly message
     */
    private fun showUserMessage(context: Context, message: String) {
        try {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Could not show user message: $message", e)
        }
    }
    
    /**
     * Log debug information
     */
    fun logDebug(tag: String, message: String) {
        Log.d("$TAG:$tag", message)
    }
    
    /**
     * Log warning information
     */
    fun logWarning(tag: String, message: String) {
        Log.w("$TAG:$tag", message)
    }
}