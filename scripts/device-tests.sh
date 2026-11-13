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
# AGP's unified test platform may uninstall the target after instrumentation,
# deleting its external files. Reinstall explicitly and capture the real UI via adb.
set -e
mkdir -p screenshots
# The Pixel launcher can ANR under CI load even after tests have passed.
# Stop that unrelated background app before capturing, and reject any error dialog.
adb shell am force-stop com.google.android.apps.nexuslauncher
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell appops set com.dysland.app SYSTEM_ALERT_WINDOW allow
adb shell pm grant com.dysland.app android.permission.POST_NOTIFICATIONS
adb shell am start -W -n com.dysland.app/.MainActivity
sleep 2
# UIAutomator waits for idle and is unsuitable for an endlessly rotating preview.
# Check the actual focused window instead; an ANR dialog would own focus.
adb shell dumpsys window > /tmp/dysland-window.txt
python3 -c 'from pathlib import Path; s=Path("/tmp/dysland-window.txt").read_text(); focus=[line for line in s.splitlines() if "mCurrentFocus=" in line]; assert focus and "com.dysland.app" in focus[0], "Screenshot blocked by another window: "+str(focus)'
adb exec-out screencap -p > screenshots/dysland-home.png
python3 -c 'from pathlib import Path; p=Path("screenshots/dysland-home.png"); assert p.read_bytes().startswith(b"\x89PNG\r\n\x1a\n")'
