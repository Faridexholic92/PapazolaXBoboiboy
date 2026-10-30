# Papazola × Boboiboy TV Launcher

Colourful, animated Android TV launcher. **0.1.0 technical preview — not a stable release.**

## Implemented in source

- Landscape TV home, clock and date, bright elemental colour palette.
- Installed app discovery (TV apps first), D-pad focus and app launching.
- Persistent favourites: hold OK or press Menu on an app card.
- Subtle animated background and focus transitions; motion toggle under **Pilihan**. System animator scale is respected.
- Device settings shortcut and Home settings shortcut with fallback. Default Home selection depends on device policy; no restrictions are bypassed.
- No internet permission, ads, account or analytics.

Original typography and geometric icon assets only. Official character illustrations are not included. This is a functional starting point, not completed character artwork.

## Build

Requires JDK 17, **Gradle 8.9**, Android SDK 35 and network access to Google/Maven repositories on first build.

```sh
gradle :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

This initial scaffold uses the exact Gradle version installed by CI rather than a checked-in wrapper. Kotlin + native Android Views keep the preview dependency-light; Compose for TV remains an option rather than a dependency.

GitHub Actions builds a **debug-signed preview APK**, checksum and build metadata as an artifact, then runs a separate API 28 Android TV emulator smoke job. A successful build alone does not mean the emulator job or physical-device testing passed. See each job's result.

## Install and recover safely

1. Download the preview artifact from a successful Actions build and extract it.
2. With your TV's developer options enabled, use `adb install -r PapazolaXBoboiboy-0.1.0-preview.apk` or your trusted sideload method.
3. Open **Papazola × Boboiboy TV** from the app list before selecting it as Home.
4. Try the remote, app launch, favourites and **Tetapan TV**. Keep your original launcher installed.
5. Only if supported, select this app through **Pilihan → Pilih Home launcher**.
6. To revert, use **Tetapan TV → Apps → Default apps → Home** (wording varies). If needed, `adb shell am start -a android.settings.SETTINGS` opens device settings. Never disable/uninstall the stock launcher to force this preview.

Some non-TV apps may not work with a remote. Discovery is limited to activities visible to Android's launcher queries. The app excludes itself.

## Delivery requirements

Every project change is committed here. Stable, device-validated APKs will be stored in [`apk/`](apk/README.md). No stable APK is available yet. Debug artifacts use disposable CI debug signing and may require uninstalling an earlier preview before installing a later one; uninstalling clears favourites. A consistent private release key is required before stable distribution.

## Validation

Unit tests cover case-insensitive app ordering and reversible favourites. Instrumentation smoke tests cover startup, initial focus, tabs, Back, motion toggle and screenshot capture. Physical remote navigation, external app launch, system Home handling, older/newer Android versions and performance still require real-device validation.
