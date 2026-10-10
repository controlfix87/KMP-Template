# KMP architecture and infrastructure alignment handoff

Reviewed 2026-10-10. This is the reusable adoption contract for the template and the
projects under `kmp/`, including `toDev/`. It does not claim that sibling projects were
built or migrated. Existing `docs/MODERNIZATION_PLAN.md`, repository instructions and
product requirements determine each migration's scope. The referenced workspace
`MODERNIZATION.md` was absent at review time; use this self-contained inventory rather
than assuming that link or an old migration completion statement establishes acceptance.

## Architecture contract

The starter uses one compilation module per feature with domain/data/ui/di/navigation
packages. Clean Architecture is enforced by inward dependencies, not by a mandatory
three-module split. Extract layer modules only when ownership, reuse, build isolation
or product complexity justifies them. No empty use-case classes are required for simple
repository delegation; business rules merit use cases when they coordinate or validate work.

| Boundary | Allowed responsibility and dependencies |
|---|---|
| `common/core/model` | Domain value types; Kotlin and portable serialization only; no transport DTOs, UI, DI or platform services |
| `common/core/common` | Portable result/error types and dispatcher contracts; no features, network or design system |
| Feature `domain/` | Repository contracts/business rules using domain models, portable coroutines and common results |
| Feature `data/` | Implement domain contracts; own DTO mapping, cache/persistence and transport failure mapping; no UI/navigation/DI imports |
| Feature `ui/` | State/action/ViewModel and stateless screens; consume domain interfaces; no concrete data/DI imports |
| Feature `di/` | Bind implementations and lifecycle ownership; production selection belongs here |
| Feature `navigation/` | Define serializable destination keys and entries; callbacks for outbound navigation |
| `common/core/network` | Ktor configuration and per-platform engines; transport infrastructure, not product business policy |
| `common/core/designsystem` | Shared resources, themes and locale seams; no feature imports |
| `common/sharedApp` | Shared composition root: feature module list, route serializers, back stack and entry lifecycle |
| Android/iOS hosts | Application setup and platform adapters; no duplicated shared business rules |

Features never import another feature; move reusable contracts to a suitable core/domain
module or orchestrate in the host. Core cannot depend on features or hosts. Keep platform
APIs in target source sets and inject seams/expect-actual implementations. A feature can
remain physically co-located while domain stays framework independent.

`check_project.py` guards common source purity, obvious project dependency declarations,
co-located layer imports, translations and manifest restrictions. It covers direct
`project(":common:...")` and `projects.common...` accessors. It is a static text guard,
not a complete Kotlin/Gradle parser: aliases, dynamic declarations, fully qualified uses
and arbitrary plugin-injected dependencies require review and actual compilation. Do
not treat its success as a complete dependency-graph proof.

## Infrastructure baseline and extension rules

Use checked-in wrapper/checksum, centralized version catalog, convention plugins,
JDK toolchain and repository policy. Pure modules use JVM tests; Android KMP libraries
must enable `withHostTest`, otherwise commonTest can exist without executing. Keep
Compose resources enabled on consuming KMP Android libraries. Check every platform
engine/driver before adding a new target. Keep machine SDK and signing configuration
outside version control.

The default sample is deliberately offline. Its repository contract, DTO mapping,
fake-backed ViewModel tests, real Ktor MockEngine fixtures, explicit cancellation
propagation, production DI resolution and saved-state conventions are the examples
to copy. Replace it when the first real feature supplies equivalent contracts/tests.
Avoid shipping unreachable example routes or obsolete sample bindings after replacement.

A product needing persistence must add its platform drivers, committed schema history,
migration fixtures for every supported shipped version, backup/restore policy and
interruption recovery before replacing its data layer. Follow [PERSISTENCE.md](PERSISTENCE.md).
Authentication requires injectable session storage and refresh/expiry/logout tests.
Background writes/jobs require stable IDs, idempotency, restart and cancellation tests.
Backend products need server contract/integration gates; web/Wear/watchOS targets need
their own compile/runtime lanes. These belong to products that use them, not to all starters.

