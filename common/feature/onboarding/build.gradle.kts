plugins {
    alias(libs.plugins.cmptemplate.cmp.feature)
}

// OPTIONAL first-run setup. `scripts/new_project.py --without-onboarding` removes this module and
// every line marked `[onboarding]` (or between `region onboarding` / `endregion onboarding`).

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(project(":common:core:datastore"))
        implementation(libs.compose.ui.backhandler)
    }
}
