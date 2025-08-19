# Design Document

## Overview

The minimal launcher UI will be implemented as an Android application that registers itself as a home screen launcher. The design focuses on creating a clean, text-based interface that displays configurable favorite apps (1-7 apps) while maintaining wallpaper visibility and adhering to minimal design principles. The launcher includes a settings page for managing favorites, accessible through long press gestures on the home screen.

## Architecture

### Application Structure
- **MainActivity**: Primary launcher activity that handles home screen display, long press gestures, and swipe up search
- **SearchActivity**: Full-screen search interface with fuzzy app search functionality
- **SettingsActivity**: Configuration page for managing favorite apps
- **LauncherTheme**: Custom theme system for minimal styling
- **AppListView**: Custom view component for displaying configurable favorite apps (1-7)
- **SearchView**: Custom search interface with fuzzy search capabilities
- **WallpaperManager**: Handles background transparency and wallpaper integration
- **FavoritesManager**: Manages favorite app storage, retrieval, and persistence
- **AppRepository**: Handles installed app discovery and metadata retrieval
- **SearchManager**: Handles fuzzy search logic and app filtering

### Android Launcher Integration
The app will register as a launcher through AndroidManifest.xml configuration with:
- `android.intent.category.HOME` intent filter
- `android.intent.category.DEFAULT` category
- Appropriate launcher permissions and metadata

## Components and Interfaces

### MainActivity
```kotlin
class MainActivity : AppCompatActivity() {
    // Primary launcher activity
    // Handles system home button presses
    // Manages configurable app list display
    // Implements wallpaper transparency
    // Handles long press gestures for settings access
    // Detects swipe up gestures for search interface
}
```

**Responsibilities:**
- Display the home screen UI with configurable favorites
- Handle launcher lifecycle events
- Manage wallpaper background integration
- Coordinate with AppListView for dynamic app display
- Detect long press gestures and navigate to settings
- Detect swipe up gestures and launch search interface
- Load and display user-configured favorite apps

### AppListView
```kotlin
class AppListView : LinearLayout {
    // Custom view for displaying configurable favorite apps
    // Renders text-based app entries (1-7 apps)
    // Handles app selection interactions
    // Dynamically adapts to user-configured favorites
}
```

**Responsibilities:**
- Render user-configured favorite apps as text labels (1-7 apps)
- Apply minimal styling to text elements
- Handle touch interactions for app launching (future functionality)
- Maintain consistent spacing and alignment regardless of app count
- Dynamically update display when favorites change

### SettingsActivity
```kotlin
class SettingsActivity : AppCompatActivity() {
    // Configuration page for managing favorite apps
    // Displays installed apps for selection
    // Handles favorite app persistence
}
```

**Responsibilities:**
- Display all installed apps in a selectable list
- Allow users to select/deselect up to 7 favorite apps
- Provide reordering functionality for selected favorites
- Save favorite configurations to persistent storage
- Maintain minimal design aesthetic consistent with launcher

### FavoritesManager
```kotlin
class FavoritesManager {
    // Manages favorite app storage and retrieval
    // Handles persistence using SharedPreferences
    // Provides default fallback configuration
}
```

**Responsibilities:**
- Store and retrieve user-configured favorite apps
- Provide default apps (Phone, Messages, Browser) when no favorites configured
- Validate favorite app selections (1-7 apps limit)
- Handle app uninstallation scenarios (remove from favorites)
- Persist favorites across app restarts

### SearchActivity
```kotlin
class SearchActivity : AppCompatActivity() {
    // Full-screen search interface
    // Handles fuzzy search input and results display
    // Manages keyboard visibility and search interactions
}
```

**Responsibilities:**
- Display full-screen black search interface
- Show search input field at top of screen
- Automatically display keyboard on interface open
- Handle search input and trigger fuzzy search
- Display search results in minimal text format
- Handle app selection and launch from search results
- Manage swipe down/back gestures to close search

