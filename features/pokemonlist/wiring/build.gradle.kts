plugins {
    id("convention.feature.wiring")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.pokemonlist.api)
            implementation(projects.features.pokemonlist.data)
            api(projects.features.pokemonlist.presentation)  // API: types exposed in @Provides functions
            implementation(projects.core.httpclient)
            implementation(libs.ktor.client.core)
        }
        
        // Platform-specific source sets for UI dependencies
        // Android and JVM can depend on :ui module
        androidMain.dependencies {
            implementation(projects.features.pokemonlist.ui)
        }
        
        jvmMain.dependencies {
            implementation(projects.features.pokemonlist.ui)
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
