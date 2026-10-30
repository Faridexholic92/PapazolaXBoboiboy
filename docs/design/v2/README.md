# Cinematic v2 — design approval preview

The requested on-screen title is now **Dunia Ayah Fayyadh**.

This replaces the **visual direction** of the v1 boxed dashboard. It follows the user-supplied FayyadhOS reference: full-screen cinematic 3D character wallpaper, dark Kinabalu/sunset scenery, a slim top information band and a floating bottom icon dock with cyan focus.

**This is a browser-based visual prototype, NOT an Android APK.** The Android source and APK on `main` have not yet been redesigned. This branch is for visual review before porting the approved layout into the native launcher.

## View

Download this folder including `assets/papazola-boboiboy-atmosphere-loop.mp4`, then open `index.html`. The still background and SVG icons are embedded; the MP4 is a relative local asset. No API calls are made by the visual preview. A missing/failed video falls back to the still background.

- Target composition: 1920×1080 and 1280×720 landscape.
- Left/Right: move dock focus; Home/End: first/last item.
- Enter/click: show an explicit design-only notice, not launch an Android app.
- Escape: dismiss the notice.
- The silent atmospheric MP4, decorative particles and focus transitions respect `prefers-reduced-motion`. Video also pauses when the page is hidden.
- A readable, vertically scrolling 390 px layout is provided for reviewing the prototype on a phone; this does not turn the Android app into a phone launcher.

## Important boundaries

- Prayer and weather values are now **unconfigured placeholders**, not demo numbers. Source endpoints have been verified, but native wiring and user location selection are pending. Time/date, Jarvis status and birthday content are still visual reference/demo values, not live services or verified personal facts. See `API-CONTRACT.md`.
- `FayyadhOS`, `Fayyadh`, `5 tahun` and `Kinarut, Sabah` are copied from the supplied visual reference for review. They are not treated as confirmed biographical information.
- The character artwork is an AI-edited fan-theme background, not an official asset or endorsement. Check rights before distribution.
- The new MP4 loops camera/particle motion over static character artwork. It is not a full-motion running-character animation. See `VIDEO.md` for the exact capability boundary.
- Dock icons illustrate the intended layout. The Android implementation must use installed apps and clearly handle missing apps, not imply that YouTube/Netflix are bundled.

## Native Android implementation after approval

1. Replace the boxed `buildHome()` layout with a full-screen wallpaper layer, independently rendered widgets and a bottom dock.
2. Keep labels, clock, focus and widgets as genuine UI, never bake them into the wallpaper.
3. Preserve installed-app discovery, app launch, favourites, settings access, Home behaviour and reduced-motion support.
4. Use the verified JAKIM and METMalaysia/data.gov.my contracts for prayer/weather, with confirmed zone/location, date validation, cache/error states and proper forecast labels. Jarvis remains unavailable until its capability is implemented; do not ship sample times as real information.
5. Make birthday text configurable/optional rather than assuming the reference content is the user's own data.
6. Build a newly versioned preview APK, run native emulator checks and inspect actual Android screenshots before publishing it. Physical-TV checks are still required before any stable designation.

## QA performed

- Rendered and individually inspected TV 1920×1080, reference-size 1280×720, phone review 390×1140, settings-icon focus and the design-only notice.
- No horizontal page overflow or missing resources in tested captures.
- Tested initial focus, Left/Right navigation, focus wrap, End, Enter notice, Escape dismissal, ambient animation and reduced-motion stop using Playwright.

`qa.js` requires Node, Playwright and a locally installed Chromium. `CHROMIUM_PATH` can override Chromium discovery. Run `node qa.js` from this folder.

The wallpaper can be extracted from the standalone HTML with `python extract-wallpaper.py`, for later asset preparation without embedding UI text in it.
