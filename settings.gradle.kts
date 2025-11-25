rootProject.name = "MultiplatformPOC"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":composeApp")
include(":server")
include(":shared")

// Core modules
include(":core:designsystem")
include(":core:di")
include(":core:httpclient")

// Pokemon List feature
include(":features:pokemonlist:api")
include(":features:pokemonlist:data")
include(":features:pokemonlist:presentation")
include(":features:pokemonlist:ui")
include(":features:pokemonlist:wiring")

// Pokemon Detail feature (placeholder)
include(":features:pokemondetail:api")