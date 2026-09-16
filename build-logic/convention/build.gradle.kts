plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.compiler.gradle.plugin)
    compileOnly(libs.compose.multiplatform.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("kmpPure") {
            id = "kmptemplate.kmp.pure"
            implementationClass = "KmpPureConventionPlugin"
        }
        register("kmpLibrary") {
            id = "kmptemplate.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("kmpCompose") {
            id = "kmptemplate.kmp.compose"
            implementationClass = "KmpComposeConventionPlugin"
        }
        register("kmpFeature") {
            id = "kmptemplate.kmp.feature"
            implementationClass = "KmpFeatureConventionPlugin"
        }
    }
}
