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
                    implementation(libs.getLibrary("compose-runtime"))
                    implementation(libs.getLibrary("compose-foundation"))
                    implementation(libs.getLibrary("compose-material3"))
                    implementation(libs.getLibrary("compose-ui"))
                    implementation(libs.getLibrary("compose-components-resources"))
                    implementation(libs.getLibrary("compose-ui-tooling-preview"))
                    
                    // Lifecycle
                    implementation(libs.getLibrary("androidx-lifecycle-viewmodelCompose"))
                    implementation(libs.getLibrary("androidx-lifecycle-runtimeCompose"))
                }

                androidMain.dependencies {
                    implementation(libs.getLibrary("androidx-activity-compose"))
                }

                jvmMain.dependencies {
                    implementation(compose.desktop.currentOs)
                }
            }
        }

        dependencies {
            add("debugImplementation", libs.getLibrary("compose-ui-tooling"))
        }
    }
}
