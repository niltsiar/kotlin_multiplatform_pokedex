import com.android.build.gradle.LibraryExtension
import com.minddistrict.multiplatformpoc.configureKmpTargets
import com.minddistrict.multiplatformpoc.configureTests
import com.minddistrict.multiplatformpoc.libs
import com.minddistrict.multiplatformpoc.getLibrary
import com.minddistrict.multiplatformpoc.getVersion
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Base convention plugin for all feature modules.
 * 
 * This plugin provides common configuration that all feature layers (api, data, presentation, 
 * ui, wiring) can compose:
 * - KMP targets (Android, JVM, iOS)
 * - Android library configuration
 * - Test configuration
 * - Common feature dependencies (Arrow, Coroutines, Collections)
 * 
 * Layer-specific plugins (api, data, presentation, ui, wiring) should apply this plugin
 * and then add their specific configuration on top.
 * 
 * Note: UI modules should NOT use this plugin as they need explicit target configuration
 * (Android + JVM only, no iOS).
 */
class ConventionFeatureBasePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("com.android.library")
        }
        
        // Configure KMP targets (Android, JVM, iOS)
        extensions.configure<KotlinMultiplatformExtension> {
            configureKmpTargets(this, includeIos = true)
            
            // Add common feature dependencies
            sourceSets.apply {
                commonMain.dependencies {
                    // Arrow for functional error handling
                    implementation(libs.getLibrary("arrow-core"))
                    
                    // Coroutines for async operations
                    implementation(libs.getLibrary("kotlinx-coroutines-core"))
                    
                    // Immutable collections for UI state
                    implementation(libs.getLibrary("kotlinx-collections-immutable"))
                }
            }
        }
        
        // Configure Android library
        extensions.configure<LibraryExtension> {
            compileSdk = libs.getVersion("android-compileSdk").toInt()

            defaultConfig {
                minSdk = libs.getVersion("android-minSdk").toInt()
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
        }
        
        // Configure tests
        configureTests()
    }
}
