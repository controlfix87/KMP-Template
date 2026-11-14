plugins {
    alias(libs.plugins.cmptemplate.kmp.library)
}

// Jetpack DataStore (Preferences) for small app-wide settings on Android, iOS and desktop.
// One DataStore instance per file per process: it is a Koin `single`, never created twice.
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":common:core:model"))
            implementation(libs.datastore.preferences)
        }
    }
}
