# Template completion tasks

Review date: 2026-09-17. This file tracks the requested implementation, not tasks in the
other applications. Detailed future migration tasks are in each project's
`docs/MODERNIZATION_PLAN.md` and the workspace `../MODERNIZATION.md` index.

| ID | Status | Work and acceptance |
|---|---|---|
| TPL-001 | done | Inventory baseline, preserve local edits, run original host tests before changes |
| TPL-002 | done | Repair fail-open purity checker; test a nested platform import regression |
| TPL-003 | done | Compile/target API 37, retain min 26; explicit edge-to-edge, IME resize, normal recreation |
| TPL-004 | done | Offline runnable sample, working detail/back navigation and duplicate destination guard |
| TPL-005 | done | Nav3 shared serializable back stack, per-entry ViewModels/saveable state |
| TPL-006 | done | Saved search, stable list keys, one initial load and single in-flight retry; behavioral tests |
| TPL-007 | done | HTTP timeout/status/serialization/cancellation handling with MockEngine tests and HTTPS defaults |
| TPL-008 | done | Central production DI module list and resolution test; Android and iOS hosts consume shared UI |
| TPL-009 | done | Darwin engine, Apple Silicon framework and SwiftUI/XcodeGen host; macOS CI definition |
| TPL-010 | done | Generator validates identities, excludes machine files and renames sources; Python tests |
| TPL-011 | done | English/Hebrew resource parity, manifest/XML checks and module boundary guardrails |
| TPL-012 | done | Host/unit/APK tests, lint, release shrinking pass; renamed-project (`SmokeApp`) smoke build passes `scripts/verify.sh` end-to-end |
| TPL-013 | external validation | Run instrumentation on API 26/35/37, real process-death/tablet/IME checks; no device currently attached |
| TPL-014 | external validation | Run macOS framework tests, Swift host build and iOS simulator journey; local host is ARM64 Linux |
| TPL-015 | done | Fixed a missing `onClose` import build bug found while running the gate; final code review pass; `docs/VALIDATION.md` records executed vs. external-validation evidence |
| TPL-016 | done | Language stack in `core:designsystem/i18n`: English/Hebrew/Russian/French by default; one `LocaleManager` and one picker as the only way to change it; the layered Android fix that stops the platform resetting the app to the device language (Application context override, API 33+ per-app locale, config-change/resume re-assert, composition drift heal); `values-he` → `values-iw` build-time mirror so Hebrew resolves on API ≤ 33. `AppLocaleTest` (11 tests) plus a required-locale check in `scripts/check_project.py`; rationale in `docs/LOCALIZATION.md`. **iOS actuals are written but unexecuted** (ARM64 Linux host) and carry a documented CMP 1.11.1 limitation |

## Completion policy

Source/configuration implementation and actual platform validation are separate.
Do not mark device/macOS checks complete from a build script or from compiling an APK.
For a new product, add its database/authentication/background tasks only when needed,
with migrations, DI and tests in the same feature change.
