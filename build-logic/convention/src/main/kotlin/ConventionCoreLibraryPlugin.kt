import com.android.build.gradle.LibraryExtension
import com.minddistrict.multiplatformpoc.configureKmpTargets
import com.minddistrict.multiplatformpoc.configureTests
import com.minddistrict.multiplatformpoc.libs
import com.minddistrict.multiplatformpoc.getVersion
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Convention plugin for core library modules.
 * 
 * Core modules are KMP libraries that need Android support for:
 * - Android-specific implementations (e.g., Ktor OkHttp)
 * - Android namespace for publishing
 * 
 * Configures:
 * - KMP with Android, JVM, and iOS targets
 * - Android library settings
 * - Test configuration
 * 
 * Examples: :core:httpclient, :core:database, :core:util
 * 
 * Note: Does NOT include common feature dependencies (Arrow, Collections) since
 * core modules may not need them. Add dependencies explicitly in module build.gradle.kts.
 */
class ConventionCoreLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.kotlin.multiplatform")
            apply("com.android.library")
        }

        extensions.configure<KotlinMultiplatformExtension> {
            configureKmpTargets(this, includeIos = true)
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

        configureTests()
    }
}
