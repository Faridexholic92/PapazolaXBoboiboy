# Preview validation — 2026-10-30

Status: **0.1.0 debug-signed preview, not stable**.

- Application source: `3d13224d3d50a72ea4ca7197c3f8b3002786a616`.
- Successful workflow: https://github.com/Faridexholic92/PapazolaXBoboiboy/actions/runs/41000977497
- Build job passed APK compilation, three JUnit unit tests, Android lint and instrumentation APK compilation.
- API 28 Android TV emulator smoke job passed startup, initial focus, D-pad Right/OK, empty favourites, Back, options-menu OK and persisted motion-off preference.
- Emulator uses a real `tv_1080p` profile and a 1920×1080 framebuffer. Screenshot dimensions are enforced by the test.
- Individually inspected final screenshots: home, empty favourites, options menu and motion-off state. Labels were readable, controls did not overlap, and the focused options button border was fully visible after fixing clipping.
- Downloaded the published APK and verified its SHA-256 against `apk/preview/SHA256SUMS.txt`.

## Files

- APK, source/build metadata, checksum: `apk/preview/`.
- Actual emulator screenshots: `docs/screenshots/`.
- Earlier failure diagnostics under `docs/ci/` refer to the source commits and run IDs printed in those files. They are historical, not the outcome of the successful run above.

## Not validated yet

Physical TV compatibility, stock/Home launcher selection and rollback on OEM firmware, external-app launching on a real TV, many-app scrolling, long-press favourite editing on a physical remote, long idle performance, and other Android versions. The declared minimum API 23 is not a claim of tested Android 6 compatibility.

## Before stable

Obtain the target TV model and Android version, test the exact candidate APK on it, validate Home/settings recovery and performance, and establish a consistent private release signing key. Never label a debug or merely emulator-tested APK stable.
