# PixelVibe — Project Architecture Tree View

```
pixelvibe/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradle/
│   └── libs.versions.toml
├── AGENTS.md
├── GUIDE.md
├── docs/
│   ├── todo.md
│   ├── project-arch-tree-view.md
│   ├── research.md
│   └── app-features.md
│
├── app/
│   └── src/main/kotlin/com/pixelvibe/vedioplayer/
│       ├── PixelVibeApp.kt              ← Application (startKoin)
│       ├── MainActivity.kt              ← Single activity, edge-to-edge
│       └── navigation/
│           └── PixelVibeNavGraph.kt     ← NavHost wiring all routes
│
├── core/
│   ├── common/
│   │   └── src/main/kotlin/com/pixelvibe/vedioplayer/core/common/
│   │       ├── result/
│   │       │   ├── Result.kt
│   │       │   ├── DataError.kt
│   │       │   └── UiText.kt
│   │       ├── route/
│   │       │   └── Routes.kt
│   │       └── util/
│   │           └── Extensions.kt
│   │
│   ├── data/
│   │   └── src/main/kotlin/com/pixelvibe/vedioplayer/core/data/
│   │       ├── db/
│   │       │   ├── AppDatabase.kt
│   │       │   ├── dao/
│   │       │   │   ├── VideoDao.kt
│   │       │   │   ├── PlaylistDao.kt
│   │       │   │   └── HistoryDao.kt
│   │       │   └── entity/
│   │       │       ├── VideoEntity.kt
│   │       │       ├── PlaylistEntity.kt
│   │       │       └── HistoryEntity.kt
│   │       ├── repository/
│   │       │   └── VideoRepository.kt
│   │       ├── scanner/
│   │       │   └── MediaScanner.kt
│   │       └── di/
│   │           └── DataModule.kt
│   │
│   ├── player/
│   │   └── src/main/kotlin/com/pixelvibe/vedioplayer/core/player/
│   │       ├── engine/
│   │       │   └── PlaybackEngine.kt
│   │       ├── controller/
│   │       │   └── PlayerController.kt
│   │       ├── subtitle/
│   │       │   ├── SubtitleManager.kt
│   │       │   └── SubtitleParser.kt
│   │       ├── audio/
│   │       │   └── AudioEffectManager.kt
│   │       ├── pip/
│   │       │   └── PipHandler.kt
│   │       └── di/
│   │           └── PlayerModule.kt
│   │
│   └── ui/
│       └── src/main/kotlin/com/pixelvibe/vedioplayer/core/ui/
│           ├── theme/
│           │   ├── Theme.kt
│           │   ├── Color.kt
│           │   ├── Type.kt
│           │   └── Shape.kt
│           ├── component/
│           │   ├── VideoCard.kt
│           │   ├── LoadingIndicator.kt
│           │   └── ErrorView.kt
│           └── di/
│               └── UiModule.kt
│
└── feature/
    ├── home/
    │   └── src/main/kotlin/com/pixelvibe/vedioplayer/feature/home/
    │       ├── HomeScreen.kt
    │       ├── HomeViewModel.kt
    │       ├── component/
    │       │   ├── VideoGrid.kt
    │       │   ├── FolderTab.kt
    │       │   └── CategoryTab.kt
    │       └── di/
    │           └── HomeFeatureModule.kt
    │
    ├── recent/
    │   └── src/main/kotlin/com/pixelvibe/vedioplayer/feature/recent/
    │       ├── RecentScreen.kt
    │       ├── RecentViewModel.kt
    │       ├── component/
    │       │   └── HistoryItem.kt
    │       └── di/
    │           └── RecentFeatureModule.kt
    │
    ├── network/
    │   └── src/main/kotlin/com/pixelvibe/vedioplayer/feature/network/
    │       ├── NetworkScreen.kt
    │       ├── NetworkViewModel.kt
    │       ├── browser/
    │       │   ├── SmbBrowser.kt
    │       │   ├── FtpBrowser.kt
    │       │   └── WebDavBrowser.kt
    │       ├── dlna/
    │       │   └── DlnaRenderer.kt
    │       ├── cast/
    │       │   └── ChromecastHandler.kt
    │       └── di/
    │           └── NetworkFeatureModule.kt
    │
    ├── player/
    │   └── src/main/kotlin/com/pixelvibe/vedioplayer/feature/player/
    │       ├── PlayerScreen.kt
    │       ├── PlayerViewModel.kt
    │       ├── overlay/
    │       │   ├── ControlsOverlay.kt
    │       │   ├── GestureOverlay.kt
    │       │   └── SpeedSelectorSheet.kt
    │       └── di/
    │           └── PlayerFeatureModule.kt
    │
    └── settings/
        └── src/main/kotlin/com/pixelvibe/vedioplayer/feature/settings/
            ├── SettingsScreen.kt
            ├── SettingsViewModel.kt
            ├── equalizer/
            │   └── EqualizerSheet.kt
            ├── security/
            │   └── AppLockScreen.kt
            └── di/
                └── SettingsFeatureModule.kt
```

---

## Dependency Graph

```
app → core:common, core:data, core:player, core:ui
app → feature:home, feature:recent, feature:network, feature:player, feature:settings

feature:home      → core:common, core:data, core:ui
feature:recent    → core:common, core:data, core:ui
feature:network   → core:common, core:data, core:player, core:ui
feature:player    → core:common, core:data, core:player, core:ui
feature:settings  → core:common, core:data, core:player, core:ui

core:data   → core:common
core:player → core:common
core:ui     → core:common
```

**Rule:** No `:feature:*` depends on another `:feature:*`. Cross-feature navigation uses route objects in `core:common`.
