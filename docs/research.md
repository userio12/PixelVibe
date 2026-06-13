# PixelVibe — Open Source Video Player Research

Research conducted 2026-06-11. Top open-source Android video player repositories analyzed for tech stack, features, and architecture patterns.

---

## Players Analyzed

| Player | Stars | Engine | UI | Database | Tests | Modular |
|---|---|---|---|---|---|---|
| [Next Player](https://github.com/anilbeesetti/nextplayer) | 3.8k | Media3/ExoPlayer | Compose M3 | Room | No | No |
| [Just Player](https://github.com/moneytoo/Player) | 2.6k | Media3/ExoPlayer | XML | None | No | No |
| [Asuka Player](https://github.com/qianmokano/Asukaplayer) | new | Media3/ExoPlayer | Compose M3 | Room | Yes | Yes (10 modules) |
| [NekoVideo](https://github.com/FellipitoPV/NekoVideo) | 12 | Media3/ExoPlayer | Compose M3 | Room | No | No |
| [LitPlayer](https://github.com/daluobo/LitPlayer-release) | 68 | Media3/ExoPlayer | Compose M3 | Room | No | No |
| [mpvRex](https://github.com/sfsakhawat999/mpvRex) | 71 | libmpv | Compose M3 | Room | No | No |
| [SHS Player](https://github.com/hamsazzad/SHS-Player) | 2 | Media3+FFmpeg | Compose M3 | Room | No | No |

---

## Key Findings

### 1. Media3/ExoPlayer is the industry standard

6 of 7 players use Media3. The only exception is mpvRex (libmpv) chosen for its scripting capabilities. Media3 has first-party support for:

- HLS, DASH, SmoothStreaming adaptive streaming
- PiP (Picture-in-Picture)
- Chromecast via `media3-cast`
- DRM (Widevine)
- Compose integration via `AndroidView` wrapping `PlayerView`
- FFmpeg extension for software codec fallback

### 2. No existing player has all planned PixelVibe features

| Feature gap | Present in any OSS player? |
|---|---|
| Chromecast | **No** (Media3 supports it, no player implements it) |
| Equalizer with presets | **No** |
| Full metadata scraping (TMDB) | **No** |
| Comprehensive test suite | **No** (only Asuka has partial tests) |

Every planned PixelVibe feature exists in at least one OSS player, but no player combines them all.

### 3. Modular architecture is rare

Only **Asuka Player** uses true multi-module structure (10 modules with build-time boundary checks). Its architecture is the closest reference to PixelVibe's design — same tech stack, same module philosophy. The author states it is designed as a "maintainable architecture exercise."

**Adopt from Asuka:** Playback engine isolated behind contract interfaces, Room-backed media index, async persistence for playback state.

### 4. Testing is almost nonexistent in OSS

Zero popular players have meaningful test suites. Asuka Player is the only one with "regression-oriented JVM, Robolectric, and Compose test coverage." This is PixelVibe's competitive advantage — production-quality testing from day one.

### 5. Network features fragment across players

| Feature | Which players have it |
|---|---|
| SMB | mpvRex, LitPlayer |
| FTP | mpvRex, LitPlayer |
| WebDAV | mpvRex, LitPlayer |
| DLNA/UPnP | NekoVideo, LitPlayer |
| Jellyfin | LitPlayer |
| Chromecast | **None** |

### 6. Tech stack consensus

| Layer | Consensus choice | Used by |
|---|---|---|
| Player | Media3 1.9.x | Next, Asuka, NekoVideo, SHS |
| UI | Jetpack Compose + Material 3 | Next, Asuka, NekoVideo, LitPlayer, mpvRex, SHS |
| Database | Room | Next, Asuka, NekoVideo, LitPlayer, SHS |
| Preferences | DataStore | Next, Asuka |
| Image loading | Coil | Next, Asuka |
| Subtitle parsing | SRT, ASS, SSA, VTT | All players |
| FFmpeg | Optional extension | Just Player, Next Player, SHS |
| DI | Manual or Hilt | No Koin in major players, but viable |

---

## Competitive Positioning

| App | Position |
|---|---|
| **VLC** | Full desktop-grade features, libVLC engine, heavy, dated UI |
| **MX Player** | Proprietary, ads, best codec support via custom players |
| **Just Player** | Minimalist, no library, single-purpose player |
| **Next Player** | Best modern OSS, Compose M3, but no network features |
| **Nova Video Player** | Material Design, no Compose, Android TV focus |
| **PixelVibe (planned)** | **Only app combining: Compose M3 + modular architecture + tests + network streaming + Chromecast + equalizer + metadata scraping** |

---

## References

- Next Player: https://github.com/anilbeesetti/nextplayer
- Just Player: https://github.com/moneytoo/Player
- Asuka Player: https://github.com/qianmokano/Asukaplayer
- NekoVideo: https://github.com/FellipitoPV/NekoVideo
- LitPlayer: https://github.com/daluobo/LitPlayer-release
- mpvRex: https://github.com/sfsakhawat999/mpvRex
- SHS Player: https://github.com/hamsazzad/SHS-Player
- AndroidX Media3: https://github.com/androidx/media
- Media3 docs: https://developer.android.com/media/media3
