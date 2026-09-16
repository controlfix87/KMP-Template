#!/usr/bin/env bash
# Fails if commonMain (or commonTest) imports an Android- or JVM-only type.
# Run locally with ./scripts/check_common_main_purity.sh; CI runs this before
# any Gradle task so a violation fails in seconds, not after a full build.
set -euo pipefail

pattern='^import (android\.|androidx\.(?!compose|lifecycle|navigation)|java\.time|java\.util\.UUID|kotlinx\.coroutines\.Dispatchers$)'

violations=$(grep -RnE --include='*.kt' -P "$pattern" -- */src/commonMain */src/commonTest 2>/dev/null || true)

if [[ -n "$violations" ]]; then
    echo "commonMain/commonTest must stay platform-neutral. Found:"
    echo "$violations"
    echo
    echo "Put the platform-bound piece behind expect/actual instead (see core/network's"
    echo "HttpClientFactory.kt / HttpClientFactory.android.kt for the pattern), or inject"
    echo "a DispatcherProvider (core/common) instead of referencing kotlinx.coroutines.Dispatchers directly."
    exit 1
fi

echo "commonMain/commonTest: no Android/JVM-only types found."
