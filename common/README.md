# Shared KMP code

This directory contains the reusable KMP modules and shared Compose application
composition. Platform entry points remain at the repository root:

- `androidApp/` owns Android application setup and Android-only wiring.
- `iosApp/` owns the Swift/Xcode entry point and iOS packaging.
- `common/` owns `core/`, `feature/`, and `sharedApp/` modules.

Inside each shared module, keep platform boundaries in Kotlin source sets:
`commonMain`/`commonTest`, then `androidMain`, `iosMain`, or another target source
set. Do not import platform APIs into `commonMain`; use an abstraction with a
platform implementation when the behavior genuinely differs.
