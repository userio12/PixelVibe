# PixelVibe

## Project Overview

**PixelVibe** is a **feature-rich Android video player** built with **Jetpack Compose** and **Kotlin**, designed as an exact clone of the open-source [mpvExtended (mpvEx)](https://github.com/marlboro-advance/mpvEx) project. It features **Material You dynamic color**, **Material Expressive animations**, comprehensive **multi-touch gesture controls**, **extensive subtitle support** (28+ formats), **dual subtitles**, **network streaming** (SMB/FTP/WebDAV), **background playback**, and **Picture-in-Picture** mode.

The package namespace is `com.pixelvibe.vedioplayer` and the video playback engine is powered by **libmpv** via a custom AAR (`mpv-android-lib`).

### Key Technologies

- **Language:** Kotlin 2.1.0
- **UI Framework:** Jetpack Compose (Material 3) with ConstraintLayout Compose
- **Video Engine:** libmpv (custom AAR — mpv-android fork)
- **DI:** Koin 4.2.0
- **Database:** Room 2.8.4 with KSP
- **Build System:** Gradle (Kotlin DSL) with Version Catalog
- **Min SDK:** 24 (Android 7.0 Nougat)
- **Target SDK:** 34
- **Compile SDK:** 36
- **Java Compatibility:** Java 17

### Architecture

MVVM + Repository pattern with Koin DI and Flow-based state management:

```
PixelVibe/
├── app/src/main/kotlin/com/pixelvibe/vedioplayer/
│   ├── PixelVibeApp.kt              # Application class (Koin init)
│   ├── MainActivity.kt              # Main entry (4-tab NavHost)
│   ├── PlayerActivity.kt            # Player (MPVView + Compose overlay)
│   ├── MediaInfoActivity.kt         # Media metadata viewer
│   ├── MediaPlaybackService.kt      # Background playback service
│   ├── di/                          # Koin modules
│   ├── ui/
│   │   ├── theme/                   # Material You themes (32 themes)
│   │   ├── player/                  # Player UI
│   │   │   ├── controls/            # Gestures, seekbar, overlays
│   │   │   ├── sheets/              # Bottom modal sheets (12)
│   │   │   └── panels/              # Draggable side panels (5)
│   │   ├── browser/                 # File browser screens
│   │   └── preferences/             # Settings screens (14)
│   ├── presentation/                # Shared components
│   ├── preferences/                 # Typed preference system
│   ├── domain/                      # Business logic / use cases
│   ├── database/                    # Room (entities, DAOs, repos, migrations)
│   ├── repository/                  # Data repositories
│   ├── utils/                       # Utilities
│   └── network/                     # SMB/FTP/WebDAV clients + proxy
├── docs/
│   ├── research.md                  # Reference project analysis
│   ├── app-feature.md               # Complete feature list
│   ├── project-architecture.md      # Architecture documentation
│   ├── plan.md                      # 6-phase implementation plan
│   └── todo.md                      # Detailed task checklist
├── reference/mpvExtended/           # Cloned reference project
├── gradle/libs.versions.toml
├── build.gradle.kts
└── settings.gradle.kts
```

## Building and Running

### Prerequisites

- Android Studio (latest stable recommended)
- JDK 17
- Android SDK with API level 36
- MPV AAR library (extracted from reference project into `app/libs/`)

### Commands

```bash
# Build the project
./gradlew build

# Assemble debug APK
./gradlew assembleDebug

# Run on connected device
./gradlew installDebug

# Run tests
./gradlew test

# Clean build
./gradlew clean
```

## Development Conventions

- **Kotlin Code Style:** Official Kotlin conventions (`kotlin.code.style=official`)
- **AndroidX:** Enabled with non-transitive R class for smaller APK size
- **Compose-First:** UI is built entirely with Jetpack Compose (MPVView embedded via `AndroidView`)
- **Material 3:** Uses Material 3 design system with dynamic color support on Android 12+
- **Edge-to-Edge:** `enableEdgeToEdge()` is used for immersive UI
- **MVVM + Repository:** Clean separation of UI, domain, and data layers
- **Flow-based State:** All state flows through `StateFlow` → `collectAsState()` pattern
- **Koin DI:** All dependencies injected via Koin modules

## Dependencies

Managed via Gradle Version Catalog (`gradle/libs.versions.toml`):

### Core
- `androidx-core-ktx`, `androidx-lifecycle-runtime-ktx`, `androidx-activity-compose`
- `androidx-compose-bom`, `androidx-material3`, `androidx-ui` primitives
- `constraintlayout-compose` — Compose ConstraintLayout

### DI & Database
- `koin-bom` (4.2.0) — Dependency injection
- `room-runtime` + `room-ktx` (2.8.4) — Local database
- `kotlinx-serialization-json`, `kotlinx-collections-immutable`

### UI Components
- `accompanist-permissions` — Runtime permissions
- `compose.prefs` — Compose preferences library
- `seeker` — Custom seekbar with segments
- `lazy-column-scrollbar` — Scrollbar for lists
- `reorderable` — Drag-and-drop lists

### Networking
- `okhttp` (5.3.2) — HTTP/HTTPS
- `smbj` — SMB (Samba)
- `commons-net` — FTP
- `sardine-android` — WebDAV
- `nanohttpd` — Local HTTP proxy for network streaming

### Media
- `mpv-android-lib` (local AAR) — Video playback engine
- `androidx-media` (1.7.1) — MediaSession / MediaBrowserService
- `truetypeparser-light` — Font parsing for subtitles
- `mediainfoAndroid` — Media metadata extraction
- `FSAF` — Storage Access Framework replacement

## Key Features

| Category | Highlights |
|----------|-----------|
| **Playback** | HW decoding, GPU-next, Vulkan, precise seek, A-B loop, repeat/shuffle, speed 0.25x-4.0x, frame navigation |
| **Gestures** | 10 gesture types: double-tap, multi-tap, long-press, vertical/horizontal swipe, pinch-to-zoom, pan & zoom |
| **Subtitles** | 28+ formats, dual subtitles, online search, full typography control, ASS/SSA support |
| **Audio** | Track selection, volume boost, pitch correction, normalization, channel selection, delay |
| **Video** | 13 filter presets, debanding, Anime4K, zoom, aspect ratio, mirroring, flip |
| **UI** | 32 themes, Material You, AMOLED mode, wavy seekbar animation, expressive transitions |
| **Network** | SMB, FTP, WebDAV with local HTTP proxy streaming |
| **Background** | MediaSession service, PiP mode, notification controls |
| **Browser** | File browser, recents, playlists (M3U import), network connections |
| **Settings** | 14 preference screens, settings search, config editors, export/import |

## Notes

- The project is a **6-phase implementation** documented in `docs/plan.md` and `docs/todo.md`
- Full reference project analysis is in `docs/research.md`
- Complete architecture is in `docs/project-architecture.md`
- Feature checklist is in `docs/app-feature.md`
- The MPV AAR (`mpv-android-lib-v0.0.1.aar`) must be extracted from the reference project
- Supports **3 product flavors**: standard, playstore, fdroid (optional)
- Supports **ABI splits**: armeabi-v7a, arm64-v8a, x86, x86_64, universal
