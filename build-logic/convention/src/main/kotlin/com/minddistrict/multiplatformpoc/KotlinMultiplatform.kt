package com.minddistrict.multiplatformpoc

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import com.minddistrict.multiplatformpoc.libs
import com.minddistrict.multiplatformpoc.getLibrary

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
        // Configure Android target only when Android plugin is applied
        val hasAndroid = pluginManager.hasPlugin("com.android.library") || pluginManager.hasPlugin("com.android.application")
        if (hasAndroid) {
            androidTarget {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
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
                implementation(libs.getLibrary("kotlin-test"))
            }
        }
    }
}
