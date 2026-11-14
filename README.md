# CMPTemplate

Compose Multiplatform starter for **Android, iOS and desktop**: one shared Compose UI (screens,
strings, fonts, icons, theme all in `commonMain`), Navigation 3, Koin, state restoration, tested
networking, Roborazzi screenshot tests and repeatable project generation. It ships the ControlFix
branding, an About screen, an in-app report/suggestion screen wired to the ControlFix reports
service, and an optional first-run onboarding.
Android compiles and targets **Android 17 / API 37**, with **minSdk 26**.

## Create a project

From this template directory:

```bash
python3 scripts/new_project.py --name MyNotes --package com.example.mynotes --destination ../MyNotes
# add --without-onboarding to leave out the first-run setup module
cd ../MyNotes
# Set ANDROID_HOME, or create local.properties containing sdk.dir=/your/android/sdk
./scripts/verify.sh
```

Use a PascalCase project name and lowercase reverse-DNS package. The destination must
not exist and must be outside the template. The generator renames Kotlin packages,
application/theme classes, namespaces, plugin IDs, root project, Swift host and display
name. It copies a fixed set of template files and excludes `.git`, build/cache output,
local settings and signing files. It never creates a remote repository or publishes.

The demo runs without a server, API key, database, signing secret, or internet request.
Gradle's first build still needs network access to download its toolchain dependencies.
Replace the sample when creating your first real feature; keep its restoration/testing patterns.

## Requirements and targets

- JDK 21; use the checked-in Gradle wrapper (distribution checksum is pinned).
- Android SDK platform `platforms;android-37.0` and compatible build tools.
- Python 3.11+ for project generation/static checks (no Python packages required).
- macOS with Xcode and XcodeGen for iOS. Apple Silicon targets: device ARM64 and simulator ARM64.
- Android/Linux builds do not configure Apple targets. macOS enables them automatically;
  override with `-Pcmptemplate.enableIos=true` or `false`.

Versions are centralized in `gradle/libs.versions.toml`; this is a tested pinned baseline,
not a promise that each dependency is the newest available. Navigation 3 uses the
JetBrains multiplatform artifacts. Database/storage catalog entries are optional;
no database plugin or schema is imposed on a new app before it needs persistence.

## Module map

| Module | Responsibility |
|---|---|
| `common:core:model` | Pure shared domain models (`AppInfo`, sample item), JVM + optional iOS |
| `common:core:common` | Typed results/errors, coroutine dispatcher abstraction |
| `common:core:designsystem` | ControlFix theme (palette, Exo 2), light/dark/system `ThemeManager`, `AppPreferences`, shared components (`ScreenHeader`, `SectionCard`, `OptionCard`, `StepProgress`, `AppMark`, `Wordmark`, `AppIcons`), the language stack (`i18n/`) and English/Hebrew/Russian/French resources |
| `common:core:network` | HTTPS Ktor client, timeouts, redacted optional logging, OkHttp/Darwin engines |
| `common:feature:example` | Repository seam, offline demo, tested HTTP adapter, MVI list/search and detail entries |
| `common:feature:about` | Branded About, report/suggestion screen, `HttpReportSender`, `Redactor`, e-mail fallback, crash recorder + diagnostics per platform |
| `common:feature:onboarding` | **Optional** first-run setup: language + theme, a product step, done |
| `common:sharedApp` | Shared navigation, serializable keys and production Koin module list; iOS framework |
| `androidApp` | Android Application/Activity, edge-to-edge, release signing, Robolectric screenshots, device tests |
| `desktopApp` | Compose Desktop window (JVM) and desktop screenshots |
| `iosApp` | SwiftUI host and reproducible XcodeGen specification |
| `build-logic:convention` | `cmptemplate.kmp.pure`, `cmptemplate.kmp.library`, `cmptemplate.cmp.compose`, `cmptemplate.cmp.feature` (Android + desktop + optional iOS targets) |

Feature modules never depend on another feature; share contracts in core modules.
Keep feature domain/data/ui/di/navigation packages together until separate compilation
or ownership merits layer modules. Pure modules must not import Android/JVM-specific
APIs into common source sets. Platform interfaces use expect/actual or injection.

## ControlFix branding

