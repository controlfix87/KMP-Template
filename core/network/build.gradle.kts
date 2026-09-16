plugins {
    alias(libs.plugins.kmptemplate.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(project(":core:common"))
            // api, not implementation: createHttpClient() returns HttpClient, so every
            // consumer needs this type on its own compile classpath, not just this module's.
            api(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        commonTest.dependencies {
            implementation(libs.ktor.client.mock)
        }
    }
}
