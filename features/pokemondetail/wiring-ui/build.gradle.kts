plugins {
    id("convention.feature.wiring-ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.navigation)

            implementation(projects.features.pokemondetail.api)
            implementation(projects.features.pokemondetail.presentation)
            implementation(projects.features.pokemondetail.ui)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemondetail.wiringui"
}
