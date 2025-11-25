import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("convention.compose.multiplatform")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm()
    
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    
    sourceSets {
        commonMain.dependencies {
            implementation(projects.features.pokemonlist.api)
            implementation(projects.core.httpclient)
            
            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.json)
            
            // Serialization
            implementation(libs.kotlinx.serialization.json)
            
            // Arrow
            implementation(libs.arrow.core)
            
            // Lifecycle & ViewModel
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            
            // Collections
            implementation(libs.kotlinx.collections.immutable)
            
            // Image Loading
            implementation(libs.coil3.core)
            implementation(libs.coil3.compose)
            implementation(libs.coil3.network)
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        
        jvmTest.dependencies {
            implementation(libs.kotest.assertions)
            implementation(libs.kotest.framework)
            implementation(libs.kotest.property)
            implementation(libs.kotest.runner.junit5)
            implementation(libs.kotest.assertions.arrow)
            implementation(libs.mockk)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "com.minddistrict.multiplatformpoc.features.pokemonlist.impl"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
