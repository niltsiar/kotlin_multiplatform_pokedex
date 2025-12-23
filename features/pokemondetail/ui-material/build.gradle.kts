plugins {
    id("convention.feature.ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystemMaterial)
            implementation(projects.features.pokemondetail.api)
            implementation(projects.features.pokemondetail.presentation)
            
            // Coil for image loading
            implementation(libs.coil3.compose)
            implementation(libs.coil3.network)
            
            // Collections
            implementation(libs.kotlinx.collections.immutable)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemondetail.ui"
}
