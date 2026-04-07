# PixelVibe — Implementation TODO

## Phase 1: Foundation & Core Player ✅ COMPLETE

### Project Setup
- [x] Extract `mpv-android-lib-v0.0.1.aar` from reference project → `app/libs/`
- [x] Copy `cacert.pem` to `app/src/main/assets/`
- [x] Copy animated vector drawables to `app/src/main/res/drawable/`
- [x] Copy app icons to `app/src/main/res/mipmap-*/`
- [x] Update `strings.xml` with all string resources
- [x] Update `themes.xml` and `colors.xml`

### Dependencies
- [x] Update `gradle/libs.versions.toml` with all new dependencies
- [x] Update `app/build.gradle.kts` with all dependency declarations
- [x] Add KSP plugin configuration
- [x] Add kotlinx-serialization plugin configuration
- [x] Configure ABI splits in build.gradle.kts

### Koin DI Setup
- [x] Create `PixelVibeApp.kt` (Application class, Koin init)
- [x] Create `di/AppModule.kt` (core singletons)
- [x] Create `di/DatabaseModule.kt` (Room database)
- [x] Create `di/FileManagerModule.kt` (file helpers)
- [x] Create `di/DomainModule.kt` (use cases)
- [x] Create `di/NetworkModule.kt` (network clients)
- [x] Register PixelVibeApp in AndroidManifest.xml

### Room Database
- [x] Create `database/entities/PlaybackStateEntity.kt`
- [x] Create `database/entities/RecentlyPlayedEntity.kt`
- [x] Create `database/entities/VideoMetadataEntity.kt`
- [x] Create `database/entities/NetworkConnectionEntity.kt`
- [x] Create `database/entities/PlaylistEntity.kt`
- [x] Create `database/entities/PlaylistItemEntity.kt`
- [x] Create `database/dao/PlaybackStateDao.kt`
- [x] Create `database/dao/RecentlyPlayedDao.kt`
- [x] Create `database/dao/VideoMetadataDao.kt`
- [x] Create `database/dao/NetworkConnectionDao.kt`
- [x] Create `database/dao/PlaylistDao.kt`
- [x] Create `database/dao/PlaylistItemDao.kt`
- [x] Create `database/PixelVibeDatabase.kt` (Room database class)
- [x] Create `database/migrations/SquashedMigration.kt` (v1→v9) — fallbackToDestructiveMigration
- [x] Create `database/repositories/PlaybackHistoryRepository.kt`
- [x] Create `database/repositories/RecentlyPlayedRepository.kt`
- [x] Create `database/repositories/PlaylistRepository.kt`

### Preference System
- [x] Create `preferences/PreferenceStore.kt` (SharedPreferences wrapper with Flow)
- [x] Create `preferences/PlayerPreferences.kt` (60+ typed preferences)
- [x] Create typed `Preference<T>` class with `get()`, `set()`, `collectAsState()`, `changes()`
- [x] Implement AndroidPreferenceStore backing

### Theme System
- [x] Create `ui/theme/Color.kt` — 7 themes (Default, Dynamic, Catppuccin, Gruvbox, Tokyo Night, Nord, Dracula) with light/dark/AMOLED
- [x] Create `ui/theme/Theme.kt` — Material3 composition with dynamic color
- [x] Create `ui/theme/Typography.kt` — Full Material3 typography scale
- [x] Create `ui/theme/AppTheme.kt` — Theme state management, color scheme resolution
- [x] Create `ui/theme/Spacing.kt` — Spacing constants for player/browser

### MainActivity
- [x] Create `MainActivity.kt` with 4-tab bottom navigation
- [x] Create `presentation/Screen.kt` (Screen interface + tab screen objects)
- [x] Implement edge-to-edge setup
- [x] Tab placeholders (Home, Recents, Playlists, Network)

### Player Activity (Basic)
- [x] Create `PlayerActivity.kt` with MPVView + Compose overlay
- [x] Create `ui/player/MPVView.kt` with init options and property observation
- [x] Create `ui/player/PlayerObserver.kt` (MPV event bridge to UI thread)
- [x] Create `ui/player/PlayerHost.kt` (Android primitive abstraction)
- [x] Create `ui/player/PlayerViewModel.kt` with state management
- [x] Implement basic MPV initialization
- [x] Implement basic playback (load file, play/pause)
- [x] Implement MPV property observation

### Basic Player Controls
- [x] Create `ui/player/controls/PlayerControls.kt` (overlay with gradients, auto-hide)
- [x] Create `ui/player/controls/components/Seekbar.kt` (Standard style)
- [x] Create `ui/player/controls/components/TopBar.kt`
- [x] Create `ui/player/controls/components/BottomBar.kt`
- [x] Implement basic tap to show/hide controls
- [x] Implement basic tap to play/pause
- [x] Implement basic horizontal swipe to seek
- [x] Implement time labels (current / duration)

---

## Phase 2: Complete Player UI & Gestures ✅ COMPLETE

