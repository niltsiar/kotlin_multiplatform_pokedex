plugins {
    id("convention.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Koin core for all platforms
            api(libs.koin.core)
        }
    }
}
