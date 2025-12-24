plugins {
    id("convention.core.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Core design tokens
            api(projects.core.designsystemCore)
            
            // Compose Unstyled
            implementation(libs.composeunstyled)
            implementation(libs.composeunstyled.theming)
            implementation(libs.composeunstyled.platformtheme)
            implementation(libs.composeunstyled.primitives)

            // Compose Multiplatform
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)

            // Immutable Collections
            implementation(libs.kotlinx.collections.immutable)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.core.designsystem.unstyled"
}
