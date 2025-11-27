import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.gradle.kotlin.dsl.dependencies
import com.minddistrict.multiplatformpoc.libs
import com.minddistrict.multiplatformpoc.getLibrary

/**
 * Convention plugin for feature data modules.
 *
 * Composes:
 * - convention.feature.base (targets, Android config, tests, Arrow/Coroutines/Immutable)
 * - Applies Kotlin Serialization plugin
 * - Adds Ktor + Kotlinx Serialization dependencies centralized via libs.versions.toml
 */
class ConventionFeatureDataPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // Layer on top of feature base
        pluginManager.apply("convention.feature.base")
        // Serialization compiler plugin for DTOs
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                // commonMain
                commonMain.dependencies {
                    implementation(libs.getLibrary("ktor-client-core"))
                    implementation(libs.getLibrary("ktor-client-contentNegotiation"))
                    implementation(libs.getLibrary("ktor-serialization-json"))
                    implementation(libs.getLibrary("kotlinx-serialization-json"))
                    implementation(libs.getLibrary("ktor-client-logging"))
                }

                // Android
                androidMain.dependencies {
                    implementation(libs.getLibrary("ktor-client-okhttp"))
                }

                // JVM (Desktop)
                jvmMain.dependencies {
                    implementation(libs.getLibrary("ktor-client-java"))
                }

                // iOS (Darwin)
                // iosMain is present when iOS targets are configured via feature.base
                if (names.contains("iosMain")) {
                    getByName("iosMain").dependencies {
                        implementation(libs.getLibrary("ktor-client-darwin"))
                    }
                }
            }
        }
    }
}
