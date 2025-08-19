package com.example.m_launcher.manager

import android.content.res.Resources
import android.view.Gravity
import android.widget.FrameLayout
import com.example.m_launcher.AppListView
import com.example.m_launcher.data.HorizontalPosition
import com.example.m_launcher.data.LayoutConfig
import com.example.m_launcher.data.VerticalPosition

class LayoutManager {

    fun applyLayout(appListView: AppListView, layoutConfig: LayoutConfig) {
        // Delegate alignment to the view itself
        appListView.updatePosition(layoutConfig.horizontalPosition, layoutConfig.verticalPosition)

        // Apply margins responsive to screen size and position for visual balance
        val params = (appListView.layoutParams as? FrameLayout.LayoutParams)
            ?: return

        val margins = calculateMargins(appListView.resources, layoutConfig)
        params.setMargins(margins.left, margins.top, margins.right, margins.bottom)
        appListView.layoutParams = params
        appListView.requestLayout()
    }

    private fun calculateMargins(resources: Resources, layoutConfig: LayoutConfig): Margins {
        val basePadding = resources.getDimensionPixelSize(
            com.example.m_launcher.R.dimen.material_expressive_padding_medium
        )
        val largePadding = resources.getDimensionPixelSize(
            com.example.m_launcher.R.dimen.material_expressive_padding_large
        )

        // Horizontal margins: add spacing when pinned to edges
        val left = when (layoutConfig.horizontalPosition) {
            HorizontalPosition.LEFT -> largePadding
            HorizontalPosition.CENTER -> basePadding
            HorizontalPosition.RIGHT -> basePadding
        }
        val right = when (layoutConfig.horizontalPosition) {
            HorizontalPosition.RIGHT -> largePadding
            HorizontalPosition.CENTER -> basePadding
            HorizontalPosition.LEFT -> basePadding
        }

        // Vertical margins: add spacing when pinned to edges
        val top = when (layoutConfig.verticalPosition) {
            VerticalPosition.TOP -> largePadding * 2
            VerticalPosition.CENTER -> basePadding
            VerticalPosition.BOTTOM -> basePadding
        }
        val bottom = when (layoutConfig.verticalPosition) {
            VerticalPosition.BOTTOM -> largePadding * 2
            VerticalPosition.CENTER -> basePadding
            VerticalPosition.TOP -> basePadding
        }

        return Margins(left, top, right, bottom)
    }

    data class Margins(val left: Int, val top: Int, val right: Int, val bottom: Int)
}


