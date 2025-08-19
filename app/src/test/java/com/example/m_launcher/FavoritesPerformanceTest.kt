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
import kotlin.system.measureTimeMillis

/**
 * Performance tests for favorites functionality
 * Tests response times and memory usage for various operations
 */
@RunWith(MockitoJUnitRunner::class)
class FavoritesPerformanceTest {
    
    @Mock
    private lateinit var mockContext: Context
    
    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences
    
    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor
    
    @Mock
    private lateinit var mockPackageManager: PackageManager
    
    private lateinit var favoritesManager: FavoritesManager
    
    companion object {
        private const val MAX_LOAD_TIME_MS = 100L
        private const val MAX_SAVE_TIME_MS = 50L
        private const val MAX_VALIDATION_TIME_MS = 10L
    }
    
    @Before
    fun setup() {
        `when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPreferences)
        `when`(mockContext.packageManager).thenReturn(mockPackageManager)
        `when`(mockSharedPreferences.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.putInt(anyString(), anyInt())).thenReturn(mockEditor)
        `when`(mockEditor.putLong(anyString(), anyLong())).thenReturn(mockEditor)
        
        // Mock all apps as installed for performance tests
        `when`(mockPackageManager.getPackageInfo(anyString(), anyInt())).thenReturn(mock())
        
        favoritesManager = FavoritesManager(mockContext)
    }
    
    @Test
    fun testLoadFavorites_Performance_CompletesWithinTimeLimit() {
        // Given: Stored favorites (maximum 7 apps)
        val favorites = (1..7).map { 
            FavoriteApp("com.test.app$it", "App $it", it - 1)
        }
        mockStoredFavorites(favorites)
        
        // When: Loading favorites and measuring time
        val loadTime = measureTimeMillis {
            repeat(10) {
                favoritesManager.loadFavorites()
            }
        }
        val averageLoadTime = loadTime / 10
        
        // Then: Should complete within time limit
        assertTrue("Load time should be under ${MAX_LOAD_TIME_MS}ms, was ${averageLoadTime}ms", 
            averageLoadTime < MAX_LOAD_TIME_MS)
    }
    
    @Test
    fun testSaveFavorites_Performance_CompletesWithinTimeLimit() {
        // Given: Favorites to save
        val favorites = (1..7).map { 
            FavoriteApp("com.test.app$it", "App $it", it - 1)
        }
        
        // When: Saving favorites and measuring time
        val saveTime = measureTimeMillis {
            repeat(10) {
                favoritesManager.saveFavorites(favorites)
            }
        }
        val averageSaveTime = saveTime / 10
        
        // Then: Should complete within time limit
        assertTrue("Save time should be under ${MAX_SAVE_TIME_MS}ms, was ${averageSaveTime}ms", 
            averageSaveTime < MAX_SAVE_TIME_MS)
    }
    
    @Test
    fun testValidation_Performance_CompletesWithinTimeLimit() {
        // Given: Favorites to validate
        val favorites = (1..7).map { 
            FavoriteApp("com.test.app$it", "App $it", it - 1)
        }
        
        // When: Validating favorites and measuring time
        val validationTime = measureTimeMillis {
            repeat(100) {
                com.example.m_launcher.data.FavoritesValidation.validateFavorites(favorites)
            }
        }
        val averageValidationTime = validationTime / 100
        
        // Then: Should complete within time limit
        assertTrue("Validation time should be under ${MAX_VALIDATION_TIME_MS}ms, was ${averageValidationTime}ms", 
            averageValidationTime < MAX_VALIDATION_TIME_MS)
    }
    
    @Test
    fun testMemoryUsage_LoadFavorites_DoesNotLeakMemory() {
        // Given: Stored favorites
        val favorites = (1..7).map { 
            FavoriteApp("com.test.app$it", "App $it", it - 1)
        }
        mockStoredFavorites(favorites)
        
        // When: Loading favorites multiple times
        val initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        
        repeat(1000) {
            favoritesManager.loadFavorites()
        }
        
        // Force garbage collection
        System.gc()
        Thread.sleep(100)
        
        val finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val memoryIncrease = finalMemory - initialMemory
        
        // Then: Memory increase should be minimal (less than 1MB)
        assertTrue("Memory increase should be minimal, was ${memoryIncrease / 1024}KB", 
            memoryIncrease < 1024 * 1024)
    }
    
    @Test
    fun testConcurrentAccess_LoadAndSave_HandlesCorrectly() {
        // Given: Favorites for concurrent operations
        val favorites = listOf(
            FavoriteApp("com.test.app1", "App 1", 0),
            FavoriteApp("com.test.app2", "App 2", 1)
        )
        mockStoredFavorites(favorites)
        
        // When: Performing concurrent load and save operations
        val threads = mutableListOf<Thread>()
        val results = mutableListOf<Boolean>()
        
        repeat(10) { i ->
            val thread = Thread {
                if (i % 2 == 0) {
                    // Load operation
                    val loaded = favoritesManager.loadFavorites()
                    results.add(loaded.isNotEmpty())
                } else {
                    // Save operation
                    val saved = favoritesManager.saveFavorites(favorites)
                    results.add(saved)
                }
            }
            threads.add(thread)
            thread.start()
        }
        
        // Wait for all threads to complete
        threads.forEach { it.join() }
        
        // Then: All operations should succeed
        assertTrue("All concurrent operations should succeed", 
            results.all { it })
    }
    
    @Test
    fun testLargeDataSet_Validation_HandlesEfficiently() {
        // Given: Large number of validation operations
        val favorites = (1..7).map { 
            FavoriteApp("com.test.app$it", "App $it", it - 1)
        }
        
        // When: Performing many validation operations
        val validationTime = measureTimeMillis {
            repeat(10000) {
                com.example.m_launcher.data.FavoritesValidation.validateFavorites(favorites)
                com.example.m_launcher.data.FavoritesValidation.normalizeFavoriteOrders(favorites)
            }
        }
        
        // Then: Should handle large datasets efficiently (under 1 second total)
        assertTrue("Large dataset validation should be efficient, took ${validationTime}ms", 
            validationTime < 1000)
    }
    
    @Test
    fun testFrequentUpdates_Performance_MaintainsResponsiveness() {
        // Given: Scenario simulating frequent favorites updates
        val baseFavorites = listOf(
            FavoriteApp("com.test.app1", "App 1", 0),
            FavoriteApp("com.test.app2", "App 2", 1)
        )
        
        // When: Performing frequent add/remove operations
        val operationTime = measureTimeMillis {
            repeat(100) { i ->
                if (i % 2 == 0) {
                    favoritesManager.addFavorite("com.test.temp$i", "Temp $i")
                } else {
                    favoritesManager.removeFavorite("com.test.temp${i-1}")
                }
            }
        }
        val averageOperationTime = operationTime / 100
        
        // Then: Each operation should be fast (under 10ms average)
        assertTrue("Frequent updates should be responsive, average ${averageOperationTime}ms", 
            averageOperationTime < 10)
    }
    
    private fun mockStoredFavorites(favorites: List<FavoriteApp>) {
        val json = """[${favorites.joinToString(",") { 
            """{"packageName":"${it.packageName}","displayName":"${it.displayName}","order":${it.order}}"""
        }}]"""
        
        `when`(mockSharedPreferences.getString("favorite_apps_json", null)).thenReturn(json)
    }
}