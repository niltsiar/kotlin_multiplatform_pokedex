import com.minddistrict.multiplatformpoc.configureKmpTargets
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ConventionKmpLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
        }

        extensions.configure<KotlinMultiplatformExtension> {
            // Always delegate to the shared helper which is Android-aware
            configureKmpTargets(this, includeIos = true)
        }

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
}
