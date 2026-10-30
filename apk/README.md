# Stable APK downloads

No APK is available yet. This folder is reserved for verified stable builds.

Before publishing a stable APK:

- Build successfully and pass automated checks.
- Smoke-test cold start, D-pad navigation, app launching, Back behaviour, launcher selection and settings access.
- Verify performance and animation behaviour on the target Android TV device.
- Include version, minimum Android version, source commit, test environment and known issues in release notes.
- Include a SHA-256 checksum alongside the APK.

Do not label untested or debug builds as stable. Keep a consistent release signing key for updates; never commit keystores, signing passwords or other secrets.
