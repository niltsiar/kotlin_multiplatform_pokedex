# Koin Modules Reference

## Defining Modules

```kotlin
// Singleton - single instance shared across app
val httpClientModule = module {
    single<HttpClient> { createHttpClient() }
}

// Factory - new instance each request
val repositoryModule = module {
    factory<MyRepository> { MyRepositoryImpl(get()) }
}

// Named dependency
val apiModule = module {
    single(qualifier = named("baseUrl")) { "https://api.example.com" }
}
```

## Module Architecture

```
core:di/src/commonMain/.../AppModules.kt
├── coreModule(baseUrl: String)
├── httpClientModule()
└── navigationModule()

features:*:wiring/src/commonMain/.../FeatureModule.kt
├── FeatureModule (repos, ViewModels - commonMain)
└── FeatureNavigationModule (platform-specific)
```

## App Initialization

```kotlin
@Composable
fun App() {
    KoinApplication(
        application = {
            modules(
                coreModule(baseUrl = "...") +
                featureModule +
                navigationModule
            )
        }
    ) {
        // App content
    }
}
```

## Dependency Resolution

```kotlin
// In Composables
val viewModel: MyViewModel = koinInject()

// In Module Definitions
factory<MyService> { MyService(get(), get(named("key"))) }

// Named dependencies
single(qualifier = named("apiKey")) { "secret-key" }
factory<ApiService> { ApiService(get(named("apiKey"))) }
```

## Platform Source Sets

```kotlin
// commonMain - all platforms
val commonModule = module {
    factory<Repository> { Repository(get()) }
}

// androidMain/jvmMain - platform UI
val navigationModule = module {
    single<Set<EntryProviderInstaller>> { ... }
}
```
