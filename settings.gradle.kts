rootProject.name = "KMPTemplate"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// ---- core ---------------------------------------------------------------
include(":common:core:model")         // pure Kotlin domain/DTO types, no Android, no Compose
include(":common:core:common")        // AppResult/AppError, dispatchers, small shared utilities
include(":common:core:designsystem")  // theme, typography, shared composables, all user-facing strings
include(":common:core:network")       // Ktor HttpClient factory + base API plumbing

// ---- features -------------------------------------------------------------
// One feature module per screen/flow. Never depend on another :common:feature:*
// module directly -- shared code moves into :core instead.
include(":common:feature:example")

// ---- app ------------------------------------------------------------------
include(":androidApp")
include(":common:sharedApp")
