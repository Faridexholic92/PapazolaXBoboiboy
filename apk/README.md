# Stable APK downloads

**No stable APK is available yet.** This folder is reserved for verified stable builds.

After build and emulator smoke checks pass, an early **debug-signed preview APK** is published under `apk/preview/`, together with its checksum and source commit. Actions artifacts are also available (sign-in required). These previews are not stable releases. Stable releases will later be stored separately in this folder after physical-device validation.

Before publishing a stable APK:

- Build successfully and pass unit tests, lint and emulator smoke checks.
- Test cold start, physical D-pad navigation, app launching, Back, launcher selection and settings access on the target TV.
- Confirm animations do not block input or cause crashes.
- Record version, minimum Android version, source commit, test device and known issues.
- Publish a SHA-256 checksum and release notes with the APK.
- Use a consistent private release signing key for updates. Never commit keystores, signing passwords or other secrets.

The target TV model and Android version have not yet been supplied; no device-specific compatibility or stability is claimed.
