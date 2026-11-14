# Lessons from LockdownControl and GameController

Both apps were built from this template (GameController) or next to it (LockdownControl) and then
shipped. What they learned is applied here; keep it when you replace the sample.

## Applied in the template

| Lesson | Where it came from | Where it lives now |
|---|---|---|
| The generator allowlist must follow the folder layout: listing `core/feature/sharedApp` at the root (they are under `common/`) produced projects without any shared module. Its boundary checker had the same blind spot and never fired. | GameController scaffold | `scripts/new_project.py` `DIRECTORIES`, `scripts/check_project.py` `strip_common`, regression tests |
| `Dispatchers.IO` is an extension outside the JVM: common code needs `import kotlinx.coroutines.IO` or iOS does not compile. Linux builds never notice. | GameController ("Import Dispatchers.IO extension for non-JVM targets") | `core:common/DispatcherProvider.kt` |
| Compose Multiplatform on iOS wants `CADisableMinimumFrameDurationOnPhone` in Info.plist. | GameController | `iosApp/project.yml` |
| Screenshot tests on the JVM (Roborazzi + Robolectric) at phone and Fold sizes found light text on dark screens, a button covering another and a drawing overflowing landscape phones, none of which a unit test saw. | GameController | `androidApp/.../ScreenshotTest.kt`, desktop twin in `desktopApp` |
| Robolectric screenshot tests use a plain `Application` and start/stop Koin per test; the real Application starts Koin itself and Robolectric creates one per test. `setContent` runs once per test: change state to shoot several steps. | GameController | `ScreenshotTest` |
| Device tests break when onboarding is added: skip it first. | GameController ("Device tests: onboarding skip") | `RestorationTest.skipOnboarding` |
| One shared reports service for every ControlFix app, ticket per app id, logs redacted on the device, logs only with a consent box (on by default), device details always, crash recorder installed first, crash cleared only after a successful send. | GameController | `feature:about` (`HttpReportSender`, `Redactor`, `CrashRecorder`) |
| When the service is unreachable the user still needs a way out: e-mail to support. | owner (support@controlfix.net) | `SupportMail`, "Send by e-mail instead" |
| An in-app light/dark choice means the status-bar icons must follow the app's theme, not the phone's, or they vanish. | LockdownControl | `SystemBarsEffect` in `CMPTemplateTheme` |
| System back walks back through onboarding steps (`BackHandler`, Compose Multiplatform `ui-backhandler`). | LockdownControl | `OnboardingScreen` |
| Device tests must wait for the first screen: the app draws nothing until the settings are read. | this template | `RestorationTest.skipOnboarding` |
| Outlined buttons need the brand accent: Material 3's default reads as disabled on navy cards. | screenshot review | `brandOutlinedButtonColors()` |
| ControlFix palette with a readable deep lime on light surfaces, Exo 2 only for headings/labels. | both | `Theme.kt` |
| Onboarding: language and theme first, as big cards with flags/icons; a progress bar split per step, each with its title, upcoming steps visible, finished steps tappable; Skip on every step but the last; the step saved so a language change (Activity recreation) lands on the same step; "Run setup again" in settings. | LockdownControl, GameController | `feature:onboarding`, About |
| About: mark + wordmark, version, "Made by ControlFix", website, report a problem, send a suggestion. | GameController | `AboutScreen` |
| Readable width on wide screens (cards `widthIn(max = 720.dp)`), large 48dp back buttons, `safeDrawing` insets. | GameController (Fold) | `ContentMaxWidth`, `ScreenHeader` |
| A test tag on every control the tests or a future automation touch. | both | screens in `feature:about`, `feature:onboarding` |
| Release signing from a `keystore.properties` in the private assets repo, located by a Gradle property on the build machine; never in the repo. | GameController | `androidApp/build.gradle.kts` |
| AGP 9: do not apply `org.jetbrains.kotlin.android` to the app; set `buildConfig = true` explicitly. | both | `androidApp/build.gradle.kts` (already) |

## Architecture rules (best practice, enforced by structure)

- **State holders:** every screen has a ViewModel exposing one `StateFlow<UiState>`
  (`stateIn(viewModelScope, WhileSubscribed(5_000), initial)`), a sealed `Action` interface and a
  stateless `Screen(state, onAction)`; the `Root` composable only wires `koinViewModel()` and
  `collectAsStateWithLifecycle()`. Screens never inject repositories or managers.
- **Navigation results are state, not events:** onboarding sets `finished` after DataStore has
  stored "done"; the root leaves on it. No one-off event channels.
- **Saved state:** what the user typed or where they are (drafts, step, query) goes in
  `SavedStateHandle`; settings in DataStore; nothing user-visible only in `remember`.
- **Settings:** Jetpack DataStore (Preferences), one instance per file per process (Koin `single`),
  read as a `Flow`, written with `edit`, corrupt files replaced with defaults. The app waits for the
  first read (`AppUiState.Loading`) instead of drawing a guessed theme or start screen. Exception:
  the language, which Android must read synchronously before DI exists.
- **Koin:** one module per feature (`viewModelOf(::X)`), platform pieces as `expect fun xModule()`,
  host-only facts (`AppInfo`, Android `Context`) supplied by the host; a DI test resolves every
  ViewModel from the production module list with only host bindings faked.
- **Layers:** `core` never depends on `feature`, features never on each other (checked by
  `scripts/check_project.py`); `commonMain` never imports platform APIs.

## Not applied (product decisions)

- **Ads and consent (LockdownControl: AdMob banner + UMP):** add per product, never on a lock or
  security screen, and fail the release build while test ids are set.
- **Crash SDK (GameController: Firebase Crashlytics, no Analytics):** the template's own crash
  recorder covers reports; add Crashlytics per product with sessions and ad-id collection off.
- **Play Integrity on uploads:** only for apps that upload user content (GameController share codes).
- **Android-only APIs** (accessibility services, overlays, Shizuku): keep them in `androidMain` or the
  Android app module behind an interface, as GameController's `core:platform` seams do. An app whose
  core feature cannot exist on iOS (both apps) drops the iOS target rather than shipping a shell.
