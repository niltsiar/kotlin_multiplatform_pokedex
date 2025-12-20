# Architecture Patterns

Last Updated: November 26, 2025

> Comprehensive code examples for split-by-layer architecture, module structure, and vertical slicing patterns.

## Split-by-Layer Feature Structure

**Each feature is split into focused layer modules:**

```
:features:pokemonlist:api           → Public contracts only
:features:pokemonlist:data          → Network + Data layer (all KMP targets)
:features:pokemonlist:presentation  → ViewModels, UI state (all KMP targets, exported to iOS)
:features:pokemonlist:ui            → Compose UI screens (Android + JVM + iOS Compose)
:features:pokemonlist:wiring        → DI assembly (platform-specific source sets)
```

### Key Rules
1. Each feature has its own network layer (API service, DTOs) in `:data` module
2. Each feature has its own data layer (repositories, mappers) in `:data` module
3. Each feature has its own presentation (ViewModels, UI state) in `:presentation` module - **shared with iOS**
4. Each feature has its own Compose UI in `:ui` module - **Android + JVM + iOS Compose**
5. Wiring uses platform-specific source sets: `commonMain` provides repos/ViewModels, `androidMain`/`jvmMain` provide UI

### Anti-Pattern: Generic Core Modules

❌ **DON'T create generic network/data layers:**
```
:core:network:api        // Generic network layer - WRONG
:core:data:repository    // Generic repository patterns - WRONG
:core:api:service        // Generic API services - WRONG
```

✅ **DO create feature-specific layers:**
```kotlin
// :features:pokemonlist:data/PokemonListApiService.kt
internal interface PokemonListApiService {
    suspend fun getPokemonList(limit: Int, offset: Int): PokemonListDto
}

// :features:pokemonlist:data/PokemonListRepositoryImpl.kt
internal class PokemonListRepositoryImpl(
    private val api: PokemonListApiService
) : PokemonListRepository {
    override suspend fun loadPage(offset: Int): Either<RepoError, PokemonPage> =
        Either.catch {
            val response = api.getPokemonList(20, offset)
            response.results.map { it.toDomain() }.toImmutableList()
        }.mapLeft { it.toRepoError() }
}
```

### Shared Infrastructure (Use Sparingly)

**ONLY create :core modules for:**
- ✅ Design system (`:core:designsystem`) - Material 3 Expressive theme, reusable Compose components
- ✅ Generic utilities used by 3+ features (`:core:util`)
- ✅ Cross-cutting domain types (`:core:domain`)
- ✅ Platform abstractions (`:core:platform`)

## Convention Plugin Architecture

