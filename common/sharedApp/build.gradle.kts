plugins { alias(libs.plugins.cmptemplate.cmp.compose) }
kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.common.feature.example)
        implementation(projects.common.feature.about)
        implementation(projects.common.feature.onboarding) // [onboarding]
        implementation(projects.common.core.designsystem)
        implementation(projects.common.core.model) // AppInfo for the iOS host module
        implementation(projects.common.core.datastore)
    }
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework { baseName = "SharedApp"; isStatic = true }
    }
}
