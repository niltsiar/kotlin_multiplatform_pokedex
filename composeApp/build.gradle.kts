import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("convention.kmp.android.app")
    id("convention.compose.multiplatform")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    // iOS targets for Compose Multiplatform
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            // Core DI module contains AppGraph
            implementation(projects.core.di)
            implementation(projects.core.diUi)
            implementation(projects.core.designsystemMaterial)
            implementation(projects.core.designsystemUnstyled)
            
            // Feature UI modules - Material variants
            implementation(projects.features.pokemonlist.uiMaterial)
            implementation(projects.features.pokemonlist.wiring)
            implementation(projects.features.pokemonlist.wiringUiMaterial)
            implementation(projects.features.pokemondetail.uiMaterial)
            implementation(projects.features.pokemondetail.wiring)
            implementation(projects.features.pokemondetail.wiringUiMaterial)
            
            // Feature UI modules - Unstyled variants
            implementation(projects.features.pokemonlist.uiUnstyled)
            implementation(projects.features.pokemonlist.wiringUiUnstyled)
            implementation(projects.features.pokemondetail.uiUnstyled)
            implementation(projects.features.pokemondetail.wiringUiUnstyled)
            
            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            
            // Navigation 3
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)
            
            // Material 3 Adaptive
            implementation(libs.compose.material3.adaptive)
            implementation(libs.compose.material3.adaptive.layout)
            implementation(libs.compose.material3.adaptive.navigation.suite)

            // Window Size Classes
            implementation(libs.androidx.window.core)
            
            // Kotlinx Serialization (required for SavedStateHandle delegate)
            implementation(libs.kotlinx.serialization.json)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutinesSwing)
        }
        iosMain.dependencies {
            // iOS-specific dependencies if needed
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc"

    defaultConfig {
        applicationId = "com.minddistrict.multiplatformpoc"
        versionCode = 1
        versionName = "1.0"
    }
    
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.minddistrict.multiplatformpoc.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.minddistrict.multiplatformpoc"
            packageVersion = "1.0.0"
        }
    }
}
