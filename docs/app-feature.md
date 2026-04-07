# PixelVibe — App Features

## Overview

PixelVibe is a **feature-rich Android video player** built entirely with **Jetpack Compose** and **Material You** design principles. It is an exact clone of the [mpvExtended](https://github.com/marlboro-advance/mpvEx) project, featuring **dynamic color theming**, **Material Expressive animations**, **gesture-based controls**, **extensive subtitle support**, **network streaming**, and **background playback**.

---

## 1. 🎬 Video Playback

### Core Playback
- [x] **Hardware-accelerated decoding** via mediacodec / mediacodec-copy
- [x] **Software decoding fallback** when hardware decoding fails
- [x] **GPU-next renderer** toggle for enhanced rendering
- [x] **Vulkan rendering** support (Android 13+, Vulkan 1.3)
- [x] **YUV420P pixel format** toggle
- [x] **Precise seeking** (frame-accurate, auto-enabled for short videos)
- [x] **A-B Loop** with repeat between two points
- [x] **Repeat modes:** Off, One (current file), All (playlist)
- [x] **Shuffle** with persisted state
- [x] **Autoplay next** video from playlist
- [x] **Frame-by-frame navigation** with snapshot previews
- [x] **Video mirroring** (horizontal flip) and **vertical flip**
- [x] **Custom skip duration** button (configurable, default 90s)
- [x] **Playback speed:** 0.25x to 4.0x with presets
- [x] **Hold-to-speed-up** gesture (long-press to temporarily increase speed)
- [x] **Sleep timer** with configurable duration
- [x] **Snapshot/Screenshot** with or without subtitles
- [x] **Include subtitles in snapshot** option

### Video Filters & Enhancement
- [x] **Brightness** adjustment (-5 to +5)
- [x] **Saturation** adjustment
- [x] **Contrast** adjustment
- [x] **Gamma** adjustment
- [x] **Hue** adjustment
- [x] **Sharpness** adjustment
- [x] **13 Filter Presets:**
  1. None
  2. Vivid
  3. Warm Tone
  4. Cool Tone
  5. Soft Pastel
  6. Cinematic
  7. Dramatic
  8. Night Mode
  9. Nostalgic
  10. Ghibli Style
  11. Neon Pop
  12. Deep Black
- [x] **Debanding:** None, CPU (gradfun), GPU
- [x] **Anime4K upscaling** (experimental) with modes A/B/C/A+/B+/C+ and quality levels
- [x] **Video zoom** with percentage display
- [x] **Pan and zoom** (manual)
- [x] **Aspect ratio:** Fit, Crop, Stretch, custom ratios

### Orientation
- [x] **Free** (follows sensor)
- [x] **Video** (based on video aspect ratio)
- [x] **Portrait**
- [x] **Reverse Portrait**
- [x] **Sensor Portrait**
- [x] **Landscape**
- [x] **Reverse Landscape**
- [x] **Sensor Landscape**

---

## 2. 🎵 Audio Features

- [x] **Audio track selection** from embedded tracks
- [x] **External audio track loading** from file
- [x] **Volume boost** with configurable cap
- [x] **Audio pitch correction**
- [x] **Volume normalization** (dynaudnorm filter)
- [x] **Audio channels:** Auto, Auto Safe, Mono, Stereo, Reversed Stereo
- [x] **Preferred audio language(s)**
- [x] **Audio delay** adjustment with sound-heard/sound-spotted UI
- [x] **Background playback** with media notification

---

## 3. 📝 Subtitle System

### Supported Formats (28+)
`srt`, `vtt`, `ass`, `ssa`, `sub`, `idx`, `sup`, `xml`, `ttml`, `dfxp`, `itt`, `ebu`, `imsc`, `usf`, `sbv`, `srv1`, `srv2`, `srv3`, `json`, `sami`, `smi`, `mpl`, `pjs`, `stl`, `rt`, `psb`, `cap`, `scc`, `vttx`, `lrc`, `krc`, `txt`, `pgs`

### Features
- [x] **Dual subtitles** (primary + secondary simultaneously)
- [x] **External subtitle loading** (file URIs, content URIs)
- [x] **Online subtitle search** via TMDB API
- [x] **Local subtitle scanning** (by title match, checksum, full title scan)
- [x] **Auto-load subtitles** with matching filename
- [x] **Subtitle save location** preference

### Typography Control
- [x] **Font** selection (from installed system fonts)
- [x] **Font size** adjustment
- [x] **Bold / Italic** toggles
- [x] **Text justification** (left, center, right, auto)
- [x] **Text color** picker
- [x] **Border color** picker
- [x] **Background color** picker
- [x] **Border size** and **style**
- [x] **Shadow offset**
- [x] **Scale** adjustment
- [x] **Position** adjustment (vertical)
- [x] **ASS/SSA override** (force styled or preserve original)
- [x] **Scale by window** option
- [x] **Subtitle delay** (independent for primary/secondary)
- [x] **Subtitle speed** (independent for primary/secondary)

---

## 4. 🎮 Gesture Controls

### Touch Gestures
| Gesture | Region | Default Action | Configurable |
|---------|--------|---------------|--------------|
| **Double-tap** | Left (33%) | Seek backward | ✅ Yes |
| **Double-tap** | Center (33%) | Play/Pause | ✅ Yes (single-tap toggle) |
| **Double-tap** | Right (33%) | Seek forward | ✅ Yes |
| **Multi-tap** | Any | Continue seeking same direction | ✅ Yes |
| **Long-press** | Any | Speed up playback (hold) | ✅ Yes |
| **Vertical swipe** | Left half | Brightness control | ✅ Yes (side swap) |
| **Vertical swipe** | Right half | Volume control | ✅ Yes (side swap) |
| **Horizontal swipe** | Bottom | Seek | ✅ Yes (sensitivity) |
| **Pinch-to-zoom** | Any | Video zoom | ✅ Yes |
| **Pan & zoom** | Any | Manual pan/zoom | ✅ Yes |

### Gesture Configuration Options
- [x] Double-tap actions per region (seek, play/pause, custom)
- [x] Single-tap center toggle (single vs double-tap for play/pause)
- [x] Media control side swap (brightness/volume sides)
- [x] Double-tap seek area width
- [x] Gesture isolation (prevent interference)
- [x] Dynamic speed overlay with swipe-to-adjust
- [x] Haptic feedback on long-press

---

## 5. 🎨 Player Controls

### Overlay Layout
- [x] **ConstraintLayout-based** Compose overlay
- [x] **Gradient bars** (top/bottom, black, alpha 0.8)
- [x] **Media title** with marquee scrolling for long titles
- [x] **Current chapter** display in top bar
- [x] **Configurable button slots:**
  - Top-right (3 buttons)
  - Bottom-left (3 buttons)
  - Bottom-right (2 buttons)
  - Portrait-bottom (additional slot)

### 22 Available Button Types
1. Back Arrow
2. Video Title
3. Chapters
4. Speed
5. Decoder
6. Rotation
7. Frame Navigation
8. Zoom
9. Picture-in-Picture
10. Aspect Ratio
11. Lock Controls
12. Audio Tracks
13. Subtitles
14. More Options
15. Current Chapter
16. Repeat Mode
17. Shuffle
18. Mirror (Horizontal Flip)
19. Vertical Flip
20. A-B Loop
21. Custom Skip
22. Background Playback

### 8 Preset Control Layouts
1. Action Movie
2. Anime
3. Music Video
4. Minimal
5. Classic
6. Default
7. Advanced
8. Custom (user-defined via drag-and-drop editor)

### Seekbar
- [x] **Three styles:** Standard, Wavy (animated), Thick
- [x] **Wavy seekbar** — continuous sine wave animation (10px/s), flattens when paused (550ms), regrows on play (800ms)
- [x] **A-B loop** visual indicators (gold markers)
- [x] **Chapter markers** on seekbar
- [x] **Expanded touch area** (64dp)
- [x] **Precise position** (double) + integer position
- [x] **Animated position** with 200ms tween
- [x] **Loading indicator** for buffering state

### Slide to Unlock
- [x] Swipe-to-unlock slider when controls are locked

### Double-tap Seek Indicators
- [x] Left/right oval shapes with arrow icons and time delta display

---

## 6. 📋 Player Sheets (Bottom Modals)

All sheets use Material3 `ModalBottomSheet`:

1. **Playback Speed Sheet** — Speed presets: 0.25x, 0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 1.75x, 2.0x, 2.5x, 3.0x, 4.0x
2. **Subtitle Tracks Sheet** — Primary/secondary subtitle selection, disable, external load
3. **Online Subtitle Search Sheet** — TMDB API search with download
4. **Audio Tracks Sheet** — Audio track selection, external load
5. **Chapters Sheet** — Chapter navigation with thumbnails
6. **Decoders Sheet** — HW/SW decoder selection
7. **More Sheet** — Timer, sleep, snapshot, background playback toggle
8. **Video Zoom Sheet** — Zoom level selector (50% to 300%)
9. **Aspect Ratio Sheet** — Fit, Crop, Stretch, custom ratios (16:9, 4:3, 1:1, 21:9)
10. **Playlist Sheet** — Playlist item list with drag-to-reorder
11. **Frame Navigation Sheet** — Frame-by-frame with snapshot previews
12. **Generic Tracks Sheet** — Generic track list for any track type

---

## 7. 📱 Side Panels (Draggable)

1. **Subtitle Settings Panel** — Font, size, border, colors, ASS override, scale, position, delay
2. **Subtitle Delay Panel** — Primary/secondary delay with voice-heard/text-seen UI
3. **Audio Delay Panel** — Audio delay with sound-heard/sound-spoken UI
4. **Video Settings Panel** — Filters (brightness, saturation, contrast, gamma, hue, sharpness) + 13 presets + debanding card + filter presets card
5. **Multi-Card Panel** — Compound panel with multiple settings cards

---

## 8. 🎨 Material You Theme System

### Dynamic Color
- [x] **Material You** dynamic color extraction (Android 12+, API 31+)
- [x] **32 Built-in Themes** with Light/Dark/AMOLED variants:
  1. Default
  2. Dynamic (Material You)
  3. Catppuccin
  4. Cloudflare
  5. Cotton Candy
  6. Doom
  7. Green Apple
  8. Gruvbox
  9. Kanagawa
  10. Lavender
  11. Midnight
  12. Mocha
  13. Strawberry
  14. Tidal
  15. Nord
  16. Rose Pine
  17. Tako Green
  18. Tokyo Night
  19. Yin Yang
  20. Yotsuba
  21. Sapphire
  22. Sunset
  23. Ocean
  24. Forest
  25. Rose Gold
  26. Violet
  27. Amber
  28. Coral
  29. Slate
  30. Dracula
  31. Monochrome

### AMOLED Mode
- [x] **Pure black** backgrounds (`Color.Black`) for OLED displays
- [x] Reduced contrast for accessibility

### Theme Preview
- [x] **Preview cards** showing theme appearance before applying
- [x] **Circular reveal** transition animation (tap position → expand)

---

## 9. 📂 File Browser

### Home Tab (Folder Browser)
- [x] **Tree view** or **flat view** file scanning
- [x] **Video cards** with thumbnails, duration badge, file size
- [x] **"NEW" label** for unplayed videos
- [x] **Sort dialog** — by name, date, size, duration; ascending/descending
- [x] **Selection mode** — long-press to multi-select
- [x] **Copy/paste** operations
- [x] **Delete** with confirmation dialog
- [x] **Auto-scroll** to last played video
- [x] **Watched threshold** marking (configurable %)
- [x] **Pull-to-refresh**
- [x] **Folder navigation** with breadcrumb

### Recents Tab
- [x] **Recently played** videos and playlists
- [x] **Watch history** with timestamps
- [x] **Resume playback** from last position

### Playlists Tab
- [x] **Custom playlists** — create, edit, delete
- [x] **M3U/M3U8 import** from URLs
- [x] **Drag-to-reorder** playlist items
- [x] **Playlist playback** with repeat/shuffle

### Network Tab
- [x] **SMB (Samba)** connections
- [x] **FTP** connections
- [x] **WebDAV** connections
- [x] **Connection management** — add, edit, delete
- [x] **Anonymous authentication** support
- [x] **Auto-connect** on app launch
- [x] **Network thumbnails** (experimental)
- [x] **HTTPS** toggle for WebDAV

---

## 10. ⚙️ Settings & Preferences

### Appearance
- [x] Theme picker with preview
- [x] AMOLED mode toggle
- [x] Material You dynamic color toggle
- [x] File browser settings (view mode, sort, hidden files)
- [x] Network thumbnails toggle
- [x] Unplayed video labels toggle
- [x] Auto-scroll to last played toggle
- [x] Watched threshold slider
- [x] Player button background style
- [x] Full file names toggle

### Player
- [x] Default orientation
- [x] Gesture configuration
- [x] Control layout editor
- [x] Display settings (show status bar, keep screen on)
- [x] Save position on exit
- [x] Close after EOF
- [x] Remember brightness
- [x] Autoplay next
- [x] Auto PiP

### Gestures
- [x] Double-tap left/center/right configuration
- [x] Single-tap center toggle
- [x] Media controls side swap
- [x] Thumbnail tap to select
- [x] Double-tap seek area width

### Player Controls
- [x] Seekbar style (Standard/Wavy/Thick)
- [x] Wavy seekbar toggle
- [x] Custom skip duration
- [x] Playlist view mode

### Decoder
- [x] MPV profile selection
- [x] Hardware decoding toggle
- [x] GPU-next renderer toggle
- [x] Vulkan renderer toggle
- [x] YUV420P pixel format toggle
- [x] Debanding selection
- [x] Anime4K mode + quality

### Subtitles
- [x] Online subtitle search toggle
- [x] Preferred subtitle languages
- [x] Custom fonts directory
- [x] Auto-load subtitles toggle
- [x] Subtitle save location
- [x] Clear downloaded subtitles

### Audio
- [x] Audio pitch correction toggle
- [x] Volume normalization toggle
- [x] Audio channels selection
- [x] Preferred audio language
- [x] Volume boost cap

### Advanced
- [x] **mpv.conf editor** — raw config file editing
- [x] **input.conf editor** — key binding configuration
- [x] **Lua scripts management** — enable/disable scripts
- [x] **Clear config cache**
- [x] **Clear thumbnail cache**
- [x] **Clear playback history**
- [x] **Clear font cache**
- [x] **Recently played** toggle
- [x] **Export/Import settings**
- [x] **Verbose logging** toggle
- [x] **Dump logs** option
- [x] **Settings search** — full searchable preferences

### Folders
- [x] Folder view settings
- [x] Scan directories configuration
- [x] Hidden files toggle

---

## 11. 🔧 Background & PiP

### Background Playback
- [x] **MediaSessionCompat** integration
- [x] **Foreground service** with `FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK`
- [x] **MediaStyle notification** with previous/play-pause/next
- [x] **Playback state** updates in notification
- [x] **Throttled notification** updates (1 second interval)
- [x] **Kill process** on task removal

### Picture-in-Picture
- [x] **Auto PiP** on home button press (configurable)
- [x] **Manual PiP** via button
- [x] **State preservation** across PiP enter/exit
- [x] **Minimal UI** in PiP mode

---

## 12. 🗃️ Database & Persistence

### Room Database Tables
- [x] **Playback State** — per-file position, speed, zoom, tracks, delays
- [x] **Recently Played** — watch history with metadata
- [x] **Video Metadata** — file metadata cache (size, duration, dimensions, fps)
- [x] **Network Connections** — saved SMB/FTP/WebDAV connections
- [x] **Playlists** — custom playlists + M3U source tracking
- [x] **Playlist Items** — playlist entries with FK cascade delete

### Migrations
- [x] **9 database versions** with squashed migrations

### Preferences
- [x] **SharedPreferences-backed** typed preference system
- [x] **Flow-based** preference changes (`collectAsState()`)
- [x] **Typed Preference<T>** wrappers with `get()`, `set()`, `changes()`

---

## 13. 🌐 Network & Permissions

### Permissions
- `INTERNET`
- `READ_MEDIA_VIDEO` (Android 13+)
- `READ_EXTERNAL_STORAGE` (maxSdk 32)
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK`
- `POST_NOTIFICATIONS`

### Network Protocols
- [x] **SMB** (Samba) via SMBJ
- [x] **FTP** via Commons Net
- [x] **WebDAV** via Sardine Android
- [x] **HTTP/HTTPS** via OkHttp
- [x] **Local HTTP proxy** via NanoHTTPD for streaming

---

## 14. 🎭 Animations & Transitions

### Navigation
- [x] **Slide + fade** transitions (220ms, FastOutSlowInEasing)
- [x] **Predictive back** with scale (0.9x, 220ms)

### Tab Transitions
- [x] **Material 3 Expressive** slide (48dm distance, 250ms, FastOutSlowInEasing)

### Player Controls
- [x] **Controls enter** — 100ms LinearOutSlowInEasing
- [x] **Controls exit** — 300ms FastOutSlowInEasing
- [x] **Seekbar wave animation** — continuous sine wave
- [x] **Seekbar flatten** — 550ms when paused
- [x] **Seekbar regrow** — 800ms on play
- [x] **Player button ripples** — custom ripple configuration
- [x] **Double-tap ovals** — left/right seek indicators
- [x] **Play/pause icon** — AnimatedVectorDrawable morphing
- [x] **Loading indicator** — Material3 buffering animation
- [x] **Marquee text** — `basicMarquee` for long titles

### Theme
- [x] **Circular reveal** transition with screenshot capture

---

## 15. 📊 Media Information

- [x] **MediaInfo viewer** — codec, resolution, bitrate, frame rate, audio channels, subtitle tracks
- [x] **Exportable** as text/share

---

## 16. 🛡️ Error Handling

- [x] **Custom crash handler** — catches uncaught exceptions
- [x] **Crash activity** — displays crash details, allows reporting
- [x] **Graceful degradation** — SW fallback for HW decoding
- [x] **Network error handling** — retry, reconnect logic

---

## Feature Priority Matrix

| Priority | Features |
|----------|----------|
| **P0 — Core** | Video playback, gestures, controls, seekbar, Material You theme |
| **P1 — Essential** | Subtitles, audio tracks, playlists, recents, file browser, preferences |
| **P2 — Advanced** | Network streaming, dual subtitles, online subtitle search, PiP, background playback |
| **P3 — Premium** | Anime4K, Lua scripts, M3U import, filter presets, 32 themes, control layout editor |
