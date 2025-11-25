import com.android.build.gradle.LibraryExtension
import com.minddistrict.multiplatformpoc.getVersion
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class ConventionAndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.android.library")
            apply("org.jetbrains.kotlin.android")
        }

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

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

        extensions.configure<KotlinAndroidProjectExtension> {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }
    }
}
