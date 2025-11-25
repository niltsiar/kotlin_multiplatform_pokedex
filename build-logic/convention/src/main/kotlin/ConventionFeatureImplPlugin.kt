import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for feature implementation modules (data, presentation).
 * 
 * These modules contain:
 * - Internal implementations of API contracts
 * - Repository implementations
 * - Data sources (network, database)
 * - DTO to domain mappers
 * - ViewModels and UI state
 * 
 * Data modules: NOT exported to iOS (only :api modules are exported)
 * Presentation modules: Exported to iOS via :shared umbrella (ViewModels shared across platforms)
 * 
 * Composes: convention.feature.base
 * - KMP targets (Android, JVM, iOS)
 * - Test configuration
 * - Common dependencies (Arrow, Coroutines, Collections)
 * 
 * Implementation-specific dependencies should be added in individual module build.gradle.kts files.
 */
class ConventionFeatureImplPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("convention.feature.base")
        }
        
        // Implementation modules add layer-specific dependencies in their build.gradle.kts:
        // - Data modules: Ktor, Serialization, etc.
        // - Presentation modules: Lifecycle, ViewModel, etc.
    }
}
