#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../iosApp"
if ! command -v xcodegen >/dev/null; then
    echo 'Install XcodeGen on macOS with: brew install xcodegen' >&2
    exit 1
fi
xcodegen generate --spec project.yml
