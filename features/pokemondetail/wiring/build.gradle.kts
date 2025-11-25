plugins {
    id("convention.feature.wiring")
    id("convention.compose.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.features.pokemondetail.api)
            implementation(projects.core.navigation)
        }
        
        // Platform-specific source sets for UI dependencies
        androidMain.dependencies {
            implementation(projects.features.pokemondetail.ui)
            implementation(libs.androidx.navigation3.ui)  // For EntryProviderScope
        }
        
        jvmMain.dependencies {
            implementation(projects.features.pokemondetail.ui)
            implementation(libs.androidx.navigation3.ui)  // For EntryProviderScope
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemondetail.wiring"
}
