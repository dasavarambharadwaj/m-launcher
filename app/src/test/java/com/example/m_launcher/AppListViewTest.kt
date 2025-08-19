package com.example.m_launcher

import android.content.Context
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.example.m_launcher.data.FavoriteApp
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*

/**
 * Unit tests for AppListView
 * Tests dynamic favorite display and layout adaptation
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppListViewTest {
    
    private lateinit var context: Context
    private lateinit var appListView: AppListView
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        appListView = AppListView(context)
    }
    
    @Test
    fun testUpdateFavorites_ValidList_UpdatesCorrectly() {
        // Given: Valid favorites list
        val favorites = listOf(
            FavoriteApp("com.test.app1", "App 1", 0),
            FavoriteApp("com.test.app2", "App 2", 1),
            FavoriteApp("com.test.app3", "App 3", 2)
        )
        
        // When: Updating favorites
        appListView.updateFavorites(favorites)
        
        // Then: Should update correctly
        assertEquals(3, appListView.getAppCount())
        assertEquals(favorites, appListView.getCurrentFavorites())
        assertEquals(3, appListView.getAppTextViews().size)
    }
    
    @Test
    fun testUpdateFavorites_EmptyList_DoesNotUpdate() {
        // Given: Initial favorites
        val initialFavorites = listOf(
            FavoriteApp("com.test.app1", "App 1", 0)
        )
        appListView.updateFavorites(initialFavorites)
        
        // When: Updating with empty list
        appListView.updateFavorites(emptyList())
        
        // Then: Should not update
        assertEquals(1, appListView.getAppCount())
        assertEquals(initialFavorites, appListView.getCurrentFavorites())
    }
    
    @Test
    fun testUpdateFavorites_TooManyApps_DoesNotUpdate() {
        // Given: Initial favorites
        val initialFavorites = listOf(
            FavoriteApp("com.test.app1", "App 1", 0)
        )
        appListView.updateFavorites(initialFavorites)
        
        // When: Updating with too many apps (8)
        val tooManyFavorites = (1..8).map { 
            FavoriteApp("com.test.app$it", "App $it", it - 1)
        }
        appListView.updateFavorites(tooManyFavorites)
        
        // Then: Should not update
        assertEquals(1, appListView.getAppCount())
        assertEquals(initialFavorites, appListView.getCurrentFavorites())
    }
    
    @Test
    fun testUpdateFavorites_SameList_DoesNotRecreateViews() {
        // Given: Initial favorites
        val favorites = listOf(
            FavoriteApp("com.test.app1", "App 1", 0),
            FavoriteApp("com.test.app2", "App 2", 1)
        )
        appListView.updateFavorites(favorites)
        val initialTextViews = appListView.getAppTextViews()
        
        // When: Updating with same list
        appListView.updateFavorites(favorites)
        
        // Then: Should not recreate views
        assertEquals(initialTextViews, appListView.getAppTextViews())
    }
    
    @Test
    fun testUpdateFavorites_SingleApp_CreatesCorrectLayout() {
        // Given: Single favorite app
        val favorites = listOf(
            FavoriteApp("com.test.app1", "Single App", 0)
        )
        
        // When: Updating favorites
        appListView.updateFavorites(favorites)
        
        // Then: Should create single TextView
        assertEquals(1, appListView.getAppCount())
        assertEquals(1, appListView.getAppTextViews().size)
        
        val textView = appListView.getAppTextViews()[0]
        assertEquals("Single App", textView.text.toString())
    }
    
    @Test
    fun testUpdateFavorites_MaxApps_CreatesCorrectLayout() {
        // Given: Maximum favorite apps (7)
        val favorites = (1..7).map { 
            FavoriteApp("com.test.app$it", "App $it", it - 1)
        }
        
        // When: Updating favorites
        appListView.updateFavorites(favorites)
        
        // Then: Should create 7 TextViews
        assertEquals(7, appListView.getAppCount())
        assertEquals(7, appListView.getAppTextViews().size)
        
        // Check all app names
        favorites.forEachIndexed { index, favorite ->
            val textView = appListView.getAppTextViews()[index]
            assertEquals(favorite.displayName, textView.text.toString())
        }
    }
    
    @Test
    fun testGetTextViewForApp_ExistingApp_ReturnsCorrectView() {
        // Given: Favorites with specific app
        val favorites = listOf(
            FavoriteApp("com.test.app1", "App 1", 0),
            FavoriteApp("com.test.target", "Target App", 1),
            FavoriteApp("com.test.app3", "App 3", 2)
        )
        appListView.updateFavorites(favorites)
        
        // When: Getting TextView for specific app
        val textView = appListView.getTextViewForApp("com.test.target")
        
        // Then: Should return correct TextView
        assertNotNull(textView)
        assertEquals("Target App", textView?.text.toString())
    }
    
    @Test
    fun testGetTextViewForApp_NonExistingApp_ReturnsNull() {
        // Given: Favorites without target app
        val favorites = listOf(
            FavoriteApp("com.test.app1", "App 1", 0),
            FavoriteApp("com.test.app2", "App 2", 1)
        )
        appListView.updateFavorites(favorites)
        
        // When: Getting TextView for non-existing app
        val textView = appListView.getTextViewForApp("com.test.nonexisting")
        
        // Then: Should return null
        assertNull(textView)
    }
    
    @Test
    fun testUpdateTextColor_UpdatesAllTextViews() {
        // Given: Favorites with multiple apps
        val favorites = listOf(
            FavoriteApp("com.test.app1", "App 1", 0),
            FavoriteApp("com.test.app2", "App 2", 1),
            FavoriteApp("com.test.app3", "App 3", 2)
        )
        appListView.updateFavorites(favorites)
        
        // When: Updating text color
        val newColor = android.graphics.Color.RED
        appListView.updateTextColor(newColor)
        
        // Then: All TextViews should have new color
        appListView.getAppTextViews().forEach { textView ->
            assertEquals(newColor, textView.currentTextColor)
        }
    }
    
    @Test
    fun testIsEmpty_EmptyFavorites_ReturnsTrue() {
        // Given: AppListView with no favorites
        
        // When: Checking if empty
        val isEmpty = appListView.isEmpty()
        
        // Then: Should return true
        assertTrue(isEmpty)
    }
    
    @Test
    fun testIsEmpty_WithFavorites_ReturnsFalse() {
        // Given: AppListView with favorites
        val favorites = listOf(
            FavoriteApp("com.test.app1", "App 1", 0)
        )
        appListView.updateFavorites(favorites)
        
        // When: Checking if empty
        val isEmpty = appListView.isEmpty()
        
        // Then: Should return false
        assertFalse(isEmpty)
    }
    
    @Test
    fun testSetOnAppClickListener_ClickTriggersCallback() {
        // Given: Favorites and click listener
        val favorites = listOf(
            FavoriteApp("com.test.app1", "App 1", 0)
        )
        appListView.updateFavorites(favorites)
        
        var clickedApp: FavoriteApp? = null
        appListView.setOnAppClickListener { app ->
            clickedApp = app
        }
        
        // When: Clicking on app TextView
        val textView = appListView.getAppTextViews()[0]
        textView.performClick()
        
        // Then: Should trigger callback with correct app
        assertNotNull(clickedApp)
        assertEquals(favorites[0], clickedApp)
    }
}