# Project Conventions (Architecture, Modules, DI, Errors, Testing)

Purpose: Encode the cross-cutting rules we follow across modules and features. These conventions complement topic-specific guides in `.junie/guides/tech`.

## Architecture
- Clean Architecture with vertical slices. Each feature owns its code end-to-end.
- Feature modularization pattern:
  - `:features:<feature>:api` — public contracts to be shared (interfaces, navigation contracts, domain models required by others).
  - `:features:<feature>:impl` — private implementations (repositories, data sources, mappers, UI implementations, DI contributions).
- Only `api` modules are exposed to other features. `impl` must not leak types outside the feature.
- Shared/core modules are allowed (e.g., `:core:domain:api`, `:core:network:api`) but must remain small and focused.

## Dependency Injection (Metro)
- Use Metro for DI across all platforms.
- Root graph: define an `AppGraph` with `@DependencyGraph` in commonMain and a marker scope `AppScope`.
- Use `@ContributesBinding` in feature `impl` modules to bind implementations of `api` contracts into `AppScope`.
- Use multibinding (`@ContributesIntoSet`/`@ContributesIntoMap`) for sets/maps of handlers, loggers, etc.
- Graph extensions: use `@ContributesGraphExtension` for contextual/lifecycle scopes (e.g., logged-in).

## Repository Boundary and Error Handling
- Repositories return Arrow `Either<RepoError, T>` and use `Either.catch { ... }.mapLeft { it.toRepoError() }` to map failures.
- API services throw exceptions and expose DTOs; repositories map DTOs to domain models.
- Avoid returning Kotlin `Result` and avoid null for error signaling at repository boundaries.

## Coroutines & Dispatchers
- Inject dispatchers (IO/Default) rather than hardcoding. Use structured concurrency and cancellation-aware IO.
- Long-lived jobs use an ApplicationScope provided via DI; UI logic uses screen/viewmodel scopes.

## Presentation Layer
- Consume `Either` from repositories and map to UI state (e.g., sealed `UiState` with Loading/Error/Content).
- Keep navigation contracts in feature `api`; implementations in `impl`.

## iOS Shared Umbrella
- The `shared` module produces a single umbrella framework for iOS. Export only required `api` modules and shared contracts. Keep `impl` internal.

## Testing Stack
- Use Kotest as the primary test framework (unit + property-based).
- Use MockK for mocking in multiplatform (JVM/Android supported; use fakes for Native if needed).
- Prefer property-based testing (Kotest `checkAll`/`forAll`) where appropriate (parsers, mappers, invariants).

### Gradle (Multiplatform test deps example)
```kotlin
kotlin {
  sourceSets {
    val commonTest by getting {
      dependencies {
        implementation("io.kotest:kotest-assertions-core:<version>")
        implementation("io.kotest:kotest-framework-engine:<version>")
        implementation("io.kotest:kotest-property:<version>")
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation("io.mockk:mockk:<version>")
      }
    }
    // Add platform-specific MockK or fakes as needed
  }
}
```

## Naming
- Modules: `:features:<feature>:api`, `:features:<feature>:impl`.
- Files: use clear, purpose-revealing names. Avoid `Utils`, `Helper`.
- Tests: mirror the feature and layer names, suffix with `Spec` or `Test`.

## Alignment with Product Docs
- When behavior is product-driven, reference `.junie/guides/project/prd.md` and `.junie/guides/project/user_flow.md`. Resolve conflicts by preferring PRD for data rules and user_flow for sequence/UX.
