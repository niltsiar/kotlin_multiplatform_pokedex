import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

/**
 * Convention plugin for feature UI modules.
 * 
 * These modules contain:
 * - Compose Multiplatform UI (@Composable functions)
 * - Platform-specific UI code (Android + Desktop JVM + iOS Compose)
 * - Screen implementations
 * 
 * Targets: Android, JVM (Desktop), and iOS (for Compose Multiplatform iOS)
 * Note: Original iOS app uses native SwiftUI, but iosAppCompose uses Compose UI
 * 
 * This plugin COMPOSES other convention plugins rather than duplicating config:
 * - Applies `convention.feature.base` to inherit KMP targets (Android, JVM, iOS),
 *   Android library config, testing, and common deps (Coroutines, Immutable, Arrow)
 * - Applies `convention.compose.multiplatform` to add Compose-related plugins and deps
 *
 * Rationale: Avoids duplication and aligns with Now in Android's layered convention
 * plugins approach. If a given UI module should not include iOS, override targets in
 * the module build file as needed.
 */
class ConventionFeatureUiPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // Compose by layering existing convention plugins
        pluginManager.apply("convention.feature.base")
        pluginManager.apply("convention.compose.multiplatform")
    }
}
