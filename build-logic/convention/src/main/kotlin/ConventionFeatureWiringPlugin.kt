import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for feature wiring/DI modules.
 * 
 * These modules contain:
 * - @Provides functions for Metro DI
 * - Dependency graph aggregation
 * - Multi-binding contributions
 * 
 * NOT exported to iOS.
 * When Metro DI is added, this plugin will apply KSP for graph generation.
 */
class ConventionFeatureWiringPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("convention.kmp.library")
            // TODO: Apply KSP and Metro DI when dependencies are added
            // apply("com.google.devtools.ksp")
        }
        
        // Wiring modules are minimal - mostly @Provides functions
    }
}
