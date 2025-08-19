package com.example.m_launcher.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.m_launcher.R
import com.example.m_launcher.data.FontSize
import com.example.m_launcher.data.HorizontalPosition
import com.example.m_launcher.data.LayoutConfig
import com.example.m_launcher.data.VerticalPosition
import com.example.m_launcher.manager.SettingsManager

class LayoutFragment : Fragment() {

    private lateinit var settingsManager: SettingsManager

    private lateinit var horizontalPicker: Button
    private lateinit var verticalPicker: Button
    private lateinit var fontSizePicker: Button

    private var layoutConfig: LayoutConfig = LayoutConfig()
    private var fontSize: FontSize = FontSize.MEDIUM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsManager = SettingsManager(requireContext())

        horizontalPicker = view.findViewById(R.id.position_horizontal_picker)
        verticalPicker = view.findViewById(R.id.position_vertical_picker)
        fontSizePicker = view.findViewById(R.id.font_size_picker)

        layoutConfig = settingsManager.loadLayoutConfig()
        fontSize = settingsManager.loadFontSize()
        updateUi()

        horizontalPicker.setOnClickListener { showHorizontalPicker() }
        verticalPicker.setOnClickListener { showVerticalPicker() }
        fontSizePicker.setOnClickListener { showFontSizePicker() }
    }

    private fun updateUi() {
        horizontalPicker.text = when (layoutConfig.horizontalPosition) {
            HorizontalPosition.LEFT -> "Left"
            HorizontalPosition.CENTER -> "Center"
            HorizontalPosition.RIGHT -> "Right"
        }
        verticalPicker.text = when (layoutConfig.verticalPosition) {
            VerticalPosition.TOP -> "Top"
            VerticalPosition.CENTER -> "Center"
            VerticalPosition.BOTTOM -> "Bottom"
        }
        fontSizePicker.text = fontSize.displayName

        // No preview to update
    }

    private fun showHorizontalPicker() {
        val items = arrayOf("Left", "Center", "Right")
        val builder = AlertDialog.Builder(requireContext(), R.style.DarkAlertDialog)
            .setTitle("Horizontal Position")
            .setSingleChoiceItems(items, when (layoutConfig.horizontalPosition) {
                HorizontalPosition.LEFT -> 0
                HorizontalPosition.CENTER -> 1
                HorizontalPosition.RIGHT -> 2
            }) { dialog, which ->
                layoutConfig = layoutConfig.copy(
                    horizontalPosition = when (which) {
                        0 -> HorizontalPosition.LEFT
                        1 -> HorizontalPosition.CENTER
                        else -> HorizontalPosition.RIGHT
                    }
                )
                updateUi()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
        val dialog = builder.create()
        dialog.setOnShowListener { updateSingleChoiceDialogAppearance(dialog) }
        dialog.show()
    }

    private fun showVerticalPicker() {
        val items = arrayOf("Top", "Center", "Bottom")
        val builder = AlertDialog.Builder(requireContext(), R.style.DarkAlertDialog)
            .setTitle("Vertical Position")
            .setSingleChoiceItems(items, when (layoutConfig.verticalPosition) {
                VerticalPosition.TOP -> 0
                VerticalPosition.CENTER -> 1
                VerticalPosition.BOTTOM -> 2
            }) { dialog, which ->
                layoutConfig = layoutConfig.copy(
                    verticalPosition = when (which) {
                        0 -> VerticalPosition.TOP
                        1 -> VerticalPosition.CENTER
                        else -> VerticalPosition.BOTTOM
                    }
                )
                updateUi()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
        val dialog = builder.create()
        dialog.setOnShowListener { updateSingleChoiceDialogAppearance(dialog) }
        dialog.show()
    }

    private fun showFontSizePicker() {
        val items = arrayOf("Small", "Medium", "Large", "Extra Large")
        val builder = AlertDialog.Builder(requireContext(), R.style.DarkAlertDialog)
            .setTitle("Font Size")
            .setSingleChoiceItems(items, when (fontSize) {
                FontSize.SMALL -> 0
                FontSize.MEDIUM -> 1
                FontSize.LARGE -> 2
                FontSize.EXTRA_LARGE -> 3
            }) { dialog, which ->
                fontSize = when (which) {
                    0 -> FontSize.SMALL
                    1 -> FontSize.MEDIUM
                    2 -> FontSize.LARGE
                    else -> FontSize.EXTRA_LARGE
                }
                updateUi()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
        val dialog = builder.create()
        dialog.setOnShowListener { updateSingleChoiceDialogAppearance(dialog) }
        dialog.show()
    }

    private fun updateSingleChoiceDialogAppearance(dialog: AlertDialog) {
        val listView = dialog.listView ?: return
        listView.setBackgroundColor(android.graphics.Color.BLACK)
        val first = listView.firstVisiblePosition
        val last = listView.lastVisiblePosition
        for (pos in first..last) {
            val child = listView.getChildAt(pos - first)
            val tv = child as? android.widget.TextView
            tv?.setTextColor(android.graphics.Color.WHITE)
        }
    }

    fun getLayoutConfig(): LayoutConfig = layoutConfig
    fun getFontSize(): FontSize = fontSize
}


