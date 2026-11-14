plugins {
    alias(libs.plugins.cmptemplate.cmp.feature)
}

// About (ControlFix branding, language, theme, links) and the report screen. Reports go to the
// shared ControlFix reports service (AgentsHub tools/reports), e-mail to support is the fallback.
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":common:core:network"))
            implementation(project(":common:core:datastore"))
        }
        commonTest.dependencies { implementation(libs.ktor.client.mock) }
    }
}
