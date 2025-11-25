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
        }
    }

    
    sourceSets {
        commonMain.dependencies {
            api(projects.features.pokemonlist.api)
        }
    }
}
