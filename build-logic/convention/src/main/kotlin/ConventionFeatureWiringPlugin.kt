import com.minddistrict.multiplatformpoc.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin for feature wiring/DI modules.
 * 
 * These modules contain:
 * - @Provides functions for Metro DI
 * - Dependency graph aggregation
 * - Multi-binding contributions
 * 
 * NOT exported to iOS.
 * 
 * Metro's Gradle plugin automatically:
 * - Adds runtime dependency
 * - Configures Kotlin compiler plugin for code generation (not KSP)
 * - Works with all KMP targets
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
            apply("dev.zacsweers.metro")  // Metro plugin handles everything automatically
        }
        
        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                // No dependencies needed - Metro provides AppScope and other DI utilities
                // Feature-specific dependencies added in individual wiring module build files
            }
        }
    }
}
