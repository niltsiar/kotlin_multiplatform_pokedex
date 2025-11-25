plugins {
    id("convention.feature.api")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.designsystem)  // For AppDestination navigation contract
            api(libs.arrow.core)
            api(libs.kotlinx.coroutines.core)
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemonlist.api"
}
