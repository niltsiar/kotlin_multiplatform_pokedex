plugins {
    id("convention.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Koin core for all platforms
            api(libs.koin.core)

            // iOS + KMP ViewModel infrastructure (used by both Shared.framework and ComposeApp.framework)
            api(libs.androidx.lifecycle.viewmodel)
            api(libs.androidx.lifecycle.runtime)
        }
    }
}
