# Validation report

Evidence gathered on this ARM64 Linux host, JDK 21, Android SDK platform 37, on
2026-09-17. This records what was actually executed, not what compiled or what a
script claims. See `TESTING.md` for the full required matrix and `TASKS.md` for
task status.

## Executed and passing

| Check | Command | Result |
|---|---|---|
| Static project checks (purity, localization, module boundaries, manifest) | `python3 scripts/check_project.py` | Pass |
| Generator/checker unit tests | `python3 -m unittest discover -s scripts/tests -v` | 6 tests, pass |
| Shared/common tests | `./gradlew :core:model:allTests :core:common:allTests` | Pass |
| Feature ViewModel/HTTP/DI host tests | `./gradlew testAndroidHostTest` | Pass |
| Android app unit tests (Koin module resolution) | `./gradlew :androidApp:testDebugUnitTest` | Pass |
| Lint | `./gradlew :androidApp:lintDebug` | 1 informational warning only (`DataExtractionRules` attribute is a no-op below API 31, by design since minSdk is 26); no actionable findings |
| Debug APK + instrumentation APK assembly | `./gradlew :androidApp:assembleDebug :androidApp:assembleDebugAndroidTest` | Pass |
| Release build with R8 + resource shrinking | `./gradlew :androidApp:assembleRelease` | Pass, unsigned release APK produced |
| 16 KB page / ZIP alignment check | `python3 scripts/check_apk.py androidApp/build/outputs/apk/debug/androidApp-debug.apk` | Pass, 2 native 64-bit libraries checked |
| Renamed-project generation | `python3 scripts/new_project.py --name SmokeApp --package com.example.smokeapp --destination <tmp>` | Generated project has no leftover `kmptemplate`/`KMPTemplate` identifiers outside the generator script/tests themselves |
| Renamed-project full verification gate | `./scripts/verify.sh` in the generated `SmokeApp` copy | Pass end-to-end (static checks, generator tests, Gradle unit tests, lint, debug + androidTest APK assembly), then deleted |

Two real bugs were found and fixed while running the above, not by source review alone:

- `androidApp/build.gradle.kts` mipmap-anydpi adaptive icon appeared to be missing from
  AAPT (`resource mipmap/ic_launcher ... not found`) on a fresh `processDebugResources`
  run; a stale local Gradle build-cache entry from before the icon files were added was
  the actual cause. Clearing `androidApp/build` and the build cache and rerunning with
  `--rerun-tasks` fixed it; no source change was needed. Kept here because it looked
  like a resource-authoring bug until reproduced with a clean cache.
- `androidApp/src/main/kotlin/com/kmptemplate/app/di/AppModule.kt` used Koin's `onClose`
  infix without importing `org.koin.dsl.onClose`, which failed `compileDebugKotlin`
  with unresolved-reference errors. Fixed by adding the import.

## Not executed here (explicitly external validation)

- **TPL-013 — Android instrumentation on real hardware/emulators (API 26/35/37):** no
  device or emulator is attached to this host. `assembleDebugAndroidTest` compiling is
  evidence of compilation only, not of `connectedDebugAndroidTest` passing. Real phone
  rotation, foldable/tablet, process-death (`adb shell am kill`), IME, font-scale, and
  RTL checks from `TESTING.md`'s required release matrix are unexecuted.
- **TPL-014 — Apple platform build/tests:** this host is ARM64 Linux, not macOS. Darwin
  Kotlin/Native compilation, `scripts/ios-project.sh` (`xcodegen generate`), the Xcode
  build of `iosApp`, and an iOS Simulator search/detail/rotation journey are unexecuted.
  macOS CI is defined in `.github/workflows/ci.yml` but its actual run is not evidence
  gathered on this host.

Do not infer either of the above passed from a green Gradle build, a compiled test APK,
or a workflow YAML file existing. They require the stated hardware/OS and are separate
acceptance gates from this report.
