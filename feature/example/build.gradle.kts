plugins {
    alias(libs.plugins.kmptemplate.kmp.feature)
}

kotlin {
    sourceSets {
        commonTest.dependencies { implementation(libs.ktor.client.mock) }
        commonMain.dependencies {
            implementation(project(":core:network"))
        }
    }
}
