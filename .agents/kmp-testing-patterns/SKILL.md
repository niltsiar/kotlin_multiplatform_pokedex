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

## Mode Detection

| User Request | Reference File | Load When |
|--------------|----------------|-----------|
| "Write repository tests" | [repo-testing.md](references/repo-testing.md) | MANDATORY |
| "Write ViewModel tests" | [vm-testing.md](references/vm-testing.md) | MANDATORY |
| "Write property tests" | [property-testing.md](references/property-testing.md) | MANDATORY |
| "MockK patterns" | [mockk-patterns.md](references/mockk-patterns.md) | MANDATORY |
| "Kotest patterns" | [kotest-patterns.md](references/kotest-patterns.md) | MANDATORY |
| "Smart casting in tests" | [smart-casting.md](references/smart-casting.md) | MANDATORY |
| "Test troubleshooting" | [troubleshooting.md](references/troubleshooting.md) | When debugging test failures |

**MANDATORY - READ ENTIRE FILE**: Before writing repository tests, you MUST read [repo-testing.md](references/repo-testing.md) (~250 lines) for complete Either boundary patterns and error path coverage.

**MANDATORY - READ ENTIRE FILE**: Before writing ViewModel tests, you MUST read [vm-testing.md](references/vm-testing.md) (~250 lines) for Turbine flow testing and state transition patterns.

**MANDATORY - READ ENTIRE FILE**: Before writing property-based tests, you MUST read [property-testing.md](references/property-testing.md) (~280 lines) for Kotest Arb generators and invariant testing.

**MANDATORY - READ ENTIRE FILE**: Before using MockK in tests, you MUST read [mockk-patterns.md](references/mockk-patterns.md) (~190 lines) for coEvery, relaxed mocking, and verification patterns.

**MANDATORY - READ ENTIRE FILE**: Before writing Kotest tests, you MUST read [kotest-patterns.md](references/kotest-patterns.md) (~170 lines) for StringSpec, matchers, and test structure.

**MANDATORY - READ ENTIRE FILE**: Before writing tests with smart casting patterns, you MUST read [smart-casting.md](references/smart-casting.md) (~290 lines) for type-safe assertion patterns.

**Do NOT load** `smart-casting.md` for repository tests, ViewModel tests, or property tests unless specifically working with type assertions.

**Do NOT load** `troubleshooting.md` unless actively debugging test failures or build issues.

**Do NOT load** `mockk-patterns.md` for property-based tests (they don't use mocking).

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

## Decision Framework

Before writing tests, ask yourself:

1. **What type of test is needed?**
   - ViewModel → Turbine for StateFlow, TestScope for coroutines, mock repositories
   - Repository → MockK for API services, test all error paths (Network, Http, Unknown)
   - Mapper (DTO→Domain) → Property-based tests with Kotest (100% coverage target)
   - Composable → @Preview annotation (MANDATORY for all @Composable functions)

2. **What test distribution should I target?**
   - 40% property-based tests (mappers, invariant properties)
   - 60% concrete tests (ViewModels, repositories, edge cases)
   - 100% property coverage for mappers (DTO→Domain transformations)

3. **How do I verify correctness?**
   - ViewModels → Test state transitions with `awaitItem()`, verify all UiState variants
   - Repositories → Test success + all error paths, verify Either.Left/Right
   - Flows → Use Turbine, NEVER use `Thread.sleep()` or delays
   - Run validation: `./gradlew :composeApp:assembleDebug test --continue`

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
| Smart casting patterns | [smart-casting.md](references/smart-casting.md) | Working with type assertions |
| Troubleshooting | [troubleshooting.md](references/troubleshooting.md) | Debugging test failures |

## Critical Guardrails

1. NEVER skip testing error paths → test Network, Http (400-599), and Unknown RepoError cases.
2. NEVER use `runBlocking` in tests → use `runTest` with `TestScope` for deterministic behavior.
3. NEVER test implementation details → focus on public API behavior and UI state transitions.
4. NEVER skip Turbine for StateFlow testing → `awaitItem()` is essential for catching timing and emission issues.
5. NEVER mock domain models → use real domain objects/data classes; mock only external boundaries (API, DB).
6. NEVER skip property-based tests for mappers → aim for 100% property test coverage for DTO ↔ Domain mapping.
7. NEVER commit without running tests → execute `./gradlew test --continue` to ensure all 114+ tests pass.
8. NEVER use `GlobalScope` in tests → always use `TestScope` or the ViewModel's injected scope for control.

## Cross-References

**Related Skills**: @kmp-testing-strategy (philosophy), @kmp-architecture (module structure), @kmp-presentation (ViewModels), @kmp-data-layer (repositories)

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
All 114 tests PASSED
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
