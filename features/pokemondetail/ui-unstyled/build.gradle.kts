plugins {
    id("convention.feature.ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Feature dependencies
            implementation(projects.features.pokemondetail.api)
            implementation(projects.features.pokemondetail.presentation)

            // Core
            implementation(projects.core.designsystem)  // Will be designsystem-unstyled once created

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
    namespace = "com.minddistrict.multiplatformpoc.features.pokemondetail.ui.unstyled"
}
