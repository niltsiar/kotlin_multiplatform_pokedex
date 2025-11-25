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
 * Convention plugin for feature API modules.
 * 
 * These modules contain:
 * - Public interfaces (repositories, use cases)
 * - Domain models that need to be shared
 * - Navigation contracts
 * 
 * Exported to iOS via :shared umbrella.
 */
class ConventionFeatureApiPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("com.android.library")
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
            compileSdk = libs.findVersion("android-compileSdk").get().toString().toInt()

            defaultConfig {
                minSdk = libs.findVersion("android-minSdk").get().toString().toInt()
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
        }
    }
}
