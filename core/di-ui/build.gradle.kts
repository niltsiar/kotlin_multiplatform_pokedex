plugins {
    id("convention.core.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.koin.core)
            api(projects.core.navigation)
            api(projects.features.pokemonlist.api)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.core.diui"
}
