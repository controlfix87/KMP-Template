# Validation report

Evidence gathered on this ARM64 Linux host, JDK 21, Android SDK platform 37, on
2026-09-17. This records what was actually executed, not what compiled or what a
script claims. See `TESTING.md` for the full required matrix and `TASKS.md` for
task status.

## Clean-branch reconciliation, 2026-09-18

The template was rechecked from a clean branch created from `main` at commit
`8f9fd6e`. The original checkout was not modified. `android init` completed
successfully, `./gradlew --version` confirmed Gradle 9.6.1 on JDK 21, and the
dependency-free project checker plus its nine Python regression tests passed.

The Gradle verification gate could not start on this clean clone because the
execution environment did not expose an Android SDK (`ANDROID_HOME` and
`ANDROID_SDK_ROOT` were unset and no `local.properties` was present). Gradle
stopped with the expected SDK-location error while resolving
`:sharedApp:testAndroidHostTest`; this is an environment prerequisite failure,
not a source or test assertion failure. Re-run `./scripts/verify.sh` after
copying `local.properties.example` to `local.properties` and setting `sdk.dir`
to an installed SDK 37 path. No Android device or macOS/M2 host was available,
so TPL-013 and TPL-014 remain external validation tasks.

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

## Language stack (TPL-016), executed 2026-09-18

| Check | Command | Result |
|---|---|---|
| Locale unit tests | `./gradlew :core:designsystem:testAndroidHostTest` | `AppLocaleTest`, 11 tests, 0 failures (shipped-language set, code round-trip, RTL set, legacy `iw`→`he` normalisation, and `LocaleManager` repairing **and persisting** a missing/unrecognised preference) |
| Locale in the production DI graph | `./gradlew :androidApp:testDebugUnitTest` | `KoinModulesTest`, 2 tests, 0 failures — `LocaleManager` now resolves from `appModules` |
| Required-locale + string parity | `python3 scripts/check_project.py` | Pass, with `REQUIRED_LOCALES = ('he', 'iw', 'ru', 'fr')` enforced alongside the existing key/placeholder parity |
| Checker tests, including the new required-locale case | `python3 -m unittest discover -s scripts/tests` | 9 tests, pass (`test_missing_required_locale_fails` deletes a locale directory and asserts the checker reports it) |
| Full gate after the change | `./scripts/verify.sh` | Pass end-to-end |

**Not executed here.** Everything about this feature that needs a device is unexecuted on this
host: no device is attached, so the `docs/TESTING.md` "Locale stability" row — rotation, dark-mode
toggle, font scale, split screen, background-and-return, reinstall, system-language change, each on
a device whose system language differs from the app's — has not been run against this template.
Likewise the API ≤ 33 Hebrew check that the generated `values-iw` mirror exists to satisfy.

The same fix *was* validated on real hardware in the sibling FreeStore app it was derived from: a
`he-IL` Samsung SM-S938B, where the app's reported configuration went from `[he_IL,en_US…] ldrtl`
to `[en,he_IL,…] ldltr`, held through both dark-mode toggle directions and a force-stop relaunch,
and reverted an externally-set per-app locale. That is evidence for the approach, **not** for this
template's build of it. Rotation was not covered even there — the phone was locked throughout, so
`user_rotation` never took effect.

**iOS: written, never compiled, never run.** `core/designsystem/src/iosMain/.../i18n/` has no
compilation on this ARM64 Linux host (iOS targets are off unless `kmptemplate.enableIos=true` or
macOS). Beyond compilation it carries a documented CMP 1.11.1 limitation — see
[LOCALIZATION.md](LOCALIZATION.md).

## Earlier findings

Two real bugs were found and fixed while running the original gate, not by source review alone:

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
