plugins {
    id("convention.feature.ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Feature dependencies
            implementation(projects.features.pokemonlist.api)
            implementation(projects.features.pokemonlist.presentation)

            // Core design system
            implementation(projects.core.designsystemUnstyled)

            // Coil for image loading
            implementation(libs.coil3.compose)
            implementation(libs.coil3.network)
            
            // Material 3 Adaptive (for WindowAdaptiveInfo)
            implementation(libs.compose.material3.adaptive)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemonlist.ui.unstyled"
}
