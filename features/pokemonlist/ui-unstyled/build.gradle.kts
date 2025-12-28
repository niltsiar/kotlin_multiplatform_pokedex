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
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemonlist.ui.unstyled"
}
