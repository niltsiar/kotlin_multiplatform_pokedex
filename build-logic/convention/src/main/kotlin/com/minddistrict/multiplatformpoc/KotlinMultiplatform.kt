package com.minddistrict.multiplatformpoc

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Configures KMP targets for the project.
 * 
 * Sets up Android, JVM (Desktop), and optionally iOS targets with standardized compiler options.
 * 
 * @param extension The KotlinMultiplatformExtension to configure
 * @param includeIos Whether to include iOS targets (arm64, simulatorArm64, x64)
 */
internal fun Project.configureKmpTargets(
    extension: KotlinMultiplatformExtension,
    includeIos: Boolean = true,
) {
    extension.apply {
        androidTarget {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }
        
        jvm {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }

        if (includeIos) {
            iosArm64()
            iosSimulatorArm64()
            iosX64()
        }

        sourceSets.apply {
            commonTest.dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
