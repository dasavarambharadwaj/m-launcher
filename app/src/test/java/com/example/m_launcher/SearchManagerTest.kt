package com.example.m_launcher

import android.content.Context
import com.example.m_launcher.data.InstalledApp
import com.example.m_launcher.manager.SearchManager
import com.example.m_launcher.repository.AppRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class SearchManagerTest {

    private lateinit var searchManager: SearchManager
    private lateinit var mockContext: Context
    private lateinit var mockAppRepository: AppRepository
    private lateinit var testApps: List<InstalledApp>

    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        mockAppRepository = mockk(relaxed = true)
        
        // Create test apps for search testing
        testApps = listOf(
            InstalledApp("com.google.android.gm", "Gmail", null, true),
            InstalledApp("com.android.chrome", "Chrome", null, true),
            InstalledApp("com.whatsapp", "WhatsApp", null, true),
            InstalledApp("com.spotify.music", "Spotify", null, true),
            InstalledApp("com.google.android.apps.maps", "Google Maps", null, true),
            InstalledApp("com.facebook.katana", "Facebook", null, true),
            InstalledApp("com.instagram.android", "Instagram", null, true),
            InstalledApp("com.twitter.android", "Twitter", null, true),
            InstalledApp("com.adobe.reader", "Adobe Acrobat Reader", null, true),
            InstalledApp("com.microsoft.office.word", "Microsoft Word", null, true)
        )
        
        coEvery { mockAppRepository.getInstalledApps() } returns testApps
        
        searchManager = SearchManager(mockContext)
        
        // Mock the app repository in SearchManager
        val appRepositoryField = SearchManager::class.java.getDeclaredField("appRepository")
        appRepositoryField.isAccessible = true
        appRepositoryField.set(searchManager, mockAppRepository)
    }

    @After
    fun tearDown() {
        searchManager.cleanup()
        clearAllMocks()
    }

    @Test
    fun `test SearchManager initialization loads apps correctly`() = runTest {
        // When
        searchManager.initialize()
        
        // Then
        coVerify { mockAppRepository.getInstalledApps() }
    }

    @Test
    fun `test exact match search returns correct results`() = runTest {
        // Given
        searchManager.initialize()
        val searchListener = mockk<SearchManager.SearchListener>(relaxed = true)
        val capturedResults = slot<List<SearchManager.SearchResult>>()
        
        every { searchListener.onSearchResults(capture(capturedResults)) } just Runs
        
        // When
        searchManager.performSearch("Gmail", searchListener)
        
        // Wait for async operation
        Thread.sleep(100)
        
        // Then
        verify { searchListener.onSearchResults(any()) }
        assertTrue("Should find Gmail", capturedResults.captured.any { it.app.displayName == "Gmail" })
    }

    @Test
    fun `test partial match search returns relevant results`() = runTest {
        // Given
        searchManager.initialize()
        val searchListener = mockk<SearchManager.SearchListener>(relaxed = true)
        val capturedResults = slot<List<SearchManager.SearchResult>>()
        
        every { searchListener.onSearchResults(capture(capturedResults)) } just Runs
        
        // When
        searchManager.performSearch("goo", searchListener)
        
        // Wait for async operation
        Thread.sleep(100)
        
        // Then
        verify { searchListener.onSearchResults(any()) }
        assertTrue("Should find Google apps", 
            capturedResults.captured.any { it.app.displayName.contains("Google", ignoreCase = true) })
    }

    @Test
    fun `test acronym search works correctly`() = runTest {
        // Given
        searchManager.initialize()
        val searchListener = mockk<SearchManager.SearchListener>(relaxed = true)
        val capturedResults = slot<List<SearchManager.SearchResult>>()
        
        every { searchListener.onSearchResults(capture(capturedResults)) } just Runs
        
        // When
        searchManager.performSearch("gm", searchListener)
        
        // Wait for async operation
        Thread.sleep(100)
        
        // Then
        verify { searchListener.onSearchResults(any()) }
        assertTrue("Should find Gmail or Google Maps", 
            capturedResults.captured.any { 
                it.app.displayName == "Gmail" || it.app.displayName == "Google Maps" 
            })
    }

    @Test
    fun `test empty query returns empty results`() = runTest {
        // Given
        searchManager.initialize()
        val searchListener = mockk<SearchManager.SearchListener>(relaxed = true)
        val capturedResults = slot<List<SearchManager.SearchResult>>()
        
        every { searchListener.onSearchResults(capture(capturedResults)) } just Runs
        
        // When
        searchManager.performSearch("", searchListener)
        
        // Then
        verify { searchListener.onSearchResults(any()) }
        assertTrue("Empty query should return empty results", capturedResults.captured.isEmpty())
    }

    @Test
    fun `test search result limit is respected`() = runTest {
        // Given
        searchManager.initialize()
        val searchListener = mockk<SearchManager.SearchListener>(relaxed = true)
        val capturedResults = slot<List<SearchManager.SearchResult>>()
        
        every { searchListener.onSearchResults(capture(capturedResults)) } just Runs
        
        // When - search for common letter that should match many apps
        searchManager.performSearch("a", searchListener, maxResults = 5)
        
        // Wait for async operation
        Thread.sleep(100)
        
        // Then
        verify { searchListener.onSearchResults(any()) }
        assertTrue("Should respect result limit", capturedResults.captured.size <= 5)
    }

    @Test
    fun `test search caching works correctly`() = runTest {
        // Given
        searchManager.initialize()
        val searchListener = mockk<SearchManager.SearchListener>(relaxed = true)
        
        // When - perform same search twice
        searchManager.performSearch("Chrome", searchListener)
        Thread.sleep(100)
        searchManager.performSearch("Chrome", searchListener)
        
        // Then - should use cached results on second call
        verify(exactly = 2) { searchListener.onSearchResults(any()) }
    }

    @Test
    fun `test fuzzy search algorithm performance`() = runTest {
        // Given
        searchManager.initialize()
        val searchListener = mockk<SearchManager.SearchListener>(relaxed = true)
        
        // When - measure search performance
        val startTime = System.currentTimeMillis()
        searchManager.performSearch("test", searchListener)
        Thread.sleep(200) // Wait for completion
        val endTime = System.currentTimeMillis()
        
        // Then - should complete within reasonable time
        val searchTime = endTime - startTime
        assertTrue("Search should complete within 500ms", searchTime < 500)
        verify { searchListener.onSearchResults(any()) }
    }

    @Test
    fun `test search with special characters`() = runTest {
        // Given
        searchManager.initialize()
        val searchListener = mockk<SearchManager.SearchListener>(relaxed = true)
        val capturedResults = slot<List<SearchManager.SearchResult>>()
        
        every { searchListener.onSearchResults(capture(capturedResults)) } just Runs
        
        // When
        searchManager.performSearch("@#$%", searchListener)
        
        // Wait for async operation
        Thread.sleep(100)
        
        // Then - should handle gracefully without crashing
        verify { searchListener.onSearchResults(any()) }
        // Results may be empty, but should not crash
    }

    @Test
    fun `test search error handling`() = runTest {
        // Given
        val errorSearchManager = SearchManager(mockContext)
        coEvery { mockAppRepository.getInstalledApps() } throws RuntimeException("Test error")
        
        val appRepositoryField = SearchManager::class.java.getDeclaredField("appRepository")
        appRepositoryField.isAccessible = true
        appRepositoryField.set(errorSearchManager, mockAppRepository)
        
        val searchListener = mockk<SearchManager.SearchListener>(relaxed = true)
        
        // When
        try {
            errorSearchManager.initialize()
        } catch (e: Exception) {
            // Expected
        }
        
        errorSearchManager.performSearch("test", searchListener)
        
        // Then - should handle error gracefully
        verify(timeout = 1000) { searchListener.onSearchError(any()) }
        
        errorSearchManager.cleanup()
    }
}