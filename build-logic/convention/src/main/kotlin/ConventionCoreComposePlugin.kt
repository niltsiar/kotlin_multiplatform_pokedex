import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for Compose-only core modules.
 *
 * Intended for core UI infrastructure that should be available to all Compose targets
 * (Android + Desktop + iOS Compose), e.g. design system or Compose-only DI aggregation.
 *
 * Composes:
 * - `convention.core.library` for KMP targets + Android library config + tests
 * - `convention.compose.multiplatform` for Compose plugins and dependencies
 */
class ConventionCoreComposePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("convention.core.library")
        pluginManager.apply("convention.compose.multiplatform")
    }
}
