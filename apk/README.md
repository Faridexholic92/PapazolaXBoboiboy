# Stable APK downloads

**No stable APK is available yet.** This folder is reserved for verified stable builds.

Early **debug-signed preview APKs** are downloadable from the repository's GitHub Actions build artifacts (sign-in required). These are not stable builds and are not stored here.

Before publishing a stable APK:

- Build successfully and pass unit tests, lint and emulator smoke checks.
- Test cold start, physical D-pad navigation, app launching, Back, launcher selection and settings access on the target TV.
- Confirm animations do not block input or cause crashes.
- Record version, minimum Android version, source commit, test device and known issues.
- Publish a SHA-256 checksum and release notes with the APK.
- Use a consistent private release signing key for updates. Never commit keystores, signing passwords or other secrets.

The target TV model and Android version have not yet been supplied; no device-specific compatibility or stability is claimed.
