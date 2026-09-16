# CLAUDE.md

Agent instructions for working in this repo. Read `README.md` first — module
map, convention plugins, rename checklist, host notes, and hard rules all
live there; this file only adds what an agent specifically needs before
making a change.

## Before touching shared code

Run the purity check and the relevant module's test task, not `./gradlew test`:

```bash
./scripts/check_common_main_purity.sh
./gradlew :core:model:allTests :core:common:allTests testAndroidHostTest :androidApp:testDebugUnitTest
```

## Adding a module

1. Pick a convention plugin from README's table (pure / library / compose / feature).
2. `mkdir -p <module>/src/commonMain/kotlin/...`, one-line `build.gradle.kts`
   applying that plugin (see any existing module's for the shape).
3. `include(":core:whatever")` or `include(":feature:whatever")` in `settings.gradle.kts`.
4. A feature module also needs: a `di/*Module.kt`, registered in both
   `KMPTemplateApplication.kt` and `KoinModulesTest.kt` in the same commit.

## Adding a dependency

Every version goes in `gradle/libs.versions.toml` — never a literal
`implementation("group:artifact:1.2.3")` in a module's `build.gradle.kts`.

## One commit per task

Match the style already in this repo's other sibling projects: one focused
commit per task, message `<short summary>`. Don't bundle an unrelated rename
or dependency bump into a feature commit.

See README.md's "Hard rules" section for the commonMain-purity rule,
Hebrew-locale (`values-iw`) gotcha, and the fake-not-mock test-double
convention — all three are enforced by review/CI, not just documented here.
