package com.example.m_launcher.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.m_launcher.R
import com.example.m_launcher.data.FavoriteApp

/**
 * Adapter for displaying selected favorite apps with reordering and removal capabilities
 */
class FavoritesAdapter(
    private val favorites: MutableList<FavoriteApp>,
    private val onRemoveFavorite: (FavoriteApp) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_app, parent, false)
        return FavoriteViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(favorites[position])
    }
    
    override fun getItemCount(): Int = favorites.size
    
    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appNameTextView: TextView = itemView.findViewById(R.id.app_name)
        private val dragHandleImageView: ImageView = itemView.findViewById(R.id.drag_handle)
        private val removeButton: ImageView = itemView.findViewById(R.id.remove_button)
        
        fun bind(favorite: FavoriteApp) {
            appNameTextView.text = favorite.displayName
            
            // Set up remove button
            removeButton.setOnClickListener {
                onRemoveFavorite(favorite)
            }
            
            // Set content descriptions for accessibility
            appNameTextView.contentDescription = "Favorite app: ${favorite.displayName}"
            removeButton.contentDescription = "Remove ${favorite.displayName} from favorites"
            dragHandleImageView.contentDescription = "Drag to reorder ${favorite.displayName}"
        }
    }
}