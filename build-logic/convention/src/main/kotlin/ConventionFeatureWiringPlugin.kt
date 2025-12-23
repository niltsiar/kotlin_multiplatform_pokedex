import com.minddistrict.multiplatformpoc.libs
import com.minddistrict.multiplatformpoc.getLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin for feature wiring (business DI) modules.
 *
 * These modules contain business-layer Koin modules (repositories, view models, etc.).
 *
 * Boundary rules:
 * - MUST stay Compose-agnostic.
 * - MUST NOT depend on Navigation 3 UI / Compose-only wiring.
 * - MAY be exported via `Shared.framework` to support the native SwiftUI `iosApp`.
 */
class ConventionFeatureWiringPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("convention.feature.base")
            // No additional plugins needed - Koin is just a library
        }
        
        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                commonMain.dependencies {
                    // Enables `viewModel { }` DSL in commonMain wiring modules (Compose-agnostic)
                    implementation(libs.getLibrary("koin-core-viewmodel"))
                }
            }
        }
    }
}
