#!/usr/bin/env bash
# The emulator action stops its device when the script ends, so collect evidence here.
set -uo pipefail
gradle --no-daemon :app:connectedDebugAndroidTest 2>&1 | tee smoke.log
result=$?
mkdir -p screenshots
adb pull /sdcard/Download/papazola/. screenshots/ >> smoke.log 2>&1 || true
if [ "$result" -eq 0 ] && [ ! -f screenshots/home-preview.png ]; then
  echo "Required UI screenshot missing after successful tests" | tee -a smoke.log
  result=1
fi
adb logcat -d -s AndroidRuntime WeatherData > runtime.log || true
exit "$result"