`core:designsystem/Theme.kt` holds the ControlFix palette (lime `#A9F000`, teal `#17B3A4`, navy
`#101D33` → ink `#070C17`, paper `#E9F2F7`) as a dark navy and a light paper scheme, and Exo 2
(SIL OFL, `docs/branding/Exo2-OFL.txt`) for headlines, titles and labels; body text stays on the
platform font. Use `primary` for lime: on light surfaces it is the readable deep lime. The user picks
light, dark or "follow device" (`ThemeManager`, in onboarding and About); Android's status/navigation
bar icons follow the *app's* theme, not the device's. `AppMark` is a placeholder mark drawn in code:
replace it, the Android launcher icon and `about_body` with the product's own.

## About, reports and suggestions

`feature:about` is the About screen (mark, name, version, "Made by ControlFix", website, privacy,
support e-mail, report a problem, send a suggestion, language, theme, run setup again) and the report
screen. Reports go to the shared ControlFix reports service on Cloudflare,
`POST https://reports.controlfix.net/v1/reports` (contract: AgentsHub `tools/reports/README.md`), with
`x-report-app` = the Android package / iOS bundle id. Tickets land on the Orange Pi under
`~/data/tickets/<app id>/` (debug builds use the `.debug` id). Device details always go; the redacted
recent log and last crash only with the "Attach app logs" box (on by default). When the service
cannot be reached, refuses the report or rate-limits it (10 per hour), the screen offers **Send by
e-mail instead**: a `mailto:support@controlfix.net` with subject, message and device details. No
secret is in the app; the endpoint is public by design. `CrashRecorder.install()` runs first in
`Application.onCreate` (Android) and `main()` (desktop); iOS sends device details only.

Before release: set `ControlFixLinks.PRIVACY` to the product's privacy page and write `about_body`.

## Onboarding (optional)

`feature:onboarding` opens on a fresh install: a numbered, titled step bar (finished steps tappable),
language and theme first as cards, one template product step (`OnboardingStep.APP`, replace it),
and a done step. Skip is on every step but the last; the step survives language changes, rotation and
process death. About offers "Run setup again". Generate without it:
`scripts/new_project.py ... --without-onboarding` deletes the module and every line marked
`[onboarding]` (or inside `region onboarding`), and enables the lines marked `// without onboarding`.

## Desktop

`./gradlew :desktopApp:run` opens the shared App in a desktop window. Settings live in
`java.util.prefs` under `cmptemplate`; crashes in `~/.cmptemplate/diagnostics/`. Packaging:
`./gradlew :desktopApp:packageDistributionForCurrentOS` (DMG/MSI/DEB).

## Rotation, resizing and edge-to-edge

The Android Activity calls `enableEdgeToEdge()` and allows normal recreation. There is
no orientation lock, resizability opt-out or blanket `configChanges` declaration.
Android 17 does not honor large-screen orientation restrictions, so locking is not a
state-preservation mechanism.

The shared Navigation 3 stack is saved with explicit serializers for Android and iOS.
Entries own their ViewModels and saveable UI state. Search text is stored in
`SavedStateHandle`; lists use saveable lazy-list state and stable item keys. Initial
loading starts on subscription once per ViewModel, and concurrent retries share one
in-flight operation. Repeated clicks cannot stack identical detail destinations.

Each screen's Scaffold owns `safeDrawing` insets. Content consumes that padding before
applying IME padding. Lists/forms scroll in short windows and are width-limited on wide
ones. This starter supplies responsive single-pane layouts, not a complete foldable
multi-pane design for every future product.

The device suite distinguishes real phone rotation from Activity recreation. Manual
process-death, foldable/freeform, RTL, font-scale and IME checks are in
[the test matrix](docs/TESTING.md). Compiling device tests does not prove they ran.

## Language

English, Hebrew, Russian and French ship by default, listed once in
`common/core/designsystem/.../i18n/AppLocale.kt` and backed by one `values-<code>/strings.xml`
each. The app language is **never** taken from the device language and changes in exactly
one way: the user picks it in `LanguagePickerDialog`.

That is a deliberate stance, not a simplification. Android re-derives the process-global
`Locale.getDefault()` — which Compose Multiplatform resolves every `stringResource()`
from, on every recomposition — out of the *Application* context's configuration, on
process bind and on every configuration update the process receives. An app that forces
its language only on the Activity (the usual advice) leaves that context on the device
language, so rotations, dark-mode toggles and background configuration updates silently
reset it, and the wrong language then surfaces on whatever screen composes next. See
[docs/LOCALIZATION.md](docs/LOCALIZATION.md) for the mechanism and the layered fix.

