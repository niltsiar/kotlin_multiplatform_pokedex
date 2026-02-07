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
- Do NOT use for iOS-specific testing setup (use ios_integration.md for iOS)
- Do NOT use for UI screenshot testing setup (use Roborazzi-specific guides)
- Do NOT use for build configuration issues (use convention plugins guide)

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

        val page = result.shouldBeRight()
        page.pokemons shouldNotBeEmpty()
    }

    "returns Left on network error" {
        coEvery { mockApi.getPokemonList(any(), any()) } throws IOException()

        val result = repository.loadPage(20, 0)

        val error = result.shouldBeLeft()
        error shouldBe RepoError.Network
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

            val content = awaitItem().shouldBeInstanceOf<PokemonListUiState.Content>()
            content.pokemons shouldHaveSize 20

            cancelAndIgnoreRemainingEvents()
        }
    }
})
```

### Workflow 3: Write Property-Based Tests with Kotest

1. Identify invariants (data preservation, transformation rules, mathematical properties)
2. Use appropriate `Arb` generators: `Arb.int(range)`, `Arb.string(length)`, `Arb.list()`, `.orNull()`
3. Write property tests with `checkAll()` for assertions or `forAll()` for boolean returns
4. Default iterations = 1000 (do NOT override unless necessary)
5. Use descriptive names starting with "property:"
6. Balance with concrete tests (30-40% property, 60-70% concrete)
7. Remove redundant concrete tests covered by property tests
8. Target: 100% property test coverage for mappers

**Example:**
```kotlin
class PokemonMapperSpec : StringSpec({
    "property: mapper preserves all fields" {
        checkAll(Arb.pokemonDto()) { dto ->
            val domain = dto.toDomain()

            domain.id shouldBe dto.id
            domain.name shouldBe dto.name.lowercase()
            domain.imageUrl shouldContain dto.id.toString()
        }
    }

    "property: HTTP error codes always produce Error state" {
        checkAll(Arb.int(400..599)) { errorCode ->
            coEvery { mockApi.getPokemonList(any(), any()) } throws
                ClientRequestException(
                    HttpResponse(
                        status = HttpStatusCode.fromValue(errorCode),
                        requestTime = null
                    ),
                    "HTTP error"
                )

            val result = repository.loadPage(20, 0)
            val error = result.shouldBeLeft()
            error shouldBeInstanceOf<RepoError.Http>()
            (error as RepoError.Http).code shouldBe errorCode
        }
    }
})
```

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

| Command | Purpose |
|---------|---------|
| `./gradlew :composeApp:assembleDebug test --continue` | Build Android app + run all tests |
| `./gradlew :features:<feature>:testDebugUnitTest` | Run tests for specific feature module |
| `./gradlew test --continue` | Run all tests across all modules |
| `./gradlew jacocoTestReport` | Generate coverage report (if Jacoco configured) |
| `./gradlew :features:<feature>:data:testDebugUnitTest --tests "TestClass"` | Run specific test class |
| `./claude/skills/kmp-testing-strategy/scripts/test-coverage.sh [feature]` | Run tests + coverage for feature or all |

**Common Test Patterns:**
```kotlin
// MockK basics
val mockApi = mockk<ApiService>(relaxed = true)
coEvery { mockApi.method() } returns value
coVerify { mockApi.method() }

// Turbine flow testing
viewModel.uiState.test {
    awaitItem() shouldBe expectedState
    viewModel.doSomething()
    testScope.advanceUntilIdle()
    cancelAndIgnoreRemainingEvents()
}

// Property testing
checkAll(Arb.int(0..100)) { value ->
    transform(value) shouldBe expectedTransform(value)
}
```

## Cross-References

| Document | Purpose |
|----------|---------|
| [@kmp-testing-strategy skill](See @kmp-testing-strategy skill) | Complete testing strategy guide |
| [critical_patterns_quick_ref.md](See @kmp-critical-patterns skill) | Testing pattern reference |
| [conventions.md](See @kmp-architecture skill for architecture patterns) | Architecture and module structure |
| [QUICK_REFERENCE.md](../../docs/QUICK_REFERENCE.md) | Quick commands reference |

**Implementation Examples:**
- [PokemonListViewModelTest.kt](../../features/pokemonlist/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModelTest.kt) - ViewModel test with Turbine
- [PokemonDetailViewModelTest.kt](../../features/pokemondetail/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModelTest.kt) - Property tests examples
- [PokemonListRepositoryTest.kt](../../features/pokemonlist/data/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/data/PokemonListRepositoryTest.kt) - Repository test with MockK
