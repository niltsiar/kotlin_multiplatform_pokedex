plugins {
    id("convention.feature.ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.pokemonlist.api)
            implementation(projects.features.pokemonlist.presentation)
            
            // Lifecycle Compose extensions
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            
            // Collections
            implementation(libs.kotlinx.collections.immutable)
            
            // Image Loading
            implementation(libs.coil3.core)
            implementation(libs.coil3.compose)
            implementation(libs.coil3.network)
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemonlist.ui"
}
