# 0.1.0 preview

**Debug-signed, unofficial fan-themed technical preview. Not a stable release.**

## Included

- Colourful Android TV home with an AI-generated, unofficial cartoon banner.
- Device clock/date, installed app grid and app launching.
- Favourites stored locally; hold OK or press Menu on an app card to edit.
- Remote-friendly focus, device settings and Home settings shortcuts.
- Background colour-breathing and focus-scale animations, plus a motion-off option. The character illustration itself is static.
- No network permission, ads, accounts or analytics.

## Validation

Build, unit tests, lint and API 28 1080p Android TV emulator smoke checks passed. See `docs/VALIDATION.md` for exact scope and remaining checks. No physical-device stability claim is made.

Source: `3d13224d3d50a72ea4ca7197c3f8b3002786a616`.

APK SHA-256: `1af6ff9290b8d801b0c12d7d6fe8a02888d032040f643911d074eae01a62f4c3`.

Keep your stock launcher installed. Open and test this app before selecting it as Home, and only do so if the device permits it. CI debug signing keys may change; updating from another preview can require uninstalling the previous one, which clears its favourites. A private, consistent release signing key will be required for stable updates.
