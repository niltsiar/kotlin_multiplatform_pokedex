# Dependency Injection Guidelines

Purpose: Establish consistent DI patterns using Metro for Kotlin Multiplatform with compile-time graph validation and vertical-slice feature modules.

References
- Metro docs and API: zacsweers.github.io/metro (compile-time DI, K2, KSP, multiplatform)
- Examples in this guide are adapted from Metro reference docs such as dependency-graphs and aggregation pages.

## Framework Choice
- We use Metro across all platforms. It’s Kotlin Multiplatform-ready and performs full binding graph validation at compile time.
- Keep production classes free of DI annotations where possible. Prefer explicit `@Provides` functions in DI/wiring modules to assemble dependencies.
- Use Metro’s scopes, graph factories, and contribution annotations to wire feature modules cleanly.

## Architecture: Vertical Slices with api/impl
- Each feature lives in its own set of modules using the pattern:
  - :features:<feature-name>:api — public contracts (interfaces, models that need to be shared), navigation contracts, and DI entry points to be consumed by other features.
  - :features:<feature-name>:impl — private implementations, internal mappers, data sources, and DI contributions using Metro annotations.
- Only the `api` modules are visible to other features; `impl` remains private. `impl` contributes bindings to the application graph.

## Location and Structure
- DI code that declares the app/root graph lives in a top-level commonMain module (e.g., `composeApp/src/commonMain/.../di`).
- Feature-specific DI contributions live in each feature’s `impl` module under `src/commonMain/.../di`.
- Platform-specific DI (if needed) is placed in platform source sets. Prefer commonMain where possible.

## Defining the App Graph

```kotlin
// Define an application scope marker
interface AppScope

// Root graph defines app-wide dependencies
@DependencyGraph
interface AppGraph {
  val loggerSet: Set<Logger> // example multibinding

  @DependencyGraph.Factory
  fun interface Factory {
    // Factory can accept runtime inputs using @Provides
    fun create(@Provides baseUrl: String): AppGraph
  }
}
```

Notes
- Use `@DependencyGraph` for the root. Metro generates an `$$MetroGraph` implementation.
- Use a `Factory` to pass runtime parameters (e.g., base URL, configs) using `@Provides`.

## Providing and Binding Dependencies (no annotations on classes)

```kotlin
// Prefer explicit provider functions in a wiring/DI module.
// Classes remain DI-agnostic (no @Inject on constructors).

// Example provider for a client
@Provides
fun provideHttpClient(json: Json, engine: HttpClientEngine): HttpClient = buildHttpClient(json, engine)

// Bind interface to implementation without annotating the class
@Provides
fun provideUserRepository(
  api: UserApiService,
  storage: UserStorage
): UserRepository = RealUserRepository(api, storage)
```

Multibinding (sets/maps)
```kotlin
// If you want to avoid class annotations, provide sets directly
@Provides
fun provideLoggers(
  console: ConsoleLogger,
  crash: CrashLogger,
): Set<Logger> = setOf(console, crash)

// Or if convenient, you may still use Metro multibinding annotations in wiring modules only,
// keeping production classes clean.
```

## Feature Modules and DI

In :features:<name>:api
- Expose only the contracts needed cross-feature (e.g., `UserRepository`, navigation contracts, and any domain/use case interfaces that other features must call). Avoid leaking implementation details.

In :features:<name>:impl
- Implement the contracts. Keep classes DI-agnostic.
- Provide bindings in a feature wiring module via `@Provides` that returns the interface type.
- Provide feature-scoped factories when needed using Metro graph extensions.

```kotlin
// features/profile/api/src/commonMain/.../ProfileRepository.kt
interface ProfileRepository { suspend fun load(): Either<RepoError, Profile> }

// features/profile/impl/src/commonMain/.../RealProfileRepository.kt
internal class RealProfileRepository(
  private val api: ProfileApiService
) : ProfileRepository

// features/profile/wiring/src/commonMain/.../ProfileWiring.kt
@Provides
fun provideProfileRepository(api: ProfileApiService): ProfileRepository = RealProfileRepository(api)
```

### Wiring/Aggregation Modules
- We use wiring modules to assemble and aggregate dependencies for a feature or a group of features, keeping implementation classes free of DI annotations and private to their modules.
- Naming: `:features:<feature>:wiring` (feature-local) or `:wiring:<area>` (cross-feature aggregation).
- Responsibilities:
  - Provide `@Provides` functions that wire implementations to interfaces
  - Aggregate multibindings (e.g., sets of `FeatureEntry` for navigation)
  - Optionally expose graph extensions/factories for contextual scopes

## Graph Extensions for Subgraphs
Use graph extensions for lifecycle or contextual scopes (e.g., logged-in user):

```kotlin
@ContributesGraphExtension(LoggedInScope::class)
interface LoggedInGraph {
  val sessionUser: SessionUser

  @ContributesGraphExtension.Factory(AppScope::class)
  interface Factory {
    fun createLoggedInGraph(@Provides userId: String): LoggedInGraph
  }
}
```

## Initialization
- Construct the app graph at startup via the generated factory. Provide runtime parameters via `@Provides` args.

```kotlin
// Pseudo-code: exact API is generated by Metro for your graph.
// Common options include a companion `invoke` or a generated Factory.

// Example options (one of these will exist after compilation):
// val appGraph: AppGraph = AppGraph(/* @Provides args like baseUrl */)
// val appGraph: AppGraph = AppGraph.Factory.create(/* baseUrl */)

// Keep a singleton reference you can pass where needed
object DI {
  lateinit var appGraph: AppGraph
    private set

  fun init(baseUrl: String) {
    // initialize appGraph using the generated factory for AppGraph
    // e.g., appGraph = AppGraph.Factory.create(baseUrl)
  }
}
```

Tip: The exact invocation depends on the generated API for your graph (see Metro docs). Generally there is a factory or `invoke` operator on the companion.

## Platform-Specific Dependencies
- Use expect/actual when you must bind platform-specific services. Contribute the actuals in platform source sets and expose their contracts in commonMain.

## Best Practices
- Keep DI setup in commonMain where feasible; isolate platform specifics.
- Prefer small graphs and wiring modules over large monoliths.
- Use scoping as needed for repositories, network clients, and stateful services; use unscoped/`factory`-like patterns for ephemeral objects.
- Validate module visibility: only `api` modules are visible to other features; `impl` and `wiring` should not be exported.

## iOS Umbrella (shared module)
- The `shared` module is an umbrella framework for the iOS app. It exports all required feature `api` modules and any public-facing contracts while keeping implementations internal. Ensure Gradle `export` entries include only the `api` modules that the iOS wrapper must see. Do not export `impl` or `wiring` modules.

## Testing Considerations
- For tests, construct small test graphs or directly instantiate classes with fakes/mocks. Metro validates graphs at compile time; prefer unit tests with explicit constructors over heavy DI bootstrapping in tests.

Notes from Metro docs
- Metro performs full binding graph validation and supports K2/KSP. See the official docs for `@DependencyGraph`, `@Provides`, `@Binds`, `@ContributesBinding`, `@ContributesIntoSet`, and graph extension patterns.
