# Template and generated project conventions

Read the machine/workspace instructions first. Keep application identity and existing
local edits intact. Use `scripts/new_project.py` only for a new, nonexistent destination;
never generate over an existing product.

Shared modules live below `common/`. Domain contracts and models have no UI, DI,
transport or platform imports. Data implements domain contracts; UI consumes domain
contracts, never concrete data implementations. `di/` and `common/sharedApp` compose
these layers. Features communicate through core contracts and host callbacks, never
through direct dependencies on another feature. Keep DTOs in data and map to domain.

Use the checked-in convention plugins and version catalog. Configure host tests for
all KMP Android libraries. Add behavioral fake-backed tests alongside changes and run
`scripts/verify.sh`; this workspace requires all Gradle execution through `gw` (or
`gradle-queue`), including invoking the verification script with Gradle through `gw`.
Never report a compiled test APK, workflow definition or filename count as runtime
acceptance. Record exact commands, results and unexecuted device/macOS checks in
`docs/VALIDATION.md`.

Read `docs/ALIGNMENT_HANDOFF.md` for architecture standards, migration order and the
portfolio-specific adoption table. Existing product modernization plans remain the
source of product scope and target decisions. Persistence, authentication, billing,
background jobs and additional targets need product-specific implementation and tests.
