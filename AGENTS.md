# Template development rules

Read README.md and TASKS.md. Record validation honestly in docs/VALIDATION.md.

- Preserve the runnable offline sample and Android+iOS composition root.
- New project creation uses scripts/new_project.py; never copy local.properties, caches or keys.
- Every dependency version belongs in gradle/libs.versions.toml. Verify compatibility before upgrades.
- No Android/Java imports in commonMain/commonTest. Domain state contains no UI resources.
- Keep feature dependencies independent; use shared contracts and callbacks across features.
- Register actual DI modules once in common/sharedApp/App.kt; resolve feature roots in the app DI test.
- Save minimal route/draft state, retain normal Activity recreation, guard duplicate operations.
- Add English/Hebrew keys and formatting placeholders together.
- Use fake repositories for owned abstractions; cancellation must propagate.
- Run scripts/verify.sh after changes; run assembleRelease for build/dependency changes.
- Device tests require a connected device. Apple tests require macOS; do not label compilation as runtime proof.
- Keep tasks/commits focused and preserve all pre-existing user edits.