Save destination IDs/drafts/query, not whole domain objects. Keep ViewModels scoped to
navigation entries and preserve back stack/saveable state above locale re-keying. Test
collector restarts, concurrent retry deduplication and late callbacks. Follow
[LOCALIZATION.md](LOCALIZATION.md) and [TESTING.md](TESTING.md) for locale stability,
restoration, responsive layout, insets, accessibility and genuine process-death acceptance.

## Adoption sequence for an existing product

1. **Baseline:** record repository HEAD/dirty files, target decisions, app/bundle IDs,
   deployed schemas, current verification tasks and actual test reports. Keep failures
   visible; do not rewrite a dirty checkout or overwrite it with generated code.
2. **Guardrails:** port checker logic with the product's real module paths/resources;
   add failure fixtures. Discover exact tasks using `gw projects` and `gw tasks --all`.
   Preserve existing passing gates while adding architecture enforcement.
3. **Feature seam:** choose one bounded feature, extract domain contracts and map
   adapters to them. Add fake-backed business/ViewModel tests before moving its UI.
   Move concrete construction to DI; resolve actual production roots in tests.
4. **State/platform:** verify navigation serialization, draft/process restoration,
   edge-to-edge, locale stability and callbacks. Port conventions incrementally with
   product namespaces and targets. Handle web/Wear/native backend differences explicitly.
5. **Persistence and toolchain:** execute schema continuity separately from toolchain
   or navigation replacement. Keep each change reviewable and revertible; a source
   revert alone does not undo a persisted-data migration.
6. **Acceptance:** execute host/target tests, lint/compile, release shrinking/packaging,
   device journeys and relevant Apple/web/server gates. Attach reports and remaining
   external work; only then update product completion claims.

For a brand-new app, run the generator into a new directory, supply local SDK config,
run the full generated verification gate, then add product features. The generator
preserves source/configuration and project rules while excluding cache/build/signing
files, template modernization/audit reports and historical plans. Validation starts
unverified in each generated copy.

## Portfolio applicability

Paths below are relative to the workspace `kmp/`. This is a read-only inventory of
plans/README scope on 2026-10-10, not a new execution order that overrides product plans.

| Project | Apply first; retain product-specific behavior |
|---|---|
| `KMPTemplate` | Land audit fixes, execute fresh generator and Android gates, then close remaining device/macOS matrix. Keep offline sample and portable contracts. |
| `TabbyMobile` | Template-derived Android/iOS app with independent Kotlin/Rust terminal/SSH feature. Diff shared conventions and checker/generator paths; preserve native bridge, session lifecycle and terminal contracts. Use its release/QA/performance docs; no modernization plan was present. |
| `TokenControl` | Template-derived Android/iOS starter. First reconcile generator/shared directory layout, production DI and actually executed tests with current product scope; no modernization plan was present. |
| `eizeseret` | Follow EIZE plan: separate durable tickets from disposable catalogue cache, preserve booking/browser/deep-link state and backend contracts; keep phone/Wear and Ktor target lanes. |
| `freestore` | Follow FREE plan: Android/Wasm/Ktor server coverage, callback lifecycle and reconnect/error determinism. Preserve platform-specific Wasm code and existing server behavior. Do not impose Apple targets. |
| `jellyMote` | Follow JELLY plan: phone/Wear/watchOS session continuity and remote-command deduplication, media/notification lifecycle, transport tests and each wearable lane. |
| `kamaSamuLi` | Follow KAMA plan: finance snapshot migrations, exact arithmetic and duplicate-entry prevention, injectable Supabase seam, Hebrew/RTL and SwiftUI/shared Apple parity. |
| `toDev/MyCar` | Follow CAR plan: split Android host carefully, SQLDelight schema continuity on Android/iOS/Wasm, cancel repeated observers, restore document/accident drafts and renewal scheduling. |
| `toDev/mobiSync` | Follow SYNC plan: remove platform construction from ViewModels; provider/OAuth seams, cancellation and duplicate contact-photo write tests; define Apple completion boundary. |
| `toDev/babylog` | Follow BABY plan at nested `kmp/` build root. Preserve existing web/Firebase contract; isolate health-related rules and caregiver/offline conflict fixtures; maintain separate web/KMP CI. |
| `toDev/drivecontrol` | Specification-first Android/iOS/Web/Supabase track. Confirm present code and frozen product contract before bootstrap; add tenant/role and scheduling concurrency fixtures. Specification and plan naming differ; reconcile product identity before generating. |
| `toDev/mobibiz-kmp` | Same BIZ planning family but a distinct product repository: confirm identity and schema rather than copying drivecontrol wholesale. Preserve Android/iOS/Web and Supabase requirements; template does not yet supply a Web target. |
| `toDev/subtrack` | Intentionally Python/FastAPI/Jinja/HTMX, not KMP. Adopt inward application/domain/adapters boundaries, pytest/backend gates, recurrence/money/idempotency fixtures, SQLite migrations/backups and responsive web acceptance. Do not apply Kotlin plugins, Compose or mobile targets. |
| `FreeStore-Patch-Recipes` | Data-only recipe submodule, not a KMP app. Preserve JSON/index-generation contract and FreeStore server JVM fixtures; no mobile architecture rewrite. |
| `freestore-account-recovery` | No modernization plan/README was identified; treat as a local auxiliary workspace until its owner/scope is established. Do not bootstrap or modify based on its directory name. |

