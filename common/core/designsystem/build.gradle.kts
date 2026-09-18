plugins {
    alias(libs.plugins.kmptemplate.kmp.compose)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.kmptemplate.core.designsystem.generated.resources"
}

// Compose Resources matches a `values-<lang>` directory by exact string against
// `Locale.getDefault().language`. Android reports Hebrew under the withdrawn 1989 ISO code "iw" up
// to and including API 33, and under "he" from API 34 -- so one directory can only ever be right on
// half the fleet, and the failure is silent: Hebrew falls back to `values/` (English) while the
// layout still flips to RTL, giving English text in an RTL layout.
//
// Mirroring at build time covers both without any runtime code. `values-iw/` is committed only so a
// fresh checkout or IDE import has it before this task first runs; the task overwrites it
// identically on every build, so it can never drift from `values-he/`. Edit `values-he/` only.
val mirrorHebrewLegacyResources by tasks.registering(Copy::class) {
    from(layout.projectDirectory.dir("src/commonMain/composeResources/values-he"))
    into(layout.projectDirectory.dir("src/commonMain/composeResources/values-iw"))
}
tasks.matching { task ->
    task.name.startsWith("copyNonXmlValueResourcesFor") ||
        task.name.startsWith("convertXmlValueResourcesFor") ||
        task.name.startsWith("prepareComposeResourcesTaskFor") ||
        task.name.startsWith("generateResourceAccessorsFor")
}.configureEach { dependsOn(mirrorHebrewLegacyResources) }
