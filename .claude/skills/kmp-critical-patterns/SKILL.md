---
name: kmp-critical-patterns
description: Quick reference for 6 core KMP patterns - Impl+Factory, Either Boundary, ViewModel, Navigation 3, Testing, Convention Plugins. Use when Claude needs a fast pattern reminder or initial project setup guidance without full skill context. Triggers - 'show me the patterns', 'quick reference', 'pattern overview', before implementing new features, when switching skills, token-constrained scenarios.
---

# KMP Critical Patterns

6 essential patterns for this Kotlin Multiplatform codebase. Quick reference format - follow links for detailed implementation.

## When to Load This Skill

**MUST load when:**
- User asks for "quick reference" or "pattern overview"
- Token budget is tight and full skill context is too heavy
- Starting new feature implementation
- Need fast pattern reminder without detailed examples

**Do NOT load when:**
- Full implementation guidance needed → use @kmp-developer or @kmp-mobile-expert
- UI-specific work → use @compose-screen or @swiftui-screen
- Testing strategy questions → use @testing-strategy

## Pattern Overview

| # | Pattern | One-Line Rule |
|---|---------|---------------|
| 1 | **Impl+Factory** | Internal `Impl` class + public factory function |
| 2 | **Either Boundary** | Repos return `Either<RepoError, T>`, never throw |
| 3 | **ViewModel** | Pass scope to constructor, NO work in init |
| 4 | **Navigation 3** | Routes in `:api`, providers in wiring |
| 5 | **Testing** | NO CODE WITHOUT TESTS, property + Turbine |
| 6 | **Convention Plugins** | Use feature.* convention plugins for modules |

---

## Pattern 1: Impl + Factory (Koin)

**Key Rule:** Internal `Impl` class + public factory function. Production classes stay DI-agnostic.

```kotlin
// In :data module
internal class PokemonListRepositoryImpl(
    private val api: PokemonListApiService
) : PokemonListRepository {
    override suspend fun getPokemonList(): Either<RepoError, List<Pokemon>> =
        Either.catch { api.fetch().map { it.toDomain() } }
            .mapLeft { it.toRepoError() }
}

// Public factory in same file
fun PokemonListRepository(api: PokemonListApiService): PokemonListRepository =
    PokemonListRepositoryImpl(api)
```

**NEVER:**
- Make `Impl` classes public
- Use `@Inject constructor` in production code
- Create interfaces with single `Impl` differently

---

## Pattern 2: Either Boundary

**Key Rule:** Repositories return `Either<RepoError, T>`. Map errors, never throw.

```kotlin
interface PokemonListRepository {
    suspend fun getPokemonList(): Either<RepoError, List<Pokemon>>
}

// Implementation
Either.catch { api.fetch() }
    .mapLeft { it.toRepoError() }
```

**Error Types:**
- `RepoError.Network` - IO exceptions
- `RepoError.Http` - HTTP error codes
- `RepoError.Unknown` - Everything else

**NEVER:**
- Return nullable types
- Return `Result<T>`
- Throw exceptions from repositories

---

## Pattern 3: ViewModel Pattern

**Key Rule:** Pass scope to constructor, NO work in init, use `onStart()`.

```kotlin
class PokemonListViewModel(
    private val repository: PokemonListRepository,
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : ViewModel(viewModelScope), DefaultLifecycleObserver,
    UiStateHolder<PokemonListUiState, PokemonListUiEvent> {

    override fun onStart(owner: LifecycleOwner) {
        // Initialization here, NOT in init
        loadPokemon()
    }
}
```

**NEVER:**
- Store `CoroutineScope` as field
- Do work in `init` block
- Use default ViewModel() constructor without scope

---

## Pattern 4: Navigation 3 Pattern

**Key Rule:** Routes in `:api`, navigation providers in wiring modules.

```kotlin
// In :api module
@Serializable
object PokemonListRoute

// In :wiring-ui-material module
val pokemonListNavigationModule = module {
    scope<MaterialScope> {
        navigation<PokemonListRoute> { route ->
            PokemonListScreen(
                viewModel = koinViewModel(),
                onBack = { navigator.goBack() }
            )
        }
    }
}
```

**Module Structure:**
- `:api` - Route objects and navigation entry points
- `:wiring-ui-material` - Material Design navigation providers
- `:wiring-ui-unstyled` - Compose Unstyled navigation providers

---

## Pattern 5: Testing Pattern

**Key Rule:** NO CODE WITHOUT TESTS. Property tests for mappers, Turbine for flows.

**Test Distribution:**
- 40% property-based tests (mappers, invariant properties)
- 60% concrete tests (ViewModels, repositories)

**Mapper Test (Property):**
```kotlin
"dto to domain preserves all properties" {
    checkAll(Arb.pokemonDto()) { dto ->
        val domain = dto.toDomain()
        domain.id shouldBe dto.id
        domain.name shouldBe dto.name
    }
}
```

**ViewModel Test (Turbine):**
```kotlin
viewModel.uiState.test {
    awaitItem() shouldBe PokemonListUiState.Loading
    viewModel.onStart(owner)
    testScope.advanceUntilIdle()
    awaitItem() shouldBeInstanceOf PokemonListUiState.Content::class
}
```

**NEVER:**
- Skip tests for production code
- Forget Turbine for StateFlow testing
- Skip error path testing

---

## Pattern 6: Convention Plugins

**Key Rule:** Use `convention.feature.*` plugins for feature modules.

**Plugin Matrix:**

| Plugin | Use For |
|--------|---------|
| `convention.feature.api` | `:api` modules (contracts, models) |
| `convention.feature.data` | `:data` modules (repositories, mappers) |
| `convention.feature.presentation` | `:presentation` modules (ViewModels) |
| `convention.feature.ui` | `:ui-*` modules (Compose screens) |
| `convention.feature.wiring` | `:wiring*` modules (Koin modules) |
| `convention.feature.base` | Feature foundation dependencies |

**build.gradle.kts example:**
```kotlin
plugins {
    id("convention.feature.data")
}
```

---

## Quick Checklist

Before implementing any feature:

- [ ] Repository uses `Either<RepoError, T>`
- [ ] ViewModel passes scope to constructor
- [ ] ViewModel uses `onStart()` not `init`
- [ ] Tests exist for all production code
- [ ] Impl class is `internal`, factory is `public`
- [ ] Feature module uses correct convention plugin
- [ ] Navigation routes in `:api`, providers in wiring

---

## Related Skills

| Skill | When to Use |
|-------|-------------|
| @kmp-developer | Full implementation guidance, feature development |
| @kmp-mobile-expert | ViewModel details, iOS integration, repository patterns |
| @compose-screen | Compose UI implementation |
| @swiftui-screen | SwiftUI iOS screens |
| @testing-strategy | Test planning, strategy decisions |

---

## Documentation Sources

**Full Pattern Guides:**
- [critical_patterns_quick_ref.md](../../../docs/tech/critical_patterns_quick_ref.md) - Detailed pattern explanations
- [conventions.md](../../../docs/tech/conventions.md) - Architecture master reference
- [testing_strategy.md](../../../docs/tech/testing_strategy.md) - Comprehensive testing guide
- [navigation.md](../../../docs/tech/navigation.md) - Navigation 3 deep dive

**Quick References:**
- [testing_quick_ref.md](../../../docs/tech/testing_quick_ref.md) - Testing enforcement quick ref
- [QUICK_REFERENCE.md](../../../docs/QUICK_REFERENCE.md) - Project commands and workflows

**Reference Implementation:**
- `features/pokemonlist/` - Demonstrates all 6 patterns end-to-end
