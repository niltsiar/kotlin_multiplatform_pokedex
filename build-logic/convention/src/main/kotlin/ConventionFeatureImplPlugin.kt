import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin for feature implementation modules.
 * 
 * These modules contain:
 * - Internal implementations of API contracts
 * - Repository implementations
 * - Data sources (network, database)
 * - DTO to domain mappers
 * 
 * NOT exported to iOS (only :api modules are exported).
 */
class ConventionFeatureImplPlugin : Plugin<Project> {
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
            compileSdk = libs.getVersion("android-compileSdk").toInt()

            defaultConfig {
                minSdk = libs.getVersion("android-minSdk").toInt()
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
        }

        // Configure all test tasks (JVM, KMP, Android, iOS) to never be cached
        tasks.withType<AbstractTestTask>().configureEach {
            outputs.upToDateWhen { false }
            testLogging {
                events("passed", "skipped", "failed")
                showStandardStreams = false
            }
        }

        // Configure JUnit Platform specifically for JVM/Android test tasks
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }
}
