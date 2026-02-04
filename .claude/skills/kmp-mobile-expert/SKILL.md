---
name: kmp-mobile-expert
description: "Implement shared Kotlin Multiplatform business logic with ViewModels, repositories, error handling (Either), and iOS integration. Use when: (1) Creating ViewModels with lifecycle awareness and SavedStateHandle, (2) Implementing repositories with Either<RepoError,T> patterns, (3) Setting up iOS exports from KMP modules, (4) Writing Koin DI configuration, (5) Troubleshooting iOS-KMP integration, (6) Deciding between Direct Integration vs Wrapper pattern"
---

# KMP Mobile Expert Skill

Expert guidance for shared Kotlin Multiplatform business logic across Android, iOS, Desktop, and Server.

## When to Use

- Creating ViewModels with lifecycle awareness and SavedStateHandle
- Implementing repositories with Either<RepoError, T> patterns
- Setting up iOS exports from KMP modules
- Writing Koin DI configuration
- Troubleshooting iOS-KMP integration issues
- Deciding between Direct Integration vs Wrapper pattern

## Mode Detection

| User Request | Reference File | Load When |
|--------------|----------------|-----------|
| "Create a ViewModel" | [viewmodel-patterns.md](references/viewmodel-patterns.md) | MANDATORY - Read before implementing |
| "Implement a repository" | [repository-patterns.md](references/repository-patterns.md) | MANDATORY - Read before implementing |
| "Export to iOS" / "iOS integration" | [ios-export.md](references/ios-export.md) | MANDATORY - Read before setting up exports |
| "Design module structure" | See Architecture section below | N/A |

**MANDATORY - READ ENTIRE FILE**: Before implementing repositories, you MUST read [repository-patterns.md](references/repository-patterns.md) (~100 lines) for complete Either boundary pattern.

**MANDATORY - READ ENTIRE FILE**: Before implementing ViewModels, you MUST read [viewmodel-patterns.md](references/viewmodel-patterns.md) (~120 lines) for lifecycle-aware pattern.

**Do NOT load** `repository-patterns.md` for ViewModel-only tasks.
**Do NOT load** `viewmodel-patterns.md` for repository-only tasks.

---

## Architecture Overview

### Vertical Slice Pattern

```
:features:<feature>:api              → Public contracts (interfaces, domain models)
:features:<feature>:data             → Network, DTOs, repositories
:features:<feature>:presentation     → ViewModels, UI state (shared with iOS)
:features:<feature>:ui-material      → Material Design 3 UI
:features:<feature>:ui-unstyled    → Compose Unstyled UI
:features:<feature>:wiring          → Business DI (Koin modules)
```

### Module Independence Rules

1. **No feature → feature impl dependencies**
   - ✅ `:profile:impl` → `:auth:api`
   - ❌ `:profile:impl` → `:auth:impl`

2. **Each feature owns its network layer**
   - Each feature defines its own API service
   - Each feature defines its own DTOs

3. **Export only `:api` and `:presentation` to iOS**
   - ❌ NEVER export `:data`, `:ui`, `:wiring`

---

## Critical Guardrails

| Anti-Pattern | Correct Pattern | Why It Matters |
|--------------|-----------------|----------------|
| `suspend fun get(): T?` | `suspend fun get(): Either<RepoError, T>` | Type-safe error handling |
| `init { loadData() }` | `override fun onStart(owner) { loadData() }` | Lifecycle-aware |
| `private val scope = CoroutineScope(...)` | Pass `viewModelScope` to constructor | Prevents leaks |
| `return Result.success(data)` | `Either.Right(data)` | Consistent error boundary |
| `class RepositoryImpl` (public) | `internal class RepositoryImpl` | Gradle compilation avoidance |
| No factory function | `fun Repository(...): Repository = RepositoryImpl(...)` | Simplifies DI wiring |
| Export `:data` to iOS | Export only `:api` and `:presentation` | iOS boundary violation |
| Store scope as field | Pass to constructor, not stored | ViewModel pattern violation |
| Swallow `CancellationException` | Use `Either.catch` (auto-respects cancellation) | Coroutine cancellation |

---

## Quick Reference

### Key Files by Pattern

| Pattern | File | Purpose |
|---------|------|---------|
| ViewModel | `PokemonListViewModel.kt` | Lifecycle-aware pattern |
| Repository | `PokemonListRepository.kt` | Either boundary interface |
| Repository Impl | `PokemonListRepositoryImpl.kt` | Impl + Factory pattern |
| Koin Wiring | `PokemonListModule.kt` | DI configuration |

### Common Imports

```kotlin
// ViewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope

// Either
import arrow.core.Either
import arrow.core.raise.catch

// Coroutines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Immutable collections
import kotlinx.collections.immutable.toImmutableList

// DI
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
```

---

## Cross-References

| Topic | Document | Location |
|-------|----------|----------|
| Complete architecture | `conventions.md` | docs/tech/conventions.md |
| All 6 core patterns | `critical_patterns_quick_ref.md` | docs/tech/critical_patterns_quick_ref.md |
| iOS integration details | `ios_integration.md` | docs/tech/ios_integration.md |
| Testing strategy | `testing_strategy.md` | docs/tech/testing_strategy.md |
| Koin DI patterns | `dependency_injection.md` | docs/tech/dependency_injection.md |

---

## Command Reference

```bash
# Primary validation (Android build + all tests)
./gradlew :composeApp:assembleDebug test --continue

# Run specific feature tests
./gradlew :features:<feature>:presentation:testDebugUnitTest
./gradlew :features:<feature>:data:testDebugUnitTest

# Check dependency updates
./gradlew dependencyUpdates
```

---

## Anti-Pattern Quick Check

Before writing code, verify:

- [ ] Repository returns `Either<RepoError, T>` (not Result or nullable)
- [ ] ViewModel implements `DefaultLifecycleObserver`
- [ ] ViewModel has `SavedStateHandle` injected
- [ ] ViewModel uses `by saved` delegate for state
- [ ] NO work in ViewModel `init` block
- [ ] Repository uses `internal class <Name>Impl` pattern
- [ ] Repository has public factory function
- [ ] Koin uses factory function, not constructor directly
- [ ] iOS exports only `:api` and `:presentation`
- [ ] StateFlow uses `kotlinx.collections.immutable` types
