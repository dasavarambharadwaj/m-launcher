package com.example.m_launcher.manager

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.m_launcher.data.GestureConfig
import com.example.m_launcher.data.LayoutConfig
import com.example.m_launcher.data.FontSize
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val TAG = "SettingsManager"
        private const val PREFS_NAME = "launcher_settings"
        private const val KEY_GESTURE_CONFIG = "gesture_config"
        private const val KEY_LAYOUT_CONFIG = "layout_config"
        private const val KEY_FONT_SIZE = "font_size"
    }

    fun loadGestureConfig(): GestureConfig {
        return try {
            val stored = prefs.getString(KEY_GESTURE_CONFIG, null)
            if (stored.isNullOrBlank()) GestureConfig() else json.decodeFromString(stored)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load gesture config", e)
            GestureConfig()
        }
    }

    fun saveGestureConfig(config: GestureConfig): Boolean {
        return try {
            val encoded = json.encodeToString(config)
            prefs.edit().putString(KEY_GESTURE_CONFIG, encoded).apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save gesture config", e)
            false
        }
    }

    fun loadLayoutConfig(): LayoutConfig {
        return try {
            val stored = prefs.getString(KEY_LAYOUT_CONFIG, null)
            if (stored.isNullOrBlank()) LayoutConfig() else json.decodeFromString(stored)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load layout config", e)
            LayoutConfig()
        }
    }

    fun saveLayoutConfig(config: LayoutConfig): Boolean {
        return try {
            val encoded = json.encodeToString(config)
            prefs.edit().putString(KEY_LAYOUT_CONFIG, encoded).apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save layout config", e)
            false
        }
    }

    fun loadFontSize(): FontSize {
        return try {
            val name = prefs.getString(KEY_FONT_SIZE, FontSize.MEDIUM.name)
            FontSize.valueOf(name!!)
        } catch (_: Exception) {
            FontSize.MEDIUM
        }
    }

    fun saveFontSize(fontSize: FontSize): Boolean {
        return try {
            prefs.edit().putString(KEY_FONT_SIZE, fontSize.name).apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save font size", e)
            false
        }
    }
}


