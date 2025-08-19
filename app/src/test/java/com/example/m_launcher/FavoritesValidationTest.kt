package com.example.m_launcher

import com.example.m_launcher.data.FavoriteApp
import com.example.m_launcher.data.FavoritesValidation
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for FavoritesValidation
 * Tests validation logic for favorite app configurations
 */
class FavoritesValidationTest {
    
    @Test
    fun testValidateFavorites_ValidList_Success() {
        // Given: Valid favorites list
        val favorites = listOf(
            FavoriteApp("com.test.app1", "App 1", 0),
            FavoriteApp("com.test.app2", "App 2", 1),
            FavoriteApp("com.test.app3", "App 3", 2)
        )
        
        // When: Validating favorites
        val result = FavoritesValidation.validateFavorites(favorites)
        
        // Then: Should be valid
        assertTrue(result.isValid())
        assertTrue(result is FavoritesValidation.ValidationResult.Success)
    }
    
    @Test
    fun testValidateFavorites_EmptyList_Error() {
        // Given: Empty favorites list
        val favorites = emptyList<FavoriteApp>()
        
        // When: Validating favorites
        val result = FavoritesValidation.validateFavorites(favorites)
        
        // Then: Should be invalid
        assertFalse(result.isValid())
        assertTrue(result is FavoritesValidation.ValidationResult.Error)
        assertEquals("At least one favorite app must be selected", 
            (result as FavoritesValidation.ValidationResult.Error).message)
    }
    
    @Test
    fun testValidateFavorites_TooManyApps_Error() {
        // Given: Too many favorites (8 apps)
        val favorites = (1..8).map { 
            FavoriteApp("com.test.app$it", "App $it", it - 1)
        }
        
        // When: Validating favorites
        val result = FavoritesValidation.validateFavorites(favorites)
        
        // Then: Should be invalid
        assertFalse(result.isValid())
        assertTrue(result is FavoritesValidation.ValidationResult.Error)
        assertEquals("Maximum 7 favorite apps allowed", 
            (result as FavoritesValidation.ValidationResult.Error).message)
    }
    
    @Test
    fun testValidateFavorites_DuplicateApps_Error() {
        // Given: Favorites with duplicate package names
        val favorites = listOf(
            FavoriteApp("com.test.app1", "App 1", 0),
            FavoriteApp("com.test.app1", "App 1 Duplicate", 1)
        )
        
        // When: Validating favorites
        val result = FavoritesValidation.validateFavorites(favorites)
        
        // Then: Should be invalid
        assertFalse(result.isValid())
        assertTrue(result is FavoritesValidation.ValidationResult.Error)
        assertEquals("Duplicate apps are not allowed in favorites", 
            (result as FavoritesValidation.ValidationResult.Error).message)
    }
    
    @Test
    fun testValidateFavorites_InvalidApp_Error() {
        // Given: Favorites with invalid app (empty package name)
        val favorites = listOf(
            FavoriteApp("", "Invalid App", 0)
        )
        
        // When: Validating favorites
        val result = FavoritesValidation.validateFavorites(favorites)
        
        // Then: Should be invalid
        assertFalse(result.isValid())
        assertTrue(result is FavoritesValidation.ValidationResult.Error)
        assertTrue((result as FavoritesValidation.ValidationResult.Error).message.contains("Invalid favorite app"))
    }
    
    @Test
    fun testValidateFavorites_WrongOrderSequence_Warning() {
        // Given: Favorites with non-sequential orders
        val favorites = listOf(
            FavoriteApp("com.test.app1", "App 1", 2),
            FavoriteApp("com.test.app2", "App 2", 5),
            FavoriteApp("com.test.app3", "App 3", 1)
        )
        
        // When: Validating favorites
        val result = FavoritesValidation.validateFavorites(favorites)
        
        // Then: Should be valid with warning
        assertTrue(result.isValid())
        assertTrue(result is FavoritesValidation.ValidationResult.Warning)
        assertEquals("Favorite app order will be normalized", 
            (result as FavoritesValidation.ValidationResult.Warning).message)
    }
    
    @Test
    fun testNormalizeFavoriteOrders_UnorderedList_NormalizesCorrectly() {
        // Given: Favorites with wrong orders
        val favorites = listOf(
            FavoriteApp("com.test.app1", "App 1", 5),
            FavoriteApp("com.test.app2", "App 2", 1),
            FavoriteApp("com.test.app3", "App 3", 3)
        )
        
        // When: Normalizing orders
        val normalized = FavoritesValidation.normalizeFavoriteOrders(favorites)
        
        // Then: Should have correct sequential orders
        assertEquals(3, normalized.size)
        assertEquals(0, normalized[0].order) // App 2 (originally order 1)
        assertEquals(1, normalized[1].order) // App 3 (originally order 3)
        assertEquals(2, normalized[2].order) // App 1 (originally order 5)
        
        assertEquals("App 2", normalized[0].displayName)
        assertEquals("App 3", normalized[1].displayName)
        assertEquals("App 1", normalized[2].displayName)
    }
    
    @Test
    fun testFavoriteApp_IsValid_ValidApp_ReturnsTrue() {
        // Given: Valid favorite app
        val favorite = FavoriteApp("com.test.app", "Test App", 0)
        
        // When: Checking validity
        val isValid = favorite.isValid()
        
        // Then: Should be valid
        assertTrue(isValid)
    }
    
    @Test
    fun testFavoriteApp_IsValid_EmptyPackageName_ReturnsFalse() {
        // Given: Favorite app with empty package name
        val favorite = FavoriteApp("", "Test App", 0)
        
        // When: Checking validity
        val isValid = favorite.isValid()
        
        // Then: Should be invalid
        assertFalse(isValid)
    }
    
    @Test
    fun testFavoriteApp_IsValid_EmptyDisplayName_ReturnsFalse() {
        // Given: Favorite app with empty display name
        val favorite = FavoriteApp("com.test.app", "", 0)
        
        // When: Checking validity
        val isValid = favorite.isValid()
        
        // Then: Should be invalid
        assertFalse(isValid)
    }
    
    @Test
    fun testFavoriteApp_IsValid_NegativeOrder_ReturnsFalse() {
        // Given: Favorite app with negative order
        val favorite = FavoriteApp("com.test.app", "Test App", -1)
        
        // When: Checking validity
        val isValid = favorite.isValid()
        
        // Then: Should be invalid
        assertFalse(isValid)
    }
}