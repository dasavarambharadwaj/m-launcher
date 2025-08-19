# Design Document

## Overview

The minimal launcher UI will be implemented as an Android application that registers itself as a home screen launcher. The design focuses on creating a clean, text-based interface that displays configurable favorite apps (1-7 apps) while maintaining wallpaper visibility and adhering to minimal design principles. The launcher includes a settings page for managing favorites, accessible through long press gestures on the home screen.

## Architecture

### Application Structure
- **MainActivity**: Primary launcher activity that handles home screen display, gesture detection, and advanced configurations
- **SearchActivity**: Full-screen search interface with fuzzy app search functionality and configurable font sizes
- **SettingsActivity**: Enhanced tabbed configuration interface for all launcher settings
- **LauncherTheme**: Custom theme system for minimal styling
- **AppListView**: Custom view component with configurable positioning and font sizes
- **SearchView**: Custom search interface with fuzzy search capabilities
- **WallpaperManager**: Handles background transparency and wallpaper integration
- **FavoritesManager**: Manages favorite app storage, retrieval, and persistence
- **AppRepository**: Handles installed app discovery and metadata retrieval
- **SearchManager**: Handles fuzzy search logic and app filtering
- **GestureManager**: Handles left/right swipe gestures and app launching; swipe down expands notification shade
- **LayoutManager**: Manages favorites positioning and font size configurations
- **SettingsManager**: Centralized settings storage and retrieval system

### Android Launcher Integration
The app will register as a launcher through AndroidManifest.xml configuration with:
- `android.intent.category.HOME` intent filter
- `android.intent.category.DEFAULT` category
- Appropriate launcher permissions and metadata

## Components and Interfaces

### MainActivity
```kotlin
class MainActivity : AppCompatActivity() {
    // Primary launcher activity with advanced gesture support
    // Handles system home button presses
    // Manages configurable app list display with custom positioning
    // Implements wallpaper transparency
    // Handles multiple gesture types (long press, swipe up, left/right swipes, swipe down to notifications)
    // Applies dynamic font sizing and layout configurations
}
```

**Responsibilities:**
- Display the home screen UI with configurable favorites in custom positions
- Handle launcher lifecycle events and configuration changes
- Manage wallpaper background integration with dynamic text contrast
- Coordinate with AppListView for dynamic app display with custom font sizes
- Detect long press gestures and navigate to enhanced settings
- Detect swipe up gestures and launch search interface
- Handle left/right swipe gestures to launch configured apps
- Handle swipe down to expand notification shade
- Load and apply all user configurations (favorites, layout, gestures, typography)
- Provide real-time configuration updates without app restart

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
- List all apps by default when query is empty
- On submit/enter: launch the top app result; if none, open a web search in default browser
- Manage swipe down/back gestures to close search

### GestureManager
```kotlin
class GestureManager {
    // Handles advanced gesture detection and app launching
    // Manages left/right swipe gesture configurations
    // Provides gesture conflict resolution
}
```

**Responsibilities:**
- Detect left and right swipe gestures on home screen
- Launch configured apps for each swipe direction
- Handle gesture conflicts between different swipe types
- Provide haptic feedback for gesture recognition
- Store and retrieve swipe app configurations
- Validate configured apps are still installed

### LayoutManager
```kotlin
class LayoutManager {
    // Manages favorites positioning and typography configurations
    // Handles dynamic layout updates and font size changes
    // Ensures accessibility and readability across all configurations
}
```

**Responsibilities:**
- Apply horizontal positioning (left, center, right) for favorites list
- Apply vertical positioning (top, center, bottom) for favorites list
- Manage font size configurations for all text elements
- Ensure proper spacing and proportions with different font sizes
- Maintain text readability against various wallpaper backgrounds
- Validate layout configurations for accessibility compliance

