plugins {
    id("convention.core.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            
            // Compose Multiplatform Adaptive (works on Android, iOS, Desktop, Web)
            implementation(libs.compose.material3.adaptive)
            implementation(libs.compose.material3.adaptive.layout)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.core.designsystem.core"
}
