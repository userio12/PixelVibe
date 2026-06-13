# PixelVibe

## Project

- Multi-module Android app, Jetpack Compose + Material3, Kotlin 2.1.0, AGP 8.13.0, Compose BOM 2025.10.01
- Package name: `com.pixelvibe.vedioplayer` — note the **typo** ("vedio"), match it in all new files
- Min SDK 28 / Target SDK 34 / Compile SDK 36, Java 17 source/target

## Tech Stack

| Layer | Choice | Version |
|---|---|---|
| Player engine | Media3 (ExoPlayer) | 1.9.3 |
| UI | Jetpack Compose + Material 3 | BOM 2025.10.01 |
| Architecture | MVI (State/Action/Event) | — |
| DI | Koin | 4.0.2 |
| Database | Room | 2.7.1 |
| Preferences | DataStore | 1.1.3 |
| Image loading | Coil 3 | 3.1.0 |
| Navigation | Compose Navigation (type-safe) | 2.8.9 |
| Networking | OkHttp | 4.12.0 |
| Testing | JUnit5 + Turbine + AssertK | 1.2.0 / 0.28.1 |

## Build

- Gradle version catalog at `gradle/libs.versions.toml` — declare all new deps there first
- `org.gradle.configureondemand=false` — Gradle always evaluates all modules
- `android.nonTransitiveRClass=true` — R classes are per-module, not transitive
- Build: `./gradlew assembleDebug`
- Clean: `./gradlew clean`
- No lint or typecheck commands exist

## Modules (8)

```
:pixelvibe:app                  → PixelVibeApp, MainActivity, NavHost
:pixelvibe:core:common          → Result<DataError>, UiText, route objects, extensions
:pixelvibe:core:data            → AppDatabase, DAOs, VideoRepository, MediaScanner
:pixelvibe:core:player          → PlaybackEngine, PlayerController, Subtitles, AudioFX, PiP
:pixelvibe:core:ui              → Theme, VideoCard, LoadingIndicator, ErrorView

:pixelvibe:feature:home         → HomeScreen (grid + folder/favorites/IPTV tabs + search)
:pixelvibe:feature:recent       → RecentScreen (history list + resume)
:pixelvibe:feature:network      → NetworkScreen (SMB/FTP/WebDAV + DLNA/UPnP + Chromecast)
:pixelvibe:feature:player       → PlayerScreen (controls, gestures, PiP, A-B loop, sleep timer, frame step)
:pixelvibe:feature:settings     → SettingsScreen (prefs, equalizer, about, app lock, backup)
```

**Dependency rule:** `:feature:*` → `:core:*` only. No feature-to-feature imports. Cross-feature navigation uses route objects in `:core:common`.

## Bottom Navigation (4 items)

Home · Recent · Network · Settings

## Screens (5 total)

Home, Recent, Network, Settings (bottom nav) + Player (fullscreen). AppLock is a pre-nav gate.

## Standards (no exceptions)

- **No mock data** — real `MediaStore` scanning
- **Every screen** handles loading / empty / error + retry
- **Every ViewModel** survives process death via `SavedStateHandle`
- **Every ViewModel + Repository** has unit tests (Turbine + AssertK + in-memory Room)
- **Every error** uses `Result<T, DataError>` + `UiText`
- **Implementation order:** Phase 1a → 1b → 2 → 3 → 4 → 5 (see `docs/todo.md`)

## Manifest

- Declares unused `FOREGROUND_SERVICE_DATA_SYNC` permission (legacy)
- Not a git repo