### SettingsManager
```kotlin
class SettingsManager {
    // Centralized settings storage and retrieval system
    // Manages all launcher configurations with atomic updates
    // Provides settings change notifications and validation
}
```

**Responsibilities:**
- Store and retrieve all launcher settings (favorites, gestures, layout, typography)
- Provide atomic updates for multiple setting changes
- Validate setting combinations for compatibility and usability
- Notify components of setting changes for real-time updates
- Handle settings migration and default value management
- Ensure data integrity and backup/restore capabilities

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
- Provide all-apps result when query is empty

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

#### Enhanced Settings Page (SettingsActivity)
```
SettingsActivity (Full Screen)
├── StatusBar (Transparent)
├── ToolBar (Minimal)
│   └── "Launcher Settings" (Title)
├── TabLayout (Material Expressive)
│   ├── "Favorites" (Tab)
│   ├── "Gestures" (Tab)
│   └── "Layout" (Tab)
├── ViewPager2 (Tab Content)
│   ├── FavoritesFragment (Selected apps + Edit button popup for up to 7)
│   ├── GesturesFragment (Left/Right app selectors in dark dialogs)
│   └── LayoutFragment (Horizontal/Vertical position + Font size, dark dialogs)
├── Save Button (Fixed Bottom)
└── NavigationBar (Transparent)
```

#### Search Interface (SearchActivity)
```
SearchActivity (Full Screen Black)
├── StatusBar (Black)
├── SearchInputField (Top, Focused)
│   └── Keyboard (Auto-displayed)
├── SearchResults (RecyclerView)
│   ├── All apps by default (sorted)
│   ├── MatchingApp1 (TextView)
│   ├── MatchingApp2 (TextView)
│   └── ...
└── NavigationBar (Black)
```

### Visual Design
- Home Screen: transparent background; text color dynamic contrast
- Settings: dark with white text, minimalist
- Search: black background with white text; shows all apps by default

## User Interaction Design (additions)
- Swipe down on Home: expand notifications panel
- Search submit: launch top result; if none, open default browser with Google search query

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
    val spacing: Int,
    val horizontalPosition: HorizontalPosition,
    val verticalPosition: VerticalPosition
)
```

**Purpose:** Configuration object for UI styling and layout parameters

### GestureConfig
```kotlin
data class GestureConfig(
    val leftSwipeApp: String? = null,
    val rightSwipeApp: String? = null,
    val leftSwipeAppName: String? = null,
    val rightSwipeAppName: String? = null
)
```

**Purpose:** Configuration for left and right swipe gesture app assignments

### LayoutConfig
```kotlin
data class LayoutConfig(
    val horizontalPosition: HorizontalPosition = HorizontalPosition.CENTER,
    val verticalPosition: VerticalPosition = VerticalPosition.CENTER,
    val fontSize: FontSize = FontSize.MEDIUM
)
```

**Purpose:** Configuration for favorites positioning and typography settings

### FontSize
```kotlin
enum class FontSize(val spValue: Float, val displayName: String) {
    SMALL(14f, "Small"),
    MEDIUM(18f, "Medium"),
    LARGE(22f, "Large"),
    EXTRA_LARGE(26f, "Extra Large")
}
```

**Purpose:** Enumeration of available font size options

### HorizontalPosition
```kotlin
enum class HorizontalPosition {
    LEFT, CENTER, RIGHT
}
```

**Purpose:** Enumeration of horizontal positioning options for favorites list

### VerticalPosition
```kotlin
enum class VerticalPosition {
    TOP, CENTER, BOTTOM
}
```

**Purpose:** Enumeration of vertical positioning options for favorites list

### AdvancedSettings
```kotlin
data class AdvancedSettings(
    val favorites: List<FavoriteApp> = emptyList(),
    val gestureConfig: GestureConfig = GestureConfig(),
    val layoutConfig: LayoutConfig = LayoutConfig()
)
```

**Purpose:** Comprehensive settings container for all launcher configurations

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