# Validation and release checklist

## Automated

- `gradle :app:testDebugUnitTest`: ordering, empty input, favourites add/remove without mutation.
- `gradle :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest`: static analysis and builds.
- `gradle :app:connectedDebugAndroidTest`: startup, initial focus, favourites tab, Back, motion toggle, screenshot.
- CI requests an API 28 Android TV emulator, 1920×1080 at 320 dpi (960×540 dp).
- Download `emulator-smoke-results` and visually inspect `home-preview.png` before claiming visual QA passed.

## Physical TV gate (not yet performed)

- Record device model, Android version, resolution, overscan settings and remote type.
- Confirm every nav action and every app card is reachable with D-pad, including more than one screen of apps.
- Check default focus, long-press OK/Menu, cancellation, empty favourites and removing last favourite.
- Launch a TV app, return Home, uninstall an app, resume launcher and check state.
- Reboot and confirm Home behaviour only after manually choosing the launcher where supported.
- Keep stock launcher installed; confirm settings and rollback work.
- Confirm no clipping at 720p/1080p/4K with typical TV display scaling.
- Toggle animations, set system animator scale to zero and verify input remains responsive.
- Check 30-minute idle use and repeated launches for crashes, excessive CPU or memory use.

## Known preview limits

AI-generated, unofficial fan-themed artwork; not official character assets. No rearranging tiles, custom wallpaper, search or app hiding yet. Back returns to the all-apps tab rather than exiting Home. CI debug signatures are not a stable update channel. API 23 is the declared minimum, not a guarantee of tested compatibility. OEM Home restrictions are respected.