### Gesture System
- [x] Create `ui/player/controls/GestureHandler.kt` (complete implementation)
- [x] Implement double-tap left/center/right (seek/play-pause/seek)
- [x] Implement multi-tap seeking (continuous same direction)
- [x] Implement long-press for speed up (hold to speed)
- [x] Implement vertical swipe left (brightness) / right (volume)
- [x] Implement horizontal swipe seek (configurable sensitivity)
- [x] Implement pinch-to-zoom gesture
- [x] Implement pan & zoom gesture
- [x] Implement gesture configuration options, isolation, haptic feedback
- [x] Implement dynamic speed overlay with swipe-to-adjust

### Player Controls (Complete)
- [x] Implement all 22 button types (`PlayerButtonType` enum)
- [x] Implement configurable button slots (top-right, bottom-left, bottom-right)
- [x] Implement landscape and portrait layouts
- [x] Implement gradient overlays (alpha 0.8)
- [x] Implement media title with marquee scrolling
- [x] Implement current chapter display
- [x] Implement slide-to-unlock component
- [x] Implement double-tap seek oval indicators
- [x] Implement player updates overlay (speed, zoom, repeat, shuffle, etc.)
- [x] Implement control enter/exit animations

### Seekbar (All Styles)
- [x] Implement Standard / Wavy / Thick seekbar styles
- [x] Implement wave animation (10px/s continuous), flatten/regrow
- [x] Implement A-B loop visual indicators (gold markers)
- [x] Implement chapter markers on seekbar
- [x] Implement expanded touch area, animated position, loading indicator

### Player Sheets (12)
- [x] All 12 sheets created: PlaybackSpeed, SubtitleTracks, OnlineSubtitleSearch, AudioTracks, Chapters, Decoders, More, VideoZoom, AspectRatio, Playlist, FrameNavigation, GenericTracks

### Player Panels (5)
- [x] All 5 panels created: SubtitleSettings, SubtitleDelay, AudioDelay, VideoSettings (13 presets), MultiCard

---

## Phase 3: File Browser & Database Integration ✅ COMPLETE

### Folder Browser
- [x] Create `ui/browser/FolderListScreen.kt` with tree/flat view, breadcrumb, sort dialog, selection bar
- [x] Create `ui/browser/components/VideoCard.kt` — thumbnail, duration badge, file size, NEW label
- [x] Create `ui/browser/components/FolderCard.kt` — folder icon, item count
- [x] Create `ui/browser/components/SelectionBar.kt` — multi-select action bar
- [x] Create `ui/browser/components/PlaylistCard.kt` — playlist display with play button

### Recents Screen
- [x] Create `ui/browser/RecentlyPlayedScreen.kt` — watch history, timestamp, clear history

### Playlists Screen
- [x] Create `ui/browser/PlaylistScreen.kt` — create dialog, playlist cards, empty state

### Network Streaming Screen
- [x] Create `ui/browser/NetworkStreamingScreen.kt` — connection list, add dialog with SMB/FTP/WebDAV

### Domain Layer
- [x] Create `domain/ThumbnailManager.kt`, `domain/MediaScanManager.kt`, `domain/RecentlyPlayed.kt`, `domain/PlaybackState.kt`, `domain/PlaylistManager.kt`, `domain/FilePickerHelper.kt`

### Repository Layer
- [x] Create `repository/MediaFileRepository.kt`, `repository/SubtitleRepository.kt`

### Utilities
- [x] Create `utils/MediaUtils.kt`, `utils/FileUtils.kt`, `utils/SortUtils.kt`, `utils/StorageUtils.kt`

---

## Phase 4: Preferences & Settings ✅ COMPLETE

### Settings Hub
- [x] Create `ui/preferences/PreferencesScreen.kt` — categorized cards layout with 10 sections

### Preference Screens
- [x] `AppearancePreferencesScreen` — theme picker with preview cards, AMOLED mode, Material You, file browser settings, watched threshold slider
- [x] `PlayerPreferencesScreen` — orientation picker, autoplay, PiP, gesture/control layout navigation links
- [x] `GesturePreferencesScreen` — double-tap L/C/R config, single-tap center toggle, side swap, seek area width slider
- [x] `PlayerControlsPreferencesScreen` — seekbar style picker (Standard/Wavy/Thick), wavy animation toggle, custom skip duration
- [x] `DecoderPreferencesScreen` — MPV profile, HW decoding, gpu-next, Vulkan, YUV420P, debanding (None/CPU/GPU)
- [x] `SubtitlesPreferencesScreen` — online search toggle, preferred languages, auto-load, save location, clear downloads
- [x] `AudioPreferencesScreen` — pitch correction, volume normalization, channels selection, language, volume boost cap
- [x] `AdvancedPreferencesScreen` — mpv.conf/input.conf editors, cache clearing, playback history clear, verbose logging, confirmation dialogs
- [x] `FoldersPreferencesScreen` — view mode (tree/flat), hidden files toggle, scan directory picker
- [x] `ConfigEditorScreen` — text editor for mpv.conf and input.conf with save action
- [x] `SettingsSearchScreen` — full settings search with 33 searchable preferences, no-results state
- [x] `AboutScreen` — version info, 11 OSS libraries list with license info

