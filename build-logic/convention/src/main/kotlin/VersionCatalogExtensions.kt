import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.provider.Provider

/**
 * Extension function to get a version from the catalog as a String.
 * 
 * Example: libs.getVersion("android-compileSdk") returns "36"
 */
fun VersionCatalog.getVersion(alias: String): String =
    findVersion(alias).get().toString()

/**
 * Extension function to get a library from the catalog.
 * 
 * Example: libs.getLibrary("androidx-lifecycle-viewmodelCompose")
 */
fun VersionCatalog.getLibrary(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).get()
