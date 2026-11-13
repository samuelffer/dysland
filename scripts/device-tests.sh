#!/usr/bin/env bash
set +e
set -o pipefail
./gradlew --no-daemon connectedDebugAndroidTest 2>&1 | tee /tmp/device-tests.log
code=${PIPESTATUS[0]}
if [ "$code" -ne 0 ]; then
  adb logcat -d -s AndroidRuntime:E >> /tmp/device-tests.log
  python3 -c 'from pathlib import Path; s="\n".join(Path("/tmp/device-tests.log").read_text().splitlines()[-160:]); print("::error::"+s.replace("%","%25").replace("\n","%0A").replace("\r","%0D"))'
  exit "$code"
fi
mkdir -p screenshots
adb pull /sdcard/Android/data/com.dysland.app/files/dysland-home.png screenshots/dysland-home.png
