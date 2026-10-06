# T0021 — AndroidTemplate: Android-only project template (plan only, nothing built)

## Goal
A second template next to `~/data/develop/kmp/KMPTemplate` for apps that will never need iOS, with the
same practices and the same "generate -> verify" workflow. Independent of TokenControl (which uses KMP).

## Part A — fix KMPTemplate first (prerequisite, small)
Found while planning TokenControl (T0020 design.md §0):
1. `scripts/new_project.py`: `DIRECTORIES` lists `core`, `feature`, `sharedApp` at the root, but they live
   under `common/` -> generated projects have no `:common:*` modules and cannot build.
2. `scripts/check_project.py`: module-boundary rules never match paths under `common/` (rules are inactive).
3. `.github/workflows/ci.yml`: macOS lane uses the old module paths.
4. Add a generator test that builds the file list of a generated project and asserts every `include(...)`
   in settings.gradle.kts has a directory (would have caught 1).
Exit: renamed-project smoke build passes the gate.

## Part B — AndroidTemplate
Location `~/data/develop/android/AndroidTemplate` (new folder; owner may prefer another), private repo
`controlfix87/AndroidTemplate`, plus a workspace AGENTS.md note like the KMP one.

Keep from KMPTemplate (same versions via a copied `gradle/libs.versions.toml`, trimmed):
- JDK 21, pinned wrapper, compile/target API 37, minSdk 26, edge-to-edge, no orientation lock.
- `build-logic` convention plugins, renamed and simplified: `android.library`, `android.compose`,
  `android.feature`, `jvm.pure` (replacing the four `Kmp*ConventionPlugin`s).
- Module map without the `common/` level: `core:model` (pure JVM), `core:common`, `core:designsystem`
  (theme + i18n stack en/he/ru/fr incl. `values-he` -> `values-iw` mirror and the locale fix),
  `core:network` (Ktor + OkHttp, timeouts, redacted logging), `feature:example` (offline demo, MVI
  list/search + detail), `app`.
- Navigation 3 with serializable keys, Koin, per-entry ViewModels, SavedStateHandle patterns.
- `scripts/`: `new_project.py` (allowlist copy, renames, tests), `check_project.py` (boundaries, locale
  parity, manifest checks), `check_apk.py`, `verify.sh`; docs LOCALIZATION / TESTING / VALIDATION / PERSISTENCE.
- CI: API 26/35/37 device lanes, renamed-project build, release shrinking. No macOS lane.

Change because there is no KMP:
- Plain Android/Jetpack artifacts instead of JetBrains multiplatform ones (Compose, lifecycle, Navigation 3,
  resources via `strings.xml` instead of Compose Multiplatform resources).
- No `sharedApp`, `iosApp`, expect/actual, Darwin engine, common-main purity script, `enableIos` flag.
- Tests: JUnit host tests + Robolectric where the KMP template used Android host tests; same device suite.
- Optional catalog entries only (not wired): Room, DataStore, WorkManager, Glance.

Decide during design (defaults in brackets): keep Ktor or switch to Retrofit [keep Ktor, same patterns as
KMP]; keep Koin or Hilt [Koin, same as KMP]; folder location [~/data/develop/android].

## Steps
1. Part A fix in a KMPTemplate clone, gate + renamed-project smoke build.
2. Design doc: exact module/plugin list and the diff against KMPTemplate.
3. Create AndroidTemplate from a KMPTemplate copy; remove KMP/iOS; convert modules one at a time, gate green each.
4. Port scripts + their Python tests; generator smoke test (`SmokeApp`) passes `verify.sh`.
5. Device run on a leased device (recreation, rotation, insets, language) and docs/VALIDATION.md.

## Acceptance
- Generated project builds and passes `verify.sh` with no manual edits; no KMP plugin or iOS file remains.
- Boundary and locale checks proven active by unit tests.
- KMPTemplate generator bug fixed and covered by a test.

## Recommended agent crew (owner approves before spawning; max 5)
architect Sonnet high (design = diff against an existing template), developer Opus medium (wide mechanical
conversion with build-logic changes), reviewer Sonnet high, qa Sonnet medium (gate + device lease),
committer Sonnet low. Part A alone, if done separately: developer Sonnet medium + qa Sonnet low + committer Sonnet low.

## Open for owner
- Folder for Android-only projects/template, and whether Part A should run now as its own small fix.
