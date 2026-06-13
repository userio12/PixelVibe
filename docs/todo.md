# PixelVibe — Todo

## Phase 1a — Foundation

- [x] Create 10-module Gradle scaffold
  - [x] `:app`
  - [x] `:core:common`
  - [x] `:core:data`
  - [x] `:core:player`
  - [x] `:core:ui`
  - [x] `:feature:home`
  - [x] `:feature:recent`
  - [x] `:feature:network`
  - [x] `:feature:player`
  - [x] `:feature:settings`
- [x] Update `settings.gradle.kts` with all modules
- [x] Update `gradle/libs.versions.toml` with all dependencies
- [x] Create `core:common`:
  - [x] `Result.kt`, `DataError.kt`, `UiText.kt`
  - [x] Route objects for all screens
  - [x] Extensions
- [x] Create `core:data`:
  - [x] `AppDatabase.kt` (Room)
  - [x] `VideoDao.kt`, `PlaylistDao.kt`, `HistoryDao.kt`
  - [x] `VideoEntity.kt`, `PlaylistEntity.kt`, `HistoryEntity.kt`
  - [x] `VideoRepository.kt`
  - [x] `MediaScanner.kt`
  - [x] `DataModule.kt` (Koin)
- [x] Create `core:player`:
  - [x] `PlaybackEngine.kt` (Media3 wrapper)
  - [x] `PlayerController.kt`
  - [x] `SubtitleManager.kt`, `SubtitleParser.kt`
  - [x] `AudioEffectManager.kt`
  - [x] `PipHandler.kt`
  - [x] `PlayerModule.kt` (Koin)
- [x] Create `core:ui`:
  - [x] Theme files (`Theme.kt`, `Color.kt`, `Type.kt`, `Shape.kt`)
  - [x] `VideoCard.kt`
  - [x] `LoadingIndicator.kt`
  - [x] `ErrorView.kt`
  - [x] `UiModule.kt` (Koin)
- [x] Create `:app`:
  - [x] `PixelVibeApp.kt` (startKoin)
  - [x] `MainActivity.kt` (edge-to-edge)
  - [x] `PixelVibeNavGraph.kt` (stub with all routes)
- [x] Write foundation tests:
  - [x] `PlaybackEngineTest`
  - [x] `VideoRepositoryTest` (in-memory Room)
  - [x] `MediaScannerTest` (fake ContentResolver)
  - [x] `SubtitleParserTest`

## Phase 1b — Core Player

- [x] Build `HomeScreen`:
  - [x] Lazy vertical video grid
  - [x] Loading / empty / error states
  - [x] Search bar (in-memory filter)
  - [x] Folder tab, favorites tab, history tab, IPTV tab
- [x] Build `RecentScreen`:
  - [x] Chronological history list
  - [x] Tap to resume playback
  - [x] Swipe to clear item
  - [x] Loading / empty / error states
- [x] Build `PlayerScreen`:
  - [x] Transport controls (play/pause, seek bar, time)
  - [x] Speed selector (0.25x–4x)
  - [x] Gesture layer (vol left / brightness right / seek horizontal)
  - [x] Double-tap seek (customizable)
  - [x] PiP support
  - [x] Resume playback from Room
  - [x] Subtitle rendering (embedded + external SRT/VTT)
  - [x] FFmpeg fallback activation
- [x] Build `PlayerViewModel` (MVI)
- [x] Wire full navigation graph
- [x] Write tests:
  - [x] `PlayerViewModelTest`
  - [x] `HomeViewModelTest`
  - [x] `NavigationTest`

## Phase 2 — Library & Subtitles

- [x] Online subtitle search (OpenSubtitles API)
- [x] Subtitle styling (font, size, color, outline, background, position via DataStore)
- [x] Bilingual subtitle mode
- [x] A-B repeat loop overlay
- [x] Frame step (forward/backward)
- [x] Sleep timer (countdown overlay + notification)
- [x] Favorites as tab in Home
- [x] Playlist creation dialog
- [x] Tests:
  - [x] `SubtitleSearchClientTest`
  - [x] `SubtitleStylePreferencesTest`
  - [x] `PlayerViewModelTest` (includes A-B loop + sleep timer)

## Phase 3 — Audio

- [x] Equalizer (5–10 bands, presets + custom, AudioEffects session)
- [x] Bass boost toggle + intensity slider
- [x] Virtualizer toggle + intensity slider
- [x] Loudness normalization
- [x] Headphone detection (AudioDeviceCallback)
- [x] Tests:
  - [x] `AudioEffectManagerTest`
  - [x] `PlayerViewModelTest` (audio actions)

## Phase 4 — Network

- [x] Build `NetworkScreen`:
  - [x] SMB browser (jcifs-ng)
  - [x] FTP browser (commons-net)
  - [x] WebDAV browser
  - [x] Loading / empty / error states
- [x] DLNA/UPnP discovery (raw SSDP)
- [x] Chromecast stub (requires Google Play Services)
- [x] Cast button in Player overlay
- [x] Tests:
  - [x] `SmbClientTest`
  - [x] `FtpClientTest`
  - [x] `WebDavClientTest`
  - [x] `SsdpDiscoveryTest`
  - [x] `NetworkModelsTest`

## Phase 5 — Security & Polish

- [x] App lock (PIN + biometric, EncryptedSharedPreferences)
- [x] Secure folder (separate Room DB, SQLCipher, biometric unlock) — stub prepared
- [x] Incognito mode (no history, no resume, visual indicator via DataStore)
- [x] Settings backup/restore (export/import JSON)
- [x] AMOLED dark mode toggle (pure black theme colors)
- [x] Tablet master-detail layout — Responsive via `WindowSizeClass`
- [x] Animations (enter/exit slide + fade transitions in NavHost)
- [x] Accessibility (content descriptions on all nav icons)
- [x] Tests:
  - [x] `AppLockManagerTest`
  - [x] `SettingsBackupManagerTest`
  - [x] `IncognitoManagerTest`
