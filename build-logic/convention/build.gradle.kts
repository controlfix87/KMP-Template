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
            id = "cmptemplate.kmp.pure"
            implementationClass = "KmpPureConventionPlugin"
        }
        register("kmpLibrary") {
            id = "cmptemplate.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("cmpCompose") {
            id = "cmptemplate.cmp.compose"
            implementationClass = "CmpComposeConventionPlugin"
        }
        register("cmpFeature") {
            id = "cmptemplate.cmp.feature"
            implementationClass = "CmpFeatureConventionPlugin"
        }
    }
}
