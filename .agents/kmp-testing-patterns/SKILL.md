---
name: kmp-testing-patterns
description: "Test implementation patterns for Kotlin Multiplatform with Kotest, MockK, Turbine, and property-based testing. Use when: (1) Writing tests for repositories, ViewModels, or mappers, (2) Implementing property-based tests with Kotest, (3) Testing Flow/StateFlow with Turbine, (4) Using MockK for mocking in tests, (5) Writing screenshot tests with Roborazzi"
---

# KMP Testing Patterns Skill

Test implementation patterns for Kotlin Multiplatform with Kotest, MockK, Turbine, and property-based testing.

## When to Use This Skill

**MANDATORY**: Load this skill when working on:
- Writing tests for repositories, ViewModels, or mappers
- Implementing property-based tests with Kotest
- Testing Flow/StateFlow with Turbine
- Using MockK for mocking in tests
- Writing screenshot tests with Roborazzi

**Do NOT use for**: Testing strategy decisions → use @kmp-testing-strategy, Architecture decisions → use @kmp-architecture

## Core Principle

**NO CODE WITHOUT TESTS** - Every production file MUST have a corresponding test file.

## Test Location Strategy

| Production Code | Test Location | Framework |
|----------------|---------------|-----------|
| Repository | androidUnitTest/ | Kotest + MockK + Turbine |
| ViewModel | androidUnitTest/ | Kotest + MockK + Turbine |
| Mapper | androidUnitTest/ | Kotest properties |
| Use Case | androidUnitTest/ | Kotest + MockK |
| API Service | androidUnitTest/ | Kotest + MockK |
| @Composable | Same file | @Preview + Roborazzi |
| Simple Utility | commonTest/ | kotlin-test |

## Essential Workflows

### Workflow 1: Write ViewModel Test with Turbine

1. Inject `SavedStateHandle()` and `testScope` in ViewModel constructor.
2. Use Turbine `.test { }` for flow assertions.
3. Advance time with `testDispatcher.scheduler.advanceUntilIdle()`.

```kotlin
"state transitions correctly" {
    viewModel.uiState.test {
        awaitItem() shouldBe UiState.Loading
        viewModel.onStart(owner)
        testScope.advanceUntilIdle()
        awaitItem().shouldBeInstanceOf<UiState.Content>()
        cancelAndIgnoreRemainingEvents()
    }
}
```

### Workflow 2: Write Repository Test with MockK

1. Mock the API service using `mockk()`.
2. Test both success (Right) and all error (Left) paths.
3. Verify DTO-to-domain mapping.

```kotlin
"returns Left on Network error" {
    coEvery { api.getData() } throws IOException()
    val result = repository.getData()
    result.shouldBeLeft { it shouldBe RepoError.Network }
}
```

### Workflow 3: Write Property-Based Test with Kotest

1. Use `checkAll` with `Arb` generators.
2. Define invariants (e.g., data preservation).

```kotlin
"property: mapper preserves all fields" {
    checkAll(Arb.dto()) { dto ->
        val domain = dto.toDomain()
        domain.id shouldBe dto.id
    }
}
```

### Workflow 4: Write Screenshot Test with Roborazzi

1. Add `@Preview` to your `@Composable`.
2. Establish baseline with `recordRoborazziDebug`.
3. Verify regressions with `verifyRoborazziDebug`.

```bash
./gradlew recordRoborazziDebug
./gradlew verifyRoborazziDebug
```

## Quick Reference

### Repository Test Pattern

```kotlin
class PokemonListRepositoryTest : StringSpec({
    lateinit var mockApi: PokemonListApiService
    lateinit var repository: PokemonListRepository

    beforeTest {
        mockApi = mockk()
        repository = PokemonListRepository(mockApi)
    }

    "should return Right on success" {
        coEvery { mockApi.getPokemonList(20, 0) } returns mockDto

        val result = repository.loadPage()

        result.shouldBeRight { page ->
            page.pokemons shouldHaveSize 2
        }
    }
})
```

### ViewModel Test with Turbine

```kotlin
class PokemonListViewModelTest : StringSpec({
    lateinit var mockRepository: PokemonListRepository
    lateinit var testScope: TestScope
    lateinit var viewModel: PokemonListViewModel

    beforeTest {
        mockRepository = mockk()
        testScope = TestScope()
        viewModel = PokemonListViewModel(mockRepository, testScope)
    }

    "should transition Loading to Content" {
        viewModel.uiState.test {
            awaitItem() shouldBe PokemonListUiState.Loading
            viewModel.onStart(TestLifecycleOwner())
            testScope.advanceUntilIdle()
            awaitItem().shouldBeInstanceOf<PokemonListUiState.Content>()
            cancelAndIgnoreRemainingEvents()
        }
    }
})
```

### Property-Based Test

