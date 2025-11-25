import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("convention.kmp.android.app")
    id("convention.compose.multiplatform")
    alias(libs.plugins.metro)  // Needed for createGraphFactory() extension
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Core DI module contains AppGraph with Metro plugin applied
            implementation(projects.core.di)
            implementation(projects.features.pokemonlist.ui)
            implementation(projects.features.pokemonlist.wiring)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutinesSwing)
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
