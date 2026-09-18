plugins { alias(libs.plugins.kmptemplate.kmp.compose) }
kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.common.feature.example)
        implementation(projects.common.core.designsystem)
    }
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework { baseName = "SharedApp"; isStatic = true }
    }
}
