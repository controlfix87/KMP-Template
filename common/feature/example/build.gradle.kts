plugins {
    alias(libs.plugins.cmptemplate.cmp.feature)
}

kotlin {
    sourceSets {
        commonTest.dependencies { implementation(libs.ktor.client.mock) }
        commonMain.dependencies {
            implementation(project(":common:core:network"))
        }
    }
}
