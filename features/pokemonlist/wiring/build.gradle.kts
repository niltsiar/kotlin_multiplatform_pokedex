plugins {
    id("convention.feature.wiring")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.features.pokemonlist.api)
            implementation(projects.features.pokemonlist.data)
            api(projects.features.pokemonlist.presentation)
            implementation(projects.core.httpclient)
            implementation(libs.ktor.client.core)
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemonlist.wiring"
}
