import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.minddistrict.multiplatformpoc.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "convention.kmp.library"
            implementationClass = "ConventionKmpLibraryPlugin"
        }
        register("androidApp") {
            id = "convention.android.app"
            implementationClass = "ConventionAndroidAppPlugin"
        }
        register("androidLibrary") {
            id = "convention.android.library"
            implementationClass = "ConventionAndroidLibraryPlugin"
        }
        register("composeMultiplatform") {
            id = "convention.compose.multiplatform"
            implementationClass = "ConventionComposeMultiplatformPlugin"
        }
        register("featureApi") {
            id = "convention.feature.api"
            implementationClass = "ConventionFeatureApiPlugin"
        }
        register("featureImpl") {
            id = "convention.feature.impl"
            implementationClass = "ConventionFeatureImplPlugin"
        }
        register("featureWiring") {
            id = "convention.feature.wiring"
            implementationClass = "ConventionFeatureWiringPlugin"
        }
        register("kmpAndroidApp") {
            id = "convention.kmp.android.app"
            implementationClass = "ConventionKmpAndroidAppPlugin"
        }
        register("coreLibrary") {
            id = "convention.core.library"
            implementationClass = "ConventionCoreLibraryPlugin"
        }
        register("server") {
            id = "convention.server"
            implementationClass = "ConventionServerPlugin"
        }
    }
}
