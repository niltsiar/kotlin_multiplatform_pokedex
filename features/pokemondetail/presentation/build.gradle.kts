plugins {
    id("convention.feature.impl")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.pokemondetail.api)
            implementation(projects.features.pokemondetail.data)
            
            // Lifecycle & ViewModel
            implementation(libs.androidx.lifecycle.viewmodel)
            
            // Collections
            implementation(libs.kotlinx.collections.immutable)
            
            // Arrow
            implementation(libs.arrow.core)
            
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        
        androidUnitTest.dependencies {
            implementation(libs.kotest.assertions)
            implementation(libs.kotest.framework)
            implementation(libs.kotest.property)
            implementation(libs.kotest.runner.junit5)
            implementation(libs.kotest.assertions.arrow)
            implementation(libs.mockk)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemondetail.presentation"
}
