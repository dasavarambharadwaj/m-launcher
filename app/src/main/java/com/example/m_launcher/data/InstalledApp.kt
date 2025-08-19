package com.example.m_launcher.data

import android.graphics.drawable.Drawable

/**
 * Data class representing an installed app available for favorite selection
 * Used in the settings page for app selection
 */
data class InstalledApp(
    val packageName: String,
    val displayName: String,
    val icon: Drawable? = null,
    val isLaunchable: Boolean = true,
    val isSystemApp: Boolean = false
) {
    /**
     * Check if this app can be added as a favorite
     */
    fun canBeAddedAsFavorite(): Boolean {
        return isLaunchable && packageName.isNotBlank() && displayName.isNotBlank()
    }
    
    /**
     * Convert to FavoriteApp with specified order
     */
    fun toFavoriteApp(order: Int): FavoriteApp {
        return FavoriteApp(
            packageName = packageName,
            displayName = displayName,
            order = order
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as InstalledApp
        return packageName == other.packageName
    }
    
    override fun hashCode(): Int {
        return packageName.hashCode()
    }
}