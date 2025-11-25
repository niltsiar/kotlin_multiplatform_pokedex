plugins {
    id("convention.feature.api")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.core.designsystem)  // For AppDestination navigation contract
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemondetail.api"
}
