import com.android.build.api.dsl.ApplicationExtension
import com.minddistrict.multiplatformpoc.getVersion
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
 * Convention plugin for Kotlin Multiplatform Android applications.
 * 
 * Configures:
 * - Android target with proper JVM target
 * - JVM target for Desktop
 * - Android application settings (compileSdk, minSdk, targetSdk, packaging)
 * 
 * Use for main app module that has both Android and Desktop (JVM) targets.
 */
class ConventionKmpAndroidAppPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("com.android.application")
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

            sourceSets.apply {
                commonTest.dependencies {
                    implementation(kotlin("test"))
                }
            }
        }

        extensions.configure<ApplicationExtension> {
            compileSdk = libs.getVersion("android-compileSdk").toInt()

            defaultConfig {
                minSdk = libs.getVersion("android-minSdk").toInt()
                targetSdk = libs.getVersion("android-targetSdk").toInt()
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }

            packaging {
                resources {
                    excludes += "/META-INF/{AL2.0,LGPL2.1}"
                }
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
