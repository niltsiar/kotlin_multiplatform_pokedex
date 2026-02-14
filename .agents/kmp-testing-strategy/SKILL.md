---
name: kmp-testing-strategy
description: This skill should be used when planning test approach, writing tests, and analyzing coverage. Use for Kotest, MockK, property tests, and coverage analysis.
---

## When to Use

**Triggers:**
- "write tests for", "add test coverage", "create test file"
- "plan testing approach", "testing strategy", "test design"
- "analyze coverage", "check test coverage", "coverage report"
- "mock repository", "mock ViewModel", "test with MockK"
- "property test", "property-based testing", "checkAll", "forAll"
- "flow test", "StateFlow test", "SharedFlow test", "test with Turbine"
- "run tests", "execute tests", "test execution"

**Exclusions:**
- Do NOT use for iOS-specific testing setup (use @kmp-ios skill for iOS)
- Do NOT use for UI screenshot testing setup (use Roborazzi-specific guides)
- Do NOT use for build configuration issues (use convention plugins guide)

## Mode Detection

| User Request | Reference File | Load When |
|--------------|----------------|-----------|
| "Property test examples" / "write property-based tests" | [property-testing-examples.md](references/property-testing-examples.md) | MANDATORY |

**MANDATORY - READ ENTIRE FILE**: Before writing property-based tests, you MUST read [property-testing-examples.md](references/property-testing-examples.md) (~130 lines) for Kotest property test patterns, custom Arb generators, and coverage targets specific to this project.

**Do NOT load** `property-testing-examples.md` when:
- Writing only concrete tests (repository success paths, ViewModel state transitions)
- Running or analyzing existing tests
- Setting up test infrastructure

## Decision Framework

Before planning tests, ask yourself:

1. **What coverage level is required?**
   - Mappers (DTO→Domain) → 100% property-based test coverage (MANDATORY)
   - ViewModels → All state transitions + error paths
   - Repositories → Success path + all error types (Network, Http, Unknown)
   - Overall target → 40% property tests, 60% concrete tests

2. **What testing tools should I use?**
   - State flows → Turbine (NEVER use Thread.sleep or delays)
   - Mocking → MockK for interfaces, Fake implementations for complex logic
   - Property tests → Kotest with Arb generators
   - Coroutines → TestScope + StandardTestDispatcher

3. **How do I verify test quality?**
    - Run full suite: `./gradlew test --continue` (114 tests passing baseline)
   - Check coverage: All production code has corresponding test file
   - Verify patterns: Property tests for mappers, Turbine for flows, all error paths
   - NEVER skip tests when adding production code

## Essential Workflows

### Workflow 1: Write Repository Tests with Kotest + MockK

1. Create test file in `androidUnitTest/` source set with `*Test.kt` or `*Spec.kt` suffix
2. Use Kotest spec (`StringSpec`, `FreeSpec`, `DescribeSpec`) for test structure
3. Mock dependencies with MockK: `val mockApi = mockk<ApiService>(relaxed = true)`
4. Stub methods with `coEvery { mockApi.method() } returns value`
5. Test Either returns with Kotest Arrow extensions: `result.shouldBeRight()` or `result.shouldBeLeft()`
6. Cover all error paths: Network, Http (with codes 400-599), Unknown
7. Verify interactions with `coVerify { mockApi.method() }`
8. Clean up mocks in `afterTest` if needed

**Example:**
```kotlin
class PokemonListRepositoryTest : StringSpec({
    lateinit var mockApi: PokemonListApiService
    lateinit var repository: PokemonListRepository

    beforeTest {
        mockApi = mockk(relaxed = true)
        repository = PokemonListRepository(mockApi)
    }

    "returns Right on success" {
        coEvery { mockApi.getPokemonList(20, 0) } returns mockDto
        val result = repository.loadPage(20, 0)
        result.shouldBeRight().pokemons shouldNotBeEmpty()
    }

    "returns Left on network error" {
        coEvery { mockApi.getPokemonList(any(), any()) } throws IOException()
        val result = repository.loadPage(20, 0)
        result.shouldBeLeft() shouldBe RepoError.Network
    }
})
```

### Workflow 2: Write ViewModel Tests with Turbine + TestScope

1. Create test file in `androidUnitTest/` source set
2. Create `TestScope` and `TestDispatcher`: `val testScope = TestScope(StandardTestDispatcher())`
3. Inject `testScope` into ViewModel constructor (not `Dispatchers.Main`)
4. Mock repository with MockK: `val mockRepository = mockk<Repository>(relaxed = true)`
5. Use Turbine `.test { }` to collect StateFlow emissions
6. Call ViewModel methods directly (not through lifecycle)
7. Advance time with `testScope.advanceUntilIdle()`
8. Verify state transitions: `awaitItem()` for initial state, after action, after advance
9. Clean up with `cancelAndIgnoreRemainingEvents()` at end
10. NO `Dispatchers.setMain()` or `Dispatchers.resetMain()` needed

