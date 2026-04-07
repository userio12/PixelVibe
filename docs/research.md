# Research: mpvExtended → PixelVibe Compose Clone

## Reference Project

**Source:** [mpvExtended (mpvEx)](https://github.com/marlboro-advance/mpvEx.git)  
**License:** Open source (MIT-style, fork of mpv-android)  
**Version Analyzed:** 1.2.9 (build 129)

---

## 1. Core Video Player Engine

### libmpv Architecture

mpvExtended uses a **custom AAR** (`mpv-android-lib-v0.0.1.aar`) that wraps the [mpv-android](https://github.com/mpv-android/mpv-android) project. Key classes from the AAR:

| Class | Purpose |
|-------|---------|
| `MPVLib` | Static JNI bridge to libmpv. Methods: `setOptionString`, `command`, `observeProperty`, `setProperty*`, `getProperty*`, `addObserver` |
| `BaseMPVView` | Base Android View that renders video frames. Handles EGL/Vulkan surface, lifecycle, input events |
| `MPVNode` | Node-based property type (for complex data structures) |
| `Utils` | Utility functions (URI parsing, file utilities) |
| `KeyMapping` | Maps Android keycodes to mpv key names |
| `FastThumbnails` | Fast thumbnail generation for media browser |

### Property Observation Pattern

mpvExtended extends `MPVLib` with Kotlin Flow-based property observers:

```kotlin
fun MPVLib.Companion.propBoolean(property: String, initial: Boolean = false): Flow<Boolean>
fun MPVLib.Companion.propInt(property: String, initial: Int = 0): Flow<Int>
fun MPVLib.Companion.propFloat(property: String, initial: Float = 0f): Flow<Float>
fun MPVLib.Companion.propString(property: String): Flow<String>
fun MPVLib.Companion.propNode(property: String): Flow<MPVNode?>
```

These observe mpv properties like `pause`, `time-pos`, `duration`, `eof-reached`, `video-params/aspect`, etc.

### MPV Initialization Options

The `MPVView.initOptions()` method configures:

- **Profile:** `--profile=mpvex-gpu` or `mpvex-gpu-next`
- **GPU Backend:** `--gpu-context=android` or `--gpu-context=androidvk` (Vulkan)
- **Hardware Decoding:** `--hwdec=mediacodec` or `--hwdec=mediacodec-copy`
- **Demuxer Cache:** `--demuxer-max-bytes`, `--demuxer-max-back-bytes`
- **TLS:** `--tls-verify`, `--tls-ca-file=cacert.pem`
- **Screenshot Directory:** `--screenshot-dir`
- **Video Filters:** `--vf=lavfi=[...]` for brightness/contrast/etc.
- **Speed:** `--speed`, `--prefetch-playlist=no`
- **Precise Seeking:** `--hr-seek=yes`
- **Audio:** `--audio-pitch-correction`, `--volume-max`
- **Subtitles:** `--sub-ass`, `--sub-scale`, `--sub-fonts-dir`
- **Anime4K:** Full shader chain via `--glsl-shaders`

### Key MPV Properties Observed

| Property | Type | Purpose |
|----------|------|---------|
| `pause` | Boolean | Playback state |
| `paused-for-cache` | Boolean | Buffering state |
| `time-pos` | Double | Current position (seconds) |
| `duration` | Double | Total duration |
| `eof-reached` | Boolean | End of file |
| `video-params/aspect` | Double | Video aspect ratio |
| `video-params/w`, `video-params/h` | Int | Video dimensions |
| `video-params/fps` | Double | Frame rate |
| `speed` | Double | Playback speed |
| `sid`, `aid` | Int | Selected subtitle/audio track |
| `sub-delay`, `audio-delay` | Double | Track delays |
| `chapter` | Int | Current chapter |
| `chapter-list` | Node | Chapter list data |
| `track-list` | Node | All available tracks |
| `file-path`, `filename` | String | Current file |
| `media-title` | String | Media title |

### MPV Commands Used

| Command | Purpose |
|---------|---------|
| `loadfile` | Load a media file |
| `playlist-next`, `playlist-prev` | Navigate playlist |
| `seek` | Seek by relative/absolute amount |
| `frame-step`, `frame-back-step` | Frame-by-frame navigation |
| `screenshot` | Take snapshot |
| `cycle`, `cycle-values` | Cycle through values |
| `set`, `add`, `multiply` | Property manipulation |
| `ab-loop` | Set A-B loop points |
| `script-message`, `script-message-to` | Lua script communication |

---

## 2. Architecture Pattern

### Layer Structure (MVVM + Repository)

```
┌─────────────────────────────────────────────────┐
│                    UI Layer                      │
│  Activities, Compose Screens, ViewModels         │
│  PlayerActivity, MainActivity, PlayerViewModel    │
├─────────────────────────────────────────────────┤
│               Presentation Layer                 │
│  Shared Compose components, Screen interface      │
├─────────────────────────────────────────────────┤
│                 Domain Layer                     │
│  Use Cases: Anime4KManager, RecentlyPlayed,       │
│  PlaybackState, ThumbnailManager, NetworkManager  │
├─────────────────────────────────────────────────┤
│              Data / Repository Layer             │
│  Room Database, Network Repositories,             │
│  Preference Store, File System                    │
├─────────────────────────────────────────────────┤
│                 DI Layer (Koin)                  │
│  Modules: Preferences, Database, File, Domain     │
└─────────────────────────────────────────────────┘
```

### Dependency Injection (Koin)

```kotlin
// Koin modules structure
val appModule = module {
    single { PreferenceStore(...) }
    single { PlayerPreferences(...) }
    single { AudioPreferences(...) }
    single { Database }
    single { PlaybackHistoryRepository }
    single { NetworkRepository }
    single { RecentlyPlayedRepository }
    single { MediaScanManager }
    single { ThumbnailManager }
    single { NetworkConnectionManager }
    single { FilePickerHelper }
    single { PlaylistRepository }
}
```

### Navigation (Navigation3)

mpvExtended uses the **new AndroidX Navigation3** API:

```kotlin
interface Screen : NavKey {
    @Composable fun Content()
}

// Usage in MainActivity
NavDisplay(
    navBackStack = backStack,
    onBack = { if (backStack.size > 1) backStack.pop() else finish() },
    transitionProvider = object : NavTransitionProvider<Screen> { ... }
)
```

**Transition patterns:**
- Forward: `SlideIntoContainer` (220ms, FastOutSlowInEasing)
- Backward: `SlideOutOfContainer` (220ms, FastOutSlowInEasing)
- Predictive pop: Scale to 0.9x with fade

---

## 3. UI/UX Design System

### Theme System

**32 Built-in Themes** with Light/Dark/AMOLED variants:

| Theme | Light Primary | Dark Primary | Notes |
|-------|--------------|--------------|-------|
| Default | Purple | Purple | Standard Material |
| Dynamic | Extracted | Extracted | Material You |
| Catppuccin | Rosewater | Rosewater | Pastel |
| Gruvbox | Orange | Orange | Retro |
| Kanagawa | Green | Green | Japanese aesthetic |
| Nord | Frost Blue | Frost Blue | Arctic theme |
| Dracula | Red | Red | Dark vampire theme |
| Tokyo Night | Blue | Blue | Neon city |
| ... | ... | ... | + 23 more |

**Theme implementation:**
- Each theme defines `ColorScheme` objects for light, dark, and AMOLED
- AMOLED mode uses `ColorScheme(primary = Color.Black, ...)` with pure black surfaces
- Material You dynamic color via `dynamicDarkColorScheme(context)` / `dynamicLightColorScheme(context)` on Android 12+
- Theme transition: Circular reveal overlay (currently disabled in code)

### Typography

- Uses `Typography` from Material3 with custom font families
- Supports custom font selection from user's installed fonts
- Subtitle typography is independently configurable (font, size, bold, italic, etc.)

### Animation System

| Animation | Duration | Easing | Purpose |
|-----------|----------|--------|---------|
| Nav transitions | 220ms | FastOutSlowIn | Screen transitions |
| Tab transitions | 250ms | FastOutSlowIn | Bottom nav tabs |
| Controls enter | 100ms | LinearOutSlowIn | Player controls appear |
| Controls exit | 300ms | FastOutSlowIn | Player controls hide |
| Seekbar wave | Continuous | - | Sine wave animation |
| Seekbar flatten | 550ms | - | Wave disappears on pause |
| Seekbar regrow | 800ms | - | Wave returns on play |
| Player button ripples | - | Custom alpha | Button touch feedback |
| Speed overlay slide | - | - | Volume/brightness sliders |
| Double-tap ovals | - | - | Seek direction indicators |
| Play/pause icon | - | AnimatedVectorDrawable | Morphing icon |

### Gesture System

**Multi-touch gesture handler** with these regions and actions:

| Gesture | Region | Default Action |
|---------|--------|---------------|
| Double-tap | Left (33%) | Seek backward |
| Double-tap | Center (33%) | Play/Pause (or double-tap to play) |
| Double-tap | Right (33%) | Seek forward |
| Multi-tap | Any | Continue seeking in same direction |
| Long-press | Any | Speed up playback (hold) |
| Vertical swipe | Left half | Brightness control |
| Vertical swipe | Right half | Volume control |
| Horizontal swipe | Bottom | Seek (configurable sensitivity) |
| Pinch-to-zoom | Any | Video zoom |
| Pan & zoom | Any | Manual pan/zoom |

**Gesture configuration options:**
- Double-tap actions configurable per region (seek, play/pause, custom)
- Single-tap center toggle (single-tap vs double-tap for play/pause)
- Media control side swap (brightness/volume sides)
- Double-tap seek area width
- Gesture isolation (prevent interference between gestures)

### Player Controls Layout

**ConstraintLayout-based overlay** with:
- Top gradient bar (black, alpha 0.8) with back button + media title (marquee)
- Bottom gradient bar with seekbar + play/pause + next/prev + time labels
- Configurable button slots: top-right (3), bottom-left (3), bottom-right (2)
- 22 available button types
- 8 preset control layouts (action movie, anime, music video, minimal, classic, default, advanced, custom)

### Player Sheets (Bottom Modals)

All sheets use `ModalBottomSheet` with Material3:
- Playback Speed Sheet (speed presets)
- Subtitle Tracks Sheet (primary/secondary)
- Audio Tracks Sheet
- Chapters Sheet
- Video Zoom Sheet
- Aspect Ratio Sheet
- Playlist Sheet
- Frame Navigation Sheet (with snapshots)
- More Sheet (timer, sleep, background playback, snapshot)
- Decoders Sheet (HW/SW decoder selection)

### Side Panels (Draggable)

- **Subtitle Settings Panel:** Font, size, border, colors, ASS override, scale, position, delay
- **Audio Delay Panel:** Delay adjustment with sound-heard/sound-spotted visual
- **Video Settings Panel:** Brightness, saturation, contrast, gamma, hue, sharpness + 13 presets
- **Multi-Card Panel:** Compound panel with multiple settings cards

---

## 4. Key Libraries & Dependencies

### Core
| Library | Version | Purpose |
|---------|---------|---------|
| AGP | 8.13.0 → 9.1.0 | Android build |
| Kotlin | 2.1.0 → 2.3.20 | Language |
| Compose BOM | 2025.10.01 → 2026.03.00 | Compose version alignment |
| Navigation3 | - → 1.0.1 | Compose-native navigation |
| Koin | - → 4.2.0 | Dependency injection |
| Room | - → 2.8.4 | Local database |
| KSP | - → 2.3.6 | Kotlin Symbol Processing |

### UI Components
| Library | Purpose |
|---------|---------|
| Material3 1.5.0-alpha15 | Material You components |
| ConstraintLayout Compose 1.1.1 | Complex layouts |
| Animation Graphics | Animated vector drawables |
| LazyColumnScrollbar 2.2.0 | Scrollable lists with scrollbar |
| Reorderable 3.0.0 | Drag-and-drop lists |
| Seeker 2.0.1 | Custom seekbar with segments |
| Accompanist Permissions 0.37.3 | Runtime permissions |
| compose.prefs 2.2.0 | Compose preferences |

### Networking
| Library | Protocol |
|---------|----------|
| OkHttp 5.3.2 | HTTP/HTTPS |
| SMBJ 0.14.0 | SMB (Samba) |
| Commons Net 3.13.0 | FTP |
| Sardine Android 0.8 | WebDAV |
| NanoHTTPD 2.3.1 | Local HTTP proxy |

### Media
| Library | Purpose |
|---------|---------|
| mpv-android-lib (custom AAR) | Video playback engine |
| androidx.media 1.7.1 | MediaSession / MediaBrowserService |
| TrueType Parser 2.1.4 | Font parsing for subtitles |
| MediaInfo Android | Media metadata extraction |
| FSAF 1.1.3 | Storage Access Framework replacement |

### Data
| Library | Purpose |
|---------|---------|
| kotlinx-serialization-json 1.10.0 | JSON serialization |
| kotlinx-collections-immutable 0.4.0 | Immutable collections |

---

## 5. Database Schema (Room)

### Tables

**playback_state** (stores per-file playback settings)
```
mediaTitle TEXT PRIMARY KEY
lastPosition INTEGER
playbackSpeed REAL
videoZoom INTEGER
sid INTEGER
secondarySid INTEGER
subDelay REAL
subSpeed REAL
aid INTEGER
audioDelay REAL
timeRemaining REAL
externalSubtitles TEXT (JSON)
hasBeenWatched INTEGER
```

**recently_played** (watch history)
```
id INTEGER PRIMARY KEY AUTOINCREMENT
filePath TEXT
fileName TEXT
videoTitle TEXT
duration INTEGER
fileSize INTEGER
width INTEGER
height INTEGER
timestamp INTEGER
launchSource TEXT
playlistId INTEGER
```

**video_metadata** (file metadata cache)
```
path TEXT PRIMARY KEY
size INTEGER
dateModified INTEGER
duration INTEGER
width INTEGER
height INTEGER
fps REAL
lastScanned INTEGER
subtitleCodec TEXT
hasEmbeddedSubtitles INTEGER
```

**network_connections** (saved network connections)
```
id INTEGER PRIMARY KEY AUTOINCREMENT
name TEXT
protocol TEXT (SMB/FTP/WebDAV)
host TEXT
port INTEGER
username TEXT
password TEXT
path TEXT
isAnonymous INTEGER
lastConnected INTEGER
autoConnect INTEGER
useHttps INTEGER
```

**playlists**
```
id INTEGER PRIMARY KEY AUTOINCREMENT
name TEXT
createdAt INTEGER
updatedAt INTEGER
m3uSourceUrl TEXT
isM3uPlaylist INTEGER
```

**playlist_items** (FK → playlists, cascade delete)
```
id INTEGER PRIMARY KEY AUTOINCREMENT
playlistId INTEGER
filePath TEXT
fileName TEXT
position INTEGER
addedAt INTEGER
lastPlayedAt INTEGER
playCount INTEGER
lastPosition INTEGER
```

### Migrations
9 database versions with squashed migrations (v1 → v9).

---

## 6. Network Streaming Architecture

### Protocol Support
- **SMB (Samba):** Using SMBJ library for Windows file shares
- **FTP:** Using Apache Commons Net
- **WebDAV:** Using Sardine Android library

### Local HTTP Proxy (NanoHTTPD)

Network files cannot be played directly by mpv, so a local HTTP proxy server streams them:

```
Network Source → SMB/FTP/WebDAV Client → NanoHTTPD Proxy → MPV (http://localhost:port/file)
```

**Key components:**
- `NetworkStreamingProvider` (ContentProvider) for URI-based access
- `NetworkConnectionManager` (domain layer) manages connections
- Proxy lifecycle tied to app lifecycle (start on connect, stop on disconnect)
- Thumbnails generated from network files (experimental)

---

## 7. Subtitle System

### Supported Formats (28+)
srt, vtt, ass, ssa, sub, idx, sup, xml, ttml, dfxp, itt, ebu, imsc, usf, sbv, srv1, srv2, srv3, json, sami, smi, mpl, pjs, stl, rt, psb, cap, scc, vttx, lrc, krc, txt, pgs

### Features
- **Dual subtitles:** Primary + secondary simultaneously
- **External subtitle loading:** From file URIs, content URIs
- **Online subtitle search:** Via TMDB API (Wyzie integration)
- **Auto-detection:** By filename match, checksum, full title scan
- **Typography control:** Font, size, bold, italic, justification, colors (text/border/background), border size/style, shadow, scale, position
- **ASS/SSA support:** With override options (force styled or preserve original)
- **Delay & speed:** Independent for primary and secondary

---

## 8. Key Implementation Challenges

### View + Compose Hybrid
The video surface (`MPVView`) is a **Android View**, not a Compose component. It's embedded in a `ConstraintLayout` XML with a `ComposeView` overlay:

```xml
<androidx.constraintlayout.widget.ConstraintLayout>
    <app.marlboroadvance.mpvex.ui.player.MPVView android:id="@+id/player" />
    <androidx.compose.ui.platform.ComposeView android:id="@+id/composeView" />
</androidx.constraintlayout.widget.ConstraintLayout>
```

This requires:
- `ViewBinding` for the player layout
- Coordination between View lifecycle and Compose state
- MPV events → Compose state flow conversion

### Hardware Acceleration
- `mediacodec` decoder directly renders to surface (faster, less flexible)
- `mediacodec-copy` decoder copies frames (allows video filters)
- `gpu-next` renderer requires different initialization
- Vulkan support requires Android 13+ and Vulkan 1.3 hardware

### Storage Access
- FSAF library bypasses Storage Access Framework limitations
- Direct filesystem access for internal/external storage
- Content URI handling for SAF-selected files

### Background Playback
- `MediaPlaybackService` extends `MediaBrowserServiceCompat`
- Foreground service with `FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK`
- MediaSessionCompat for system media controls
- Notification with MediaStyle (previous/play-pause/next)

### PiP (Picture-in-Picture)
- Activity must handle lifecycle transitions
- State preservation across PiP enter/exit
- Different UI layout for PiP mode

---

## 9. What We Need to Replicate

### Essential Components
1. **MPV engine** (AAR or equivalent) - The video playback core
2. **Gesture system** - Complete multi-touch gesture handler
3. **Player controls** - Compose overlay with seekbar, buttons, sheets
4. **Browser UI** - File browser, recents, playlists, network
5. **Preference system** - Extensive settings screens
6. **Database** - Room for state, history, playlists, connections
7. **Theme system** - 32 themes + Material You + AMOLED
8. **Network streaming** - SMB/FTP/WebDAV with local proxy
9. **Subtitle system** - Dual subtitles, online search, typography
10. **Background service** - MediaSession + notification

### What Can Be Simplified for v1
- Network streaming (can be added later)
- Anime4K shaders (advanced feature)
- Lua script support (advanced feature)
- M3U playlist import (can be added later)
- Wyzie subtitle search (can use simpler alternative)
- 32 themes (start with Material You + 5 built-ins)

---

## 10. Compose-Specific Adaptations

Since we're creating an **exact clone using Jetpack Compose**, we need to adapt:

| mpvExtended Approach | PixelVibe Compose Approach |
|---------------------|---------------------------|
| XML ConstraintLayout + ComposeView overlay | `AndroidView` wrapper for MPVView + Compose overlay |
| Navigation3 NavDisplay | Compose Navigation with NavHost |
| ViewBinding for player layout | Pure Compose with `AndroidView` for MPVView |
| compose.prefs library | Custom Compose preference components |
| Koin ViewModel factory | `koin-compose-viewmodel` integration |
| Custom Screen interface | Sealed class navigation destinations |
| XML-based theme transition | Compose animation with screenshot capture |
