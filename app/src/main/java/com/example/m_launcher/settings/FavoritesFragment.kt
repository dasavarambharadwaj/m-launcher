package com.example.m_launcher.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.Button
import android.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.m_launcher.R
import com.example.m_launcher.adapter.FavoritesAdapter
import com.example.m_launcher.adapter.InstalledAppsAdapter
import com.example.m_launcher.data.FavoriteApp
import com.example.m_launcher.data.InstalledApp
import com.example.m_launcher.manager.FavoritesManager
import com.example.m_launcher.repository.AppRepository
import com.example.m_launcher.utils.ErrorHandler
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private lateinit var favoritesManager: FavoritesManager
    private lateinit var appRepository: AppRepository

    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var editFavoritesButton: Button
    private lateinit var favoritesAdapter: FavoritesAdapter
    private lateinit var installedAppsAdapter: InstalledAppsAdapter

    private val currentFavorites: MutableList<FavoriteApp> = mutableListOf()
    private var installedApps: List<InstalledApp> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoritesManager = FavoritesManager(requireContext())
        appRepository = AppRepository(requireContext())

        favoritesRecyclerView = view.findViewById(R.id.favorites_recycler_view)
        editFavoritesButton = view.findViewById(R.id.edit_favorites_button)

        favoritesAdapter = FavoritesAdapter(
            favorites = currentFavorites,
            onRemoveFavorite = { favorite -> removeFavorite(favorite) }
        )
        favoritesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        favoritesRecyclerView.adapter = favoritesAdapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                if (fromPosition < currentFavorites.size && toPosition < currentFavorites.size) {
                    val moved = currentFavorites.removeAt(fromPosition)
                    currentFavorites.add(toPosition, moved)
                    currentFavorites.forEachIndexed { index, fav ->
                        currentFavorites[index] = fav.copy(order = index)
                    }
                    favoritesAdapter.notifyItemMoved(fromPosition, toPosition)
                    return true
                }
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                if (pos < currentFavorites.size) removeFavorite(currentFavorites[pos])
            }
        })
        itemTouchHelper.attachToRecyclerView(favoritesRecyclerView)

        editFavoritesButton.setOnClickListener { showFavoritesEditor() }

        loadData()
    }

    private fun loadData() {
        currentFavorites.clear()
        currentFavorites.addAll(favoritesManager.loadFavorites())
        favoritesAdapter.notifyDataSetChanged()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                installedApps = appRepository.getAllLaunchableApps()
                // no-op, apps used in editor dialog
            } catch (e: Exception) {
                ErrorHandler.handleAppRepositoryError(requireContext(), e)
            }
        }
    }

    private fun addFavorite(app: InstalledApp) {
        if (currentFavorites.size >= FavoriteApp.MAX_FAVORITES) {
            ErrorHandler.handleMaxFavoritesReached(requireContext())
            return
        }
        if (currentFavorites.any { it.packageName == app.packageName }) return
        val newFavorite = app.toFavoriteApp(currentFavorites.size)
        currentFavorites.add(newFavorite)
        favoritesAdapter.notifyItemInserted(currentFavorites.size - 1)
    }

    private fun removeFavorite(favorite: FavoriteApp) {
        val index = currentFavorites.indexOf(favorite)
        if (index >= 0) {
            currentFavorites.removeAt(index)
            currentFavorites.forEachIndexed { newIndex, fav ->
                currentFavorites[newIndex] = fav.copy(order = newIndex)
            }
            favoritesAdapter.notifyItemRemoved(index)
            favoritesAdapter.notifyItemRangeChanged(index, currentFavorites.size - index)
        }
    }

    private fun removeFavoriteByPackage(packageName: String) {
        val fav = currentFavorites.find { it.packageName == packageName }
        if (fav != null) removeFavorite(fav)
    }

    fun getSelectedFavorites(): List<FavoriteApp> = currentFavorites.toList()

    private fun showFavoritesEditor() {
        if (installedApps.isEmpty()) return
        val names = installedApps.map { it.displayName }
        val checked = installedApps.map { app -> currentFavorites.any { it.packageName == app.packageName } }.toBooleanArray()

        val builder = AlertDialog.Builder(requireContext(), R.style.DarkAlertDialog)
            .setTitle("Select up to 7 apps")
            .setMultiChoiceItems(names.toTypedArray(), checked) { dialogInterface, which, isChecked ->
                val app = installedApps[which]
                val alreadySelected = currentFavorites.any { it.packageName == app.packageName }
                if (isChecked && !alreadySelected) {
                    if (currentFavorites.size >= FavoriteApp.MAX_FAVORITES) {
                        // Revert selection in dialog due to limit
                        (dialogInterface as? AlertDialog)?.listView?.setItemChecked(which, false)
                    } else {
                        addFavorite(app)
                    }
                } else if (!isChecked && alreadySelected) {
                    removeFavoriteByPackage(app.packageName)
                }
                (dialogInterface as? AlertDialog)?.let { updateDialogAppearance(it) }
            }
            .setPositiveButton("Done", null)
            .setNegativeButton("Cancel", null)

        val dialog = builder.create()
        dialog.setOnShowListener {
            updateDialogAppearance(dialog)
        }
        dialog.show()
    }

    private fun updateDialogAppearance(dialog: AlertDialog) {
        val listView = dialog.listView ?: return
        listView.setBackgroundColor(android.graphics.Color.BLACK)
        val selectedCount = currentFavorites.size
        val maxReached = selectedCount >= FavoriteApp.MAX_FAVORITES
        val first = listView.firstVisiblePosition
        val last = listView.lastVisiblePosition
        for (pos in first..last) {
            val child = listView.getChildAt(pos - first)
            val checked = listView.isItemChecked(pos)
            val allow = checked || !maxReached
            child?.isEnabled = allow
            val textView = child as? android.widget.CheckedTextView
            if (textView != null) {
                if (allow) {
                    textView.setTextColor(android.graphics.Color.WHITE)
                    textView.alpha = 1f
                } else {
                    textView.setTextColor(android.graphics.Color.parseColor("#88FFFFFF"))
                    textView.alpha = 0.6f
                }
            }
        }
    }
}


