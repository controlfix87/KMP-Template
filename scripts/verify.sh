#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 scripts/check_project.py
python3 -m unittest discover -s scripts/tests -v
# Pure common tests, KMP Android host tests, desktop (JVM) tests incl. desktop screenshots, Android
# unit + Robolectric screenshot tests, lint, and app/test APK compilation. Screenshot tests only
# draw here; record/compare with :androidApp:recordRoborazziDebug / verifyRoborazziDebug and
# :desktopApp:recordRoborazziDesktop / verifyRoborazziDesktop (docs/TESTING.md).
./gradlew :common:core:model:allTests :common:core:common:allTests testAndroidHostTest desktopTest \
  :androidApp:testDebugUnitTest :androidApp:lintDebug :androidApp:assembleDebug :androidApp:assembleDebugAndroidTest \
  :desktopApp:compileKotlinDesktop "$@"
