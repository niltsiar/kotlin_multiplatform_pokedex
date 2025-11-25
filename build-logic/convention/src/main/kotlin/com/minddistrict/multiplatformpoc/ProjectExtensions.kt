package com.minddistrict.multiplatformpoc

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

/**
 * Extension property to access the version catalog in a cleaner way.
 * 
 * Usage:
 * ```kotlin
 * val libs = project.libs
 * implementation(libs.getLibrary("arrow-core"))
 * ```
 */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

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
