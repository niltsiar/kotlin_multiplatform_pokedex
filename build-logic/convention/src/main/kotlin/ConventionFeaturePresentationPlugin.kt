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

        // Needed for `@Serializable` persisted UI snapshots used with SavedStateHandle.
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                commonMain.dependencies {
                    // Lifecycle Runtime (DefaultLifecycleObserver, LifecycleOwner)
                    implementation(libs.getLibrary("androidx-lifecycle-runtime"))
                    // Lifecycle ViewModel core APIs (commonized in androidx.lifecycle KMP)
                    implementation(libs.getLibrary("androidx-lifecycle-viewmodel"))
                    // SavedStateHandle support (KMP-compatible in lifecycle-viewmodel-savedstate)
                    implementation(libs.getLibrary("androidx-lifecycle-viewmodel-savedstate"))

                    // SavedState registry (required for SavedStateHandle plumbing)
                    // Note: ktx extensions merged into base module as of 1.3.0
                    implementation(libs.getLibrary("androidx-savedstate"))

                    // Kotlinx Serialization runtime (used by SavedStateHandle delegates)
                    implementation(libs.getLibrary("kotlinx-serialization-json"))
                }

                commonTest.dependencies {
                    // Keep commonTest fully multiplatform.
                    // Android-specific lifecycle test utilities are added in androidUnitTest below.
                }

                androidUnitTest.dependencies {
                    // Android-specific lifecycle test utilities (TestLifecycleOwner etc.)
                    implementation(libs.getLibrary("androidx-lifecycle-runtime-testing"))
                }
            }
        }
    }
}
