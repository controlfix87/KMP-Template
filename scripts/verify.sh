#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 scripts/check_project.py
python3 -m unittest discover -s scripts/tests -v
# Use the machine's serialized build queue when available; portable CI uses the wrapper.
if command -v gw >/dev/null 2>&1; then
    gradle_runner=(gw)
elif [[ -x "$HOME/AgentsHub/bin/gw" ]]; then
    gradle_runner=("$HOME/AgentsHub/bin/gw")
else
    gradle_runner=(./gradlew)
fi
"${gradle_runner[@]}" :common:core:model:allTests :common:core:common:allTests testAndroidHostTest :androidApp:testDebugUnitTest :androidApp:lintDebug :androidApp:assembleDebug :androidApp:assembleDebugAndroidTest "$@"
