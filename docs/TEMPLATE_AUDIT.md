# Template structure, architecture and infrastructure audit

Review date: 2026-10-10. Baseline commit: `7c73d62`.
Task branch: `audit/architecture-infrastructure`.
Scope: this template; read-only inspection of sibling modernization plans/README scope.
No other product was modified, no targets were changed, and nothing was pushed.

## Architecture assessment

The existing structure is appropriate for a small Clean Architecture starter:
`common/core/{model,common,network,designsystem}`, `common/feature/example`, shared
composition at `common/sharedApp`, and Android/Swift platform hosts. Co-location of
feature layers is intentional; it avoids empty modules while keeping imports inward.

Inspected implementation evidence:

- `feature/example/domain/ExampleRepository.kt` defines a portable Flow/result seam.
- `data/ExampleRepositoryImpl.kt` privately owns HTTP DTOs and maps them to core models;
  expected HTTP/IO/serialization failures map to typed results and cancellation propagates.
- `data/DemoExampleRepository.kt` provides default offline rows; the template needs no service.
- `ui/ExampleViewModel.kt` consumes the repository interface, owns saved query and controls
  initial/subsequent refresh work; stateless UI is separate from DI entry points.
- `di/ExampleModule.kt` selects implementation and ViewModel wiring; shared `App.kt`
  owns the exact production DI module list and serializable navigation composition.
- Pure/library/Compose/feature convention plugins separate target capabilities and enable
  Android host tests/resources. Core and features have no observed outward module dependencies.
- Test fixtures cover result behavior, locale selection/storage, ViewModel restoration,
  retries/subscription churn, transport mapping/cancellation, production DI roots and
  Android recreation/rotation/insets. Device tests remain a separate runtime gate.

No mandatory domain/data/UI module explosion or new database/authentication framework
was justified. Product-specific persistence, jobs, auth, server/Wear/Web additions must
follow their own accepted contracts. See [ALIGNMENT_HANDOFF.md](ALIGNMENT_HANDOFF.md).

## Reproduced defects and corrections

| Finding | Evidence and change |
|---|---|
| Generator omitted all shared modules | Baseline Python suite: 8 passed, 1 errored; generated project's localization file did not exist. Old root `core/feature/sharedApp` allowlist replaced with `common`. Added assertions for every shared build file and renamed domain package path. |
| Module guards did not inspect actual layout | Source classification used first path segment `core/feature`, but all modules now begin `common`. Type-safe pattern also missed `projects.common.*`. Normalize both forms; reject core-to-feature/host, cross-feature, feature-to-host and pure-core-to-infrastructure declarations; added failure fixtures. |
| Co-located layers had no automated inward-import guard | Added supplementary commonMain checks for domain framework/infrastructure imports, UI-to-data, data-to-UI, direct cross-feature imports and pure core outer dependencies. Failure fixtures test representative violations and allowed UI-to-domain direction. |
| Apple CI used pre-move task/artifact paths | Corrected framework/common/feature tasks to `:common:*` and artifact path to `common/sharedApp/build`; Swift build uses explicit `iosApp/build` DerivedData so retained host artifacts actually exist. Renamed generated project now runs the full verification gate in CI. Swift host specification already had the correct shared path. |
| Workspace verification bypassed serialized builds | `verify.sh` selects installed `gw`, workspace `~/AgentsHub/bin/gw`, then wrapper for portable CI. Local Gradle run therefore uses the required queue. |
| Documentation navigation/rules absent or stale | Added template/generated project `AGENTS.md`, aligned README module paths and removed the absent parent index link in favor of durable handoff. Generator copies project rules/standards, excludes audit and historical task plans, and resets generated validation. |

Static import/project scanners are guardrails, not a Kotlin or Gradle parser. Dynamic
project declarations, aliases, plugin-injected dependency edges, fully qualified symbol
uses and arbitrary package structures can evade checks. Review and actual compilation
remain required. No dependency version upgrades were attempted.

## Validation evidence

- Baseline checker passed despite the module/generator defects; baseline Python suite
  failed as described above.
- Corrected static checker and 12 Python regressions passed, including generated-copy
  resource/package/module preservation and forbidden architecture fixtures.
- `git diff --check` passed during review.
- Full template Android gate passed in 1m 42s, 230 actionable tasks (162 executed,
  68 from cache). Exact command: `ANDROID_HOME=/home/mobihen87/data/develop/sdk/sdk
  ./scripts/verify.sh`, which invoked `gw` for the Gradle tasks.
- JUnit XML confirms 23 tests, zero failures/errors: 2 common result tests,
  11 locale tests, 4 ViewModel tests, 4 HTTP repository tests, 2 production DI tests.
  Reports: `common/core/common/build/test-results/jvmTest`,
  `common/core/designsystem/build/test-results/testAndroidHostTest`,
  `common/feature/example/build/test-results/testAndroidHostTest`, and
  `androidApp/build/test-results/testDebugUnitTest`.
- Debug/instrumentation APKs assembled and lint passed with the existing
  `DataExtractionRules` warning (API 26 baseline vs API 31 attribute); backup is
  disabled by policy. Lint report: `androidApp/build/reports/lint-results-debug.xml`.
- `python3 scripts/check_apk.py androidApp/build/outputs/apk/debug/androidApp-debug.apk`
  passed: 2 native 64-bit libraries inspected for 16 KB segment/ZIP packaging.
- `bash -n scripts/verify.sh` passed.
- Generated `AuditNotes` (`org.example.auditnotes`) into a new sibling directory and
  ran the same full gate with the installed SDK. Initial invocation passed all 23
  JUnit tests but failed in Android instrumentation-source lint analysis with
  `OutOfMemoryError: Metaspace` after fresh convention/module compilation under the
  host's required 512 MiB metaspace cap. No resource caps or production settings were
  raised. The fresh queued retry passed the full gate in 2m 23s (226 actionable tasks,
  75 executed, 146 up to date, 5 from cache), including lint and both APKs. Generated
  JUnit reports likewise contain 23 tests with zero failures/errors. Static/Python
  checks passed in both invocations; final checker/test-fixture edits were rechecked
  on the source template after generation. The generated debug APK packaging check
  also passed for two native libraries. Retained renamed project:
  `../template-audit-generated`; logs: `generated-verify.log` and
  `generated-verify-retry.log` in the task work directory.

No release build, device lease/runtime test, macOS/Xcode build, Apple simulator run or CI workflow
execution was performed in this audit. Previous `docs/VALIDATION.md` results remain
historical evidence, not results of this branch. Apple CI path correction is source
review/regression-fixture evidence only until its macOS job actually runs.

Logs and task notes are archived in this project's `.agentshub/tasks/done/T0149-work` directory
outside Git. Exact commands/report paths and final build results will be recorded below.
