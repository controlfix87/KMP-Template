# KMPTemplate review, implementation and acceptance plan

Reviewed 2026-09-17. The implementation tracker is `TASKS.md`; actual results belong in
`docs/VALIDATION.md`. Unlike the other workspace projects, this template was changed
as part of the review. The original snapshot contained 29 Kotlin source files and four
Kotlin files in test paths. Filename counts were not treated as executed test counts.

## TPL-A — Restore trust in the verification gate (P0)

**Observed:** `scripts/check_common_main_purity.sh` combined `grep -E` and `-P`, hid
stderr, ignored command failure, and used a glob that missed nested core/feature
modules. Consequently, an apparent pass was not evidence of clean common sources.

**Implemented tasks:**

- Replace the mixed-regex shell implementation with dependency-free Python recursion.
- Scan commonMain/commonTest beneath nested modules, exclude generated/cache folders,
  reject Android/Java/Javax imports, and propagate read/parse failures.
- Parse XML, compare translation keys/placeholders, and reject obvious cross-feature
  and core-to-feature project dependencies in direct and type-safe declarations.
- Reject orientation/resizability overrides and recreation-hiding manifest flags.
- Add a regression fixture with a forbidden import deep under feature/domain.

**Acceptance:** a seeded bad import fails, valid common code passes, malformed XML
fails, and all project checks run before Gradle. These are static guardrails; Kotlin
compilation and actual dependency/task validation remain required.

## TPL-B — Make creation usable without manual global replacement (P0)

**Observed:** the README required manual package/class/plugin renaming and had no
reproducible project creation entrypoint.

**Implemented tasks:**

- Add a Python CLI with required name, package and destination arguments.
- Validate identifiers, reject existing destinations and template-contained targets.
- Copy only documented source/configuration directories; exclude build output,
  caches, local settings, VCS history and signing-file patterns.
- Rename package declarations/directories, root project, convention plugin prefix,
  Android Application/theme, Swift host and user-facing app label.
- Reset the generated project’s startup tasks and validation report so template test
  results are never mistaken for evidence about the new project.
- Support generation from a renamed copy by deriving source identity from build files.
- Add workspace instructions selecting this template for future new KMP requests.

**Acceptance:** generator tests pass, the generated package paths/namespace match,
Python checks run in the copy, and a fresh renamed project passes its Android build and
test gate with only the local SDK path supplied.

## TPL-C — Provide a genuinely runnable sample (P0)

**Observed:** the default repository called a nonexistent HTTP service; item text was
not clickable; the detail destination and callback were placeholders.

**Implemented tasks:**

- Bind an explicit offline demo repository with stable IDs and enough rows to exercise
  scrolling. Keep the HTTP repository as a separately tested production-shaped example.
- Implement list/search/empty/error/loading/detail/back behavior.
- Use synchronous UI navigation callbacks for user clicks and suppress identical
  destination pushes; avoid a transient event channel for this simple navigation.
- Keep Screen composables independent of DI so previews can render real states.

**Acceptance:** a newly installed debug app should show sample rows without credentials
or a backend, search and detail should work, and empty/error previews should render.
The implementation and compilation are locally verified; on-device interaction remains
a separate runtime gate.

## TPL-D — Make navigation and rotation state portable (P0)

**Observed:** the sample used a partially wired Nav2 host and an unguarded init load;
there was no saved draft or rotation test contract.

**Implemented tasks:**

- Introduce `sharedApp` as the Android/iOS composition root.
- Adopt multiplatform Navigation 3 with explicit key serializers, saved back stack,
  saveable-state and ViewModel-store entry decorators.
- Store query in SavedStateHandle and use saveable lazy-list state with stable keys.
- Load once on first subscription, retain the initialized flag across collector
  restarts, and keep one refresh job active at a time.
- Add fake-backed tests for resubscription after the sharing timeout, retry after
  failure, ten rapid retries, saved query rehydration and case-insensitive filtering.

**Acceptance:** no repeated initial request after rotation-related resubscription;
one in-flight refresh; restored query yields the expected rows. Device recreation
preserves destination and back navigation. Real process death is explicitly not
claimed from ActivityScenario.recreate.

