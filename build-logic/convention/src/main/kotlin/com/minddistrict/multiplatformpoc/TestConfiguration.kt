package com.minddistrict.multiplatformpoc

import org.gradle.api.Project
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Configures test tasks with standardized settings and dependencies.
 * 
 * - Disables caching for test tasks (always run)
 * - Configures test logging to show passed, skipped, and failed tests
 * - Sets up JUnit Platform for JVM/Android test tasks
 * - Adds standard test dependencies (kotlin-test for commonTest, Kotest+MockK for androidUnitTest)
 */
internal fun Project.configureTests() {
    // Configure all test tasks (JVM, KMP, Android, iOS) to never be cached
    tasks.withType<AbstractTestTask>().configureEach {
        outputs.upToDateWhen { false }
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = false
        }
    }

    // Configure JUnit Platform specifically for JVM/Android test tasks
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
    
    // Add standard test dependencies
    extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.apply {
            commonTest.dependencies {
                implementation(libs.getLibrary("kotlin-test"))
                implementation(libs.getLibrary("kotlinx-coroutines-test"))
            }
            
            androidUnitTest.dependencies {
                implementation(libs.getLibrary("kotest-assertions"))
                implementation(libs.getLibrary("kotest-framework"))
                implementation(libs.getLibrary("kotest-property"))
                implementation(libs.getLibrary("kotest-runner-junit5"))
                implementation(libs.getLibrary("kotest-assertions-arrow"))
                implementation(libs.getLibrary("mockk"))
                implementation(libs.getLibrary("kotlinx-coroutines-test"))
                implementation(libs.getLibrary("turbine"))
            }
        }
    }
}
