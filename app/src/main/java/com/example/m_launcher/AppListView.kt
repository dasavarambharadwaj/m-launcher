package com.example.m_launcher

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.example.m_launcher.data.FavoriteApp
import com.example.m_launcher.data.HorizontalPosition
import com.example.m_launcher.data.VerticalPosition

/**
 * AppListView - Custom LinearLayout component for displaying configurable favorite apps (1-7)
 * Uses Material Expressive design system for minimal launcher interface
 * Dynamically adapts to user-configured favorites
 */
class AppListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var appTextViews: MutableList<TextView> = mutableListOf()
    private var currentFavorites: List<FavoriteApp> = emptyList()
    private var onAppClickListener: ((FavoriteApp) -> Unit)? = null
    private var currentHorizontalPosition: HorizontalPosition = HorizontalPosition.CENTER
    private var currentVerticalPosition: VerticalPosition = VerticalPosition.CENTER

    init {
        setupAppListView()
    }

    /**
     * Configure the LinearLayout with Material Expressive spacing and alignment
     */
    private fun setupAppListView() {
        // Configure LinearLayout orientation and default alignment (center)
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL

        // Apply Material Expressive spacing guidelines (24dp between items)
        val spacingPx = resources.getDimensionPixelSize(R.dimen.material_expressive_spacing_large)
        
        // Initial layout parameters; container (e.g., FrameLayout) controls overall placement
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )

        // Configure padding using Material Expressive guidelines
        val paddingPx = resources.getDimensionPixelSize(R.dimen.material_expressive_padding_medium)
        setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
    }

    /**
     * Update the app list with new favorite apps (1-7 apps)
     */
    fun updateFavorites(favorites: List<FavoriteApp>) {
        if (favorites == currentFavorites) {
            return // No change needed
        }
        
        // Validate favorites list
        if (favorites.isEmpty()) {
            android.util.Log.w("AppListView", "Empty favorites list provided")
            return
        }
        
        if (favorites.size > FavoriteApp.MAX_FAVORITES) {
            android.util.Log.w("AppListView", "Too many favorites provided: ${favorites.size}")
            return
        }
        
        currentFavorites = favorites
        
        // Clear existing views
        removeAllViews()
        appTextViews.clear()
        
        // Create new TextViews for each favorite
        favorites.forEachIndexed { index, favorite ->
            val textView = createAppTextView(favorite)
            appTextViews.add(textView)
            addView(textView)
            
            // Add spacing between items (except after the last item)
            if (index < favorites.size - 1) {
                addSpacingView()
            }
        }
        
        // Request layout update
        requestLayout()
    }

    /**
     * Update positioning according to provided horizontal and vertical enums
     */
    fun updatePosition(horizontalPosition: HorizontalPosition, verticalPosition: VerticalPosition) {
        currentHorizontalPosition = horizontalPosition
        currentVerticalPosition = verticalPosition

        val containerGravity = when (horizontalPosition) {
            HorizontalPosition.LEFT -> Gravity.START
            HorizontalPosition.CENTER -> Gravity.CENTER_HORIZONTAL
            HorizontalPosition.RIGHT -> Gravity.END
        } or when (verticalPosition) {
            VerticalPosition.TOP -> Gravity.TOP
            VerticalPosition.CENTER -> Gravity.CENTER_VERTICAL
            VerticalPosition.BOTTOM -> Gravity.BOTTOM
        }

        // If parent is a FrameLayout (as in activity_main.xml), set its layout_gravity via LayoutParams
        val params = layoutParams
        if (params is FrameLayout.LayoutParams) {
            params.gravity = containerGravity
            layoutParams = params
        } else if (params is LayoutParams) {
            // Fallback for LinearLayout parent
            params.gravity = containerGravity
            layoutParams = params
        }

        // Align children horizontally within this LinearLayout
        gravity = when (horizontalPosition) {
            HorizontalPosition.LEFT -> Gravity.START
            HorizontalPosition.CENTER -> Gravity.CENTER_HORIZONTAL
            HorizontalPosition.RIGHT -> Gravity.END
        }

        updateChildTextAlignment()
        normalizeChildWidths()

        requestLayout()
        invalidate()
    }
    
    /**
     * Set click listener for app selection
     */
    fun setOnAppClickListener(listener: (FavoriteApp) -> Unit) {
        onAppClickListener = listener
    }

    /**
     * Create a single app TextView with Material Expressive styling for a favorite app
     */
    private fun createAppTextView(favorite: FavoriteApp): TextView {
        return TextView(context).apply {
            text = favorite.displayName
            
            // Apply Material Expressive typography system
            setTextAppearance(R.style.LauncherTextAppearance)
            
            // Configure text alignment according to current horizontal position
            gravity = when (currentHorizontalPosition) {
                HorizontalPosition.LEFT -> Gravity.START
                HorizontalPosition.CENTER -> Gravity.CENTER
                HorizontalPosition.RIGHT -> Gravity.END
            }
            
            // Ensure single line text display
            setSingleLine(true)
            
            // Set layout parameters with proper width for text and touch targets
            val touchTargetSize = resources.getDimensionPixelSize(R.dimen.material_expressive_touch_target_min)
            val minWidth = resources.getDimensionPixelSize(R.dimen.material_expressive_app_list_min_width)
            
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                touchTargetSize
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
            
            // Set minimum width to ensure proper text display and touch target accessibility
            this.minWidth = maxOf(touchTargetSize, minWidth)
            
            // Configure touch feedback and click handling
            isClickable = true
            isFocusable = true
            
            // Set click listener for app launching
            setOnClickListener {
                onAppClickListener?.invoke(favorite)
            }
            
            // Apply Material Expressive minimal styling with proper padding for touch targets
            background = null // No background for minimal appearance
            val touchPadding = resources.getDimensionPixelSize(R.dimen.material_expressive_padding_small)
            setPadding(touchPadding, touchPadding, touchPadding, touchPadding)
            
            // Set content description for accessibility
            contentDescription = "Launch ${favorite.displayName}"
        }
    }

    private fun updateChildTextAlignment() {
        val childGravity = when (currentHorizontalPosition) {
            HorizontalPosition.LEFT -> Gravity.START
            HorizontalPosition.CENTER -> Gravity.CENTER
            HorizontalPosition.RIGHT -> Gravity.END
        }
        appTextViews.forEach { tv ->
            tv.gravity = childGravity
        }
    }

    private fun normalizeChildWidths() {
        if (appTextViews.isEmpty()) return

        var maxTextWidth = 0
        appTextViews.forEach { tv ->
            val textWidth = tv.paint.measureText(tv.text.toString()).toInt()
            val total = textWidth + tv.paddingLeft + tv.paddingRight
            if (total > maxTextWidth) maxTextWidth = total
        }

        appTextViews.forEach { tv ->
            val min = tv.minWidth
            val target = maxOf(maxTextWidth, min)
            val lp = tv.layoutParams as LayoutParams
            lp.width = target
            tv.layoutParams = lp
        }
    }

    /**
     * Add adaptive spacing view between app items using Material Expressive spacing guidelines
     * Spacing adapts based on the number of apps to maintain proper visual balance
     */
    private fun addSpacingView() {
        val spacingSize = calculateAdaptiveSpacing()
        val spacingView = TextView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                spacingSize
            )
            // Invisible spacer
            text = ""
        }
        addView(spacingView)
    }
    
    /**
     * Calculate adaptive spacing based on the number of favorite apps
     * More apps = less spacing to fit properly on screen
     */
    private fun calculateAdaptiveSpacing(): Int {
        val baseSpacing = resources.getDimensionPixelSize(R.dimen.material_expressive_spacing_large)
        
        return when (currentFavorites.size) {
            1 -> baseSpacing * 2 // Extra spacing for single app
            2, 3 -> baseSpacing // Standard spacing for 2-3 apps
            4, 5 -> (baseSpacing * 0.8).toInt() // Slightly less spacing for 4-5 apps
            6, 7 -> (baseSpacing * 0.6).toInt() // Compact spacing for 6-7 apps
            else -> baseSpacing
        }
    }

    /**
     * Get all current app TextViews for external access
     */
    fun getAppTextViews(): List<TextView> = appTextViews.toList()
    
    /**
     * Get current favorite apps
     */
    fun getCurrentFavorites(): List<FavoriteApp> = currentFavorites.toList()
    
    /**
     * Get TextView for a specific favorite app by package name
     */
    fun getTextViewForApp(packageName: String): TextView? {
        val index = currentFavorites.indexOfFirst { it.packageName == packageName }
        return if (index >= 0 && index < appTextViews.size) {
            appTextViews[index]
        } else {
            null
        }
    }

    /**
     * Update text color dynamically based on wallpaper contrast
     * This method will be used by the dynamic text contrast system
     */
    fun updateTextColor(textColor: Int) {
        appTextViews.forEach { textView ->
            textView.setTextColor(textColor)
        }
    }
    
    /**
     * Get the number of currently displayed apps
     */
    fun getAppCount(): Int = currentFavorites.size
    
    /**
     * Check if the app list is empty
     */
    fun isEmpty(): Boolean = currentFavorites.isEmpty()
}