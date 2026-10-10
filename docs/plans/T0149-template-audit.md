# T0149: template architecture and infrastructure audit

Scope: KMPTemplate only, with read-only portfolio inspection. Preserve current product
stacks, identity and target choices. Work branch: audit/architecture-infrastructure.

1. Reproduce baseline checker/generator behavior and inspect source/module/DI boundaries.
2. Correct concrete architecture guard, generation and platform CI failures.
3. Add regression fixtures for actual shared paths and inward layer dependencies.
4. Execute static tests and serialized Gradle verification; report host limits explicitly.
5. Leave reusable adoption standards, per-project priorities and validation handoff.
6. Review diff, record task completion/changelog and commit locally; no push or PR.

Baseline: static checker passed; Python suite failed because generator omitted common/.
Architecture checker likewise did not recognize common-prefixed module/accessor paths.
Apple CI used old task/artifact paths. Detailed results: docs/TEMPLATE_AUDIT.md.