### SearchManager
```kotlin
class SearchManager {
    // Handles fuzzy search logic and app filtering
    // Provides real-time search results
    // Manages search performance optimization
}
```

**Responsibilities:**
- Perform fuzzy search across all installed app names
- Filter and rank search results by relevance
- Provide real-time search suggestions as user types
- Cache search results for performance optimization
- Handle special characters and multilingual app names

### AppRepository
```kotlin
class AppRepository {
    // Handles installed app discovery and metadata
    // Provides app information for settings page and search
}
```

**Responsibilities:**
- Query all installed apps from PackageManager
- Filter launchable apps for settings display and search
- Provide app metadata (name, package name, icon)
- Handle app installation/uninstallation events
- Maintain cached app list for search performance

### LauncherTheme
**Responsibilities:**
- Define minimal color palette
- Specify typography settings
- Ensure high contrast for readability
- Support both light and dark wallpapers

## Data Models

### AppEntry
```kotlin
data class AppEntry(
    val displayName: String,
    val packageName: String,
    val isEnabled: Boolean = true,
    val isFavorite: Boolean = false,
    val favoriteOrder: Int = -1
)
```

**Purpose:** Represents each app with favorite status and ordering information

### FavoriteApp
```kotlin
data class FavoriteApp(
    val packageName: String,
    val displayName: String,
    val order: Int
)
```

**Purpose:** Represents a user-configured favorite app with display order

### InstalledApp
```kotlin
data class InstalledApp(
    val packageName: String,
    val displayName: String,
    val icon: Drawable?,
    val isLaunchable: Boolean = true
)
```

**Purpose:** Represents an installed app available for favorite selection

### LauncherConfig
```kotlin
data class LauncherConfig(
    val textSize: Float,
    val textColor: Int,
    val backgroundColor: Int,
    val spacing: Int
)
```

**Purpose:** Configuration object for UI styling and layout parameters

## UI Design Specifications

### Layout Structure

#### Home Screen (MainActivity)
```
MainActivity (Full Screen)
├── StatusBar (Transparent)
├── AppListView (Centered)
│   ├── FavoriteApp1 (TextView)
│   ├── FavoriteApp2 (TextView)
│   ├── ... (1-7 apps total)
│   └── FavoriteAppN (TextView)
└── NavigationBar (Transparent)
```

#### Settings Page (SettingsActivity)
```
SettingsActivity (Full Screen)
├── StatusBar (Transparent)
├── ToolBar (Minimal)
│   └── "Favorites Settings" (Title)
├── ScrollView
│   ├── "Selected Favorites" (Section)
│   │   └── RecyclerView (Selected Apps, Reorderable)
│   ├── "Available Apps" (Section)
│   │   └── RecyclerView (All Installed Apps, Selectable)
│   └── Save Button (Minimal)
└── NavigationBar (Transparent)
```

#### Search Interface (SearchActivity)
```
SearchActivity (Full Screen Black)
├── StatusBar (Black)
├── SearchInputField (Top, Focused)
│   └── Keyboard (Auto-displayed)
├── SearchResults (ScrollView)
│   ├── MatchingApp1 (TextView)
│   ├── MatchingApp2 (TextView)
│   └── ... (Fuzzy search results)
└── NavigationBar (Black)
```

### Visual Design

#### Home Screen
- **Background**: Fully transparent to show device wallpaper
- **Text Color**: Dynamic based on wallpaper (white/black for contrast)
- **Typography**: Clean, sans-serif font (Roboto)
- **Text Size**: 18sp for optimal readability
- **Spacing**: 24dp between app entries (adaptive for 1-7 apps)
- **Alignment**: Center-aligned vertically and horizontally
- **Long Press**: Haptic feedback with subtle visual indication

#### Settings Page
- **Background**: Semi-transparent overlay maintaining wallpaper visibility
- **Text Color**: Consistent with home screen dynamic contrast
- **Typography**: Same Roboto font family for consistency
- **Text Size**: 16sp for settings content, 18sp for section headers
- **Layout**: Clean list-based interface with minimal dividers
- **Selection**: Subtle highlight for selected favorites
- **Buttons**: Text-based buttons maintaining minimal aesthetic

