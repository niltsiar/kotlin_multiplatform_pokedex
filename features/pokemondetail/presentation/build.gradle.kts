plugins {
    id("convention.feature.presentation")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.pokemondetail.api)
            implementation(projects.features.pokemondetail.data)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemondetail.presentation"
}
