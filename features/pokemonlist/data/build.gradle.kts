plugins {
    id("convention.feature.data")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.pokemonlist.api)
            implementation(projects.core.httpclient)
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        
        androidUnitTest.dependencies {
            implementation(libs.kotest.assertions)
            implementation(libs.kotest.framework)
            implementation(libs.kotest.property)
            implementation(libs.kotest.runner.junit5)
            implementation(libs.kotest.assertions.arrow)
            implementation(libs.mockk)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemonlist.data"
}