#### Search Interface
- **Background**: Solid black (#000000) full-screen overlay
- **Search Field**: White text on black background at top of screen
- **Text Color**: White (#FFFFFF) for maximum contrast on black
- **Typography**: Same Roboto font family for consistency
- **Text Size**: 18sp for search input, 16sp for results
- **Layout**: Vertical list of search results below input field
- **Keyboard**: Automatically displayed when interface opens
- **Results**: Real-time filtering as user types
- **Spacing**: 20dp between search results for easy touch targets

### Theme Configuration
```xml
<style name="LauncherTheme" parent="Theme.MaterialComponents.NoActionBar">
    <item name="android:windowBackground">@android:color/transparent</item>
    <item name="android:windowIsTranslucent">true</item>
    <item name="android:windowNoTitle">true</item>
    <item name="android:windowFullscreen">true</item>
</style>
```

### Color Palette
- **Primary Text**: #FFFFFF (white) or #000000 (black) based on wallpaper
- **Background**: Transparent
- **Accent**: None (minimal approach)

## User Interaction Design

### Long Press Gesture Detection
```kotlin
// MainActivity gesture handling
private val longPressDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
    override fun onLongPress(e: MotionEvent) {
        // Trigger haptic feedback
        // Navigate to SettingsActivity
        // Apply transition animation
    }
})
```

**Implementation:**
- Detect long press anywhere on home screen background
- Provide haptic feedback (vibration) on gesture recognition
- Smooth transition animation to settings page
- Minimum 500ms press duration for activation

### Favorites Configuration Flow
1. **Access Settings**: Long press on home screen
2. **View Current Favorites**: Display currently selected apps at top
3. **Browse Available Apps**: Scrollable list of all installed apps
4. **Select/Deselect**: Tap to add/remove from favorites (max 7)
5. **Reorder Favorites**: Drag and drop in selected favorites section
6. **Save Configuration**: Persist changes and return to home screen
7. **Update Display**: Home screen reflects new favorite configuration

### App Selection Logic
- **Minimum**: At least 1 app must be selected as favorite
- **Maximum**: Up to 7 apps can be selected as favorites
- **Default Fallback**: If no favorites configured, show Phone, Messages, Browser
- **Validation**: Prevent selection of more than 7 apps
- **Persistence**: Save configuration immediately on changes

## Data Persistence

### SharedPreferences Storage
```kotlin
// Favorites storage structure
private const val PREFS_NAME = "launcher_favorites"
private const val KEY_FAVORITE_APPS = "favorite_apps_json"
private const val KEY_FAVORITES_COUNT = "favorites_count"

// JSON structure for favorite apps
{
  "favorites": [
    {
      "packageName": "com.android.dialer",
      "displayName": "Phone",
      "order": 0
    },
    {
      "packageName": "com.android.mms",
      "displayName": "Messages", 
      "order": 1
    }
  ]
}
```

**Storage Strategy:**
- Use SharedPreferences for lightweight favorite app storage
- JSON serialization for complex favorite app data
- Atomic updates to prevent data corruption
- Backup and restore capability for app data migration

### Default Configuration
```kotlin
// Default apps when no favorites configured
private val defaultApps = listOf(
    FavoriteApp("com.android.dialer", "Phone", 0),
    FavoriteApp("com.android.mms", "Messages", 1),
    FavoriteApp("com.android.browser", "Browser", 2)
)
```

## Error Handling

### Wallpaper Integration Failures
- **Fallback**: Use system default background color
- **Detection**: Monitor wallpaper changes and adapt text color accordingly

### Theme Application Issues
- **Fallback**: Use high-contrast white text on transparent background
- **Validation**: Ensure text remains readable in all scenarios

### Layout Rendering Problems
- **Fallback**: Use standard LinearLayout with default spacing
- **Recovery**: Graceful degradation to basic text list

### Favorites Configuration Errors
- **Invalid App Selection**: Validate package names against installed apps
- **Storage Failures**: Fallback to default apps if favorites cannot be loaded
- **Corrupted Data**: Reset to default configuration and notify user
- **App Uninstallation**: Remove uninstalled apps from favorites automatically

### Settings Page Navigation Issues
- **Long Press Detection Failure**: Provide alternative settings access method
- **Activity Launch Problems**: Graceful error handling with user notification
- **Data Synchronization**: Ensure home screen updates reflect settings changes

### App Repository Failures
- **PackageManager Errors**: Handle security exceptions gracefully
- **Missing App Metadata**: Use package name as fallback display name
- **Permission Issues**: Request necessary permissions or disable affected features

## Testing Strategy

### UI Testing
1. **Visual Regression Tests**: Verify layout consistency across different screen sizes and app counts (1-7)
2. **Wallpaper Integration Tests**: Test with various wallpaper types (light, dark, colorful)
3. **Theme Application Tests**: Validate minimal styling is applied correctly on both home and settings screens
4. **Launcher Registration Tests**: Confirm app appears in launcher selection
5. **Long Press Gesture Tests**: Verify gesture detection and settings navigation
6. **Favorites Configuration Tests**: Test app selection, reordering, and persistence
7. **Dynamic Layout Tests**: Verify proper spacing and alignment with different favorite counts

### Device Testing
1. **Screen Size Compatibility**: Test on phones and tablets with various favorite counts
2. **Android Version Compatibility**: Verify on target API levels
3. **Wallpaper Variety**: Test with different wallpaper types and colors
4. **Orientation Changes**: Ensure layout adapts to portrait/landscape
5. **Gesture Sensitivity**: Test long press detection across different devices
6. **Performance Testing**: Verify smooth transitions between home and settings
7. **Storage Testing**: Test favorites persistence across app restarts and device reboots

### Accessibility Testing
1. **Text Contrast**: Verify readability against various backgrounds on both screens
2. **Touch Targets**: Ensure adequate size for text selection areas and settings interactions
3. **Screen Reader**: Test with TalkBack for accessibility compliance
4. **Gesture Accessibility**: Ensure long press gesture is accessible to users with motor impairments
5. **Settings Navigation**: Verify settings page is fully accessible with screen readers
6. **Content Descriptions**: Provide proper descriptions for all interactive elements

## Implementation Notes

### Launcher Registration
The AndroidManifest.xml must include:
```xml
<activity android:name=".MainActivity"
    android:exported="true"
    android:launchMode="singleTask"
    android:stateNotNeeded="true">
    <intent-filter android:priority="1">
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.HOME" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
```

### Wallpaper Transparency
Achieve wallpaper visibility through:
- Transparent window background
- Translucent window flag
- No action bar or title bar
- Full-screen immersive mode

### Text Contrast Algorithm
Implement dynamic text color selection:
1. Sample wallpaper colors at text positions
2. Calculate luminance of sampled area
3. Choose white or black text for optimal contrast
4. Update text color dynamically when wallpaper changes
5. Apply consistent contrast algorithm to both home and settings screens

### Favorites Management Implementation
```kotlin
// Core favorites management workflow
class FavoritesManager {
    fun loadFavorites(): List<FavoriteApp> {
        // Load from SharedPreferences
        // Validate against installed apps
        // Return default if none configured
    }
    
    fun saveFavorites(favorites: List<FavoriteApp>) {
        // Validate 1-7 app limit
        // Serialize to JSON
        // Store in SharedPreferences
        // Notify observers of changes
    }
}
```

### Settings Activity Navigation
```kotlin
// Long press gesture implementation
private fun setupLongPressGesture() {
    rootView.setOnLongClickListener {
        // Haptic feedback
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        
        // Launch settings with transition
        val intent = Intent(this, SettingsActivity::class.java)
        startActivityForResult(intent, SETTINGS_REQUEST_CODE)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        
        true
    }
}
```