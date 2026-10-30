# Dunia Ayah Fayyadh 0.2 — native Android TV preview

This is a native APK implementation, not the HTML design preview. Android TV / landscape, API 23+. Debug-signed **preview**, not stable; physical-TV checks are still required.

## Included

- Full-bleed static illustrations, real clock/date in Malaysia time, prayer widget and forecast widget, actual installed-app icons and launch intents.
- Papa Zola umbrella scene for rainy forecasts; playground with Boboiboy for dry/clear daytime forecasts; noodles by the window for thunderstorm forecasts; original darkened wallpaper with twinkling stars for dry night forecasts.
- Transparent native rain/star overlay; no video, camera zoom, moving characters or flashing-lightning effects. Effects pause when hidden and respect the in-app toggle plus system animator scale.
- Auto and manual wallpaper choices under **Pilihan > Wallpaper / cuaca**. Unknown/unavailable forecasts use the original image, not an invented weather condition. Manual choices never change or fabricate the weather data.
- Installed apps, favourites (long OK), drawer, TV settings and Home launcher selection. No Netflix/YouTube binaries are bundled. Icons shown depend on actual installed packages.

## Fixed location and data

User selected **Kinarut, Sabah**. JAKIM zone **SBH07** covers Papar. Forecast coverage is **Papar district, Ds548**, explicitly labelled, because the official feed did not return a Kinarut-named location. This is district forecast coverage, **not** an exact point observation.

- Prayer: `https://www.e-solat.gov.my/index.php?r=esolatApi/TakwimSolat&period=today&zone=SBH07`
- Forecast: `https://api.data.gov.my/weather/forecast`, filtered by location and Malaysia calendar date.
- Weather provider: METMalaysia through data.gov.my, attribution CC BY 4.0. Documentation: https://developer.data.gov.my/realtime-api/weather

Wallpaper follows the Malaysia-time forecast period (morning 06–11, afternoon 12–18, night 19–05). Thunder takes priority over rain; “Tiada hujan” is not classified as rain. The feed updates daily, **not the instant rain begins at the user's house**. Day/night boundaries are an app convention, not calculated sunset times.

Requests run off the UI thread with HTTPS and timeouts, at startup/resume and at most every 30 minutes while visible. Manual refresh is available. Prayer responses validate zone, date, server date and time format. Cached prayer times are accepted only for the same Malaysia date/zone. Forecast cache requires matching date/location and age under 24h; errors retain a labelled valid cache or show unavailable placeholders. No GPS, API secret, account or analytics is used.

## Reproduction and publication

JDK 17, Gradle 8.9, Android SDK 35 and build tools 34.0.0. Build:

```sh
gradle :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest
```

GitHub Actions on `feature/weather-wallpaper-v02` builds a versioned preview, runs Android TV API 28 instrumentation checks and publishes the APK under `apk/preview/` only if those checks pass. Check `BUILD.txt` for the source/run and actual test status; do not mistake a previous 0.1 APK for this 0.2 version. Screenshots are in `docs/screenshots/` after the emulator job.

The artwork is AI-edited unofficial character fan art; no endorsement or distribution licence is implied. Review rights before wider redistribution.
