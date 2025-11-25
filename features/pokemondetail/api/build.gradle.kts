plugins {
    id("convention.feature.api")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // No additional dependencies needed for route objects
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemondetail.api"
}
