package com.example.m_launcher.data

/**
 * Validation utilities for favorite app configuration
 */
object FavoritesValidation {
    
    /**
     * Validate a list of favorite apps
     */
    fun validateFavorites(favorites: List<FavoriteApp>): ValidationResult {
        // Check count limits
        if (favorites.isEmpty()) {
            return ValidationResult.Error("At least one favorite app must be selected")
        }
        
        if (favorites.size > FavoriteApp.MAX_FAVORITES) {
            return ValidationResult.Error("Maximum ${FavoriteApp.MAX_FAVORITES} favorite apps allowed")
        }
        
        // Check for duplicates
        val packageNames = favorites.map { it.packageName }
        if (packageNames.size != packageNames.distinct().size) {
            return ValidationResult.Error("Duplicate apps are not allowed in favorites")
        }
        
        // Check individual app validity
        favorites.forEach { favorite ->
            if (!favorite.isValid()) {
                return ValidationResult.Error("Invalid favorite app: ${favorite.displayName}")
            }
        }
        
        // Check order sequence
        val orders = favorites.map { it.order }.sorted()
        if (orders != (0 until favorites.size).toList()) {
            return ValidationResult.Warning("Favorite app order will be normalized")
        }
        
        return ValidationResult.Success
    }
    
    /**
     * Normalize favorite app orders to ensure proper sequence (0, 1, 2, ...)
     */
    fun normalizeFavoriteOrders(favorites: List<FavoriteApp>): List<FavoriteApp> {
        return favorites.sortedBy { it.order }.mapIndexed { index, favorite ->
            favorite.copy(order = index)
        }
    }
    
    /**
     * Result of favorites validation
     */
    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Warning(val message: String) : ValidationResult()
        data class Error(val message: String) : ValidationResult()
        
        fun isValid(): Boolean = this is Success || this is Warning
    }
}