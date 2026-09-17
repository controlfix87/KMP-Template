# Adding persistence and background infrastructure

The starter has no database or account secrets. This document is the acceptance
contract for adding them; catalog entries do not mean those capabilities exist.

## Choose storage from product requirements

Use DataStore for small durable preferences, SavedStateHandle for recoverable scalar
screen drafts, and a database for structured durable records/transactional queries.
Room is suitable for Android/iOS/JVM projects; check supported browser targets before
choosing it. Retain SQLDelight in an existing browser-enabled app unless a proven
migration needs a change. Neither plain DataStore nor preferences encrypt tokens;
use Android Keystore-backed encryption and iOS Keychain where credentials are needed.

## Tasks for the first database feature

- [ ] Define domain repository contracts and transaction/uniqueness rules; keep DTOs,
  database entities and platform handles out of screen state.
- [ ] Apply KSP/Room or the chosen SQL plugin only in the module that uses it; verify
  published compatibility instead of deriving a KSP version from Kotlin's version.
- [ ] Configure platform database paths/drivers, dispatcher and application scope
  through DI. Expose observable queries and retain a single database owner.
- [ ] Export and commit schema v1. Add deterministic fixture data and a real database
  integration test with unique IDs, null/optional fields and boundary amounts/dates.
- [ ] Each schema bump adds an explicit migration and upgrades every supported prior
  version in tests. Validate row count, all durable fields and constraints. Never use
  destructive fallback for user-authored records.
- [ ] Test transaction interruption, malformed records, disk/full failures and
  duplicate import retries. Keep cancellation distinct from expected data errors.
- [ ] Define backup inclusion/exclusion and restore behavior. The template disables
  Android backup until a product has reviewed its data/credential policy.
- [ ] Add an offline/reconnect contract: source of truth, stale data display,
  conflict resolution, sync cursor, deletion tombstones and idempotency IDs.
- [ ] Add CI jobs for actual drivers and migrations. JVM in-memory tests do not prove
  Android file permissions, device migration or iOS file-protection behavior.

## Long-running work

Use ViewModel scope for screen work that may be cancelled when the destination is
removed. Use WorkManager on Android for deferrable durable jobs, with unique work,
constraints, retry/backoff and progress IDs saved in durable storage. Use foreground
services only for an appropriate platform-supported use case. Map iOS background work
separately; do not promise identical scheduling semantics across platforms.

Tests must cover enqueue-twice, worker restart, network loss, account change,
notification denial, process death and cancellation. The screen reconnects to the
same operation ID after rotation; it must not enqueue another operation from composition.

## Release and rollback

Build unsigned release artifacts in CI; supply product signing and deployment
credentials externally. Reverting source code does not reverse an applied schema.
Use additive compatible migrations where possible and exercise a restore plan before
shipping irreversible schema or identity changes. Keep release publishing separate
from local compilation/verification.