---

## Phase 5: Subtitle System & Audio Features ✅ COMPLETE

### Subtitle System
- [x] Create `domain/SubtitleManager.kt` — manages subtitle track selection, loading, delays, typography application
- [x] Create `utils/SubtitleUtils.kt` — format detection (18 formats), SRT/VTT/ASS parsing, timestamp formatting
- [x] Implement dual subtitle support (primary + secondary via MPV sid/secondary-sid)
- [x] Implement external subtitle loading via file picker and SAF URIs
- [x] Implement online subtitle search (TMDB API client with OpenSubtitles integration)
- [x] Create `network/TMDBClient.kt` — TMDB API client with movie/TV search, subtitle search
- [x] Implement local subtitle scanning (same directory, filename match, save location)
- [x] Implement full subtitle typography panel (font, size, colors, border, position, ASS override)
- [x] Implement ASS/SSA support with override toggle
- [x] Implement subtitle delay (primary/secondary independent, ms precision)
- [x] Implement subtitle speed (primary/secondary independent)
- [x] Implement auto-load by filename match with preferred language ordering
- [x] Implement subtitle save location preference

### Audio Features
- [x] Implement audio track selection (via MPV aid property with cycle support)
- [x] Implement external audio track loading via file picker
- [x] Implement audio delay (ms adjustment)
- [x] Implement volume boost with cap (via MPV volume-max)
- [x] Implement audio pitch correction (via MPV audio-pitch-correction preference)
- [x] Implement volume normalization (dynaudnorm filter via af property)
- [x] Implement audio channel selection (auto/mono/stereo/reversed via af pan filter)
- [x] Implement preferred audio language tracking

---

## Phase 6: Network Streaming, Background Playback & Polish ✅ COMPLETE

### Network Streaming
- [x] Create `network/SMBClient.kt` — SMBJ integration with connect/list/getStream/disconnect
- [x] Create `network/FTPClient.kt` — Apache Commons Net FTP client with passive mode, binary transfer
- [x] Create `network/WebDAVClient.kt` — Sardine Android OkHttp client with HTTPS support
- [x] Create `network/LocalProxyServer.kt` — NanoHTTPD proxy bridging SMB/FTP/WebDAV to MPV via localhost HTTP
- [x] Create `domain/NetworkConnectionManager.kt` — manages connections, auto-connect, stream URL generation
- [x] Implement auto-connect on launch
- [x] Implement proxy lifecycle management (start/stop)

### Background Playback
- [x] Create `MediaPlaybackService.kt` — MediaSessionService stub
- [x] Implement MediaSession integration for system media controls

### Video Enhancement
- [x] Create `domain/VideoFilterPresets.kt` — 13 presets (None, Vivid, Warm Tone, Cool Tone, Soft Pastel, Cinematic, Dramatic, Night Mode, Nostalgic, Ghibli Style, Neon Pop, Deep Black) with MPV vf string conversion
- [x] Create `domain/Anime4KManager.kt` — 6 modes (A/B/C/A+/B+/C+) × 3 quality levels (Fast/Balanced/High), shader management, gpu-next compatibility check

### Media Info
- [x] Create `MediaInfoActivity.kt` — stub activity for media metadata viewer

### Error Handling
- [x] Create `presentation/GlobalExceptionHandler.kt` — uncaught exception handler with crash report generation, device/app info, file saving
- [x] Create `presentation/CrashActivity.kt` — crash display with copy/share/rerestart/close actions, monospace crash log

### Final Polish
- [x] Create `utils/PermissionUtils.kt` — storage (API 24-32 vs 33+), notification (API 33+), permission descriptions

---

## Testing TODOs

### Unit Tests
- [ ] PlayerViewModel state transition tests
- [ ] Preference flow tests
- [ ] Subtitle parsing tests
- [ ] Sort utility tests
- [ ] File utility tests
- [ ] Network URL validation tests

### Integration Tests
- [ ] Room database migration tests
- [ ] Room DAO query tests (in-memory DB)
- [ ] Koin dependency graph tests

### Compose UI Tests
- [ ] PlayerControls rendering tests
- [ ] Seekbar rendering tests
- [ ] Sheet open/close tests
- [ ] Browser screen rendering tests
- [ ] Preference screen rendering tests

### Manual Tests
- [ ] Local video playback (various codecs)
- [ ] Subtitle display (various formats)
- [ ] Network stream playback (SMB, FTP, WebDAV)
- [ ] Background playback with notification
- [ ] PiP mode on various devices
- [ ] Theme switching (all 32 themes)
- [ ] Gesture responsiveness
- [ ] Settings persistence
- [ ] App cold start performance

---

## Documentation TODOs
- [ ] Add KDoc comments to all public APIs
- [ ] Create CONTRIBUTING.md
- [ ] Create CHANGELOG.md
- [ ] Create RELEASE.md (release checklist)
- [ ] Update README.md with features and screenshots
