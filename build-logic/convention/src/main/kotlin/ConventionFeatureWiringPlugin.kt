import com.minddistrict.multiplatformpoc.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin for feature wiring/DI modules.
 * 
 * These modules contain:
 * - Koin module definitions
 * - Dependency graph aggregation
 * 
 * NOT exported to iOS.
 * 
 * Composes: convention.feature.base
 * - KMP targets (Android, JVM, iOS)
 * - Test configuration
 * - Common dependencies (Arrow, Coroutines, Collections)
 */
class ConventionFeatureWiringPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("convention.feature.base")
            // No additional plugins needed - Koin is just a library
        }
        
        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                // Koin dependencies added in individual wiring module build files
            }
        }
    }
}
