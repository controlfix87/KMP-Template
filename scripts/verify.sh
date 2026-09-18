#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 scripts/check_project.py
python3 -m unittest discover -s scripts/tests -v
./gradlew :common:core:model:allTests :common:core:common:allTests testAndroidHostTest :androidApp:testDebugUnitTest :androidApp:lintDebug :androidApp:assembleDebug :androidApp:assembleDebugAndroidTest "$@"
