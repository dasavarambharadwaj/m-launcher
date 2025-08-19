package com.example.m_launcher.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.m_launcher.R
import com.example.m_launcher.data.InstalledApp

/**
 * Adapter for displaying installed apps with selection capabilities
 */
class InstalledAppsAdapter(
    private var apps: List<InstalledApp>,
    private var selectedPackages: Set<String>,
    private val onAppSelected: (InstalledApp) -> Unit,
    private val onAppDeselected: (InstalledApp) -> Unit
) : RecyclerView.Adapter<InstalledAppsAdapter.AppViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_installed_app, parent, false)
        return AppViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(apps[position])
    }
    
    override fun getItemCount(): Int = apps.size
    
    /**
     * Update the list of apps and selected packages
     */
    fun updateApps(apps: List<InstalledApp>, selectedPackages: Set<String>) {
        this.apps = apps
        this.selectedPackages = selectedPackages
        notifyDataSetChanged()
    }
    
    /**
     * Update only the selected packages
     */
    fun updateSelectedPackages(selectedPackages: Set<String>) {
        this.selectedPackages = selectedPackages
        notifyDataSetChanged()
    }
    
    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appIconImageView: ImageView = itemView.findViewById(R.id.app_icon)
        private val appNameTextView: TextView = itemView.findViewById(R.id.app_name)
        private val appPackageTextView: TextView = itemView.findViewById(R.id.app_package)
        private val selectionCheckBox: CheckBox = itemView.findViewById(R.id.selection_checkbox)
        
        fun bind(app: InstalledApp) {
            // Set app information
            appNameTextView.text = app.displayName
            appPackageTextView.text = app.packageName
            
            // Set app icon (use default if not available)
            if (app.icon != null) {
                appIconImageView.setImageDrawable(app.icon)
            } else {
                appIconImageView.setImageResource(R.drawable.ic_default_app)
            }
            
            // Set selection state
            val isSelected = selectedPackages.contains(app.packageName)
            selectionCheckBox.isChecked = isSelected
            
            // Disable selection if maximum favorites reached and not already selected
            val canSelect = selectedPackages.size < 7 || isSelected
            selectionCheckBox.isEnabled = canSelect
            itemView.alpha = if (canSelect) 1.0f else 0.5f
            
            // Set up click listeners
            val clickListener = View.OnClickListener {
                if (canSelect) {
                    if (isSelected) {
                        onAppDeselected(app)
                    } else {
                        onAppSelected(app)
                    }
                }
            }
            
            itemView.setOnClickListener(clickListener)
            selectionCheckBox.setOnClickListener(clickListener)
            
            // Set content descriptions for accessibility
            appNameTextView.contentDescription = "App: ${app.displayName}"
            selectionCheckBox.contentDescription = if (isSelected) {
                "Remove ${app.displayName} from favorites"
            } else {
                "Add ${app.displayName} to favorites"
            }
        }
    }
}