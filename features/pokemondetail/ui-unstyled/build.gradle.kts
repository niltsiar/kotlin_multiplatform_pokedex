plugins {
    id("convention.feature.ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Feature dependencies
            implementation(projects.features.pokemondetail.api)
            implementation(projects.features.pokemondetail.presentation)

            // Core design system
            implementation(projects.core.designsystemUnstyled)

            // Coil3 for image loading
            implementation(libs.coil3.compose)
            implementation(libs.coil3.network)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemondetail.ui.unstyled"
}
