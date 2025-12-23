plugins {
    id("convention.feature.wiring-ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.navigation)

            implementation(projects.features.pokemondetail.api)
            implementation(projects.features.pokemondetail.presentation)
            implementation(projects.features.pokemondetail.uiUnstyled)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.navigation3)  // Koin Navigation 3 DSL
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemondetail.wiringui.unstyled"
}
