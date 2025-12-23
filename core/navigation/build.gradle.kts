plugins {
    id("convention.kmp.library")
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            api(libs.androidx.navigation3.ui)       // API: EntryProviderScope must be visible
            api(libs.koin.compose.navigation3)      // API: navigation<T> DSL for wiring modules
        }
    }
}
