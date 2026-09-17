// Root build script intentionally declares plugins only, with `apply false` --
// every module applies what it needs via the build-logic convention plugins
// (see build-logic/convention). This keeps every version resolved from
// gradle/libs.versions.toml, never from a stray `version = "..."` in a
// module's own build.gradle.kts.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.multiplatform) apply false
}
