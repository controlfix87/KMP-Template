import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Pure-Kotlin multiplatform module: no Android, no Compose.
 *
 * Targets JVM (so a future JVM server or CLI consumes the exact same classes
 * the apps do) plus the Apple targets when opted in. Use this for modules that
 * must stay platform-neutral by construction -- domain models and small shared
 * utilities, the two places you most want a compile error, not a code-review
 * comment, if an `android.*`/`java.time` import sneaks in.
 */
class KmpPureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

        extensions.configure<KotlinMultiplatformExtension> {
            jvmToolchain(21)
            jvm()
            if (iosEnabled) {
                iosArm64()
                iosSimulatorArm64()
                iosX64()
            }

            sourceSets.getByName("commonMain").dependencies {
                implementation(libs.lib("kotlinx-serialization-json"))
                implementation(libs.lib("kotlinx-datetime"))
                implementation(libs.lib("kotlinx-coroutines-core"))
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
