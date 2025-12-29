plugins {
    id("convention.feature.ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.designsystemMaterial)
            implementation(projects.features.pokemonlist.api)
            implementation(projects.features.pokemonlist.presentation)
            
            // Image Loading
            implementation(libs.coil3.core)
            implementation(libs.coil3.compose)
            implementation(libs.coil3.network)
            
            // Material 3 Adaptive (for WindowAdaptiveInfo)
            implementation(libs.compose.material3.adaptive)
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemonlist.ui.material"
}
