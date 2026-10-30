#!/usr/bin/env bash
# The emulator action stops its device when the script ends, so collect evidence here.
set -uo pipefail
gradle --no-daemon :app:connectedDebugAndroidTest 2>&1 | tee smoke.log
result=$?
mkdir -p screenshots
adb pull /sdcard/Android/data/my.papazola.tv/files/. screenshots/ || true
adb logcat -d -s AndroidRuntime > runtime.log || true
exit "$result"
