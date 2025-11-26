plugins {
    id("convention.feature.wiring")
    id("convention.compose.multiplatform")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.features.pokemonlist.api)
            implementation(projects.features.pokemonlist.data)
            api(projects.features.pokemonlist.presentation)  // API: types exposed in providers
            implementation(projects.features.pokemondetail.api)  // For PokemonDetail route
            implementation(projects.core.navigation)          // For Navigator and EntryProviderInstaller
            implementation(projects.core.httpclient)
            implementation(libs.ktor.client.core)
            implementation(libs.koin.core)
        }
        
        // Platform-specific source sets for UI dependencies
        // Android and JVM can depend on :ui module
        androidMain.dependencies {
            implementation(projects.features.pokemonlist.ui)
            implementation(libs.androidx.navigation3.ui)  // For EntryProviderScope
            implementation(libs.koin.compose)
        }
        
        jvmMain.dependencies {
            implementation(projects.features.pokemonlist.ui)
            implementation(libs.androidx.navigation3.ui)  // For EntryProviderScope
            implementation(libs.koin.compose)
        }
        
        // iOS targets for Compose Multiplatform iOS
        iosMain.dependencies {
            implementation(projects.features.pokemonlist.ui)
            implementation(libs.androidx.navigation3.ui)  // For EntryProviderScope
            implementation(libs.koin.compose)
        }
        
        // iOS targets use only commonMain (no UI module dependency)
        // iOS will consume ViewModels and repositories from :presentation and :api
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemonlist.wiring"
}
