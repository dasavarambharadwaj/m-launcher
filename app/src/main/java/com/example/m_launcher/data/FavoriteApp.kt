package com.example.m_launcher.data

import kotlinx.serialization.Serializable

/**
 * Data class representing a user-configured favorite app
 * Used for storing and displaying favorite apps on the home screen
 */
@Serializable
data class FavoriteApp(
    val packageName: String,
    val displayName: String,
    val order: Int
) {
    companion object {
        const val MIN_FAVORITES = 1
        const val MAX_FAVORITES = 7
        
        /**
         * Default favorite apps when no configuration exists
         */
        val DEFAULT_FAVORITES = listOf(
            FavoriteApp("com.android.dialer", "Phone", 0),
            FavoriteApp("com.android.mms", "Messages", 1),
            FavoriteApp("com.android.browser", "Browser", 2)
        )
    }
    
    /**
     * Validate if this favorite app is properly configured
     */
    fun isValid(): Boolean {
        return packageName.isNotBlank() && 
               displayName.isNotBlank() && 
               order >= 0
    }
}