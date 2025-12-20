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
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.kotlin.serialization.gradlePlugin)
    implementation(libs.compose.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
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
        register("featureBase") {
            id = "convention.feature.base"
            implementationClass = "ConventionFeatureBasePlugin"
        }
        register("featureApi") {
            id = "convention.feature.api"
            implementationClass = "ConventionFeatureApiPlugin"
        }
        register("featureData") {
            id = "convention.feature.data"
            implementationClass = "ConventionFeatureDataPlugin"
        }
        register("featurePresentation") {
            id = "convention.feature.presentation"
            implementationClass = "ConventionFeaturePresentationPlugin"
        }
        register("featureWiring") {
            id = "convention.feature.wiring"
            implementationClass = "ConventionFeatureWiringPlugin"
        }
        register("featureWiringUi") {
            id = "convention.feature.wiring-ui"
            implementationClass = "ConventionFeatureWiringUiPlugin"
        }
        register("featureUi") {
            id = "convention.feature.ui"
            implementationClass = "ConventionFeatureUiPlugin"
        }
        register("kmpAndroidApp") {
            id = "convention.kmp.android.app"
            implementationClass = "ConventionKmpAndroidAppPlugin"
        }
        register("coreLibrary") {
            id = "convention.core.library"
            implementationClass = "ConventionCoreLibraryPlugin"
        }
        register("coreCompose") {
            id = "convention.core.compose"
            implementationClass = "ConventionCoreComposePlugin"
        }
        register("server") {
            id = "convention.server"
            implementationClass = "ConventionServerPlugin"
        }
    }
}