```kotlin
"dto to domain preserves all fields" {
    checkAll(
        Arb.int(1..1000),
        Arb.string(1..50).filter { it.isNotBlank() }
    ) { id, name ->
        val dto = PokemonSummaryDto(name.lowercase(), "url/$id/")
        val domain = dto.toDomain()
        domain.id shouldBe id
        domain.name shouldBe name.lowercase().replaceFirstChar { it.uppercase() }
    }
}
```

## Reference Loading Guide

| Task | Reference | Load When |
|------|-----------|-----------|
| Kotest patterns & matchers | [kotest-patterns.md](references/kotest-patterns.md) | Writing Kotest tests |
| MockK mocking patterns | [mockk-patterns.md](references/mockk-patterns.md) | Using MockK |
| Property-based testing | [property-testing.md](references/property-testing.md) | Writing property tests |
| ViewModel testing | [vm-testing.md](references/vm-testing.md) | Testing ViewModels |
| Repository testing | [repo-testing.md](references/repo-testing.md) | Testing repositories |

## Critical Guardrails

1. NEVER skip testing error paths → test Network, Http (400-599), and Unknown RepoError cases.
2. NEVER use `runBlocking` in tests → use `runTest` with `TestScope` for deterministic behavior.
3. NEVER test implementation details → focus on public API behavior and UI state transitions.
4. NEVER skip Turbine for StateFlow testing → `awaitItem()` is essential for catching timing and emission issues.
5. NEVER mock domain models → use real domain objects/data classes; mock only external boundaries (API, DB).
6. NEVER skip property-based tests for mappers → aim for 100% property test coverage for DTO ↔ Domain mapping.
7. NEVER commit without running tests → execute `./gradlew test --continue` to ensure all 84+ tests pass.
8. NEVER use `GlobalScope` in tests → always use `TestScope` or the ViewModel's injected scope for control.

## Cross-References

### Skills (by Category)

**Architecture**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-architecture | Module structure, vertical slice organization | [SKILL.md](../kmp-architecture/SKILL.md) |
| @kmp-critical-patterns | Quick reference for 6 core patterns | [SKILL.md](../kmp-critical-patterns/SKILL.md) |

**Layer Implementation**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-presentation | ViewModels, UI state management | [SKILL.md](../kmp-presentation/SKILL.md) |
| @kmp-data-layer | Repository patterns with Either<RepoError,T> | [SKILL.md](../kmp-data-layer/SKILL.md) |

**Testing**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-testing-strategy | Testing philosophy, coverage guidelines | [SKILL.md](../kmp-testing-strategy/SKILL.md) |
| @kmp-testing-patterns | Kotest, MockK, Turbine, property tests | [SKILL.md](../kmp-testing-patterns/SKILL.md) |

### Documents

| Document | Purpose | Link |
| --- | --- | --- |
| Testing Strategy | Kotest, MockK, Turbine, property tests guide | [@kmp-testing-strategy skill](See @kmp-testing-strategy skill) |
| Conventions | Master architecture and testing reference | [conventions.md](See @kmp-architecture skill for architecture patterns) |
| Quick Reference | Essential commands and workflows | [QUICK_REFERENCE.md](../../docs/QUICK_REFERENCE.md) |

## Quick Reference

### Test Checklist

- [ ] Test file created in correct source set
- [ ] Success + all error paths covered (repositories)
- [ ] State transitions covered with Turbine (ViewModels)
- [ ] Property tests added (30-40% of tests)
- [ ] Smart casting used (no manual casts)
- [ ] NO Thread.sleep - use testScope.advanceUntilIdle()

### Anti-Patterns to Avoid

| ❌ DON'T | ✅ DO |
|----------|-------|
| Manual cast after shouldBeInstanceOf | Use smart casting |
| Thread.sleep in tests | Use testScope.advanceUntilIdle() |
| Ignore shouldBeLeft return value | Use return value directly |
| Concrete tests covered by properties | Remove redundant tests |

## Troubleshooting Common Testing Issues

### Tests Pass But Build Shows Failures

**Symptom:**
```
> Task :features:pokemonlist:wiring-ui-unstyled:compileDebugKotlinAndroid FAILED
BUILD SUCCESSFUL in 1m 23s
All 84 tests PASSED
```

**Cause:** `--continue` flag allows tests to run despite task failures.

**Interpretation:**
- Task failures shown are from earlier in build
- Tests actually passed (verify with explicit test run)
- Subsequent clean build resolves stale task states

**Solution:** Run explicit test verification:
```bash
./gradlew test --rerun-tasks
```

---

### Validation Commands

```bash
# Run all tests
./gradlew test --continue

# Record screenshots
./gradlew recordRoborazziDebug

# Verify screenshots
./gradlew verifyRoborazziDebug
```

### Property-Based Coverage Targets

| Code Type | Coverage Target |
|-----------|----------------|
| Mappers | 100% |
| Repositories | 40-50% |
| ViewModels | 30-40% |
| Validators | 60-80% |
