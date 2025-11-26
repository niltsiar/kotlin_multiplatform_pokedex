plugins {
    id("convention.kmp.library")
    alias(libs.plugins.skie)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export(projects.features.pokemonlist.api)
            export(projects.features.pokemonlist.presentation)
            export(projects.features.pokemondetail.api)
            export(projects.features.pokemondetail.presentation)
        }
    }

    
    sourceSets {
        commonMain.dependencies {
            // Export Pokemon List modules to iOS
            api(projects.features.pokemonlist.api)
            api(projects.features.pokemonlist.presentation)
            
            // Export Pokemon Detail modules to iOS
            api(projects.features.pokemondetail.api)
            api(projects.features.pokemondetail.presentation)
            
            // Dependencies needed for KoinIos.kt
            api(projects.core.di)
            api(projects.features.pokemonlist.wiring)
            api(projects.features.pokemondetail.wiring)
            
            // Koin for dependency injection
            api(libs.koin.core)
        }
    }
}
