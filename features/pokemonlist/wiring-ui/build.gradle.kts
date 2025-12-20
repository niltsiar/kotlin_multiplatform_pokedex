plugins {
    id("convention.feature.wiring-ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.navigation)

            implementation(projects.features.pokemonlist.api)
            implementation(projects.features.pokemonlist.presentation)
            implementation(projects.features.pokemonlist.ui)

            implementation(projects.features.pokemondetail.api)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemonlist.wiringui"
}
