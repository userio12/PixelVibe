# PixelVibe — Implementation Plan

## Goal

Create an **exact Jetpack Compose clone** of [mpvExtended](https://github.com/marlboro-advance/mpvEx) — a feature-rich Android video player with **Material You dynamic color**, **Material Expressive animations**, comprehensive **gesture controls**, **extensive subtitle support**, **network streaming**, and **background playback**.

---

## Implementation Strategy

The project is implemented in **6 phases**, from foundational infrastructure to premium features. Each phase produces a buildable, testable artifact.

---

## Phase 1: Foundation & Core Player (Weeks 1-3)

### Objective
Establish the project skeleton, dependency injection, theme system, and a working video player with basic controls.

### Deliverables
- ✅ Project structure matching architecture document
- ✅ Koin DI setup with all modules
- ✅ Room database with all entities, DAOs, and migrations
- ✅ Preference system (SharedPreferences wrapper with Flow)
- ✅ Material You theme with 5 built-in themes + AMOLED
- ✅ MainActivity with 4-tab bottom navigation
- ✅ PlayerActivity with MPVView + Compose overlay
- ✅ Basic playback (load file, play/pause, seek)
- ✅ Standard seekbar with time labels
- ✅ Basic gesture handling (tap to play/pause, swipe to seek)

### Key Files Created
```
app/src/main/kotlin/com/pixelvibe/vedioplayer/
├── PixelVibeApp.kt
├── MainActivity.kt
├── PlayerActivity.kt (basic)
├── di/
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   ├── FileManagerModule.kt
│   ├── DomainModule.kt
│   └── NetworkModule.kt
├── database/
│   ├── PixelVibeDatabase.kt
│   ├── dao/ (6 DAOs)
│   └── entities/ (6 entities)
├── ui/theme/
│   ├── Color.kt
│   ├── Theme.kt
│   ├── Typography.kt
│   └── AppTheme.kt
├── preferences/
│   ├── PreferenceStore.kt
│   └── PlayerPreferences.kt
└── ui/player/
    ├── PlayerViewModel.kt (basic)
    ├── MPVView.kt
    ├── PlayerObserver.kt
    └── controls/
        ├── PlayerControls.kt (basic)
        └── components/
            ├── Seekbar.kt (Standard)
            └── TopBar.kt
```

### Dependencies to Add
- Koin (BOM 4.2.0)
- Room (2.8.4) + KSP
- kotlinx-serialization-json (1.10.0)
- kotlinx-collections-immutable (0.4.0)
- Compose ConstraintLayout (1.1.1)
- Accompanist Permissions (0.37.3)
- compose.prefs (2.2.0)
- Seeker (2.0.1)
- LazyColumnScrollbar (2.2.0)
- Reorderable (3.0.0)

### MPV AAR
- Extract and integrate `mpv-android-lib` AAR from reference project
- Set up JNI native library loading

---

## Phase 2: Complete Player UI & Gestures (Weeks 4-6)

### Objective
Build the full player control overlay with gesture system, sheets, and panels.

### Deliverables
- ✅ Complete multi-touch gesture handler (all 10 gestures)
- ✅ Player controls with all button slots and 22 button types
- ✅ Wavy + Thick seekbar styles with animations
- ✅ All 12 player sheets (bottom modals)
- ✅ All 5 side panels (draggable settings panels)
- ✅ Player updates overlay (transient text feedback)
- ✅ Slide to unlock component
- ✅ Double-tap seek indicators (oval overlays)
- ✅ Playback state persistence (position, speed, tracks, delays)
- ✅ Playlist support within player (next/prev, repeat, shuffle)
- ✅ A-B loop functionality
- ✅ Video zoom, aspect ratio, mirroring, flip
- ✅ Snapshot (screenshot) functionality
- ✅ Sleep timer
- ✅ Playback speed (0.25x - 4.0x with presets)

### Key Files Created
```
ui/player/
├── PlayerViewModel.kt (complete - 1900+ lines)
├── PlayerHost.kt
├── controls/
│   ├── PlayerControls.kt (complete - 1200+ lines)
│   ├── GestureHandler.kt (complete - 1100+ lines)
│   ├── PlayerUpdates.kt
│   ├── SlideToUnlock.kt
│   └── components/
│       ├── Seekbar.kt (all 3 styles + wave animation)
│       ├── TopBar.kt
│       ├── BottomBar.kt
│       ├── PlayerButtons.kt
│       └── DoubleTapOvals.kt
├── sheets/ (12 sheet components)
└── panels/ (5 panel components)
```

### Key Technical Challenges
- **Multi-touch gesture isolation** — preventing gesture interference
- **Wavy seekbar animation** — continuous sine wave using Canvas draw
- **State synchronization** — MPV events → Flow → Compose state → UI updates
- **Sheet state management** — which sheet is open, scroll position preservation

---

## Phase 3: File Browser & Database Integration (Weeks 7-8)

### Objective
Build the file browsing experience with database-backed history, playlists, and metadata.

### Deliverables
- ✅ Folder browser (tree view and flat view)
- ✅ Video cards with thumbnails, duration, file size, "NEW" label
- ✅ Sort dialog (name, date, size, duration; ascending/descending)
- ✅ Multi-select mode with copy/paste/delete
- ✅ Pull-to-refresh
- ✅ Recently played screen with watch history
- ✅ Playlist screen with create/edit/delete + M3U import
- ✅ Auto-scroll to last played video
- ✅ Watched threshold marking
- ✅ Media scan on app launch
- ✅ Thumbnail caching with LRU
- ✅ Playback state auto-restore from database

### Key Files Created
```
ui/browser/
├── MainScreen.kt (4-tab nav with TabRow)
├── FolderListScreen.kt
├── RecentlyPlayedScreen.kt
├── PlaylistScreen.kt
├── NetworkStreamingScreen.kt
├── FileSystemBrowserScreen.kt
└── components/
    ├── VideoCard.kt
    ├── NetworkVideoCard.kt
    ├── FolderCard.kt
    ├── PlaylistCard.kt
    └── SelectionBar.kt

domain/
├── ThumbnailManager.kt
├── MediaScanManager.kt
├── RecentlyPlayed.kt
├── PlaybackState.kt
└── PlaylistManager.kt

repository/
├── MediaFileRepository.kt
└── SubtitleRepository.kt
```

### Database Migration
- Squash migrations v1 → v9 into single migration
- Populate with initial schema
- Test migration from empty to latest version

---

## Phase 4: Preferences & Settings (Weeks 9-10)

### Objective
Build the comprehensive settings system with all preference screens.

### Deliverables
- ✅ Settings hub screen with categorized cards
- ✅ All 14 preference screens
- ✅ Settings search functionality
- ✅ Config editor (mpv.conf, input.conf)
- ✅ Control layout editor (drag-and-drop)
- ✅ Export/import settings
- ✅ Cache clearing utilities
- ✅ About screen with OSS libraries

### Key Files Created
```
ui/preferences/
├── PreferencesScreen.kt
├── AppearancePreferencesScreen.kt
├── PlayerPreferencesScreen.kt
├── GesturePreferencesScreen.kt
├── PlayerControlsPreferencesScreen.kt
├── DecoderPreferencesScreen.kt
├── SubtitlesPreferencesScreen.kt
├── AudioPreferencesScreen.kt
├── AdvancedPreferencesScreen.kt
├── FoldersPreferencesScreen.kt
├── ControlLayoutEditorScreen.kt
├── ConfigEditorScreen.kt
├── SettingsSearchScreen.kt
└── AboutScreen.kt

preferences/
├── AudioPreferences.kt
├── SubtitlePreferences.kt
├── AppearancePreferences.kt
├── DecoderPreferences.kt
├── GesturePreferences.kt
└── NetworkPreferences.kt
```

---

## Phase 5: Subtitle System & Audio Features (Weeks 11-12)

### Objective
Implement the complete subtitle and audio feature set.

### Deliverables
- ✅ Dual subtitle support (primary + secondary)
- ✅ External subtitle loading
- ✅ Online subtitle search (TMDB API)
- ✅ Local subtitle scanning
- ✅ Full subtitle typography panel
- ✅ ASS/SSA support with override options
- ✅ Subtitle delay & speed (independent)
- ✅ Audio track selection
- ✅ External audio track loading
- ✅ Audio delay
- ✅ Volume boost with cap
- ✅ Audio pitch correction
- ✅ Volume normalization
- ✅ Audio channel selection
- ✅ Preferred audio language

### Key Files Created
```
domain/
└── SubtitleManager.kt (new)

utils/
└── SubtitleUtils.kt (new)

network/
└── TMDBClient.kt (new — for subtitle search)
```

---

## Phase 6: Network Streaming, Background Playback & Polish (Weeks 13-14)

### Objective
Add network streaming, background playback, PiP, animations, and final polish.

### Deliverables
- ✅ SMB client (SMBJ integration)
- ✅ FTP client (Commons Net integration)
- ✅ WebDAV client (Sardine Android integration)
- ✅ Local HTTP proxy (NanoHTTPD)
- ✅ Network connection management
- ✅ Auto-connect on launch
- ✅ Network streaming via proxy to MPV
- ✅ MediaPlaybackService (foreground service)
- ✅ MediaSessionCompat integration
- ✅ MediaStyle notification
- ✅ Picture-in-Picture mode
- ✅ Video filter presets (13 presets)
- ✅ Anime4K upscaling (experimental)
- ✅ MediaInfo viewer
- ✅ Crash handler & crash activity
- ✅ All animations & transitions
- ✅ 32 built-in themes
- ✅ AMOLED mode
- ✅ Theme circular reveal transition
- ✅ Edge-to-edge with proper cutout handling
- ✅ Permission handling
- ✅ Performance optimization
- ✅ Final testing & bug fixes

### Key Files Created
```
network/
├── SMBClient.kt
├── FTPClient.kt
├── WebDAVClient.kt
├── LocalProxyServer.kt
└── NetworkStreamingProvider.kt

MediaPlaybackService.kt
utils/PermissionUtils.kt
presentation/GlobalExceptionHandler.kt
presentation/CrashActivity.kt

ui/theme/
└── Color.kt (32 themes)

domain/
├── Anime4KManager.kt
└── VideoFilterPresets.kt (complete 13 presets)

ui/mediainfo/
└── MediaInfoScreen.kt
```

---

## Build Configuration Updates

### `gradle/libs.versions.toml` Additions
```toml
[versions]
koin = "4.2.0"
room = "2.8.4"
ksp = "2.3.6"
kotlinx-serialization = "1.10.0"
kotlinx-collections = "0.4.0"
constraintlayout-compose = "1.1.1"
accompanist-permissions = "0.37.3"
compose-prefs = "2.2.0"
seeker = "2.0.1"
lazy-column-scrollbar = "2.2.0"
reorderable = "3.0.0"
okhttp = "5.3.2"
smbj = "0.14.0"
commons-net = "3.13.0"
sardine-android = "0.8"
nanohttpd = "2.3.1"
media = "1.7.1"
truetype-parser = "2.1.4"
mediainfo = "1.0.0"
fsaf = "1.1.3"

[libraries]
# DI
koin-bom = { group = "io.insert-koin", name = "koin-bom", version.ref = "koin" }
koin-core = { group = "io.insert-koin", name = "koin-core" }
koin-android = { group = "io.insert-koin", name = "koin-android" }
koin-compose = { group = "io.insert-koin", name = "koin-compose" }
koin-compose-viewmodel = { group = "io.insert-koin", name = "koin-compose-viewmodel" }

# Database
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }

# Serialization
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
kotlinx-collections-immutable = { group = "org.jetbrains.kotlinx", name = "kotlinx-collections-immutable", version.ref = "kotlinx-collections" }

# UI
constraintlayout-compose = { group = "androidx.constraintlayout", name = "constraintlayout-compose", version.ref = "constraintlayout-compose" }
accompanist-permissions = { group = "com.google.accompanist", name = "accompanist-permissions", version.ref = "accompanist-permissions" }
compose-prefs = { group = "me.zhanghai.compose.preference", name = "library", version.ref = "compose-prefs" }
seeker = { group = "dev.vivvvek.seeker", name = "seeker", version.ref = "seeker" }
lazy-column-scrollbar = { group = "com.github.nanihadesuka", name = "LazyColumnScrollbar", version.ref = "lazy-column-scrollbar" }
reorderable = { group = "sh.calvin.reorderable", name = "reorderable", version.ref = "reorderable" }

# Networking
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
smbj = { group = "com.hierynomus", name = "smbj", version.ref = "smbj" }
commons-net = { group = "commons-net", name = "commons-net", version.ref = "commons-net" }
sardine-android = { group = "com.github.thegrizzlylabs", name = "sardine-android", version.ref = "sardine-android" }
nanohttpd = { group = "org.nanohttpd", name = "nanohttpd", version.ref = "nanohttpd" }

# Media
androidx-media = { group = "androidx.media", name = "media", version.ref = "media" }
truetype-parser = { group = "io.github.yubyf", name = "truetypeparser-light", version.ref = "truetype-parser" }
mediainfo = { group = "com.github.marlboro-advance", name = "mediainfoAndroid", version.ref = "mediainfo" }
fsaf = { group = "com.github.K1rakishou", name = "FSAF", version.ref = "fsaf" }

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

### `app/build.gradle.kts` Updates
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.pixelvibe.vedioplayer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pixelvibe.vedioplayer"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget("17"))
        }
    }

    // ABI splits (optional)
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }
}

dependencies {
    // Existing Compose deps
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // DI
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)

    // Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.collections.immutable)

    // UI
    implementation(libs.constraintlayout.compose)
    implementation(libs.accompanist.permissions)
    implementation(libs.compose.prefs)
    implementation(libs.seeker)
    implementation(libs.lazy.column.scrollbar)
    implementation(libs.reorderable)

    // Networking
    implementation(libs.okhttp)
    implementation(libs.smbj)
    implementation(libs.commons.net)
    implementation(libs.sardine.android)
    implementation(libs.nanohttpd)

    // Media
    implementation(libs.androidx.media)
    implementation(libs.truetype.parser)
    implementation(libs.mediainfo)
    implementation(libs.fsaf)

    // MPV AAR (local)
    implementation(files("libs/mpv-android-lib-v0.0.1.aar"))
}
```

---

## Testing Strategy

### Phase 1-2 Testing
- Unit tests for ViewModel state transitions
- Compose UI tests for basic controls rendering
- Manual testing: load local video, verify playback

### Phase 3-4 Testing
- Room database integration tests (in-memory)
- Preference flow tests
- File browser UI tests (mock file system)
- Manual testing: browse folders, create playlists

### Phase 5-6 Testing
- Subtitle parsing tests (unit)
- Network client tests (MockWebServer)
- MediaSession callback tests
- Manual testing: network streams, PiP, background playback

---

## Risk Mitigation

| Risk | Mitigation |
|------|-----------|
| MPV AAR compatibility | Extract from reference project, test on multiple devices |
| Performance on low-end devices | Thumbnail caching, lazy loading, debounce gestures |
| Compose + View hybrid complexity | Clean abstraction layer between MPVView and Compose overlay |
| Network protocol edge cases | Retry logic, connection pooling, error dialogs |
| Gesture conflicts | Isolated gesture regions, state machine for gesture detection |
| Theme consistency across Android versions | Fallback color schemes, test on API 24-36 |

---

## Success Criteria

1. ✅ Builds successfully with `./gradlew assembleDebug`
2. ✅ Installs and runs on Android 7.0+ (API 24)
3. ✅ Plays local video files with hardware decoding
4. ✅ All gestures respond correctly
5. ✅ Material You dynamic color works on Android 12+
6. ✅ Subtitles load and display correctly (dual)
7. ✅ Background playback with notification
8. ✅ PiP mode functional
9. ✅ Network streams play via local proxy
10. ✅ All settings persist across app restarts
11. ✅ No crashes on malformed input
12. ✅ APK size reasonable (<50MB for standard flavor)

---

## Future Enhancements (Post-v1)

- Cast to Chromecast / AirPlay
- Download manager for offline subtitle/audio tracks
- Cloud storage integration (Google Drive, Dropbox)
- Equalizer UI with visualizer
- Watch party / sync playback
- TRakt.tv scrobbling
- Kodi/Jellyfin/Plex integration
- Advanced Lua scripting support
- Custom gesture creator
- Widget for home screen
- Wear OS companion app
- Tablet-optimized layouts
- Foldable device support
