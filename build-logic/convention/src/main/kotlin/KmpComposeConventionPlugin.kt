import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Compose Multiplatform library with no dependency on the feature stack.
 * Use this for a shared design-system module (theme, typography, shared
 * composables, strings) that everything else builds on top of -- it must not
 * itself depend on any :feature:* module, or the dependency graph cycles back.
 */
class KmpComposeConventionPlugin : Plugin<Project> {
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
    }
}
