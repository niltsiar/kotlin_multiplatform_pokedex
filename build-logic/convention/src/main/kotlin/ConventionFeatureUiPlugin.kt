import com.android.build.gradle.LibraryExtension
import com.minddistrict.multiplatformpoc.libs
import com.minddistrict.multiplatformpoc.getLibrary
import com.minddistrict.multiplatformpoc.getVersion
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

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
 * Note: Does NOT compose convention.feature.base because:
 * - UI modules need explicit target configuration (Android + JVM + iOS)
 * - UI modules have different dependency needs (Compose instead of just base dependencies)
 */
class ConventionFeatureUiPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("com.android.library")
            apply("convention.compose.multiplatform")
        }
        
        extensions.configure<KotlinMultiplatformExtension> {
            // Explicit target configuration: Android + JVM + iOS
            androidTarget {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
            
            jvm {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_11)
                }
            }
            
            // iOS targets for Compose Multiplatform iOS
            listOf(
                iosArm64(),
                iosSimulatorArm64(),
                iosX64()
            )

            // Note: iOS Compose support added for iosAppCompose experimental app
            // iOS uses native SwiftUI, not Compose

            sourceSets.apply {
                commonMain.dependencies {
                    // Arrow for functional error handling
                    implementation(libs.getLibrary("arrow-core"))
                    
                    // Coroutines for async operations
                    implementation(libs.getLibrary("kotlinx-coroutines-core"))
                    
                    // Immutable collections for UI state
                    implementation(libs.getLibrary("kotlinx-collections-immutable"))
                }
                
                commonTest.dependencies {
                    implementation(kotlin("test"))
                }
            }
        }
        
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
    }
}
