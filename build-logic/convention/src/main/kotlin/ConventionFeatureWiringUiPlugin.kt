import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import com.minddistrict.multiplatformpoc.libs
import com.minddistrict.multiplatformpoc.getLibrary

/**
 * Convention plugin for feature Compose wiring modules.
 *
 * These modules contain Compose-only wiring such as Navigation 3 destination registration
 * and screen/entry providers. They should never be exported via `Shared.framework`.
 *
 * Targets: Android, JVM (Desktop), and iOS (for iosAppCompose).
 *
 * Composes:
 * - `convention.feature.base` for KMP targets + Android library config + tests + common deps
 * - `convention.compose.multiplatform` for Compose plugins and dependencies
 */
class ConventionFeatureWiringUiPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("convention.feature.base")
        pluginManager.apply("convention.compose.multiplatform")

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                commonMain.dependencies {
                    implementation(libs.getLibrary("koin-compose-viewmodel"))
                    implementation(libs.getLibrary("koin-compose-navigation3"))
                }
            }
        }
    }
}