Following [Now in Android](https://github.com/android/nowinandroid) patterns with shared configuration utilities.

### Shared Configuration Utilities

Located in `build-logic/convention/src/main/kotlin/com/minddistrict/multiplatformpoc/`:

```kotlin
// KotlinMultiplatform.kt
internal fun Project.configureKmpTargets() {
    with(extensions.getByType<KotlinMultiplatformExtension>()) {
        androidTarget {
            compilations.all {
                compileTaskProvider.configure {
                    compilerOptions {
                        jvmTarget.set(JvmTarget.JVM_17)
                    }
                }
            }
        }
        
        jvm("desktop")
        
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "ComposeApp"
                isStatic = true
            }
        }
    }
}

// TestConfiguration.kt
internal fun Project.configureTests() {
    val libs = extensions.getByType<LibrariesForLibs>()
    
    extensions.configure<KotlinMultiplatformExtension> {
        sourceSets {
            commonTest.dependencies {
                implementation(kotlin("test"))
            }
            
            androidUnitTest.dependencies {
                implementation(libs.kotest.runner.junit5)
                implementation(libs.kotest.assertions.core)
                implementation(libs.kotest.property)
                implementation(libs.mockk)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.turbine)
            }
        }
    }
}

// ComposeConfiguration.kt
internal fun Project.configureComposeMultiplatform() {
    val libs = extensions.getByType<LibrariesForLibs>()
    
    extensions.configure<ComposeCompilerExtension> {
        // Compose compiler configuration
    }
    
    extensions.configure<KotlinMultiplatformExtension> {
        sourceSets {
            commonMain.dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
            }
        }
    }
}

// ProjectExtensions.kt
internal val Project.libs
    get() = extensions.getByType<LibrariesForLibs>()

internal fun Project.getVersion(alias: String): String =
    libs.versions.findVersion(alias).get().toString()

internal fun Project.getLibrary(alias: String): Provider<MinimalExternalModuleDependency> =
    libs.findLibrary(alias).get()
```

### Base Plugin Composition

```kotlin
// convention.feature.base plugin
class FeatureBasePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            
            configureKmpTargets()  // Shared configuration
            configureTests()       // Automatic test setup
            
            // Common dependencies for all features
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets {
                    commonMain.dependencies {
                        implementation(libs.arrow.core)
                        implementation(libs.kotlinx.coroutines.core)
                        implementation(libs.kotlinx.collections.immutable)
                    }
                }
            }
        }
    }
}

// Feature layer plugins compose the base
// convention.feature.api
plugins {
    id("convention.feature.base")
}

// convention.feature.data
plugins {
    id("convention.feature.base")
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.httpclient)
            implementation(libs.ktor.client.core)
        }
    }
}

// convention.feature.presentation
plugins {
    id("convention.feature.base")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.lifecycle.viewmodel)
        }
    }
}

// convention.feature.ui - Explicit targets (not base)
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    androidTarget()
    jvm("desktop")
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
        }
    }
}
```

**Benefits**: 38% code reduction, single source of truth, automatic dependencies

**See**: [convention_plugins_guide.md](../tech/convention_plugins_guide.md) for complete usage guide

## Module Structure Examples

### Current Modules (Production)

```
:composeApp  → Compose Multiplatform UI (Android + Desktop + iOS)
  ├── commonMain/kotlin    ← Shared Compose UI code
  ├── androidMain/kotlin   ← Android-specific UI
  ├── jvmMain/kotlin       ← Desktop-specific UI
  ├── iosMain/kotlin       ← iOS Compose-specific code
  └── commonTest/kotlin    ← Shared UI tests

:shared      → iOS umbrella framework (exports other modules to native SwiftUI)
  └── build.gradle.kts     ← Configures which modules to export to iOS
              Exports: :features:*:api, :features:*:presentation, :core:*
              Does NOT export: :features:*:data, :features:*:ui, :features:*:wiring

:iosApp      → Native SwiftUI iOS app (production)
  ├── SwiftUI views        ← iOS-specific UI implementation
  └── import Shared        → Accesses KMP modules via shared.framework

:iosAppCompose → Compose Multiplatform iOS app (experimental)
  ├── ContentView.swift    ← Wraps MainViewController from ComposeApp
  └── import ComposeApp    → Accesses Compose UI framework from :composeApp

:server      → Ktor Backend-for-Frontend (BFF)
  └── src/main/kotlin      ← REST API for all clients

:features:pokemonlist     → FULLY IMPLEMENTED ✅
  ├── :api                → Public contracts (exported to iOS)
  ├── :data               → Network + Data layer (NOT exported)
  ├── :presentation       → ViewModels, UI state (exported to iOS)
  ├── :ui                 → Compose UI screens (NOT exported)
  └── :wiring             → DI assembly (NOT exported)

:features:pokemondetail   → IMPLEMENTED ✅
  ├── :api                → Parameterized route: PokemonDetail(id: Int)
  ├── :data               → Nested DTOs, complex mapping
  ├── :presentation       → Parametric ViewModel with ID
  ├── :ui                 → Navigation 3 animations
  └── :wiring             → Platform-specific EntryProviderInstaller

:core:designsystem        → Material 3 Expressive theme
:core:navigation          → Navigation 3 architecture
:core:di                  → Koin DI core
:core:httpclient          → Ktor HttpClient config
```

### iOS Export Configuration

```kotlin
// :shared/build.gradle.kts
kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
            
            // ONLY export these modules to iOS
            export(projects.features.pokemonlist.api)
            export(projects.features.pokemonlist.presentation)
            export(projects.features.pokemondetail.api)
            export(projects.features.pokemondetail.presentation)
            export(projects.core.di)
            export(projects.core.domain)
            
            // NEVER export these modules
            // ❌ projects.features.pokemonlist.data
            // ❌ projects.features.pokemonlist.ui
            // ❌ projects.features.pokemonlist.wiring
            // ❌ projects.composeApp
        }
    }
}
```

## expect/actual Platform Abstractions

```kotlin
// commonMain/Platform.kt (in any KMP module)
expect class Platform() {
    val name: String
    val version: String
}

// androidMain/Platform.android.kt
actual class Platform {
    actual val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
    actual val version: String = android.os.Build.VERSION.RELEASE
}

// iosMain/Platform.ios.kt
actual class Platform {
    actual val name: String = UIDevice.currentDevice.systemName()
    actual val version: String = UIDevice.currentDevice.systemVersion
}

// This module can be exported via :shared umbrella → shared.framework
// Consumed by SwiftUI app in iosApp/

// jvmMain/Platform.jvm.kt
actual class Platform {
    actual val name: String = "Java ${System.getProperty("java.version")}"
    actual val version: String = System.getProperty("os.version")
}
```

## Shared Constants Pattern

```kotlin
// :core:config/src/commonMain/kotlin/Constants.kt
const val SERVER_PORT = 8080
const val API_TIMEOUT_MS = 30_000L
const val API_BASE_URL = "https://pokeapi.co/api/v2"

// Usage in server (Ktor BFF)
fun main() {
    embeddedServer(Netty, port = SERVER_PORT) {
        install(ContentNegotiation) { json() }
        routing { /* routes */ }
    }.start(wait = true)
}

// Usage in Android/Desktop (Compose app)
val client = HttpClient {
    install(HttpTimeout) {
        requestTimeoutMillis = API_TIMEOUT_MS
    }
    defaultRequest {
        url(API_BASE_URL)
    }
}

// Usage in iOS (SwiftUI app via shared.framework)
import Shared

let port = ConstantsKt.SERVER_PORT
let timeout = ConstantsKt.API_TIMEOUT_MS
let baseUrl = ConstantsKt.API_BASE_URL
```

## Module Naming Conventions

```kotlin
// ✅ CORRECT naming patterns
:features:pokemonlist:api
:features:pokemonlist:data
:features:pokemonlist:presentation
:features:pokemonlist:ui
:features:pokemonlist:wiring

:features:pokemondetail:api
:features:pokemondetail:data
:features:pokemondetail:presentation
:features:pokemondetail:ui
:features:pokemondetail:wiring

:core:designsystem
:core:navigation
:core:di
:core:httpclient
:core:domain
:core:util

// ❌ WRONG naming patterns
:pokemonlist                    // Missing :features prefix
:features:pokemon-list          // Hyphenated (use camelCase)
:features:pokemonList           // CamelCase (use lowercase)
:features:pokemonlist:impl      // Use :data, :presentation, :ui instead
:features:pokemonlist:domain    // Domain goes in :api or :presentation
:core:network                   // Too generic, features own their network layer
:core:repository                // Too generic, features own their repositories
```
