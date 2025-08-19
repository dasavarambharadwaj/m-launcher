package com.example.m_launcher

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.m_launcher.data.FavoriteApp
import com.example.m_launcher.manager.FavoritesManager
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Integration tests for favorites functionality
 * Tests the complete flow from storage to UI display
 */
@RunWith(AndroidJUnit4::class)
class FavoritesIntegrationTest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    private lateinit var context: Context
    private lateinit var favoritesManager: FavoritesManager
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        favoritesManager = FavoritesManager(context)
        
        // Clear any existing favorites for clean test state
        clearFavorites()
    }
    
    @Test
    fun testFavoritesFlow_SaveAndLoad_PersistsCorrectly() {
        // Given: Test favorites
        val testFavorites = listOf(
            FavoriteApp("com.android.settings", "Settings", 0),
            FavoriteApp("com.android.calculator2", "Calculator", 1)
        )
        
        // When: Saving favorites
        val saveResult = favoritesManager.saveFavorites(testFavorites)
        assertTrue("Should save successfully", saveResult)
        
        // Then: Loading should return same favorites
        val loadedFavorites = favoritesManager.loadFavorites()
        assertEquals("Should load same number of favorites", testFavorites.size, loadedFavorites.size)
        assertEquals("Should load same favorites", testFavorites, loadedFavorites)
    }
    
    @Test
    fun testFavoritesFlow_AddAndRemove_UpdatesCorrectly() {
        // Given: Initial favorite
        val initialResult = favoritesManager.addFavorite("com.android.settings", "Settings")
        assertTrue("Should add initial favorite", initialResult)
        
        // When: Adding another favorite
        val addResult = favoritesManager.addFavorite("com.android.calculator2", "Calculator")
        assertTrue("Should add second favorite", addResult)
        
        // Then: Should have 2 favorites
        assertEquals("Should have 2 favorites", 2, favoritesManager.getFavoritesCount())
        assertTrue("Settings should be favorite", favoritesManager.isFavorite("com.android.settings"))
        assertTrue("Calculator should be favorite", favoritesManager.isFavorite("com.android.calculator2"))
        
        // When: Removing one favorite
        val removeResult = favoritesManager.removeFavorite("com.android.settings")
        assertTrue("Should remove favorite", removeResult)
        
        // Then: Should have 1 favorite
        assertEquals("Should have 1 favorite", 1, favoritesManager.getFavoritesCount())
        assertFalse("Settings should not be favorite", favoritesManager.isFavorite("com.android.settings"))
        assertTrue("Calculator should still be favorite", favoritesManager.isFavorite("com.android.calculator2"))
    }
    
    @Test
    fun testFavoritesFlow_MaxLimit_EnforcesCorrectly() {
        // Given: Add maximum favorites (7)
        for (i in 1..7) {
            val result = favoritesManager.addFavorite("com.test.app$i", "App $i")
            assertTrue("Should add app $i", result)
        }
        
        // When: Trying to add 8th favorite
        val overLimitResult = favoritesManager.addFavorite("com.test.app8", "App 8")
        
        // Then: Should fail
        assertFalse("Should not add 8th favorite", overLimitResult)
        assertEquals("Should have exactly 7 favorites", 7, favoritesManager.getFavoritesCount())
    }
    
    @Test
    fun testFavoritesFlow_Reorder_UpdatesCorrectly() {
        // Given: Initial favorites
        val initialFavorites = listOf(
            FavoriteApp("com.test.app1", "App 1", 0),
            FavoriteApp("com.test.app2", "App 2", 1),
            FavoriteApp("com.test.app3", "App 3", 2)
        )
        favoritesManager.saveFavorites(initialFavorites)
        
        // When: Reordering favorites
        val reorderedFavorites = listOf(
            FavoriteApp("com.test.app3", "App 3", 0),
            FavoriteApp("com.test.app1", "App 1", 1),
            FavoriteApp("com.test.app2", "App 2", 2)
        )
        val reorderResult = favoritesManager.reorderFavorites(reorderedFavorites)
        
        // Then: Should succeed and maintain new order
        assertTrue("Should reorder successfully", reorderResult)
        
        val loadedFavorites = favoritesManager.loadFavorites()
        assertEquals("Should have same number of favorites", 3, loadedFavorites.size)
        assertEquals("First app should be App 3", "App 3", loadedFavorites[0].displayName)
        assertEquals("Second app should be App 1", "App 1", loadedFavorites[1].displayName)
        assertEquals("Third app should be App 2", "App 2", loadedFavorites[2].displayName)
    }
    
    @Test
    fun testFavoritesFlow_ResetToDefaults_RestoresDefaults() {
        // Given: Custom favorites
        val customFavorites = listOf(
            FavoriteApp("com.test.custom", "Custom App", 0)
        )
        favoritesManager.saveFavorites(customFavorites)
        
        // When: Resetting to defaults
        val resetResult = favoritesManager.resetToDefaults()
        
        // Then: Should restore default favorites
        assertTrue("Should reset successfully", resetResult)
        
        val loadedFavorites = favoritesManager.loadFavorites()
        assertTrue("Should have at least one default favorite", loadedFavorites.isNotEmpty())
        
        // Check if any of the default apps are present
        val hasDefaultApp = loadedFavorites.any { favorite ->
            FavoriteApp.DEFAULT_FAVORITES.any { default ->
                default.packageName == favorite.packageName
            }
        }
        assertTrue("Should contain default apps", hasDefaultApp)
    }
    
    @Test
    fun testFavoritesFlow_PersistenceAcrossRestarts_MaintainsData() {
        // Given: Saved favorites
        val testFavorites = listOf(
            FavoriteApp("com.android.settings", "Settings", 0),
            FavoriteApp("com.android.calculator2", "Calculator", 1)
        )
        favoritesManager.saveFavorites(testFavorites)
        
        // When: Creating new FavoritesManager instance (simulating app restart)
        val newFavoritesManager = FavoritesManager(context)
        val loadedFavorites = newFavoritesManager.loadFavorites()
        
        // Then: Should load same favorites
        assertEquals("Should persist across restarts", testFavorites.size, loadedFavorites.size)
        assertEquals("Should maintain same data", testFavorites, loadedFavorites)
    }
    
    @Test
    fun testMainActivity_LoadsFavorites_DisplaysCorrectly() {
        // Given: Saved favorites
        val testFavorites = listOf(
            FavoriteApp("com.android.settings", "Settings", 0),
            FavoriteApp("com.android.calculator2", "Calculator", 1)
        )
        favoritesManager.saveFavorites(testFavorites)
        
        // When: Activity loads
        activityRule.scenario.onActivity { activity ->
            val appListView = activity.findViewById<AppListView>(R.id.app_list_view)
            
            // Then: Should display correct favorites
            assertEquals("Should display correct number of apps", 
                testFavorites.size, appListView.getAppCount())
            
            val currentFavorites = appListView.getCurrentFavorites()
            assertEquals("Should display same favorites", testFavorites, currentFavorites)
        }
    }
    
    private fun clearFavorites() {
        // Clear SharedPreferences
        val prefs = context.getSharedPreferences("launcher_favorites", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}