## Exact template gates

```bash
python3 scripts/check_project.py
python3 -m unittest discover -s scripts/tests -v
./scripts/verify.sh
# On this workspace all Gradle builds are serialized through gw:
gw :androidApp:assembleRelease
python3 scripts/check_apk.py androidApp/build/outputs/apk/release/androidApp-release-unsigned.apk
# Obtain a device lease before using an attached device:
gw :androidApp:connectedDebugAndroidTest
# macOS only, Apple Silicon:
./scripts/ios-project.sh
gw -Pkmptemplate.enableIos=true :common:sharedApp:linkDebugFrameworkIosSimulatorArm64 :common:core:common:iosSimulatorArm64Test :common:feature:example:iosSimulatorArm64Test
```

`verify.sh` uses `gw` when available, including the workspace tool path, and otherwise
uses the wrapper on portable CI. It runs static/Python tests, pure allTests, KMP
`testAndroidHostTest`, app unit tests, lint and debug/instrumentation APK assembly.
The macOS CI additionally builds the Swift host. Use the appropriate existing product
scripts and source-set tasks when adopting this baseline; these commands are not
universally valid across the portfolio. First builds may need dependency downloads.
On the capped ARM64 workspace, an entirely renamed clean compilation can exhaust
Gradle metaspace during lint after Kotlin compilation. Record that failure; a fresh
serialized invocation can reuse successful compilation and complete lint. Preserve
host resource caps rather than raising heap/daemon settings. A recovered incremental
run is not a claim that the first uncached invocation succeeded.

## Completion checklist and next-project handoff

- [ ] Baseline commit/dirty files, toolchain and target matrix recorded.
- [ ] Core/feature dependency direction and domain/data/UI import boundaries inspected.
- [ ] Static guards include the actual layout and fail seeded regressions.
- [ ] Production DI roots resolve with only platform seams substituted in host tests.
- [ ] Behavior tests cover failures, cancellation, concurrency and state restoration.
- [ ] Existing durable schemas and app/bundle identity preserved with migration evidence.
- [ ] Host test tasks actually execute; record JUnit counts and artifact paths.
- [ ] Relevant release/packaging/device/Apple/web/server acceptance executed or marked unexecuted.
- [ ] Product-specific target choices and remaining backlog retained.
- [ ] Changed files, exact commands/results, external blockers and rollback notes documented.

Each adopting project should write a dated `docs/ALIGNMENT_STATUS.md` containing:
current/template baseline commits; scope/targets; architectural deviations with reasons;
changes by adoption phase; tests with passed/failed/skipped counts and report paths;
unexecuted acceptance and owner; database/identity continuity; next ordered task and
rollback boundary. Copy standards selectively; never copy template validation as
that project's evidence. The source template records current audit results in `docs/TEMPLATE_AUDIT.md`; that
historical report is excluded from generated copies.
