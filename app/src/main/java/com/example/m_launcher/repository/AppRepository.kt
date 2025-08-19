package com.example.m_launcher.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.util.Log
import com.example.m_launcher.data.InstalledApp
import com.example.m_launcher.utils.ErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for discovering and managing installed applications
 * Provides app information for settings page and favorites configuration
 */
class AppRepository(private val context: Context) {
    
    private val packageManager: PackageManager = context.packageManager
    private var cachedApps: List<InstalledApp>? = null
    private var lastCacheUpdate: Long = 0
    
    companion object {
        private const val TAG = "AppRepository"
        private const val CACHE_DURATION_MS = 30_000 // 30 seconds
        
        // System apps to exclude from the list
        private val EXCLUDED_PACKAGES = setOf(
            "android",
            "com.android.systemui",
            "com.android.launcher",
            "com.android.launcher2",
            "com.android.launcher3",
            "com.google.android.launcher"
        )
    }
    
    /**
     * Get all launchable installed apps
     */
    suspend fun getAllLaunchableApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        try {
            // Check cache first
            if (isCacheValid()) {
                Log.d(TAG, "Returning cached apps")
                return@withContext cachedApps!!
            }
            
            Log.d(TAG, "Loading installed apps from PackageManager")
            
            // Query all apps with launcher intent
            val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            
            val resolveInfos = packageManager.queryIntentActivities(launcherIntent, 0)
            
            val installedApps = resolveInfos.mapNotNull { resolveInfo ->
                try {
                    createInstalledApp(resolveInfo)
                } catch (e: Exception) {
                    Log.w(TAG, "Error processing app: ${resolveInfo.activityInfo?.packageName}", e)
                    null
                }
            }.filter { app ->
                // Filter out excluded packages and invalid apps
                !EXCLUDED_PACKAGES.contains(app.packageName) && app.canBeAddedAsFavorite()
            }.sortedBy { it.displayName.lowercase() }
            
            // Update cache
            cachedApps = installedApps
            lastCacheUpdate = System.currentTimeMillis()
            
            Log.d(TAG, "Loaded ${installedApps.size} launchable apps")
            installedApps
            
        } catch (e: Exception) {
            Log.e(TAG, "Error loading installed apps", e)
            // Don't show user error here as it's handled by the calling activity
            emptyList()
        }
    }
    
    /**
     * Get app information by package name
     */
    suspend fun getAppByPackageName(packageName: String): InstalledApp? = withContext(Dispatchers.IO) {
        try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val displayName = packageManager.getApplicationLabel(applicationInfo).toString()
            val icon = packageManager.getApplicationIcon(applicationInfo)
            val isSystemApp = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            
            InstalledApp(
                packageName = packageName,
                displayName = displayName,
                icon = icon,
                isLaunchable = isAppLaunchable(packageName),
                isSystemApp = isSystemApp
            )
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "App not found: $packageName")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app info for $packageName", e)
            null
        }
    }
    
    /**
     * Search apps by name
     */
    suspend fun searchApps(query: String): List<InstalledApp> = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            return@withContext getAllLaunchableApps()
        }
        
        val allApps = getAllLaunchableApps()
        val lowercaseQuery = query.lowercase()
        
        allApps.filter { app ->
            app.displayName.lowercase().contains(lowercaseQuery) ||
            app.packageName.lowercase().contains(lowercaseQuery)
        }
    }
    
    /**
     * Check if an app is installed and launchable
     */
    fun isAppInstalled(packageName: String): Boolean {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            isAppLaunchable(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    /**
     * Get app icon by package name
     */
    fun getAppIcon(packageName: String): Drawable? {
        return try {
            packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            Log.w(TAG, "Could not get icon for $packageName", e)
            null
        }
    }
    
    /**
     * Get app display name by package name
     */
    fun getAppDisplayName(packageName: String): String? {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: Exception) {
            Log.w(TAG, "Could not get display name for $packageName", e)
            null
        }
    }
    
    /**
     * Invalidate the app cache to force refresh
     */
    fun invalidateCache() {
        cachedApps = null
        lastCacheUpdate = 0
        Log.d(TAG, "App cache invalidated")
    }
    
    /**
     * Create InstalledApp from ResolveInfo
     */
    private fun createInstalledApp(resolveInfo: ResolveInfo): InstalledApp {
        val activityInfo = resolveInfo.activityInfo
        val packageName = activityInfo.packageName
        val displayName = resolveInfo.loadLabel(packageManager).toString()
        val icon = resolveInfo.loadIcon(packageManager)
        
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        val isSystemApp = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        
        return InstalledApp(
            packageName = packageName,
            displayName = displayName,
            icon = icon,
            isLaunchable = true, // Already filtered by launcher intent
            isSystemApp = isSystemApp
        )
    }
    
    /**
     * Check if an app has a launchable activity
     */
    private fun isAppLaunchable(packageName: String): Boolean {
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setPackage(packageName)
        }
        
        val activities = packageManager.queryIntentActivities(launcherIntent, 0)
        return activities.isNotEmpty()
    }
    
    /**
     * Check if the cached apps are still valid
     */
    private fun isCacheValid(): Boolean {
        return cachedApps != null && 
               (System.currentTimeMillis() - lastCacheUpdate) < CACHE_DURATION_MS
    }
}