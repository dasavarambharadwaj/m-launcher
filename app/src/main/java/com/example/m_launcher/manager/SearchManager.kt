package com.example.m_launcher.manager

import android.content.Context
import android.util.Log
import com.example.m_launcher.data.InstalledApp
import com.example.m_launcher.repository.AppRepository
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class SearchManager(private val context: Context) {
    
    private val appRepository = AppRepository(context)
    private var cachedApps: List<InstalledApp> = emptyList()
    private var searchCache: MutableMap<String, List<SearchResult>> = mutableMapOf()
    private val searchScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    companion object {
        private const val TAG = "SearchManager"
        private const val DEFAULT_MAX_RESULTS = 20
        private const val MIN_QUERY_LENGTH = 1
        private const val CACHE_SIZE_LIMIT = 100
        private const val PERFORMANCE_MAX_RESULTS = 15 // Optimized limit for better performance
    }
    
    data class SearchResult(
        val app: InstalledApp,
        val relevanceScore: Double,
        val matchType: MatchType
    )
    
    enum class MatchType {
        EXACT_START,    // Query matches start of app name
        EXACT_CONTAINS, // Query is contained in app name
        FUZZY_MATCH,    // Fuzzy string matching
        ACRONYM_MATCH   // Query matches app name acronym
    }
    
    interface SearchListener {
        fun onSearchResults(results: List<SearchResult>)
        fun onSearchError(error: Exception)
    }
    
    /**
     * Initialize SearchManager and load installed apps
     */
    suspend fun initialize() {
        try {
            Log.d(TAG, "Initializing SearchManager...")
            cachedApps = appRepository.getInstalledApps()
            Log.d(TAG, "Loaded ${cachedApps.size} installed apps for search")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing SearchManager", e)
            throw e
        }
    }

    /**
     * Return all apps as search results (sorted alphabetically)
     */
    fun getAllAppsResults(maxResults: Int = PERFORMANCE_MAX_RESULTS): List<SearchResult> {
        val list = cachedApps
            .sortedBy { it.displayName.lowercase(Locale.getDefault()) }
            .map { app -> SearchResult(app, 0.0, MatchType.EXACT_CONTAINS) }
        return list.take(maxResults)
    }
    
    /**
     * Perform fuzzy search with real-time filtering and performance optimization
     */
    fun performSearch(query: String, listener: SearchListener, maxResults: Int = PERFORMANCE_MAX_RESULTS) {
        if (query.length < MIN_QUERY_LENGTH) {
            listener.onSearchResults(emptyList())
            return
        }
        
        val normalizedQuery = query.trim().lowercase()
        val cacheKey = "$normalizedQuery:$maxResults"
        
        // Check cache first for performance optimization
        searchCache[cacheKey]?.let { cachedResults ->
            Log.d(TAG, "Returning cached results for query: '$query' (${cachedResults.size} results)")
            listener.onSearchResults(cachedResults)
            return
        }
        
        // Perform search asynchronously with performance optimization
        searchScope.launch {
            try {
                val results = withContext(Dispatchers.Default) {
                    performFuzzySearch(normalizedQuery, maxResults)
                }
                
                // Cache results for future searches with LRU eviction
                if (searchCache.size >= CACHE_SIZE_LIMIT) {
                    // Remove oldest entries when cache is full
                    val oldestKeys = searchCache.keys.take(10)
                    oldestKeys.forEach { searchCache.remove(it) }
                }
                searchCache[cacheKey] = results
                
                Log.d(TAG, "Search completed for '$query': ${results.size} results (max: $maxResults)")
                listener.onSearchResults(results)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error performing search for '$query'", e)
                listener.onSearchError(e)
            }
        }
    }
    
    /**
     * Perform optimized fuzzy search algorithm with ranking and result limiting
     */
    private fun performFuzzySearch(query: String, maxResults: Int = DEFAULT_MAX_RESULTS): List<SearchResult> {
        val results = ArrayList<SearchResult>()
        val exactMatches = ArrayList<SearchResult>()
        val partialMatches = ArrayList<SearchResult>()
        
        // Optimize search by categorizing matches for early termination
        for (app in cachedApps) {
            val appName = app.displayName.lowercase()
            val relevanceScore = calculateRelevanceScore(query, appName)
            
            if (relevanceScore > 0.0) {
                val matchType = determineMatchType(query, appName)
                val searchResult = SearchResult(app, relevanceScore, matchType)
                
                // Categorize for performance optimization
                when (matchType) {
                    MatchType.EXACT_START -> exactMatches.add(searchResult)
                    else -> partialMatches.add(searchResult)
                }
                
                // Early termination if we have enough exact matches
                if (exactMatches.size >= maxResults) {
                    break
                }
            }
        }
        
        // Combine and sort results with performance optimization
        results.addAll(exactMatches.sortedByDescending { it.relevanceScore })
        
        val remainingSlots = maxResults - results.size
        if (remainingSlots > 0) {
            results.addAll(
                partialMatches
                    .sortedWith(compareByDescending<SearchResult> { it.relevanceScore }
                        .thenBy { it.matchType.ordinal }
                        .thenBy { it.app.displayName })
                    .take(remainingSlots)
            )
        }
        
        return results.take(maxResults)
    }
    
    /**
     * Calculate relevance score for fuzzy matching
     */
    private fun calculateRelevanceScore(query: String, appName: String): Double {
        // Exact match gets highest score
        if (appName == query) {
            return 1.0
        }
        
        // Starts with query gets high score
        if (appName.startsWith(query)) {
            return 0.9
        }
        
        // Contains query gets medium score
        if (appName.contains(query)) {
            return 0.7
        }
        
        // Acronym match gets medium score
        val acronymScore = calculateAcronymScore(query, appName)
        if (acronymScore > 0.0) {
            return 0.6 + (acronymScore * 0.1)
        }
        
        // Fuzzy string matching for partial matches
        val fuzzyScore = calculateFuzzyScore(query, appName)
        if (fuzzyScore > 0.5) {
            return fuzzyScore * 0.5
        }
        
        return 0.0
    }
    
    /**
     * Determine the type of match for ranking purposes
     */
    private fun determineMatchType(query: String, appName: String): MatchType {
        return when {
            appName.startsWith(query) -> MatchType.EXACT_START
            appName.contains(query) -> MatchType.EXACT_CONTAINS
            calculateAcronymScore(query, appName) > 0.0 -> MatchType.ACRONYM_MATCH
            else -> MatchType.FUZZY_MATCH
        }
    }
    
    /**
     * Calculate acronym matching score
     * Example: "gm" matches "Google Maps"
     */
    private fun calculateAcronymScore(query: String, appName: String): Double {
        val words = appName.split(" ", "-", "_", ".")
        if (words.size < 2) return 0.0
        
        val acronym = words.mapNotNull { it.firstOrNull()?.lowercase() }.joinToString("")
        
        return when {
            acronym == query -> 1.0
            acronym.startsWith(query) -> 0.8
            acronym.contains(query) -> 0.6
            else -> 0.0
        }
    }
    
    /**
     * Calculate fuzzy string matching score using Levenshtein distance
     */
    private fun calculateFuzzyScore(query: String, appName: String): Double {
        val maxLength = maxOf(query.length, appName.length)
        if (maxLength == 0) return 1.0
        
        val distance = levenshteinDistance(query, appName)
        return 1.0 - (distance.toDouble() / maxLength)
    }
    
    /**
     * Calculate Levenshtein distance for fuzzy matching
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length
        
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }
        
        for (i in 0..len1) {
            dp[i][0] = i
        }
        
        for (j in 0..len2) {
            dp[0][j] = j
        }
        
        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        
        return dp[len1][len2]
    }
    
    /**
     * Handle special characters and multilingual app names
     */
    private fun normalizeAppName(appName: String): String {
        return appName
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "") // Remove special characters
            .replace(Regex("\\s+"), " ") // Normalize whitespace
            .trim()
    }
    
    /**
     * Clear search cache to free memory
     */
    fun clearCache() {
        searchCache.clear()
        Log.d(TAG, "Search cache cleared")
    }
    
    /**
     * Refresh installed apps cache
     */
    suspend fun refreshAppsCache() {
        try {
            cachedApps = appRepository.getInstalledApps()
            clearCache() // Clear search cache when apps change
            Log.d(TAG, "Apps cache refreshed with ${cachedApps.size} apps")
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing apps cache", e)
            throw e
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        searchScope.cancel()
        clearCache()
        Log.d(TAG, "SearchManager cleaned up")
    }
}