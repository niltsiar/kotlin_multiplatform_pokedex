plugins {
    id("convention.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Koin core for all platforms
            api(libs.koin.core)
            
            // Navigation for Navigator and EntryProviderInstaller types
            api(projects.core.navigation)
            // Need presentation module to reference ViewModels in AppGraph
            api(projects.features.pokemonlist.presentation)
            // Need wiring modules as api so Koin can discover modules
            api(projects.features.pokemonlist.wiring)
            api(projects.features.pokemondetail.wiring)
        }
    }
}
