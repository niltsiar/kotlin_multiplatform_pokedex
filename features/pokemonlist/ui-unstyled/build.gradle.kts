plugins {
    id("convention.feature.ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Feature dependencies
            implementation(projects.features.pokemonlist.api)
            implementation(projects.features.pokemonlist.presentation)

            // Core
            implementation(projects.core.designsystemUnstyled)
            implementation(projects.core.designsystemCore)

            // Coil for image loading
            implementation(libs.coil3.compose)
            implementation(libs.coil3.network)

            // Compose Unstyled
            implementation(libs.composeunstyled)
            implementation(libs.composeunstyled.theming)
            implementation(libs.composeunstyled.primitives)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            // Immutable Collections
            implementation(libs.kotlinx.collections.immutable)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemonlist.ui.unstyled"
}