Adding a language is: one `AppLocale` entry, one `values-<code>/strings.xml` with the
full key set, and one line in `REQUIRED_LOCALES` in `scripts/check_project.py`. Hebrew is
the exception — edit `values-he/` only; `values-iw/` is generated from it by a Gradle task
because Android reports Hebrew as `iw` up to API 33 and as `he` from 34.

## Verification

```bash
./scripts/verify.sh
./gradlew :androidApp:assembleRelease
# A connected test device/emulator is required:
./gradlew :androidApp:connectedDebugAndroidTest
```

`verify.sh` runs static checks, Python generator/checker tests, pure common tests, KMP
Android host tests, desktop (JVM) tests, Android app unit and Robolectric screenshot tests, lint,
app/test APK compilation and the desktop host compilation.

Screenshots (Roborazzi): `./gradlew :androidApp:recordRoborazziDebug` (phone and Fold sizes,
Robolectric) and `./gradlew :desktopApp:recordRoborazziDesktop` write PNGs to
`*/build/outputs/roborazzi/`; `verifyRoborazziDebug` / `verifyRoborazziDesktop` compare with the last
recording. Look at them after every UI change.
Do not substitute a bare `./gradlew test`: different module types expose different tasks.
`core:model` has no behavior tests because it currently contains only a data class.

CI adds APIs 26/35/37 device lanes, a renamed-project build, release shrinking, and a
macOS shared-framework/Swift-host lane. Workflow definitions are provided; see
[validation evidence](docs/VALIDATION.md) for what actually ran in this session.

## iOS

```bash
brew install xcodegen
./scripts/ios-project.sh
open iosApp/CMPTemplate.xcodeproj
```

Choose the CMPTemplate scheme and an Apple Silicon simulator. The Xcode build phase
builds/embeds `SharedApp`; no manually copied framework is required. Simulator builds
need no distribution signing. Set your development team for a physical device.
The SwiftUI wrapper delegates safe areas to Compose. The project supports both phone
and tablet orientations. iOS build/link/runtime acceptance still requires a Mac;
Linux compilation results are not evidence of an iOS build.

## Add the first feature

1. Apply a convention plugin, create commonMain/commonTest sources and include the module.
2. Put framework-independent contracts/models in domain; implement transport/persistence in data.
3. Add State/Action/ViewModel and stateless Screen/Root. Save scalar IDs/drafts, not whole models.
4. Expose Nav3 entries and serializable keys; use callbacks for cross-feature navigation.
5. Register key serializers and the feature's module in `common/sharedApp/App.kt`. The DI test
   consumes that exact production module list; extend its resolution assertions for new roots.
6. Add fake-backed tests and relevant restoration/device cases in the same change.
7. Remove the example include/dependency/module/routes/serializers/tests only after the real
   feature replaces it. Replace the neutral launcher icon with the product's approved artwork.

For HTTP, inject one `createHttpClient("https://your.service/api/")` instance and close
it with its owning scope. Use relative endpoint paths. Logging defaults off and omits
bodies; review URL query content before enabling logs. The optional HTTP sample maps
expected transport/status/serialization errors and rethrows cancellation.

For persistence, follow [migration infrastructure](docs/PERSISTENCE.md). Existing apps
must retain their schema and identity continuity; a template is not a replacement for
an upgrade plan. Authentication, background scheduling, analytics, billing and database
features are deliberately added when a product needs them.

## Host notes

This workspace uses ARM64 Linux. Android build tools may need an existing host-local
`aapt2` wrapper; do not commit absolute SDK/tool paths. If resource optimization is
broken under local emulation, override it locally rather than disabling release
optimization for everyone. Robolectric native dependencies may be unavailable here;
use device tests for actual platform behavior. Kotlin/Native framework linking needs
macOS. Debug builds run without credentials. Release builds sign when the Gradle property
`controlfix.signing.cmptemplate` (in `~/.gradle/gradle.properties` of the build machine) points to a
`keystore.properties` in the private assets repo; otherwise they are unsigned. Never commit a key.

See [implementation tasks](TASKS.md), [lessons from LockdownControl and GameController](docs/INSIGHTS.md),
[testing](docs/TESTING.md),
[validation](docs/VALIDATION.md), and the workspace [project plans](../MODERNIZATION.md).
