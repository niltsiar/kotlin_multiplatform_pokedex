plugins {
    id("convention.feature.presentation")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.pokemonlist.api)
            implementation(projects.features.pokemonlist.data)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemonlist.presentation"
}
