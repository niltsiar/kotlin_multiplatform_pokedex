import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import com.minddistrict.multiplatformpoc.libs
import com.minddistrict.multiplatformpoc.getLibrary

/**
 * Convention plugin for feature presentation modules.
 *
 * Composes:
 * - convention.feature.base (targets, Android config, tests, Arrow/Coroutines/Immutable)
 * - Adds Lifecycle/ViewModel dependencies used across platforms
 */
class ConventionFeaturePresentationPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        // Base feature configuration
        pluginManager.apply("convention.feature.base")

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                commonMain.dependencies {
                    // Lifecycle ViewModel core APIs (commonized in androidx.lifecycle KMP)
                    implementation(libs.getLibrary("androidx-lifecycle-viewmodel"))
                }
            }
        }
    }
}
