plugins {
    id("convention.kmp.library")
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
        }
    }

    
    sourceSets {
        commonMain.dependencies {
            api(projects.features.pokemonlist.api)
            api(projects.features.pokemonlist.presentation)
        }
    }
}