**Example:**
```kotlin
class PokemonListViewModelTest : StringSpec({
    lateinit var repository: PokemonListRepository
    lateinit var testScope: TestScope
    lateinit var viewModel: PokemonListViewModel

    beforeTest {
        repository = mockk(relaxed = true)
        testScope = TestScope(StandardTestDispatcher())
        viewModel = PokemonListViewModel(repository, testScope)
    }

    "loads pokemons successfully" {
        coEvery { repository.loadPage(any(), any()) } returns Either.right(mockPage)

        viewModel.uiState.test {
            awaitItem() shouldBe PokemonListUiState.Loading
            viewModel.loadInitialPage()
            testScope.advanceUntilIdle()
            awaitItem().shouldBeInstanceOf<PokemonListUiState.Content>()
                .pokemons shouldHaveSize 20
            cancelAndIgnoreRemainingEvents()
        }
    }
})
```

### Workflow 3: Write Property-Based Tests with Kotest

**MANDATORY**: Before writing property tests, read [property-testing-examples.md](references/property-testing-examples.md) for complete patterns, custom Arb generators, and examples.

**Quick checklist:**
1. Identify invariants (data preservation, transformation rules)
2. Use `Arb` generators: `Arb.int(range)`, `Arb.string()`, `Arb.list()`, custom generators
3. Write with `checkAll()` or `forAll()` (default 1000 iterations)
4. Name tests with "property:" prefix
5. Balance: 30-40% property, 60-70% concrete tests
6. Target: 100% property coverage for mappers

## Critical Guardrails

| Rule | Consequence |
|------|-------------|
| Tests MUST go in `androidUnitTest/` for business logic | Kotest/MockK unavailable on iOS/Native |
| NEVER use `Thread.sleep()` or `delay()` in tests | Use Turbine + `testScope.advanceUntilIdle()` |
| Mappers MUST have 100% property test coverage | Concrete tests insufficient for data invariants |
| 30-40% of tests MUST be property-based | Provides 1000× coverage multiplier |
| Every production file MUST have a test file | Tests are mandatory, not optional |
| 80% minimum coverage required per module | Fails CI if below threshold |
| NO `Dispatchers.setMain()` with testScope | Violates ViewModel pattern |

## Quick Reference

### Canonical Sources
- [@kmp-testing-strategy skill](@kmp-testing-strategy skill) — deep dive with rationale and playbooks
- [@kmp-testing-patterns](../kmp-testing-patterns/SKILL.md) — concise pattern reminders
- [See @kmp-critical-patterns skill#testing-pattern](See @kmp-critical-patterns skill#testing-pattern) — canonical rules
- [See @kmp-critical-patterns skill](See @kmp-critical-patterns skill) — pattern cards view

### Test Enforcement (NO CODE WITHOUT TESTS)

| Production Code | Location | Framework | Key Rule |
| --- | --- | --- | --- |
| Repository | `androidUnitTest/` | Kotest + MockK | Success + all error paths |
| ViewModel | `androidUnitTest/` | Kotest + MockK + Turbine | Initial, Loading, Success, Error + events |
| Mapper | `androidUnitTest/` | Kotest properties | Property tests proving data preservation |
| `@Composable` | Same file | `@Preview` (+ Roborazzi where used) | Realistic preview + screenshot baseline |
| Simple Utility | `commonTest/` | kotlin-test | Pure functions only |

### Flow Testing with Turbine
- Always test `Flow` / `StateFlow` / `SharedFlow` with Turbine
- Prefer injecting a `TestScope` via constructor; **NO** `Dispatchers.setMain/resetMain`
- Use `awaitItem()`, `skipItems()`, and `cancelAndIgnoreRemainingEvents()` with `advanceUntilIdle()`

### Smart Casting
- Use Kotest contracts: `shouldBeRight { }`, `shouldBeLeft { }`, `shouldBeInstanceOf<>()`
- Avoid manual casts after assertions
- See [@kmp-testing-patterns](../kmp-testing-patterns/SKILL.md)

### Commands

| Command | Purpose |
|---------|---------|
| `./gradlew :composeApp:assembleDebug test --continue` | Build Android app + run all tests |
| `./gradlew test --continue` | Run all tests across all modules |
| `./gradlew :features:<feature>:testDebugUnitTest` | Run tests for specific feature module |
| `./.agents/kmp-testing-strategy/scripts/test-coverage.sh [feature]` | Run tests + coverage for feature or all |

### Implementation Examples
- [PokemonListViewModelTest.kt](../../features/pokemonlist/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModelTest.kt) - ViewModel test with Turbine
- [PokemonDetailViewModelTest.kt](../../features/pokemondetail/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModelTest.kt) - Property tests examples
- [PokemonListRepositoryTest.kt](../../features/pokemonlist/data/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/data/PokemonListRepositoryTest.kt) - Repository test with MockK
