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
 */
class ConventionFeatureApiPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("convention.kmp.library")
        }
        
        // API modules are pure KMP, no Android or Compose dependencies by default
        // Modules can add their own dependencies as needed
    }
}
