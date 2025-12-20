plugins {
    id("convention.feature.wiring")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.features.pokemondetail.api)
            implementation(projects.features.pokemondetail.data)
            api(projects.features.pokemondetail.presentation)
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
    namespace = "com.minddistrict.multiplatformpoc.features.pokemondetail.wiring"
}
