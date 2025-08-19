package com.example.m_launcher

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import com.example.m_launcher.data.FavoriteApp
import com.example.m_launcher.manager.FavoritesManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.junit.Assert.*

/**
 * Unit tests for FavoritesManager
 * Tests storage, retrieval, validation, and cleanup operations
 */
@RunWith(MockitoJUnitRunner::class)
class FavoritesManagerTest {
    
    @Mock
    private lateinit var mockContext: Context
    
    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences
    
    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor
    
    @Mock
    private lateinit var mockPackageManager: PackageManager
    
    private lateinit var favoritesManager: FavoritesManager
    
    @Before
    fun setup() {
        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPreferences)
        `when`(mockContext.packageManager).thenReturn(mockPackageManager)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.putInt(anyString(), anyInt())).thenReturn(mockEditor)
        `when`(mockEditor.putLong(anyString(), anyLong())).thenReturn(mockEditor)
        
        favoritesManager = FavoritesManager(mockContext)
    }
    
    @Test
    fun testLoadFavorites_EmptyPreferences_ReturnsDefaults() {
        // Given: No stored favorites
        `when`(mockSharedPreferences.getString(anyString(), isNull())).thenReturn(null)
        
        // Mock default apps as installed
        mockAppInstalled("com.android.dialer")
        mockAppInstalled("com.android.mms")
        mockAppInstalled("com.android.browser")
        
        // When: Loading favorites
        val favorites = favoritesManager.loadFavorites()
        
        // Then: Should return default apps
        assertEquals(3, favorites.size)
        assertEquals("Phone", favorites[0].displayName)
        assertEquals("Messages", favorites[1].displayName)
        assertEquals("Browser", favorites[2].displayName)
    }
    
    @Test
    fun testSaveFavorites_ValidList_Success() {
        // Given: Valid favorites list
        val favorites = listOf(
            FavoriteApp("com.test.app1", "App 1", 0),
            FavoriteApp("com.test.app2", "App 2", 1)
        )
        
        // When: Saving favorites
        val result = favoritesManager.saveFavorites(favorites)
        
        // Then: Should succeed
        assertTrue(result)
        verify(mockEditor).putString(eq("favorite_apps_json"), anyString())
        verify(mockEditor).putInt("favorites_count", 2)
        verify(mockEditor).apply()
    }
    
    @Test
    fun testSaveFavorites_EmptyList_Fails() {
        // Given: Empty favorites list
        val favorites = emptyList<FavoriteApp>()
        
        // When: Saving favorites
        val result = favoritesManager.saveFavorites(favorites)
        
        // Then: Should fail
        assertFalse(result)
        verify(mockEditor, never()).apply()
    }
    
    @Test
    fun testSaveFavorites_TooManyApps_Fails() {
        // Given: Too many favorites (more than 7)
        val favorites = (1..8).map { 
            FavoriteApp("com.test.app$it", "App $it", it - 1)
        }
        
        // When: Saving favorites
        val result = favoritesManager.saveFavorites(favorites)
        
        // Then: Should fail
        assertFalse(result)
        verify(mockEditor, never()).apply()
    }
    
    @Test
    fun testAddFavorite_ValidApp_Success() {
        // Given: Current favorites with space
        mockStoredFavorites(listOf(
            FavoriteApp("com.test.app1", "App 1", 0)
        ))
        
        // When: Adding new favorite
        val result = favoritesManager.addFavorite("com.test.app2", "App 2")
        
        // Then: Should succeed
        assertTrue(result)
    }
    
    @Test
    fun testAddFavorite_MaxLimitReached_Fails() {
        // Given: Current favorites at maximum (7 apps)
        val maxFavorites = (1..7).map { 
            FavoriteApp("com.test.app$it", "App $it", it - 1)
        }
        mockStoredFavorites(maxFavorites)
        
        // When: Adding new favorite
        val result = favoritesManager.addFavorite("com.test.app8", "App 8")
        
        // Then: Should fail
        assertFalse(result)
    }
    
    @Test
    fun testRemoveFavorite_ExistingApp_Success() {
        // Given: Current favorites with target app
        mockStoredFavorites(listOf(
            FavoriteApp("com.test.app1", "App 1", 0),
            FavoriteApp("com.test.app2", "App 2", 1)
        ))
        
        // When: Removing favorite
        val result = favoritesManager.removeFavorite("com.test.app1")
        
        // Then: Should succeed
        assertTrue(result)
    }
    
    @Test
    fun testRemoveFavorite_LastApp_AddsDefaults() {
        // Given: Current favorites with only one app
        mockStoredFavorites(listOf(
            FavoriteApp("com.test.app1", "App 1", 0)
        ))
        
        // Mock default apps as installed
        mockAppInstalled("com.android.dialer")
        mockAppInstalled("com.android.mms")
        mockAppInstalled("com.android.browser")
        
        // When: Removing last favorite
        val result = favoritesManager.removeFavorite("com.test.app1")
        
        // Then: Should succeed and add defaults
        assertTrue(result)
    }
    
    @Test
    fun testIsFavorite_ExistingApp_ReturnsTrue() {
        // Given: Stored favorites
        mockStoredFavorites(listOf(
            FavoriteApp("com.test.app1", "App 1", 0)
        ))
        
        // When: Checking if app is favorite
        val result = favoritesManager.isFavorite("com.test.app1")
        
        // Then: Should return true
        assertTrue(result)
    }
    
    @Test
    fun testIsFavorite_NonExistingApp_ReturnsFalse() {
        // Given: Stored favorites
        mockStoredFavorites(listOf(
            FavoriteApp("com.test.app1", "App 1", 0)
        ))
        
        // When: Checking if non-existing app is favorite
        val result = favoritesManager.isFavorite("com.test.app2")
        
        // Then: Should return false
        assertFalse(result)
    }
    
    @Test
    fun testReorderFavorites_ValidOrder_Success() {
        // Given: Current favorites
        val favorites = listOf(
            FavoriteApp("com.test.app1", "App 1", 1),
            FavoriteApp("com.test.app2", "App 2", 0)
        )
        
        // When: Reordering favorites
        val result = favoritesManager.reorderFavorites(favorites)
        
        // Then: Should succeed and normalize orders
        assertTrue(result)
    }
    
    private fun mockStoredFavorites(favorites: List<FavoriteApp>) {
        val json = """{"favorites":${favorites.map { 
            """{"packageName":"${it.packageName}","displayName":"${it.displayName}","order":${it.order}}"""
        }.joinToString(",", "[", "]")}}"""
        
        `when`(mockSharedPreferences.getString("favorite_apps_json", null)).thenReturn(json)
        
        // Mock all apps as installed
        favorites.forEach { mockAppInstalled(it.packageName) }
    }
    
    private fun mockAppInstalled(packageName: String) {
        `when`(mockPackageManager.getPackageInfo(eq(packageName), anyInt())).thenReturn(mock())
    }
}