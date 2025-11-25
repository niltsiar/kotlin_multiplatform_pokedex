import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for feature API modules.
 * 
 * These modules contain:
 * - Public interfaces (repositories, use cases)
 * - Domain models that need to be shared
 * - Navigation contracts
 * 
 * Exported to iOS via :shared umbrella.
 * 
 * Composes: convention.feature.base
 * - KMP targets (Android, JVM, iOS)
 * - Test configuration
 * - Common dependencies (Arrow, Coroutines, Collections)
 */
class ConventionFeatureApiPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("convention.feature.base")
        }
        
        // API modules typically don't need additional dependencies beyond what the base provides
        // Specific API modules can add dependencies in their build.gradle.kts if needed
    }
}
