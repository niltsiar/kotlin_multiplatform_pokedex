plugins {
    id("convention.core.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            
            // Compose Multiplatform Adaptive (works on Android, iOS, Desktop, Web)
            implementation(libs.compose.material3.adaptive)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.core.designsystem.core"
}
