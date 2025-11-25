package com.minddistrict.multiplatformpoc

import org.gradle.api.Project
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType

/**
 * Configures test tasks with standardized settings.
 * 
 * - Disables caching for test tasks (always run)
 * - Configures test logging to show passed, skipped, and failed tests
 * - Sets up JUnit Platform for JVM/Android test tasks
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
}
