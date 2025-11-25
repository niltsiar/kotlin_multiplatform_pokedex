package com.minddistrict.multiplatformpoc

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Configures Compose Multiplatform dependencies for common, Android, and JVM source sets.
 * 
 * This function centralizes the Compose dependency setup that would otherwise be duplicated
 * across multiple feature UI modules.
 */
internal fun Project.configureComposeMultiplatform() {
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
    
    extensions.getByType<KotlinMultiplatformExtension>().apply {
        sourceSets.apply {
            commonMain.dependencies {
                implementation(libs.getLibrary("compose-runtime"))
                implementation(libs.getLibrary("compose-foundation"))
                implementation(libs.getLibrary("compose-material3"))
                implementation(libs.getLibrary("compose-ui"))
                implementation(libs.getLibrary("compose-components-resources"))
                implementation(libs.getLibrary("compose-components-uiToolingPreview"))
            }
            
            androidMain.dependencies {
                implementation(libs.getLibrary("androidx-activity-compose"))
                implementation(libs.getLibrary("compose-ui-tooling"))
            }
            
            jvmMain.dependencies {
                val compose = extensions.getByType<ComposeExtension>().dependencies
                implementation(compose.desktop.currentOs)
            }
        }
    }
}
