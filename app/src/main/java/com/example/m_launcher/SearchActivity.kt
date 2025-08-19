package com.example.m_launcher

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.m_launcher.adapter.SearchResultsAdapter
import com.example.m_launcher.manager.SearchManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity(), SearchManager.SearchListener {
    
    private lateinit var searchInput: EditText
    private lateinit var searchResults: RecyclerView
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var searchManager: SearchManager
    private lateinit var searchResultsAdapter: SearchResultsAdapter
    private lateinit var gestureDetector: GestureDetector
    private lateinit var rootView: View
    private var searchJob: Job? = null
    private var lastSearchQuery: String = ""
    
    companion object {
        private const val TAG = "SearchActivity"
        private const val SEARCH_DEBOUNCE_DELAY = 150L // Milliseconds
        private const val MAX_SEARCH_RESULTS = 15 // Limit for performance
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set the search layout
        setContentView(R.layout.activity_search)
        
        // Initialize views and components
        initializeComponents()
        
        // Apply Material Expressive theme for Android 16
        setupMaterialExpressiveSearch()
        
        // Configure full-screen black background with no action bar
        setupFullScreenBlackInterface()
        
        // Set up immersive mode to hide status and navigation bars
        setupImmersiveMode()
        
        // Configure search input field with automatic keyboard display
        setupSearchInputField()
        
        // Initialize search functionality
        setupSearchFunctionality()
        
        // Set up navigation gestures for search interface
        setupNavigationGestures()
        
        Log.d(TAG, "SearchActivity created with full-screen black interface")
    }
    
    override fun onResume() {
        super.onResume()
        // Ensure immersive mode is maintained when returning to search
        setupImmersiveMode()
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Re-apply immersive mode when window gains focus
            setupImmersiveMode()
        }
    }
    
    /**
     * Apply Material Expressive theme for Android 16 search interface
     */
    private fun setupMaterialExpressiveSearch() {
        // Material Expressive theme configuration for search
        // Enable edge-to-edge display for Material Expressive design
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Configure window for search interface with Material Expressive styling
        window.setFlags(
            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        )
    }
    
    /**
     * Configure full-screen black background (#000000) with no action bar
     */
    private fun setupFullScreenBlackInterface() {
        // Set black background colors for system bars
        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK
        
        // Ensure the activity background is black
        window.decorView.setBackgroundColor(android.graphics.Color.BLACK)
        
        // Configure window flags for full-screen black interface
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        Log.d(TAG, "Full-screen black interface configured")
    }
    
    /**
     * Set up immersive mode to hide status and navigation bars
     */
    private fun setupImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+) - Use WindowInsetsController for Android 16 optimizations
            val windowInsetsController = window.insetsController
            windowInsetsController?.let { controller ->
                // Hide system bars for immersive search experience
                controller.hide(WindowInsets.Type.systemBars())
                
                // Configure system bars behavior for search interface
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Fallback for older Android versions
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            windowInsetsController.systemBarsBehavior = 
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        
        Log.d(TAG, "Immersive mode configured for search interface")
    }
    
    /**
     * Initialize views and components
     */
    private fun initializeComponents() {
        // Initialize views
        searchInput = findViewById(R.id.search_input)
        searchResults = findViewById(R.id.search_results)
        rootView = findViewById(android.R.id.content)
        
        // Initialize input method manager for keyboard control
        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        
        // Initialize search manager
        searchManager = SearchManager(this)
        
        // Set up search results adapter with Material Expressive text styling
        searchResultsAdapter = SearchResultsAdapter { searchResult ->
            // Handle app selection from search results
            launchAppFromSearch(searchResult)
        }
        
        // Set up RecyclerView for search results with proper spacing
        searchResults.apply {
            layoutManager = LinearLayoutManager(this@SearchActivity)
            adapter = searchResultsAdapter
            // Disable scroll bars for minimal design
            isVerticalScrollBarEnabled = false
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
        
        Log.d(TAG, "Components initialized")
    }
    
    /**
     * Configure search input field with automatic keyboard display
     */
    private fun setupSearchInputField() {
        // Apply Material Expressive text styling with white text on black background
        searchInput.setTextColor(android.graphics.Color.WHITE)
        searchInput.setHintTextColor(android.graphics.Color.parseColor("#CCCCCC"))
        
        // Configure input method for text search
        searchInput.inputType = android.text.InputType.TYPE_CLASS_TEXT
        searchInput.imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
        searchInput.isSingleLine = true
        
        // Set up text change listener for real-time search
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed before text changes
            }
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Trigger search as user types for real-time results
                val query = s?.toString()?.trim() ?: ""
                Log.d(TAG, "Search query changed: '$query'")
                
                // Perform search with real-time filtering
                performSearch(query)
            }
            
            override fun afterTextChanged(s: Editable?) {
                // No action needed after text changes
            }
        })
        // Handle IME action and enter key
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                handleSearchSubmit()
                true
            } else false
        }
        
        // Set up focus management and cursor styling
        searchInput.requestFocus()
        searchInput.setSelection(searchInput.text.length) // Move cursor to end
        
        // Automatically display keyboard when search interface opens
        searchInput.post {
            showKeyboard()
        }
        
        Log.d(TAG, "Search input field configured with automatic keyboard display")
    }
    
    /**
     * Show keyboard for search input
     */
    private fun showKeyboard() {
        try {
            searchInput.requestFocus()
            inputMethodManager.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT)
            Log.d(TAG, "Keyboard displayed for search input")
        } catch (e: Exception) {
            Log.w(TAG, "Could not show keyboard", e)
        }
    }

    /**
     * Handle search submission: launch top app or default to web search
     */
    private fun handleSearchSubmit() {
        val adapterCount = searchResultsAdapter.itemCount
        if (adapterCount > 0) {
            // Launch the first result
            val field = RecyclerView::class.java.getDeclaredField("mAdapter")
            field.isAccessible = true
            val adapter = field.get(searchResults) as SearchResultsAdapter
            val method = SearchResultsAdapter::class.java.getDeclaredMethod("getItem", Int::class.javaPrimitiveType)
            method.isAccessible = true
            val result = method.invoke(adapter, 0) as com.example.m_launcher.manager.SearchManager.SearchResult
            launchAppFromSearch(result)
        } else {
            // Fallback: web search with default browser
            val q = searchInput.text.toString().trim()
            if (q.isNotEmpty()) {
                try {
                    val uri = android.net.Uri.parse("https://www.google.com/search?q=" + java.net.URLEncoder.encode(q, "UTF-8"))
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                    finish()
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                } catch (e: Exception) {
                    Log.e(TAG, "Error launching web search", e)
                }
            }
        }
    }
    
    /**
     * Hide keyboard
     */
    private fun hideKeyboard() {
        try {
            inputMethodManager.hideSoftInputFromWindow(searchInput.windowToken, 0)
            Log.d(TAG, "Keyboard hidden")
        } catch (e: Exception) {
            Log.w(TAG, "Could not hide keyboard", e)
        }
    }
    
    /**
     * Handle back button to close search interface
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        Log.d(TAG, "Back button pressed, closing search interface")
        
        // Use the centralized close method for consistent behavior
        closeSearchInterface()
    }
    
    /**
     * Set up navigation gestures for search interface
     */
    private fun setupNavigationGestures() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                
                val deltaY = e2.y - e1.y
                val deltaX = e2.x - e1.x
                
                // Check for swipe down gesture to close search interface
                if (Math.abs(deltaY) > Math.abs(deltaX) && 
                    deltaY > 100 && // Minimum swipe distance (downward)
                    Math.abs(velocityY) > 500) { // Minimum velocity threshold
                    
                    Log.d(TAG, "Swipe down detected, closing search interface")
                    
                    // Close search interface with smooth transition
                    closeSearchInterface()
                    
                    return true
                }
                
                return false
            }
            
            override fun onDown(e: MotionEvent): Boolean {
                // Return true to indicate we want to handle gestures
                return true
            }
        })
        
        // Set touch listener on root view to detect swipe gestures
        rootView.setOnTouchListener { _, event ->
            // Only handle gestures if not touching the search input or results
            val touchedView = findViewAt(rootView, event.x, event.y)
            if (touchedView != searchInput && touchedView != searchResults) {
                gestureDetector.onTouchEvent(event)
            } else {
                false
            }
        }
        
        Log.d(TAG, "Navigation gestures configured (swipe down to close)")
    }
    
    /**
     * Find the view at the given coordinates
     */
    private fun findViewAt(parent: View, x: Float, y: Float): View? {
        if (parent is ViewGroup) {
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                val location = IntArray(2)
                child.getLocationOnScreen(location)
                
                if (x >= location[0] && x <= location[0] + child.width &&
                    y >= location[1] && y <= location[1] + child.height) {
                    return findViewAt(child, x, y) ?: child
                }
            }
        }
        return parent
    }
    
    /**
     * Close search interface with smooth transition animations
     */
    private fun closeSearchInterface() {
        Log.d(TAG, "Closing search interface")
        
        // Hide keyboard before closing
        hideKeyboard()
        
        // Close search interface and return to home screen
        finish()
        
        // Apply smooth transition animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
    
    /**
     * Initialize search functionality
     */
    private fun setupSearchFunctionality() {
        // Initialize SearchManager asynchronously
        lifecycleScope.launch {
            try {
                searchManager.initialize()
                Log.d(TAG, "SearchManager initialized successfully")
                // Show all apps by default
                val all = searchManager.getAllAppsResults(MAX_SEARCH_RESULTS)
                onSearchResults(all)
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing SearchManager", e)
                // Handle initialization error gracefully
                onSearchError(e)
            }
        }
    }
    
    /**
     * Perform search with debouncing to reduce unnecessary filtering operations
     */
    private fun performSearch(query: String) {
        // Cancel previous search job to implement debouncing
        searchJob?.cancel()
        
        if (query.isEmpty()) {
            // Show all apps when empty
            val all = searchManager.getAllAppsResults(MAX_SEARCH_RESULTS)
            onSearchResults(all)
            lastSearchQuery = ""
            return
        }
        
        // Skip search if query hasn't changed (optimization)
        if (query == lastSearchQuery) {
            return
        }
        
        // Implement search debouncing for better performance
        searchJob = lifecycleScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            
            // Double-check query hasn't changed during delay
            if (query == searchInput.text.toString().trim()) {
                lastSearchQuery = query
                Log.d(TAG, "Performing debounced search for: '$query'")
                
                // Use SearchManager to perform fuzzy search with result limit
                searchManager.performSearch(query, this@SearchActivity)
            }
        }
    }
    
    /**
     * Launch app from search results
     */
    private fun launchAppFromSearch(searchResult: SearchManager.SearchResult) {
        try {
            val app = searchResult.app
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
            
            if (launchIntent != null) {
                Log.d(TAG, "Launching app from search: ${app.displayName}")
                
                // Hide keyboard before launching app
                hideKeyboard()
                
                // Launch the selected app
                startActivity(launchIntent)
                
                // Close search interface after launching app
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                
            } else {
                Log.w(TAG, "No launch intent found for ${app.packageName}")
                // TODO: Show error message to user
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching app from search", e)
            // TODO: Show error message to user
        }
    }
    
    /**
     * SearchManager.SearchListener implementation - handle search results with performance monitoring
     */
    override fun onSearchResults(results: List<SearchManager.SearchResult>) {
        val limitedResults = results.take(MAX_SEARCH_RESULTS)
        Log.d(TAG, "Received ${results.size} search results, displaying ${limitedResults.size}")
        
        // Update RecyclerView with limited search results for optimal performance
        searchResultsAdapter.updateResults(limitedResults)
    }
    
    /**
     * SearchManager.SearchListener implementation - handle search errors
     */
    override fun onSearchError(error: Exception) {
        Log.e(TAG, "Search error occurred", error)
        
        // Clear results on error
        searchResultsAdapter.clearResults()
        
        // TODO: Show error message to user if needed
    }
    
    override fun onPause() {
        super.onPause()
        // Hide keyboard when activity is paused
        hideKeyboard()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Cancel any pending search operations
        searchJob?.cancel()
        
        // Clean up SearchManager resources
        if (::searchManager.isInitialized) {
            searchManager.cleanup()
        }
        
        Log.d(TAG, "SearchActivity destroyed and resources cleaned up")
    }
}