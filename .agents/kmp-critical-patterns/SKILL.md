---
name: kmp-critical-patterns
description: Quick reference for 6 core KMP patterns - Impl+Factory, Either Boundary, ViewModel, Navigation 3, Testing, Convention Plugins. Use when Claude needs a fast pattern reminder or initial project setup guidance without full skill context. Triggers - 'show me the patterns', 'quick reference', 'pattern overview', before implementing new features, when switching skills, token-constrained scenarios.
---

# KMP Critical Patterns

6 essential patterns for this Kotlin Multiplatform codebase. Quick reference format - follow links for detailed implementation.

## When to Use

Use this skill when:
- Starting new feature implementation and need quick pattern reminders
- Token budget is constrained and full skill context too heavy
- User asks for "quick reference", "pattern overview", "show me the patterns"
- Switching between skills and need fast context refresh
- Want pattern-at-a-glance without detailed implementation

Do NOT use when:
- Need full implementation guidance → use @kmp-developer or specific layer skills
- Writing tests → use @kmp-testing-strategy or @kmp-testing-patterns
- Building UI → use @compose-screen or @swiftui-screen
- Need detailed examples with full context → use domain-specific skills

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

## Essential Workflows

### Workflow 1: Starting a New Feature with Pattern Checklist

1. Read the **Pattern Overview** table to identify which patterns apply to your feature.
2. For each applicable pattern, jump to the detailed section for a quick refresher.
3. Use the **Quick Checklist** to verify pattern compliance during implementation.
4. Cross-reference to full skills for deep implementation details when needed.

- For repository: See @kmp-data-layer for `Either<RepoError, T>` implementation.
- For ViewModel: See @kmp-presentation for lifecycle management.

### Workflow 2: Pattern Validation During Code Review

1. Compare the proposed code against the canonical examples in each pattern section.
2. Verify that no "NEVER" rules are violated (e.g., no work in `init` blocks, no public `Impl` classes).
3. Use the **Quick Checklist** to ensure all 6 core patterns are correctly applied.
4. Flag any deviations from project conventions for correction.

### Workflow 3: Quick Pattern Lookup During Implementation

1. When unsure about a specific pattern detail (e.g., "Where do routes live?"), search the skill for the pattern name.
2. Review the **Key Rule** and **Canonical Example** for immediate guidance.
3. Use the **Plugin Matrix** to quickly find the correct convention plugin for a new module.

---

## Quick Reference

### Pattern Enforcement Checklist

Before implementing any feature:

- [ ] Repository uses `Either<RepoError, T>`
- [ ] ViewModel passes scope to constructor
- [ ] ViewModel uses `onStart()` not `init`
- [ ] Tests exist for all production code
- [ ] Impl class is `internal`, factory is `public`
- [ ] Feature module uses correct convention plugin
- [ ] Navigation routes in `:api`, providers in wiring

### Pattern-to-Skill Mapping

| Pattern | Primary Skill | Secondary Skills |
|---------|--------------|------------------|
| Impl+Factory | @kmp-di | @kmp-data-layer, @kmp-presentation |
| Either Boundary | @kmp-data-layer | @kmp-domain |
| ViewModel | @kmp-presentation | @kmp-mobile-expert |
| Navigation 3 | @kmp-navigation | @compose-screen, @swiftui-screen |
| Testing | @kmp-testing-patterns | @kmp-testing-strategy |
| Convention Plugins | @kmp-gradle | @kmp-architecture |

## Critical Guardrails

1. NEVER use this skill as the only reference for implementation → always cross-reference to full skills for complete context (incomplete context leads to bugs).
2. NEVER skip the **Quick Checklist** before committing → patterns must be complete to prevent architectural violations.
3. NEVER implement patterns without understanding their rationale → read linked full skills or documentation first (avoids "cargo cult" coding).
4. NEVER mix patterns from different architectural boundaries → follow vertical slice structure to avoid leaky abstractions.
5. NEVER skip pattern validation for any feature → technical debt accumulates quickly when conventions are ignored.
6. NEVER treat these patterns as suggestions → they are mandatory project conventions for a consistent codebase.
7. NEVER use this skill for initial learning → it is a refresher, not a tutorial; it lacks the necessary depth for first-time learners.
8. NEVER skip cross-references to documentation → patterns need architectural context for proper understanding.

---

## Cross-References

### Skills (by Category)

**Pattern Implementation**
| Skill | Patterns Covered | Link |
| --- | --- | --- |
| @kmp-data-layer | Either Boundary, Impl+Factory | [SKILL.md](../kmp-data-layer/SKILL.md) |
| @kmp-presentation | ViewModel, Impl+Factory | [SKILL.md](../kmp-presentation/SKILL.md) |
| @kmp-navigation | Navigation 3 | [SKILL.md](../kmp-navigation/SKILL.md) |
| @kmp-testing-patterns | Testing | [SKILL.md](../kmp-testing-patterns/SKILL.md) |
| @kmp-gradle | Convention Plugins | [SKILL.md](../kmp-gradle/SKILL.md) |

**Comprehensive Guides**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-developer | Full implementation patterns | [SKILL.md](../kmp-developer/SKILL.md) |
| @kmp-mobile-expert | ViewModel + repository patterns | [SKILL.md](../kmp-mobile-expert/SKILL.md) |
| @kmp-architecture | Module structure, vertical slicing | [SKILL.md](../kmp-architecture/SKILL.md) |
| @kmp-di | Koin dependency injection patterns | [SKILL.md](../kmp-di/SKILL.md) |
| @kmp-domain | Domain models and use cases | [SKILL.md](../kmp-domain/SKILL.md) |
| @compose-screen | UI patterns and implementation | [SKILL.md](../compose-screen/SKILL.md) |
| @swiftui-screen | Native iOS UI patterns | [SKILL.md](../swiftui-screen/SKILL.md) |

### Documents

| Document | Purpose | Link |
| --- | --- | --- |
| critical_patterns_quick_ref.md | Detailed pattern guide | [critical_patterns_quick_ref.md](See @kmp-critical-patterns skill) |
| conventions.md | Master architecture reference | [conventions.md](See @kmp-architecture skill for architecture patterns) |
| @kmp-testing-strategy skill | Testing philosophy and coverage | [@kmp-testing-strategy skill](See @kmp-testing-strategy skill) |
| navigation.md | Navigation 3 architecture details | [navigation.md](See @kmp-navigation skill) |
| dependency_injection.md | Koin patterns and troubleshooting | [dependency_injection.md](See @kmp-di skill) |

**Reference Implementation**: `features/pokemonlist/` demonstrates all 6 patterns end-to-end.
