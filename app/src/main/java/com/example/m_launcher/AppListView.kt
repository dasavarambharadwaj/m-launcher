package com.example.m_launcher

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

/**
 * AppListView - Custom LinearLayout component for displaying essential apps
 * Uses Material Expressive design system for minimal launcher interface
 */
class AppListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var phoneTextView: TextView
    private lateinit var messagesTextView: TextView
    private lateinit var browserTextView: TextView

    init {
        setupAppListView()
        createAppTextViews()
    }

    /**
     * Configure the LinearLayout with Material Expressive spacing and alignment
     */
    private fun setupAppListView() {
        // Configure LinearLayout orientation and alignment for centered display
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL

        // Apply Material Expressive spacing guidelines (24dp between items)
        val spacingPx = resources.getDimensionPixelSize(R.dimen.material_expressive_spacing_large)
        
        // Set layout parameters for centered positioning
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }

        // Configure padding using Material Expressive guidelines
        val paddingPx = resources.getDimensionPixelSize(R.dimen.material_expressive_padding_medium)
        setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
    }

    /**
     * Create and configure the three essential app TextViews with Material Expressive typography
     */
    private fun createAppTextViews() {
        // Create Phone TextView
        phoneTextView = createAppTextView("Phone")
        addView(phoneTextView)

        // Add spacing between items
        addSpacingView()

        // Create Messages TextView
        messagesTextView = createAppTextView("Messages")
        addView(messagesTextView)

        // Add spacing between items
        addSpacingView()

        // Create Browser TextView
        browserTextView = createAppTextView("Browser")
        addView(browserTextView)
    }

    /**
     * Create a single app TextView with Material Expressive styling
     */
    private fun createAppTextView(appName: String): TextView {
        return TextView(context).apply {
            text = appName
            
            // Apply Material Expressive typography system
            setTextAppearance(R.style.LauncherTextAppearance)
            
            // Configure text alignment for centered minimal design
            gravity = Gravity.CENTER
            
            // Ensure single line text display
            setSingleLine(true)
            
            // Set layout parameters with proper width for text and touch targets
            val touchTargetSize = resources.getDimensionPixelSize(R.dimen.material_expressive_touch_target_min)
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                touchTargetSize
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
            
            // Set minimum width to ensure touch target accessibility
            minWidth = touchTargetSize
            
            // Configure touch feedback for future interaction
            isClickable = true
            isFocusable = true
            
            // Apply Material Expressive minimal styling with proper padding for touch targets
            background = null // No background for minimal appearance
            val touchPadding = resources.getDimensionPixelSize(R.dimen.material_expressive_padding_small)
            setPadding(touchPadding, touchPadding, touchPadding, touchPadding)
            
            // Set content description for accessibility
            contentDescription = "Launch $appName"
        }
    }

    /**
     * Add spacing view between app items using Material Expressive spacing guidelines
     */
    private fun addSpacingView() {
        val spacingView = TextView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.material_expressive_spacing_large)
            )
            // Invisible spacer
            text = ""
        }
        addView(spacingView)
    }

    /**
     * Get reference to Phone TextView for future interaction handling
     */
    fun getPhoneTextView(): TextView = phoneTextView

    /**
     * Get reference to Messages TextView for future interaction handling
     */
    fun getMessagesTextView(): TextView = messagesTextView

    /**
     * Get reference to Browser TextView for future interaction handling
     */
    fun getBrowserTextView(): TextView = browserTextView

    /**
     * Update text color dynamically based on wallpaper contrast
     * This method will be used by the dynamic text contrast system
     */
    fun updateTextColor(textColor: Int) {
        phoneTextView.setTextColor(textColor)
        messagesTextView.setTextColor(textColor)
        browserTextView.setTextColor(textColor)
    }
}