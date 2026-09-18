# Language

English, Hebrew, Russian and French ship by default. The app language is never taken from
the device language, and changes in exactly one way: the user picks it in
`LanguagePickerDialog`.

## The bug this design exists to prevent

The usual advice for in-app language switching on Android is to override
`Activity.attachBaseContext` with a configuration context and call `Locale.setDefault`.
That is not enough, and the way it fails is nasty: **the app switches to the device
language at random.**

Three facts combine:

1. **Compose Multiplatform resolves strings from the process-global platform locale.**
   `stringResource()` goes through `DefaultComposeEnvironment.rememberEnvironment`, which
   reads `androidx.compose.ui.text.intl.Locale.current` — on Android,
   `java.util.Locale.getDefault()` — and it re-reads it on **every recomposition**, not
   once. (Verified by disassembling `components-resources-android-1.11.1.aar`. The one
   hook that could redirect it, `LocalComposeEnvironment`, is `internal` in CMP 1.11.1, as
   is `ResourceEnvironment`'s constructor — verified by compiling against both. So there
   is no supported way to hand the resolver a different locale; the process locale *is*
   the app's language.)

2. **`Locale.getDefault()` belongs to the framework, not to the app.**
   `ActivityThread.updateLocaleListFromAppContext()` re-derives it from the
   **Application** context's configuration. It runs on process bind and on every
   configuration update the process receives: rotation, dark-mode toggle, density or
   font-scale change, window resize, a system language change, and updates delivered while
   the app is backgrounded. Override only the Activity and the Application context stays on
   the device language, so every one of those events resets the process back to it.

3. **Nothing repaints when that happens.** Already-composed text keeps the strings it
   resolved. The device language leaks into the *next* thing composed — a new screen, a
   dialog, a list item scrolling in. So the symptom is "it was English, and some time later
   parts of it were Hebrew", with no single reproducible trigger.

Layout direction does not come from the platform locale at all — it comes from
`AppLocale.direction` through `LocalLayoutDirection` — so it survives. The classic
signature of this bug is therefore **one language's strings inside the other language's
layout direction**.

## The fix, in layers

| Layer | Where | What it covers |
|---|---|---|
| Platform per-app locale (API 33+) | `AndroidAppLocale.syncSystemPerAppLocale`, driven from `applyLocale`'s `LaunchedEffect(tag)` | The system stamps the language into every Configuration it builds for this app, including the Application's, so the re-derivation in (2) yields the app's language. This is the real fix. |
| Application base context | `KMPTemplateApplication.attachBaseContext` → `AndroidAppLocale.wrap` | The API 26–32 equivalent: `createConfigurationContext` registers a persistent override in `ResourcesManager`, so the Application's `Resources` keep the language across configuration updates. |
| Application config changes | `KMPTemplateApplication.onConfigurationChanged` → `patch` | Process-level changes that reach no resumed Activity. |
| Activity base context + config changes | `MainActivity.attachBaseContext` / `onConfigurationChanged` | `context.resources` strings, date and number formatting, from the first frame. |
| Activity resume | `MainActivity.onResume` → `reassert` | Anything that reset the process while the Activity was stopped. |
| Composition | `AppLocaleProvider`'s `repeatOnLifecycle(RESUMED)` drift check | Last line of defence, and the only layer that makes an **already-composed** tree reload its strings rather than drifting screen by screen. |

`android:localeConfig` is deliberately **not** declared. It would surface a *system*
language picker for this app whose choice the code above overwrites on the next launch.
Pick one owner: either the in-app picker (what this template does) or the system picker —
in which case declare `localeConfig` and read `applicationLocales` back into
`LocaleManager` at startup instead of pushing to it.

## Hebrew needs two resource directories

Compose Resources matches a `values-<lang>` directory by exact string against
`Locale.getDefault().language`. Android reports Hebrew under the withdrawn 1989 ISO code
`iw` up to and including API 33, and under `he` from API 34. One directory is therefore
wrong on half the fleet, and the failure is silent: Hebrew falls back to `values/`
(English) while the layout still flips to RTL.

`core/designsystem/build.gradle.kts` registers `mirrorHebrewLegacyResources`, which copies
`values-he/` → `values-iw/` on every build and is wired into the Compose resource prep
tasks. **Edit `values-he/` only.** `values-iw/` is committed solely so a fresh checkout or
IDE import has it before the task first runs.

## Adding a language

1. An entry in `AppLocale` (`core/designsystem/.../i18n/AppLocale.kt`).
2. `core/designsystem/src/commonMain/composeResources/values-<code>/strings.xml`, with the
   *full* key set — `scripts/check_project.py` enforces key parity and placeholder parity.
3. Add the code to `REQUIRED_LOCALES` in `scripts/check_project.py`.

`AppLocaleTest` asserts the shipped set, the code round-trip, the RTL set, and that
`LocaleManager` repairs a missing or unrecognised preference to the default *and writes it
back* — that write is load-bearing, because Android's `attachBaseContext` reads the same
key directly, before DI exists.

## iOS: known limitation

iOS has no equivalent of the Android re-derivation above, so there is nothing to fight.
But the same CMP constraint applies in reverse: on iOS the resolver reads
`NSLocale.currentLocale`, and with `LocalComposeEnvironment` internal there is no supported
override. `forceLocale` writes `AppleLanguages` to `NSUserDefaults`, which is where
Foundation looks; whether that invalidates `NSLocale.currentLocale` within the same process
is a Foundation implementation detail. **Treat "strings change on the next launch" as the
guaranteed behaviour on iOS.** Layout direction flips immediately on every platform,
because it comes from `AppLocale.direction`.

None of the iOS code has been executed — see [VALIDATION.md](VALIDATION.md).
