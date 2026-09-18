# Test strategy and platform acceptance

## Automated local gate

Run `./scripts/verify.sh` with JDK 21 and SDK 37. It checks common-source purity,
localization/XML, direct module boundary declarations, generator behavior, typed result
helpers, ViewModel subscription/refresh/restoration logic, Ktor response mapping,
production Koin graph resolution, Android lint and compilation of app + device tests.
Static source checks are guardrails, not a substitute for Kotlin compilation or a full
semantic dependency analysis. Tests must assert observable behavior; do not create
empty tests just to increase counts.

| Suite | Task | Regression contract |
|---|---|---|
| Result helpers | `:core:common:allTests` | Success/failure transformations preserve values and types |
| ViewModel | `:feature:example:testAndroidHostTest` | Lazy initial load, resubscription, retry single-flight, saved query |
| HTTP adapter | same feature task | Base-path joining, statuses, malformed JSON, cancellation, HTTPS validation |
| DI | `:androidApp:testDebugUnitTest` | Real module list resolves repository and entry ViewModel |
| Device | `:androidApp:connectedDebugAndroidTest` | Query/detail restoration, real phone rotation, safe drawing bounds |
| Generator/checker | `python3 -m unittest discover -s scripts/tests -v` | Reject invalid destination/identity, exclude machine files, detect nested import leaks |
| Release | `:androidApp:assembleRelease` | R8/resource shrinking and packaging succeed |
| iOS | macOS CI | Native common tests, framework linkage and Swift host build |

JUnit Jupiter is used for Android app host tests. KMP tests use portable kotlin-test;
Android instrumentation uses the Android JUnit runner/Compose UI test rule. Do not
force all platforms onto a JVM-only JUnit engine.

## State restoration is several distinct tests

1. **Real phone rotation:** enter a query, switch orientation, verify it remains and
   filtered data appears. Restore unspecified orientation in test cleanup. Large
   screens may ignore requested orientation; use real resize/fold controls there.
2. **Activity recreation:** navigate to detail, recreate Activity, check destination,
   go back and check query. This is covered by the device test.
3. **Saved state:** reconstruct a ViewModel from saved scalar inputs; covered by unit tests.
4. **Real process death:** on a disposable test device, enter query/detail, press Home,
   run `adb shell am kill com.kmptemplate.app.debug`, return via Recents, and inspect
   restored destination/query. Do not use force-stop as an equivalent; force-stop
   changes task/session semantics. Repeat with a new product's form and worker IDs.
5. **Scroll restoration:** scroll far enough to dispose early rows, enter detail,
   rotate/recreate, return and confirm the same row/offset. Verify both long and
   filtered lists; a dataset change may legitimately alter the visible position.

## Required release matrix

| Dimension | Cases | Acceptance |
|---|---|---|
| Runtime | API 26, API 35, API 37 | Launch/search/detail/back, no crash |
| Navigation mode | Gesture, three buttons, predictive back cancel/commit | Correct destination, controls not obscured |
| Display | Small phone, landscape, tablet, split screen, fold/unfold, freeform caption bar | No clipping/overflow, state remains |
| Insets | Cutout side/top, navigation/taskbar, IME shown/hidden | Focused field and action visible; no double padding |
| Appearance | Light/dark and theme change | Legible text/system icons, no stale theme after recreation |
| Accessibility | 200% text, TalkBack, external keyboard | Labels/order/focus work, 48dp targets, scrollable content |
| Locale | English/Russian/French LTR and Hebrew RTL | Logical start/end order, safe mixed number/ID text |
| Locale stability | On a device whose system language differs from the app's: rotate, toggle system dark mode, change font scale, enter split screen, background the app and return, reinstall the APK over itself, change the system language | The app language **never** changes. Strings and layout direction never disagree in any frame. Check a screen opened *after* each event, not only the one already on screen: the leak surfaces on the next composition, not at the moment it happens (see [LOCALIZATION.md](LOCALIZATION.md)) |
| Locale on API ≤ 33 | Hebrew on an API 26–33 device | Hebrew *strings*, not English under an RTL layout — that pairing means the generated `values-iw` mirror did not resolve |
| Async | Offline, timeout, malformed data, cancellation, rapid retry | Typed error, one in-flight job, recoverable state |
| Persistence | Upgrade, partial/corrupt data, process death | No silent loss; migration/restore evidence |

CI phone lanes automate only part of this matrix. Device-test APK compilation does
not execute it. Keep manual/tablet/IME evidence per product release. The template's
sample has no database, OAuth callback or WorkManager operation; add their real tests
when implementing those capabilities.

## Reports and failure triage

Preserve `**/build/test-results/**`, `**/build/reports/**` and release mapping files.
Record exact commands and actual test counts from XML. Never suppress stderr or turn
an unavailable task into a green result. Classify toolchain/dependency, compile,
assertion, emulator and external-service failures separately. Tests must use synthetic
fixtures and fake credentials, not developer accounts or production data.
