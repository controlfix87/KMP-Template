import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * KMP library with an Android target, but no Compose UI.
 * Use this for data-layer modules: network clients, database, datastore.
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.android.kotlin.multiplatform.library")
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

        extensions.configure<KotlinMultiplatformExtension> {
            jvmToolchain(21)
            configureAndroidTarget(this@with)
            if (iosEnabled) {
                iosArm64()
                iosSimulatorArm64()
                iosX64()
            }

            sourceSets.getByName("commonMain").dependencies {
                implementation(libs.lib("kotlinx-coroutines-core"))
                implementation(libs.lib("kotlinx-datetime"))
                implementation(libs.lib("kotlinx-serialization-json"))
                implementation(libs.lib("koin-core"))
            }
            sourceSets.getByName("commonTest").dependencies {
                implementation(libs.lib("kotlin-test"))
                implementation(libs.lib("kotlinx-coroutines-test"))
                implementation(libs.lib("assertk"))
                implementation(libs.lib("turbine"))
            }
        }
    }
}

/**
 * The AGP KMP-library plugin registers `androidLibrary` as a nested extension on
 * the Kotlin extension. A module's own build.gradle.kts gets a generated
 * accessor for it; a precompiled convention plugin does not, so reach it
 * through ExtensionAware instead.
 */
internal fun KotlinMultiplatformExtension.configureAndroidTarget(project: Project) {
    (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryExtension>("androidLibrary") {
        namespace = project.moduleNamespace
        compileSdk = project.libs.version("android-compileSdk").toInt()
        minSdk = project.libs.version("android-minSdk").toInt()

        // Without this the AGP KMP plugin never creates a host-side test task
        // (testAndroidHostTest / androidHostTest), so everything in commonTest
        // silently never runs on the Android target. The warning it prints
        // without this call is easy to miss in a multi-module build log --
        // the tests just quietly do not exist. See CLAUDE.md's test-task-name
        // table before assuming `./gradlew test` covers a KMP module.
        withHostTest {}

        // AGP defaults `androidResources.enable` to false for KMP library
        // modules (unlike plain android libraries, where it defaults to true).
        // Left off, AGP never wires up asset/resource merging for this module,
        // so Compose Multiplatform's generated composeResources assets (see
        // :core:designsystem) are silently dropped and crash a consuming app
        // at runtime with MissingResourceException instead of failing the
        // build. Safe to enable unconditionally -- a no-op for modules with no
        // resources/assets of their own.
        androidResources.enable = true
    }
}
