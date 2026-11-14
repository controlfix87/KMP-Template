plugins {
    alias(libs.plugins.cmptemplate.kmp.library)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":common:core:model"))
            api(project(":common:core:common"))
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
        getByName("desktopMain").dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        // iosMain exists only when Apple targets are on (macOS); `findByName` ran before the
        // default hierarchy created it, so the Darwin engine was never added. configureEach waits.
        matching { it.name == "iosMain" }.configureEach {
            dependencies { implementation(libs.ktor.client.darwin) }
        }
        commonTest.dependencies {
            implementation(libs.ktor.client.mock)
        }
    }
}
