import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Convention plugin for Ktor server modules.
 *
 * Configures:
 * - Kotlin JVM plugin
 * - Ktor plugin for server tasks (run, fatJar)
 * - Application plugin for mainClass configuration
 * - Java 11 / JVM Target 11
 * - Test configuration (kotlin-test-junit, no JUnit Platform)
 * - Common server dependencies (logback, ktor-server-core)
 *
 * Apply to: :server module and future Ktor backend modules
 */
class ConventionServerPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.jvm")
            apply("io.ktor.plugin")
            apply("application")
        }

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

        // Configure Java version
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }

        // Configure Kotlin JVM target
        extensions.configure<KotlinJvmProjectExtension> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }

        // Configure test tasks
        // Note: Server uses kotlin-test-junit (NOT JUnit Platform)
        tasks.withType<Test>().configureEach {
            outputs.upToDateWhen { false }
            testLogging {
                events("passed", "skipped", "failed")
                showStandardStreams = false
            }
            // DO NOT call useJUnitPlatform() - server uses kotlin-test-junit
        }

        // Add common server dependencies
        dependencies {
            add("implementation", libs.getLibrary("logback"))
            add("implementation", libs.getLibrary("ktor-serverCore"))
            add("testImplementation", libs.getLibrary("ktor-serverTestHost"))
            add("testImplementation", libs.getLibrary("kotlin-testJunit"))
        }
    }
}
