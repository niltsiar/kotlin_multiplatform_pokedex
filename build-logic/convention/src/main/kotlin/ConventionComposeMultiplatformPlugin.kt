import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ConventionComposeMultiplatformPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("org.jetbrains.compose")
            apply("org.jetbrains.kotlin.plugin.compose")
        }

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
        val compose = extensions.getByType<ComposeExtension>().dependencies

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets.apply {
                commonMain.dependencies {
                    // Compose runtime and foundation
                    implementation(compose.runtime)
                    implementation(compose.foundation)
                    implementation(compose.material3)
                    implementation(compose.ui)
                    implementation(compose.components.resources)
                    implementation(compose.components.uiToolingPreview)
                    
                    // Lifecycle
                    implementation(libs.findLibrary("androidx-lifecycle-viewmodelCompose").get())
                    implementation(libs.findLibrary("androidx-lifecycle-runtimeCompose").get())
                }

                androidMain.dependencies {
                    implementation(compose.preview)
                    implementation(libs.findLibrary("androidx-activity-compose").get())
                }

                jvmMain.dependencies {
                    implementation(compose.desktop.currentOs)
                }
            }
        }

        dependencies {
            add("debugImplementation", compose.uiTooling)
        }
    }
}
