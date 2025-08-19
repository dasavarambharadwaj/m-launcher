package com.example.m_launcher.manager

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.m_launcher.data.GestureConfig
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
}


