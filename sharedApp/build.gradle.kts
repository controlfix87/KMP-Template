plugins { alias(libs.plugins.kmptemplate.kmp.compose) }
kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.feature.example)
        implementation(projects.core.designsystem)
    }
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework { baseName = "SharedApp"; isStatic = true }
    }
}
