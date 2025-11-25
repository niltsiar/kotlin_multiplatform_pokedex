plugins {
    id("convention.kmp.library")
    alias(libs.plugins.metro)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Metro runtime added automatically by Metro plugin
            // Need presentation module to reference ViewModels in AppGraph
            api(projects.features.pokemonlist.presentation)
            // Need wiring module as api so Metro can discover @ContributesTo contributions
            api(projects.features.pokemonlist.wiring)
        }
    }
}
