package com.example.m_launcher.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.m_launcher.R
import com.example.m_launcher.manager.SearchManager
import com.example.m_launcher.manager.SettingsManager
import com.example.m_launcher.data.FontSize

class SearchResultsAdapter(
    private val onAppClick: (SearchManager.SearchResult) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.SearchResultViewHolder>() {
    
    private var searchResults: List<SearchManager.SearchResult> = emptyList()
    
    companion object {
        private const val TAG = "SearchResultsAdapter"
    }
    
    class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appNameText: TextView = itemView.findViewById(R.id.app_name_text)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return SearchResultViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        val searchResult = searchResults[position]
        val app = searchResult.app
        
        // Display app name in white text without icons maintaining minimal design
        holder.appNameText.text = app.displayName
        holder.appNameText.setTextColor(android.graphics.Color.WHITE)

        // Apply font size from settings for search results
        try {
            val context = holder.itemView.context
            val settings = SettingsManager(context)
            val size = settings.loadFontSize()
            holder.appNameText.textSize = size.spValue
        } catch (_: Exception) {
            // Ignore if settings unavailable
        }
        
        // Set up click listener for app launching
        holder.itemView.setOnClickListener {
            onAppClick(searchResult)
        }
        
        // Add subtle visual feedback for touch
        holder.itemView.setOnTouchListener { view, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    view.alpha = 0.7f
                }
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    view.alpha = 1.0f
                }
            }
            false
        }
    }
    
    override fun getItemCount(): Int = searchResults.size
    
    /**
     * Update search results and notify adapter
     */
    fun updateResults(results: List<SearchManager.SearchResult>) {
        searchResults = results
        notifyDataSetChanged()
    }
    
    /**
     * Clear search results
     */
    fun clearResults() {
        searchResults = emptyList()
        notifyDataSetChanged()
    }
}