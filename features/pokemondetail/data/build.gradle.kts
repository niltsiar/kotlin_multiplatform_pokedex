plugins {
    id("convention.feature.data")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.pokemondetail.api)
            implementation(projects.core.httpclient)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemondetail.data"
}
