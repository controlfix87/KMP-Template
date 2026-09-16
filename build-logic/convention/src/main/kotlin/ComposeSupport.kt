import org.gradle.api.Project
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Compose Multiplatform UI wiring shared by [KmpComposeConventionPlugin] and
 * [KmpFeatureConventionPlugin]. Kept separate because :core:designsystem needs
 * the Compose setup but must NOT pick up the feature-only project deps the way
 * [KmpFeatureConventionPlugin] adds for every actual feature.
 */
internal fun Project.applyComposePlugins() {
    pluginManager.apply("org.jetbrains.kotlin.multiplatform")
    pluginManager.apply("com.android.kotlin.multiplatform.library")
    pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
    pluginManager.apply("org.jetbrains.compose")
    pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
}

internal fun KotlinMultiplatformExtension.configureComposeSourceSets(project: Project) {
    val compose = ComposePlugin.Dependencies(project)
    val libs = project.libs

    sourceSets.getByName("commonMain").dependencies {
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material3)
        implementation(compose.ui)
        implementation(compose.components.resources)
        implementation(compose.preview)

        implementation(libs.lib("kotlinx-coroutines-core"))
        implementation(libs.lib("kotlinx-datetime"))
        implementation(libs.lib("kotlinx-serialization-json"))

        implementation(libs.lib("koin-core"))
        implementation(libs.lib("koin-compose"))
        implementation(libs.lib("koin-compose-viewmodel"))

        implementation(libs.lib("androidx-lifecycle-viewmodel"))
        implementation(libs.lib("androidx-lifecycle-viewmodel-compose"))
        implementation(libs.lib("androidx-lifecycle-runtime-compose"))
        implementation(libs.lib("androidx-navigation-compose"))

        implementation(libs.lib("coil-compose"))
        implementation(libs.lib("coil-network-ktor3"))
    }
    sourceSets.getByName("commonTest").dependencies {
        implementation(libs.lib("kotlin-test"))
        implementation(libs.lib("kotlinx-coroutines-test"))
        implementation(libs.lib("assertk"))
        implementation(libs.lib("turbine"))
        implementation(libs.lib("koin-test"))
    }
}
