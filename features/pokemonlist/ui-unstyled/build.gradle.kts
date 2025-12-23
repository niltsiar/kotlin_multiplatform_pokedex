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
            implementation(projects.core.designsystem)  // Will be designsystem-unstyled once created

            // Compose Unstyled (to be added in Phase 1.4)
            // implementation(libs.composeunstyled)
            // implementation(libs.composeunstyled.theming)
            // implementation(libs.composeunstyled.primitives)

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
