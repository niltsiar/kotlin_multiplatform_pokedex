plugins {
    id("convention.feature.wiring")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.pokemonlist.api)
            implementation(projects.features.pokemonlist.data)
            implementation(projects.features.pokemonlist.presentation)
            implementation(projects.core.httpclient)
            implementation(libs.ktor.client.core)
        }
        
        // Platform-specific source sets for UI dependencies
        // Android and JVM can depend on :ui module
        val androidMain by getting {
            dependencies {
                implementation(projects.features.pokemonlist.ui)
            }
        }
        
        val jvmMain by getting {
            dependencies {
                implementation(projects.features.pokemonlist.ui)
            }
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
