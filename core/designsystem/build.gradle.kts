plugins {
    alias(libs.plugins.kmptemplate.kmp.compose)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.kmptemplate.core.designsystem.generated.resources"
}
