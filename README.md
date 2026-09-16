# KMPTemplate

A from-scratch Kotlin Multiplatform + Android starter, assembled from the
patterns that are actually proven across the other KMP projects on this
machine (jellyMote, freestore, eizeseret) rather than written from a generic
tutorial. Clone this, rename the package, delete `feature/example`, start
adding real features.

## Stack

Kotlin 2.4.10 · AGP 9.3.1 · Gradle 9.6.1 · Compose Multiplatform 1.11.1 ·
Ktor 3.5.2 (client) · Koin 4.2.2 · kotlinx.coroutines 1.11.0 ·
kotlinx.serialization 1.11.0 · Room 2.8.4 / DataStore 1.2.1 (wired in the
catalog, not yet used by any module) · JUnit5 + Turbine + AssertK + MockK +
Robolectric for tests.

Every version lives in `gradle/libs.versions.toml` — nowhere else.

## Module map

```
core/model          pure Kotlin domain types, shared with any future server/CLI
core/common          AppResult/AppError/DataError, DispatcherProvider
core/designsystem     theme, typography, ALL user-facing strings (composeResources)
core/network          Ktor HttpClient factory (expect/actual engine)
feature/example       one full vertical slice: domain -> data -> ui (MVI) -> di -> navigation
androidApp            Koin startKoin, NavHost, KoinModulesTest
build-logic/convention  4 convention plugins every module applies instead of hand-rolling its build script
```

### Dependency rule

`feature/*` never depends on another `feature/*` module. Anything two features
need moves into `core/*`. `core/designsystem` never depends on `feature/*`
(that would cycle). This is enforced in code, not just by convention — see
`KmpFeatureConventionPlugin.kt`'s trailing `dependencies { ... }` block.

### Convention plugins (`build-logic/convention`)

| Plugin ID | Targets | Use for |
|---|---|---|
| `kmptemplate.kmp.pure` | JVM (+ iOS opt-in) | platform-neutral modules: models, small utilities |
| `kmptemplate.kmp.library` | Android + JVM-ish (+ iOS opt-in) | data-layer modules: network, database, datastore |
| `kmptemplate.kmp.compose` | + Compose Multiplatform | shared UI with no feature dependency (design system) |
| `kmptemplate.kmp.feature` | + Koin + navigation, pre-wired to `core:model`/`core:common`/`core:designsystem` | one per screen/flow |

Adding a module is: pick the right plugin, apply it, done — not hand-copying
15 lines of `kotlin { androidTarget { ... } }` boilerplate into a new
`build.gradle.kts`.

## Renaming this template

1. Global find/replace `com.kmptemplate` → your real package, in every `.kt`
   file, every `build.gradle.kts` (`namespace`/`applicationId`), and
   `BuildLogicExt.kt`'s `moduleNamespace`.
2. Rename `KMPTemplate` → your app name in `settings.gradle.kts`
   (`rootProject.name`), the theme composable (`KMPTemplateTheme`), the
   `Application` subclass, and `AndroidManifest.xml`.
3. Delete `feature/example` (domain, data, ui, di, navigation, and its
   commonTest) and its two registrations (`settings.gradle.kts`'s `include`,
   `KMPTemplateApplication`'s `modules(...)` list) once you have a real first
   feature to replace it with. Keep the *shape* — domain/data/ui/di/navigation
   packages, a Fake*Repository test double, a `checkModules()` entry.
4. Add a real launcher icon (`androidApp/src/main/res/mipmap-*/ic_launcher.*`)
   — the manifest currently has none and falls back to the platform default.

## Test commands

KMP modules do **not** use `./gradlew test` — that silently runs almost
nothing. Use:

```bash
./gradlew :core:model:allTests :core:common:allTests   # pure-Kotlin modules
./gradlew testAndroidHostTest                            # every KMP module's Android-host tests (incl. :feature:example)
./gradlew :androidApp:testDebugUnitTest                   # :androidApp itself (a plain com.android.application) + KoinModulesTest
./gradlew :androidApp:assembleDebug                        # does it actually build an APK
```

`./scripts/check_common_main_purity.sh` catches an `android.*`/`java.time`
import leaking into `commonMain`/`commonTest` before you even run Gradle.

## Three real bugs this template's own build caught (keep an eye out for these)

Verified by actually building this template end to end, not just written from
memory — worth knowing before you hit the same class of error and burn time
on it:

1. **A literal `--` inside an XML `<!-- -->` comment breaks the build**, both
   in a manifest (`ManifestMerger2$MergeFailureException`) and in Compose
   Multiplatform's string-resource converter (a generic, line-number-free
   `XML file ... is not valid. Check the file content.` — it swallows the
   real parser exception). This is standard XML — comments may never contain
   `--` except as the closing delimiter — but AGP's and CMP's error messages
   for it are bad enough to cost real debugging time. Write `omitted: it`, not
   `omitted -- it`, in an XML comment.
