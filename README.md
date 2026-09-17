# KMPTemplate

Android + iOS Kotlin Multiplatform starter with an offline sample, shared Compose UI,
Navigation 3, Koin, state restoration, tested networking, and repeatable project generation.
Android compiles and targets **Android 17 / API 37**, with **minSdk 26**.

## Create a project

From this template directory:

```bash
python3 scripts/new_project.py --name MyNotes --package com.example.mynotes --destination ../MyNotes
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
  override with `-Pkmptemplate.enableIos=true` or `false`.

Versions are centralized in `gradle/libs.versions.toml`; this is a tested pinned baseline,
not a promise that each dependency is the newest available. Navigation 3 uses the
JetBrains multiplatform artifacts. Database/storage catalog entries are optional;
no database plugin or schema is imposed on a new app before it needs persistence.

## Module map

| Module | Responsibility |
|---|---|
| `core:model` | Pure shared domain models, JVM + optional iOS |
| `core:common` | Typed results/errors, coroutine dispatcher abstraction |
| `core:designsystem` | Shared theme and English/Hebrew resources |
| `core:network` | HTTPS Ktor client, timeouts, redacted optional logging, OkHttp/Darwin engines |
| `feature:example` | Repository seam, offline demo, tested HTTP adapter, MVI list/search and detail entries |
| `sharedApp` | Shared navigation, serializable keys and production Koin module list; iOS framework |
| `androidApp` | Android Application/Activity, edge-to-edge, Android/device tests |
| `iosApp` | SwiftUI host and reproducible XcodeGen specification |
| `build-logic:convention` | Pure, platform-library, Compose and feature plugins |

Feature modules never depend on another feature; share contracts in core modules.
Keep feature domain/data/ui/di/navigation packages together until separate compilation
or ownership merits layer modules. Pure modules must not import Android/JVM-specific
APIs into common source sets. Platform interfaces use expect/actual or injection.

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

## Verification

```bash
./scripts/verify.sh
./gradlew :androidApp:assembleRelease
# A connected test device/emulator is required:
./gradlew :androidApp:connectedDebugAndroidTest
```

`verify.sh` runs static checks, Python generator/checker tests, pure common tests, KMP
Android host tests, Android app unit tests, lint and both app/test APK compilation.
Do not substitute a bare `./gradlew test`: different module types expose different tasks.
`core:model` has no behavior tests because it currently contains only a data class.

CI adds APIs 26/35/37 device lanes, a renamed-project build, release shrinking, and a
macOS shared-framework/Swift-host lane. Workflow definitions are provided; see
[validation evidence](docs/VALIDATION.md) for what actually ran in this session.

## iOS

```bash
brew install xcodegen
./scripts/ios-project.sh
open iosApp/KMPTemplate.xcodeproj
```

Choose the KMPTemplate scheme and an Apple Silicon simulator. The Xcode build phase
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
5. Register key serializers and the feature's module in `sharedApp/App.kt`. The DI test
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
macOS. Debug builds run without credentials; release output is unsigned until a
product supplies its signing configuration.

See [implementation tasks](TASKS.md), [testing](docs/TESTING.md),
[validation](docs/VALIDATION.md), and the workspace [project plans](../MODERNIZATION.md).
