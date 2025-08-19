package com.example.m_launcher.data

/**
 * Enhanced data class representing an app entry with favorite status and ordering
 * Used throughout the launcher for app management and display
 */
data class AppEntry(
    val displayName: String,
    val packageName: String,
    val isEnabled: Boolean = true,
    val isFavorite: Boolean = false,
    val favoriteOrder: Int = -1
) {
    /**
     * Check if this app entry is valid for display
     */
    fun isValidForDisplay(): Boolean {
        return displayName.isNotBlank() && 
               packageName.isNotBlank() && 
               isEnabled
    }
    
    /**
     * Convert to FavoriteApp if this entry is marked as favorite
     */
    fun toFavoriteApp(): FavoriteApp? {
        return if (isFavorite && favoriteOrder >= 0) {
            FavoriteApp(
                packageName = packageName,
                displayName = displayName,
                order = favoriteOrder
            )
        } else {
            null
        }
    }
    
    /**
     * Create a copy with updated favorite status
     */
    fun withFavoriteStatus(isFavorite: Boolean, order: Int = -1): AppEntry {
        return copy(
            isFavorite = isFavorite,
            favoriteOrder = if (isFavorite) order else -1
        )
    }
}