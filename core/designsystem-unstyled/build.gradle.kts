plugins {
    id("convention.core.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Core design tokens
            api(projects.core.designsystemCore)
            
            // Compose Unstyled (design system specific) - api so consumers get it transitively
            api(libs.composeunstyled)
            api(libs.composeunstyled.theming)
            api(libs.composeunstyled.platformtheme)
            api(libs.composeunstyled.primitives)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.core.designsystem.unstyled"
}
