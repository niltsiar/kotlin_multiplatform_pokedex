plugins {
    id("convention.kmp.library")
    alias(libs.plugins.metro)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Metro runtime added automatically by Metro plugin
            // Navigation for Navigator and EntryProviderInstaller types
            api(projects.core.navigation)
            // Need presentation module to reference ViewModels in AppGraph
            api(projects.features.pokemonlist.presentation)
            // Need wiring modules as api so Metro can discover @ContributesTo contributions
            api(projects.features.pokemonlist.wiring)
            api(projects.features.pokemondetail.wiring)
        }
    }
}
