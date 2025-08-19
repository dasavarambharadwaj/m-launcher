package com.example.m_launcher.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.m_launcher.R
import com.example.m_launcher.data.GestureConfig
import com.example.m_launcher.manager.SettingsManager
import com.example.m_launcher.repository.AppRepository
import com.example.m_launcher.utils.ErrorHandler
import kotlinx.coroutines.launch

class GesturesFragment : Fragment() {

    private lateinit var settingsManager: SettingsManager
    private lateinit var appRepository: AppRepository

    private lateinit var leftPicker: Button
    private lateinit var rightPicker: Button
    private lateinit var title: TextView

    private var gestureConfig: GestureConfig = GestureConfig()
    private var installedAppNames: List<String> = emptyList()
    private var installedAppPackages: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_gestures, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsManager = SettingsManager(requireContext())
        appRepository = AppRepository(requireContext())

        leftPicker = view.findViewById(R.id.left_swipe_picker)
        rightPicker = view.findViewById(R.id.right_swipe_picker)
        title = view.findViewById(R.id.gestures_title)

        gestureConfig = settingsManager.loadGestureConfig()
        updatePickerText()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val apps = appRepository.getAllLaunchableApps()
                installedAppNames = apps.map { it.displayName }
                installedAppPackages = apps.map { it.packageName }
            } catch (e: Exception) {
                ErrorHandler.handleAppRepositoryError(requireContext(), e)
            }
        }

        leftPicker.setOnClickListener { showPicker(true) }
        rightPicker.setOnClickListener { showPicker(false) }
    }

    private fun updatePickerText() {
        leftPicker.text = gestureConfig.leftSwipeName ?: "Select app"
        rightPicker.text = gestureConfig.rightSwipeName ?: "Select app"
    }

    private fun showPicker(isLeft: Boolean) {
        if (installedAppNames.isEmpty()) return
        val builder = AlertDialog.Builder(requireContext(), R.style.DarkAlertDialog)
            .setTitle(if (isLeft) "Left Swipe App" else "Right Swipe App")
            .setItems(installedAppNames.toTypedArray()) { dialog, which ->
                val name = installedAppNames[which]
                val pkg = installedAppPackages[which]
                gestureConfig = if (isLeft) {
                    gestureConfig.copy(leftSwipeName = name, leftSwipePackage = pkg)
                } else {
                    gestureConfig.copy(rightSwipeName = name, rightSwipePackage = pkg)
                }
                updatePickerText()
                dialog.dismiss()
            }
            .setNeutralButton("Clear") { dialog, _ ->
                gestureConfig = if (isLeft) {
                    gestureConfig.copy(leftSwipeName = null, leftSwipePackage = null)
                } else {
                    gestureConfig.copy(rightSwipeName = null, rightSwipePackage = null)
                }
                updatePickerText()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
        val dialog = builder.create()
        dialog.setOnShowListener {
            val lv = dialog.listView
            lv?.setBackgroundColor(android.graphics.Color.BLACK)
            val first = lv?.firstVisiblePosition ?: 0
            val last = lv?.lastVisiblePosition ?: -1
            for (pos in first..last) {
                val child = lv?.getChildAt(pos - first)
                val tv = child as? android.widget.TextView
                tv?.setTextColor(android.graphics.Color.WHITE)
            }
        }
        dialog.show()
    }

    fun getGestureConfig(): GestureConfig = gestureConfig
}


