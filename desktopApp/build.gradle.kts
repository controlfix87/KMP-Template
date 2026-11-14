import org.jetbrains.compose.desktop.application.dsl.TargetFormat

// Compose Desktop host: runs the shared App on the JVM (`./gradlew :desktopApp:run`) and takes the
// desktop Roborazzi screenshots (`:desktopApp:recordRoborazziDesktop` / `verifyRoborazziDesktop`).
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.roborazzi)
}

kotlin {
    jvmToolchain(21)
    jvm("desktop")

    sourceSets {
        getByName("desktopMain").dependencies {
            implementation(project(":common:sharedApp"))
            implementation(project(":common:core:model"))
            implementation(project(":common:core:designsystem"))
            implementation(project(":common:core:datastore"))
            implementation(project(":common:feature:about"))
            implementation(project(":common:feature:onboarding")) // [onboarding]
            implementation(compose.desktop.currentOs)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.coroutines.swing) // Dispatchers.Main on the desktop
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }
        getByName("desktopTest").dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.roborazzi.compose.desktop)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
            implementation(compose.desktop.uiTestJUnit4)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.cmptemplate.desktop.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "CMPTemplate"
            packageVersion = "1.0.0"
        }
    }
}
