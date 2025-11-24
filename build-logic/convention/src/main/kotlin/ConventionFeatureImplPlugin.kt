import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for feature implementation modules.
 * 
 * These modules contain:
 * - Internal implementations of API contracts
 * - Repository implementations
 * - Data sources (network, database)
 * - DTO to domain mappers
 * 
 * NOT exported to iOS (only :api modules are exported).
 */
class ConventionFeatureImplPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("convention.kmp.library")
        }
        
        // Impl modules are KMP with typical dependencies for data layer
        // Modules add Ktor, SQLDelight, Arrow, etc. as needed
    }
}
