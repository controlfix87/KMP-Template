import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * A feature module: Compose Multiplatform UI + Koin + navigation, pre-wired
 * against the core modules a feature is allowed to see. Applying this plugin
 * is all a feature's build.gradle.kts should need beyond its own dependencies.
 *
 * User-facing strings do NOT live in feature modules -- they're centralised in
 * :core:designsystem's composeResources so every locale is one diff to check.
 * See CLAUDE.md's localization rule.
 */
class KmpFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        applyComposePlugins()

        extensions.configure<KotlinMultiplatformExtension> {
            jvmToolchain(21)
            configureAndroidTarget(this@with)
            if (iosEnabled) {
                iosArm64(); iosSimulatorArm64(); iosX64()
            }
            configureComposeSourceSets(this@with)
        }

        // The module dependency rule (see README's architecture section) is
        // enforced here rather than left to each feature's build script to
        // remember: features never depend on other features; anything shared
        // moves into :core.
        dependencies {
            add("commonMainImplementation", project(":core:model"))
            add("commonMainImplementation", project(":core:common"))
            add("commonMainImplementation", project(":core:designsystem"))
        }
    }
}
