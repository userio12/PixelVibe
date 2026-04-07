# PixelVibe — Project Architecture

## System Overview

PixelVibe is a **Jetpack Compose-first** Android video player application, architected as an exact clone of [mpvExtended](https://github.com/marlboro-advance/mpvEx). It follows **MVVM + Repository** pattern with **Koin** for dependency injection, **Room** for persistence, and **Flow-based** state management.

---

## 1. Architectural Pattern

```
┌──────────────────────────────────────────────────────────────────┐
│                        UI Layer (Compose)                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐   │
│  │  MainActivity │  │PlayerActivity│  │ Preference Screens   │   │
│  │  (NavHost)    │  │ (Video View) │  │ (Settings Hub)       │   │
│  └──────┬───────┘  └──────┬───────┘  └──────────┬───────────┘   │
│         │                 │                      │               │
│  ┌──────┴─────────────────┴──────────────────────┴───────────┐   │
│  │                    ViewModels                              │   │
│  │  MainViewModel, PlayerViewModel, SettingsViewModel, etc.   │   │
│  └──────────────────────────┬────────────────────────────────┘   │
├─────────────────────────────┼────────────────────────────────────┤
│                  Presentation Layer (Shared)                     │
│  Reusable Compose components, Screen interfaces, crash handler   │
├─────────────────────────────┼────────────────────────────────────┤
│                     Domain Layer (Use Cases)                     │
│  Anime4KManager, RecentlyPlayed, PlaybackState, ThumbnailManager │
│  NetworkConnectionManager, MediaScanManager, PlaylistManager     │
├─────────────────────────────┼────────────────────────────────────┤
│                   Data / Repository Layer                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐   │
│  │ Room Database│  │ Preferences  │  │ Network Repository   │   │
│  │ DAOs+Entities│  │ SharedPreferences│  │ SMB/FTP/WebDAV      │   │
│  └──────────────┘  └──────────────┘  └──────────────────────┘   │
├──────────────────────────────────────────────────────────────────┤
│                  DI Layer (Koin Modules)                         │
│  PreferencesModule, DatabaseModule, FileManagerModule,           │
│  DomainModule, NetworkModule                                     │
├──────────────────────────────────────────────────────────────────┤
│                  Native / Engine Layer                           │
│  MPVLib (AAR) — JNI bridge to libmpv (C library)                │
│  BaseMPVView, MPVView, Utils, KeyMapping                         │
└──────────────────────────────────────────────────────────────────┘
```

---

## 2. Module Structure

```
PixelVibe/
├── app/
│   ├── src/main/
│   │   ├── kotlin/com/pixelvibe/vedioplayer/
│   │   │   ├── PixelVibeApp.kt                    # Application class (Koin init)
│   │   │   ├── MainActivity.kt                    # Main entry (NavHost, 4 tabs)
│   │   │   ├── PlayerActivity.kt                  # Player (MPVView + Compose overlay)
│   │   │   ├── MediaInfoActivity.kt               # Media metadata viewer
│   │   │   ├── MediaPlaybackService.kt            # Background playback service
│   │   │   │
│   │   │   ├── di/                                # Koin dependency injection
│   │   │   │   ├── AppModule.kt                   # Core singletons
│   │   │   │   ├── DatabaseModule.kt              # Room database
│   │   │   │   ├── FileManagerModule.kt           # File system helpers
│   │   │   │   ├── DomainModule.kt                # Use cases / managers
│   │   │   │   └── NetworkModule.kt               # Network clients
│   │   │   │
│   │   │   ├── ui/
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Color.kt                   # Theme color definitions (32 themes)
│   │   │   │   │   ├── Theme.kt                   # Material3 theme composition
│   │   │   │   │   ├── Typography.kt              # Font families & text styles
│   │   │   │   │   ├── AppTheme.kt                # Theme state & management
│   │   │   │   │   └── Spacing.kt                 # Spacing/sizing constants
│   │   │   │   │
│   │   │   │   ├── player/
│   │   │   │   │   ├── PlayerViewModel.kt         # Player state management
│   │   │   │   │   ├── MPVView.kt                 # MPV View wrapper
│   │   │   │   │   ├── PlayerObserver.kt          # MPV event bridge
│   │   │   │   │   ├── PlayerHost.kt              # Android primitive abstraction
│   │   │   │   │   │
│   │   │   │   │   ├── controls/
│   │   │   │   │   │   ├── PlayerControls.kt      # Main overlay layout
│   │   │   │   │   │   ├── GestureHandler.kt      # Multi-touch gesture system
│   │   │   │   │   │   ├── PlayerUpdates.kt       # Transient text overlays
│   │   │   │   │   │   ├── SlideToUnlock.kt       # Lock slider component
│   │   │   │   │   │   └── components/
│   │   │   │   │   │       ├── Seekbar.kt         # Custom seekbar (3 styles)
│   │   │   │   │   │       ├── TopBar.kt          # Top gradient bar
│   │   │   │   │   │       ├── BottomBar.kt       # Bottom gradient bar
│   │   │   │   │   │       ├── PlayerButtons.kt   # Configurable icon buttons
│   │   │   │   │   │       └── DoubleTapOvals.kt  # Seek direction indicators
│   │   │   │   │   │
│   │   │   │   │   ├── sheets/
│   │   │   │   │   │   ├── PlaybackSpeedSheet.kt
│   │   │   │   │   │   ├── SubtitleTracksSheet.kt
│   │   │   │   │   │   ├── AudioTracksSheet.kt
│   │   │   │   │   │   ├── ChaptersSheet.kt
│   │   │   │   │   │   ├── VideoZoomSheet.kt
│   │   │   │   │   │   ├── AspectRatioSheet.kt
│   │   │   │   │   │   ├── PlaylistSheet.kt
│   │   │   │   │   │   ├── FrameNavigationSheet.kt
│   │   │   │   │   │   ├── MoreSheet.kt
│   │   │   │   │   │   ├── DecodersSheet.kt
│   │   │   │   │   │   └── OnlineSubtitleSearchSheet.kt
│   │   │   │   │   │
│   │   │   │   │   └── panels/
│   │   │   │   │       ├── SubtitleSettingsPanel.kt
│   │   │   │   │       ├── SubtitleDelayPanel.kt
│   │   │   │   │       ├── AudioDelayPanel.kt
│   │   │   │   │       ├── VideoSettingsPanel.kt
│   │   │   │   │       └── MultiCardPanel.kt
│   │   │   │   │
│   │   │   │   ├── browser/
│   │   │   │   │   ├── MainScreen.kt              # 4-tab bottom navigation
│   │   │   │   │   ├── FolderListScreen.kt        # Local file browser
│   │   │   │   │   ├── RecentlyPlayedScreen.kt    # Watch history
│   │   │   │   │   ├── PlaylistScreen.kt          # Custom playlists
│   │   │   │   │   ├── NetworkStreamingScreen.kt  # SMB/FTP/WebDAV
│   │   │   │   │   ├── FileSystemBrowserScreen.kt # Direct FS browsing
│   │   │   │   │   └── components/
│   │   │   │   │       ├── VideoCard.kt
│   │   │   │   │       ├── NetworkVideoCard.kt
│   │   │   │   │       ├── FolderCard.kt
│   │   │   │   │       ├── PlaylistCard.kt
│   │   │   │   │       └── SelectionBar.kt
│   │   │   │   │
│   │   │   │   ├── preferences/
│   │   │   │   │   ├── PreferencesScreen.kt
│   │   │   │   │   ├── AppearancePreferencesScreen.kt
│   │   │   │   │   ├── PlayerPreferencesScreen.kt
│   │   │   │   │   ├── GesturePreferencesScreen.kt
│   │   │   │   │   ├── PlayerControlsPreferencesScreen.kt
│   │   │   │   │   ├── DecoderPreferencesScreen.kt
│   │   │   │   │   ├── SubtitlesPreferencesScreen.kt
│   │   │   │   │   ├── AudioPreferencesScreen.kt
│   │   │   │   │   ├── AdvancedPreferencesScreen.kt
│   │   │   │   │   ├── FoldersPreferencesScreen.kt
│   │   │   │   │   ├── ControlLayoutEditorScreen.kt
│   │   │   │   │   ├── ConfigEditorScreen.kt
│   │   │   │   │   ├── SettingsSearchScreen.kt
│   │   │   │   │   └── AboutScreen.kt
│   │   │   │   │
│   │   │   │   └── mediainfo/
│   │   │   │       └── MediaInfoScreen.kt
│   │   │   │
│   │   │   ├── presentation/                      # Shared components
│   │   │   │   ├── Screen.kt                      # Navigation screen interface
│   │   │   │   ├── GlobalExceptionHandler.kt      # Crash handler
│   │   │   │   ├── CrashActivity.kt               # Crash display screen
│   │   │   │   └── components/                    # Reusable Compose components
│   │   │   │       ├── PullRefreshBox.kt
│   │   │   │       ├── LoadingDialog.kt
│   │   │   │       ├── ConfirmDialog.kt
│   │   │   │       ├── ThemePicker.kt
│   │   │   │       └── ColorPicker.kt
│   │   │   │
│   │   │   ├── preferences/                       # Preference system
│   │   │   │   ├── PlayerPreferences.kt
│   │   │   │   ├── AudioPreferences.kt
│   │   │   │   ├── SubtitlePreferences.kt
│   │   │   │   ├── AppearancePreferences.kt
│   │   │   │   ├── DecoderPreferences.kt
│   │   │   │   ├── GesturePreferences.kt
│   │   │   │   ├── NetworkPreferences.kt
│   │   │   │   └── PreferenceStore.kt             # SharedPreferences wrapper
│   │   │   │
│   │   │   ├── domain/                            # Business logic
│   │   │   │   ├── Anime4KManager.kt
│   │   │   │   ├── RecentlyPlayed.kt
│   │   │   │   ├── PlaybackState.kt
│   │   │   │   ├── ThumbnailManager.kt
│   │   │   │   ├── NetworkConnectionManager.kt
│   │   │   │   ├── MediaScanManager.kt
│   │   │   │   ├── PlaylistManager.kt
│   │   │   │   ├── FilePickerHelper.kt
│   │   │   │   └── VideoFilterPresets.kt
│   │   │   │
│   │   │   ├── database/                          # Room database
│   │   │   │   ├── PixelVibeDatabase.kt
│   │   │   │   ├── dao/
│   │   │   │   │   ├── PlaybackStateDao.kt
│   │   │   │   │   ├── RecentlyPlayedDao.kt
│   │   │   │   │   ├── VideoMetadataDao.kt
│   │   │   │   │   ├── NetworkConnectionDao.kt
│   │   │   │   │   ├── PlaylistDao.kt
│   │   │   │   │   └── PlaylistItemDao.kt
│   │   │   │   ├── entities/
│   │   │   │   │   ├── PlaybackStateEntity.kt
│   │   │   │   │   ├── RecentlyPlayedEntity.kt
│   │   │   │   │   ├── VideoMetadataEntity.kt
│   │   │   │   │   ├── NetworkConnectionEntity.kt
│   │   │   │   │   ├── PlaylistEntity.kt
│   │   │   │   │   └── PlaylistItemEntity.kt
│   │   │   │   ├── repositories/
│   │   │   │   │   ├── PlaybackHistoryRepository.kt
│   │   │   │   │   ├── RecentlyPlayedRepository.kt
│   │   │   │   │   └── PlaylistRepository.kt
│   │   │   │   └── migrations/
│   │   │   │       └── SquashedMigration.kt       # v1 → v9 squashed
│   │   │   │
│   │   │   ├── repository/                        # Data repositories
│   │   │   │   ├── NetworkRepository.kt
│   │   │   │   ├── MediaFileRepository.kt
│   │   │   │   └── SubtitleRepository.kt
│   │   │   │
│   │   │   ├── utils/                             # Utilities
│   │   │   │   ├── MediaUtils.kt
│   │   │   │   ├── FileUtils.kt
│   │   │   │   ├── SortUtils.kt
│   │   │   │   ├── StorageUtils.kt
│   │   │   │   ├── PermissionUtils.kt
│   │   │   │   ├── MPVExtensions.kt               # Flow property observers
│   │   │   │   └── NetworkUtils.kt
│   │   │   │
│   │   │   └── network/                           # Network clients
│   │   │       ├── SMBClient.kt
│   │   │       ├── FTPClient.kt
│   │   │       ├── WebDAVClient.kt
│   │   │       ├── LocalProxyServer.kt            # NanoHTTPD proxy
│   │   │       └── NetworkStreamingProvider.kt    # ContentProvider
│   │   │
│   │   └── AndroidManifest.xml
│   │   └── res/
│   │       ├── values/strings.xml
│   │       ├── values/themes.xml
│   │       ├── values/colors.xml
│   │       ├── mipmap-*/                          # App icons
│   │       └── drawable/
│   │           └── anim_play_to_pause.xml         # Animated vector drawable
│   │
│   ├── src/main/assets/
│   │   └── cacert.pem                             # TLS certificate bundle
│   │
│   └── build.gradle.kts
│
├── gradle/libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
└── docs/
    ├── research.md
    ├── app-feature.md
    ├── project-architecture.md
    ├── plan.md
    └── todo.md
```

---

## 3. Data Flow

### Player State Flow

```
┌──────────┐    Events     ┌──────────┐    Flow      ┌──────────────┐
│  MPVLib  │ ───────────► │MPVObserver│ ──────────► │PlayerViewModel│
│  (JNI)   │  (onEvent)    │(UI thread)│  (StateFlow)│              │
└──────────┘               └──────────┘              └───────┬──────┘
                                                             │
                                                     collectAsState()
                                                             │
                                                             ▼
                                                      ┌──────────────┐
                                                      │ PlayerControls│
                                                      │  (Compose UI) │
                                                      └──────┬───────┘
                                                             │
                                                     User gestures
                                                             │
                                                             ▼
                                                      ┌──────────────┐
                                                      │ GestureHandler│
                                                      │              │
                                                      └──────┬───────┘
                                                             │
                                                     viewModel.play()
                                                     viewModel.seek()
                                                     viewModel.setSpeed()
                                                             │
                                                             ▼
                                                      ┌──────────────┐
                                                      │  MPVLib      │
                                                      │  .command()  │
                                                      │  .setProperty│
                                                      └──────────────┘
```

### Preference Flow

```
┌─────────────────┐     get()      ┌──────────────────┐
│ SharedPreferences│ ◄──────────── │ Preference<T>    │
│                 │                │ (typed wrapper)  │
└────────┬────────┘                └────────┬─────────┘
         │                                  │
         │ registerOnSharedPreferenceChanged │
         │                                  │
         ▼                                  ▼
┌─────────────────┐    Flow<T>     ┌──────────────────┐
│  Callback       │ ────────────► │ collectAsState() │
│  (emit change)  │                │ (Compose)        │
└─────────────────┘                └──────────────────┘
```

### Database Flow

```
┌──────────┐     @Query     ┌──────┐    Flow<List>  ┌────────────┐
│  Room DB │ ────────────► │ DAO  │ ─────────────► │ Repository │
│  (SQLite)│                │      │                 │            │
└──────────┘                └──────┘                 └─────┬──────┘
                                                           │
                                                    collectAsState()
                                                           │
                                                           ▼
                                                    ┌────────────┐
                                                    │   UI        │
                                                    │ (Compose)   │
                                                    └────────────┘
```

---

## 4. Key Design Patterns

### MVVM (Model-View-ViewModel)

- **Model:** Room entities, DAOs, repositories, MPV engine
- **ViewModel:** PlayerViewModel, MainViewModel, SettingsViewModels — hold UI state as StateFlow
- **View:** Compose screens — observe StateFlow, emit user actions to ViewModel

### Repository Pattern

Repositories abstract data sources:
- `PlaybackHistoryRepository` → Room DAO
- `NetworkRepository` → SMB/FTP/WebDAV clients
- `MediaFileRepository` → File system
- `SubtitleRepository` → Local files + online search

### Observer Pattern

MPV properties observed via Flow:
```kotlin
val isPaused = MPVLib.propBoolean("pause", false).stateIn(viewModelScope)
val currentPosition = MPVLib.propDouble("time-pos", 0.0).stateIn(viewModelScope)
val duration = MPVLib.propDouble("duration", 0.0).stateIn(viewModelScope)
```

### Factory Pattern

- `PlayerViewModelProviderFactory` — custom Koin-based factory for PlayerActivity dependencies
- `NetworkConnectionFactory` — creates SMB/FTP/WebDAV connections based on protocol

### Strategy Pattern

- **Decoder strategy:** HW (mediacodec), HW-Copy (mediacodec-copy), SW
- **Seekbar strategy:** Standard, Wavy, Thick — interchangeable rendering
- **Control layout strategy:** 8 preset layouts, custom user-defined

---

## 5. Navigation Architecture

### Screen Interface

```kotlin
interface Screen : NavKey {
    val title: String
    @Composable fun Content()
}
```

### Navigation Graph

```
MainActivity (NavHost)
├── MainScreen (4 tabs via TabRow)
│   ├── Home → FolderListScreen
│   ├── Recents → RecentlyPlayedScreen
│   ├── Playlists → PlaylistScreen
│   └── Network → NetworkStreamingScreen
│
PlayerActivity (launched via intent)
├── PlayerControls (Compose overlay)
│   ├── PlayerSheets (bottom modals)
│   ├── PlayerPanels (side panels)
│   └── PlayerUpdates (transient overlays)
│
Settings Activity (or nav destination)
├── PreferencesScreen
│   ├── AppearancePreferences
│   ├── PlayerPreferences
│   ├── GesturePreferences
│   ├── PlayerControlsPreferences
│   ├── DecoderPreferences
│   ├── SubtitlesPreferences
│   ├── AudioPreferences
│   ├── AdvancedPreferences
│   ├── FoldersPreferences
│   ├── ControlLayoutEditor
│   ├── ConfigEditor
│   ├── SettingsSearch
│   └── About
│
MediaInfoActivity
└── MediaInfoScreen
```

---

## 6. Dependency Graph

```
PixelVibeApp (Application)
│
├── Koin init
│   ├── AppModule
│   │   ├── PreferenceStore → SharedPreferences
│   │   ├── PlayerPreferences
│   │   ├── AudioPreferences
│   │   ├── SubtitlePreferences
│   │   ├── AppearancePreferences
│   │   ├── DecoderPreferences
│   │   ├── GesturePreferences
│   │   └── NetworkPreferences
│   │
│   ├── DatabaseModule
│   │   ├── PixelVibeDatabase (Room)
│   │   ├── PlaybackStateDao
│   │   ├── RecentlyPlayedDao
│   │   ├── VideoMetadataDao
│   │   ├── NetworkConnectionDao
│   │   ├── PlaylistDao
│   │   └── PlaylistItemDao
│   │
│   ├── FileManagerModule
│   │   ├── FilePickerHelper
│   │   ├── MediaScanManager
│   │   └── ThumbnailManager
│   │
│   ├── DomainModule
│   │   ├── RecentlyPlayed
│   │   ├── PlaybackState
│   │   ├── PlaylistManager
│   │   ├── NetworkConnectionManager
│   │   ├── Anime4KManager
│   │   └── VideoFilterPresets
│   │
│   └── NetworkModule
│       ├── SMBClient
│       ├── FTPClient
│       ├── WebDAVClient
│       ├── LocalProxyServer (NanoHTTPD)
│       └── OkHttpClient
│
├── MediaSessionManager
├── MediaPlaybackService
└── GlobalExceptionHandler
```

---

## 7. Threading Model

| Component | Thread | Notes |
|-----------|--------|-------|
| MPVLib events | Background thread (native) | Bridged to Main via Handler/CoroutineScope |
| PlayerViewModel | Main (viewModelScope) | Collects MPV flows, emits UI state |
| Compose UI | Main (Composition) | Observes StateFlow, renders state |
| GestureHandler | Main (pointer input) | Dispatches commands to MPVLib |
| Room queries | IO dispatcher | DAOs use `suspend` + Flow |
| Network requests | IO dispatcher | OkHttp, SMBJ, etc. |
| Thumbnail generation | IO dispatcher (background) | ThumbnailManager |
| Media scan | IO dispatcher | MediaScanner |
| NanoHTTPD proxy | Background thread | Network streaming |

---

## 8. Permissions & Security

### Runtime Permissions

| Permission | API Level | Purpose |
|-----------|-----------|---------|
| `READ_MEDIA_VIDEO` | 33+ | Access video files |
| `READ_EXTERNAL_STORAGE` | 24-32 | Access files (legacy) |
| `POST_NOTIFICATIONS` | 33+ | Media playback notification |

### Manifest Permissions

| Permission | Purpose |
|-----------|---------|
| `INTERNET` | Network streaming |
| `FOREGROUND_SERVICE` | Background playback |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Media playback service type |

### Security

- TLS verification with bundled `cacert.pem`
- No sensitive data logging in release builds
- Network credentials stored in SharedPreferences (plaintext — could be improved with EncryptedSharedPreferences)

---

## 9. Build Configuration

### Gradle Properties
```
android.nonTransitiveRClass=true
android.useAndroidX=true
kotlin.code.style=official
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.daemon=true
org.gradle.configureondemand=false
```

### SDK Targets
```
minSdk = 24       // Android 7.0
targetSdk = 34    // Android 14
compileSdk = 36   // Android 15 (API 36)
Java 17           // Source & target compatibility
```

### Product Flavors (Optional)
```
standard  → Full features, update checker
playstore → Scoped storage only, no update checker
fdroid    → arm64-v8a only, no update checker
```

### ABI Splits
```
armeabi-v7a, arm64-v8a, x86, x86_64, universal
```

---

## 10. Testing Strategy

| Layer | Test Type | Tools |
|-------|-----------|-------|
| UI | Compose UI tests | `compose-ui-test`, Espresso |
| ViewModel | Unit tests | JUnit, MockK, Turbine (Flow) |
| Domain | Unit tests | JUnit, MockK |
| Repository | Unit tests | JUnit, MockK, Room in-memory |
| Database | Integration tests | Room, in-memory SQLite |
| Preferences | Unit tests | JUnit, SharedPreferences test impl |
| Network | Unit tests | JUnit, MockWebServer |
| MPV Integration | Manual testing | Real device/emulator required |

---

## 11. Performance Considerations

| Area | Optimization |
|------|-------------|
| Thumbnails | Fallback to fast thumbnails, caching with LRU |
| Database | WAL journal mode, proper indexes, Flow-based reactive queries |
| Compose | `remember` for expensive computations, `derivedStateOf` for computed state |
| Network | Connection pooling (OkHttp), lazy loading |
| Gestures | Debounced input, isolated gesture regions |
| Seekbar | `animate*AsState` with tween, not continuous computation |
| Media scan | Background coroutine, debounce on file changes |

---

## 12. Key Compose Patterns Used

| Pattern | Usage |
|---------|-------|
| `remember { mutableStateOf() }` | Local UI state |
| `collectAsState()` | Flow → Compose state |
| `derivedStateOf` | Computed state (seekbar position, formatted time) |
| `LaunchedEffect` | Side effects (load data on key change) |
| `DisposableEffect` | Cleanup (remove MPV observers) |
| `CompositionLocalProvider` | Theme, preference injection |
| `AndroidView` | Embedding MPVView in Compose |
| `ModalBottomSheet` | Player sheets |
| `ConstraintLayout` (Compose) | Player overlay positioning |
| `AnimatedContent` | Screen transitions |
| `AnimatedVisibility` | Control show/hide |
| `animate*AsState` | Seekbar wave, button animations |
| `basicMarquee` | Long title scrolling |