## TPL-E — Target current Android behavior (P0)

**Observed:** compile/target were 36 and MainActivity did not explicitly enable
edge-to-edge on older supported OS versions.

**Implemented tasks:**

- Raise compile/target to API 37 while retaining minimum API 26.
- Call enableEdgeToEdge and allow normal configuration recreation and resizing.
- Assign Scaffold safeDrawing ownership, consume scaffold padding and then add IME
  padding. Use scrollable, width-limited content in compact-height/wide windows.
- Add an adaptive placeholder icon, Android theme, explicit backup exclusions and
  cleartext-disabled network defaults.
- Compile instrumentation cases for recreation, real phone rotation and safe bounds.
- Add static checks for 16 KB ELF load segments and uncompressed APK ZIP alignment,
  with pass/fail fixtures and an actual APK inspection.

**Acceptance:** debug/device-test APKs compile, lint has no unresolved actionable
findings, release shrinking succeeds, native packaging checks pass, and device lanes
on APIs 26/35/37 execute. Tablet, RTL, keyboard, font-scale and real process-death
acceptance are recorded per `TESTING.md`, not inferred from a source review.

## TPL-F — Make transport behavior safe to reuse (P1)

**Observed:** the client had no explicit timeout/status policy; repository handling
covered only some failures and did not exercise actual request mapping in tests.

**Implemented tasks:**

- Validate HTTPS base URLs with trailing slash and no credentials/query/fragment.
- Set request/connect/socket timeouts, enable expected HTTP failure handling and
  make logging opt-in with sensitive headers redacted and no bodies.
- Add engine injection for deterministic MockEngine tests and a Darwin actual.
- Map expected HTTP, transport and serialization failures; propagate cancellation.
- Close the optional Koin client binding on scope shutdown.

**Acceptance:** base-path request fixture, 401/404/500/malformed payload, cancellation
and invalid URL tests pass. New endpoints need their own DTO/domain contract fixtures,
authentication tests and logging review; this sample is not a universal API adapter.

## TPL-G — Complete the Apple source/build path (P1)

**Observed:** iOS targets were opt-in, referenced unsupported Intel target setup,
networking had no Darwin actual, and the iOS CI job was disabled.

**Implemented tasks:**

- Enable Apple Silicon device/simulator targets on macOS and guard non-Mac builds.
- Add Darwin networking and a shared UIKit controller using the same UI/module list.
- Add a SwiftUI phone/tablet host and XcodeGen specification with framework embedding.
- Enable macOS CI for native tests, framework linking and unsigned simulator host build.

**Acceptance:** macOS CI actually links, Xcode builds the generated host, and an iOS
simulator runs search/detail/rotation. This cannot be certified on the current ARM64
Linux machine. Treat the provided Apple path as implemented but awaiting external
validation; do not hide this by disabling its workflow job.

## TPL-H — Document the boundary between starter and product (P1)

**Implemented tasks:** replace stale manual-rename instructions; describe exact test
commands and source sets; add persistence/schema/backup/background-work acceptance;
centralize agent instructions; add a full validation report and remaining runtime matrix.
The starter does not create speculative database tables, authentication, billing,
analytics, browser or Wear targets. Products add those with matching platform drivers,
DI, migrations and real behavioral tests.

**Acceptance:** a new developer can generate/build the Android example, understand
what iOS setup needs, identify every unexecuted check, and add a feature without copying
machine secrets or inheriting a misleading completed-validation claim.

## Review and rollout

Keep all changes local for review because this affects every future generated app.
Do not overwrite existing applications from the template. Preserve API/package/signing
identity in migrations, and never combine a database migration with a toolchain rewrite.
Template source can be reverted; already-generated projects remain independent copies.

References: [Android 17](https://developer.android.com/about/versions/17/setup-sdk),
[orientation restrictions](https://developer.android.com/about/versions/17/changes/ff-restrictions-ignored),
[Compose insets](https://developer.android.com/develop/ui/compose/system/insets), and
[Navigation 3 multiplatform](https://kotlinlang.org/docs/multiplatform/compose-navigation-3.html).
