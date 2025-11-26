plugins {
    id("convention.feature.wiring")
    id("convention.compose.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.features.pokemondetail.api)
            implementation(projects.features.pokemondetail.data)
            api(projects.features.pokemondetail.presentation)  // API: types exposed in providers
            implementation(projects.core.navigation)
            implementation(projects.core.httpclient)
            implementation(libs.ktor.client.core)
            implementation(libs.koin.core)
        }
        
        // Platform-specific source sets for UI dependencies
        androidMain.dependencies {
            implementation(projects.features.pokemondetail.ui)
            implementation(libs.androidx.navigation3.ui)  // For EntryProviderScope
            implementation(libs.koin.compose)
        }
        
        jvmMain.dependencies {
            implementation(projects.features.pokemondetail.ui)
            implementation(libs.androidx.navigation3.ui)  // For EntryProviderScope
            implementation(libs.koin.compose)
        }
        
        // iOS targets for Compose Multiplatform iOS
        iosMain.dependencies {
            implementation(projects.features.pokemondetail.ui)
            implementation(libs.androidx.navigation3.ui)  // For EntryProviderScope
            implementation(libs.koin.compose)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemondetail.wiring"
}
