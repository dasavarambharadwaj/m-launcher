package com.example.m_launcher.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.m_launcher.R
import com.example.m_launcher.manager.SearchManager
import com.example.m_launcher.manager.SettingsManager
import com.example.m_launcher.data.FontSize

class SearchResultsAdapter(
    private val onAppClick: (SearchManager.SearchResult) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.SearchResultViewHolder>() {
    
    private var searchResults: List<SearchManager.SearchResult> = emptyList()
    fun getItem(position: Int): SearchManager.SearchResult = searchResults[position]
    
    companion object {
        private const val TAG = "SearchResultsAdapter"
    }
    
    class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appNameText: TextView = itemView.findViewById(R.id.app_name_text)
        val appIcon: ImageView = itemView.findViewById(R.id.app_icon)
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
        // If this is the Google prompt, show the Google icon; else keep minimal without icon
        if (app.packageName.isBlank() && app.displayName.startsWith("Search Google")) {
            holder.appIcon.visibility = View.VISIBLE
            holder.appIcon.setImageResource(R.drawable.ic_google)
        } else {
            holder.appIcon.visibility = View.GONE
        }

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
        
        // Long-press for options: App info / Uninstall (if applicable), or Google search
        holder.itemView.setOnLongClickListener {
            try {
                if (app.packageName.isBlank() || !app.isLaunchable) {
                    // Google prompt row → open web search
                    val context = holder.itemView.context
                    val label = holder.appNameText.text.toString()
                    val query = label.substringAfter("\"").substringBeforeLast("\"")
                    val uri = android.net.Uri.parse("https://www.google.com/search?q=" + java.net.URLEncoder.encode(query, "UTF-8"))
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                } else {
                    showAppContextMenu(holder.itemView, app)
                }
            } catch (_: Exception) { }
            true
        }
    }

    private fun showAppContextMenu(anchor: View, app: com.example.m_launcher.data.InstalledApp) {
        val context = anchor.context
        val options = mutableListOf("App info")
        val canUninstall = !isSystemApp(context, app.packageName)
        if (canUninstall) options.add("Uninstall")

        val builder = android.app.AlertDialog.Builder(context, R.style.DarkAlertDialog)
            .setTitle(app.displayName)
            .setItems(options.toTypedArray()) { dialog, which ->
                when (options[which]) {
                    "App info" -> openAppInfo(context, app.packageName)
                    "Uninstall" -> requestUninstall(context, app.packageName)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun openAppInfo(context: android.content.Context, packageName: String) {
        try {
            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = android.net.Uri.fromParts("package", packageName, null)
            context.startActivity(intent)
        } catch (_: Exception) { }
    }

    private fun requestUninstall(context: android.content.Context, packageName: String) {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_DELETE)
            intent.data = android.net.Uri.parse("package:$packageName")
            context.startActivity(intent)
        } catch (_: Exception) { }
    }

    private fun isSystemApp(context: android.content.Context, packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (_: Exception) { false }
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