2. **Kotlin block comments nest.** Writing a literal `/*` inside a `/** ... */`
   KDoc comment (e.g. describing a glob like `di/*Module.kt`) opens a *nested*
   comment that the doc's own closing `*/` then closes instead of the outer
   one — silently swallowing every line of real code after it into the
   comment, which surfaces as a confusing "Unresolved reference" error in a
   completely different file that used to compile fine. Don't put `/*` inside
   a KDoc comment's prose, even to describe a file glob.
3. **`implementation`, not `api`, on a dependency whose type is part of your
   own public API silently breaks downstream consumers**, not the module
   that got it wrong. `core/network`'s `createHttpClient()` returns Ktor's
   `HttpClient`; declaring `implementation(libs.ktor.client.core)` there
   compiled `core:network` itself just fine, then failed `:androidApp` with
   `Cannot access class 'io.ktor.client.HttpClient'` for a dependency
   `androidApp` never touches directly. If a public function's signature
   exposes a type, that dependency needs `api(...)`, not
   `implementation(...)`.

## Host notes (aarch64 Linux SBC)

If you're on the same kind of machine this was bootstrapped on:

- Kotlin/Native publishes no `linux-aarch64` host compiler at all. iOS targets
  are opt-in (`-Pkmptemplate.enableIos=true`) and will not configure without
  a Mac or macOS CI, full stop — see `BuildLogicExt.kt`.
- Android build-tools are x86_64 and run under qemu. `aapt2` is already
  overridden globally in `~/.gradle/gradle.properties`
  (`android.aapt2FromMavenOverride`) — don't redo that per-project.
  `android.enableResourceOptimizations=false` is set in this project's
  `gradle.properties` because the release resource-optimize step corrupts
  APKs under emulation.
- Install with a real `adb` (`/usr/bin/adb install ...`), not
  `./gradlew installDebug` — the SDK's bundled `adb` is x86_64 and hangs
  under qemu on some hosts.
- Robolectric cannot run here (no conscrypt native lib for linux-aarch64).
  `KoinModulesTest` mocks `Context` directly instead of using Robolectric for
  exactly this reason — keep doing that rather than reaching for Robolectric
  in a module that needs to run in this repo's CI/local loop. A Room DB test
  that genuinely needs Robolectric should live in a JVM-only `jvmTest` source
  set instead (Room's plain-JVM builder needs no `Context`) — see eizeseret's
  `core/database` module for the worked pattern.
- `grep CLASSPATH= gradlew` returning nothing is normal on Gradle 9 (it uses
  `-jar`), not a sign of a broken wrapper.

## Hard rules

- No `android.*`, `java.time`, `java.util.UUID` in `commonMain`/`commonTest`.
  Platform-bound things go behind `expect`/`actual` (see `core/network`'s
  `HttpClientFactory`). CI enforces this before Gradle even runs.
- State classes carry no UI types — no string resources, no `Color`, no
  painters. Errors are `DataError`/`AppError` enums; the Screen composable is
  the only place that turns one into a localized string.
- Every feature module owns a `di/*Module.kt` and is registered in both
  `KMPTemplateApplication` (real app) and `KoinModulesTest` (build-time DI
  check) in the same commit.
- Test doubles are hand-written `Fake*` classes implementing the real
  interface (see `feature/example/testutil/FakeExampleRepository.kt`), not
  mocks of your own repository interfaces — a fake that tracks calls catches
  wiring bugs a relaxed mock hides. Mocking is fine for third-party/SDK types
  you don't own (see `KoinModulesTest`'s mocked `Context`).
- Hebrew strings go in `values-iw`, not `values-he` — Android's legacy ISO
  code for Hebrew is `iw`; `values-he` silently fails to match on some
  devices. Only relevant once you add a second locale.
