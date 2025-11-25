import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin for feature wiring/DI modules.
 * 
 * These modules contain:
 * - @Provides functions for Metro DI
 * - Dependency graph aggregation
 * - Multi-binding contributions
 * 
 * NOT exported to iOS.
 * When Metro DI is added, this plugin will apply KSP for graph generation.
 */
class ConventionFeatureWiringPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("com.android.library")
            // TODO: Apply KSP and Metro DI when dependencies are added
            // apply("com.google.devtools.ksp")
        }
        
        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        
        extensions.configure<KotlinMultiplatformExtension> {
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

            iosArm64()
            iosSimulatorArm64()
            iosX64()

            sourceSets.apply {
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
