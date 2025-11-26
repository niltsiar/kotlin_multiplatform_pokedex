# Testing Patterns

Last Updated: November 26, 2025

> **Canonical Reference**: See [Testing Pattern](../tech/critical_patterns_quick_ref.md#testing-pattern) for core rules.

> Comprehensive code examples for Kotest+MockK, property-based testing, Turbine flows, and Roborazzi screenshots.

## Core Principle

**NO CODE WITHOUT TESTS** - Every production file MUST have a corresponding test file.

## Test Location Strategy

| Production Code | Test Location | Framework | Rationale |
|----------------|---------------|-----------|-----------|
| Repository | androidUnitTest/ | Kotest + MockK + Turbine | Full framework support, fast JVM execution |
| ViewModel | androidUnitTest/ | Kotest + MockK + Turbine | Full framework support, deterministic flow testing |
| Mapper | androidUnitTest/ | Kotest properties | Property-based testing support |
| Use Case | androidUnitTest/ | Kotest + MockK | Full mocking support |
| API Service | androidUnitTest/ | Kotest + MockK | HTTP mocking support |
| @Composable | Same file | @Preview + Roborazzi | UI preview and screenshot testing |
| Simple Utility | commonTest/ | kotlin-test | No dependencies, platform-agnostic |

**Why Android Unit Tests for Business Logic:**
- ✅ Kotest doesn't support iOS/Native targets
- ✅ MockK doesn't support iOS/Native targets
- ✅ Both fully support Android (JVM-based via Robolectric)
- ✅ Android/iOS are primary mobile targets
- ✅ Fast JVM execution (same speed as jvmTest)
- ✅ Type safety guarantees iOS compatibility

## Basic Kotest + MockK Pattern

### Repository Testing

```kotlin
// features/pokemonlist/data/src/androidUnitTest/kotlin/.../PokemonListRepositoryTest.kt
package com.example.features.pokemonlist.data

import app.cash.turbine.test
import arrow.core.Either
import com.example.features.pokemonlist.api.RepoError
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import io.ktor.client.plugins.*
import io.mockk.*
import java.io.IOException
import java.net.ConnectTimeoutException

class PokemonListRepositoryTest : StringSpec({
    lateinit var mockApi: PokemonListApiService
    lateinit var repository: PokemonListRepository
    
    beforeTest {
        mockApi = mockk()
        repository = PokemonListRepository(mockApi)
    }
    
    "should return Right with page on success" {
        coEvery { mockApi.getPokemonList(20, 0) } returns PokemonListDto(
            count = 1292,
            next = "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20",
            previous = null,
            results = listOf(
                PokemonSummaryDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"),
                PokemonSummaryDto("ivysaur", "https://pokeapi.co/api/v2/pokemon/2/")
            )
        )
        
        val result = repository.loadPage()
        
        result.shouldBeRight { page ->
            page.pokemons shouldHaveSize 2
            page.pokemons.first().name shouldBe "Bulbasaur"
            page.hasMore shouldBe true
        }
    }
    
    "should return Network error on timeout" {
        coEvery { mockApi.getPokemonList(any(), any()) } throws 
            ConnectTimeoutException("Connection timed out")
        
        val result = repository.loadPage()
        
        result.shouldBeLeft { error ->
            error shouldBe RepoError.Network
        }
    }
    
    "should return Network error on IOException" {
        coEvery { mockApi.getPokemonList(any(), any()) } throws 
            IOException("Network error")
        
        val result = repository.loadPage()
        
        result.shouldBeLeft { error ->
            error shouldBe RepoError.Network
        }
    }
    
    "should return Http error on 404" {
        coEvery { mockApi.getPokemonList(any(), any()) } throws 
            ClientRequestException(
                mockk { every { status.value } returns 404 }
            )
        
        val result = repository.loadPage()
        
        result.shouldBeLeft { error ->
            error shouldBe RepoError.NotFound
        }
    }
    
    "should map all 4xx codes to Http error" {
        checkAll(Arb.int(400..499)) { code ->
            coEvery { mockApi.getPokemonList(any(), any()) } throws 
                ClientRequestException(
                    mockk { every { status.value } returns code }
                )
            
            val result = repository.loadPage()
            
            result.shouldBeLeft { error ->
                when (code) {
                    401 -> error shouldBe RepoError.Unauthorized
                    404 -> error shouldBe RepoError.NotFound
                    else -> {
                        error.shouldBeInstanceOf<RepoError.Http>()
                        error.code shouldBe code
                    }
                }
            }
        }
    }
})

// Helper extensions
fun <L, R> Either<L, R>.shouldBeRight(): R =
    this.getOrNull() ?: fail("Expected Right but was $this")

fun <L, R> Either<L, R>.shouldBeLeft(): L =
    this.swap().getOrNull() ?: fail("Expected Left but was $this")
```

## Property-Based Testing (PRIMARY STRATEGY)

### Mapper Testing (100% Property Tests)

```kotlin
// features/pokemonlist/data/src/androidUnitTest/kotlin/.../PokemonMappersTest.kt
package com.example.features.pokemonlist.data

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll

class PokemonMappersTest : StringSpec({
    
    "dto to domain preserves all fields correctly" {
        checkAll(
            Arb.int(1..1000),
            Arb.string(1..50).filter { it.isNotBlank() },
            Arb.string(10..200)
        ) { id, name, imageUrl ->
            val dto = PokemonSummaryDto(
                name = name.lowercase(),
                url = "https://pokeapi.co/api/v2/pokemon/$id/"
            )
            
            val domain = dto.toDomain()
            
            domain.id shouldBe id
            domain.name shouldBe name.lowercase().replaceFirstChar { it.uppercase() }
            domain.imageUrl shouldBe 
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
        }
    }
    
    "should extract ID from any valid URL" {
        checkAll(Arb.int(1..10000)) { id ->
            val url = "https://pokeapi.co/api/v2/pokemon/$id/"
            
            val extractedId = extractIdFromUrl(url)
            
            extractedId shouldBe id
        }
    }
    
    "dto list to domain list preserves size" {
        checkAll(Arb.list(Arb.pokemonSummaryDto(), 0..100)) { dtos ->
            val domains = dtos.map { it.toDomain() }
            
            domains.size shouldBe dtos.size
        }
    }
    
    "round-trip mapping preserves data" {
        checkAll(Arb.pokemon()) { originalPokemon ->
            val dto = originalPokemon.toDto()
            val roundTripped = dto.toDomain()
            
            roundTripped.id shouldBe originalPokemon.id
            roundTripped.name shouldBe originalPokemon.name
        }
    }
})

// Custom Arbs for domain objects
fun Arb.Companion.pokemonSummaryDto(): Arb<PokemonSummaryDto> = arbitrary {
    PokemonSummaryDto(
        name = Arb.string(1..20).bind().lowercase(),
        url = "https://pokeapi.co/api/v2/pokemon/${Arb.int(1..1000).bind()}/"
    )
}

fun Arb.Companion.pokemon(): Arb<Pokemon> = arbitrary {
    Pokemon(
        id = Arb.int(1..1000).bind(),
        name = Arb.string(1..20).bind(),
        imageUrl = "https://example.com/${Arb.int(1..1000).bind()}.png"
    )
}
```

### Repository Property Tests

```kotlin
"should preserve pokemon count through pagination" {
    checkAll(Arb.int(1..5), Arb.int(10..50)) { pages, pageSize ->
        val allPokemons = mutableListOf<Pokemon>()
        var offset = 0
        
        repeat(pages) { pageIndex ->
            val isLastPage = pageIndex == pages - 1
            coEvery { mockApi.getPokemonList(pageSize, offset) } returns 
                PokemonListDto(
                    count = pages * pageSize,
                    next = if (isLastPage) null else "next",
                    previous = if (offset > 0) "prev" else null,
                    results = List(pageSize) { i ->
                        PokemonSummaryDto(
                            name = "pokemon${offset + i}",
                            url = "https://pokeapi.co/api/v2/pokemon/${offset + i + 1}/"
                        )
                    }
                )
            
            val result = repository.loadPage(offset = offset)
            result.shouldBeRight { page ->
                allPokemons.addAll(page.pokemons)
            }
            
            offset += pageSize
        }
        
        allPokemons.size shouldBe pages * pageSize
        allPokemons.distinctBy { it.id }.size shouldBe allPokemons.size  // No duplicates
    }
}
```

### Property Test Coverage Targets

| Code Type | Coverage Target | Example Tests |
|-----------|----------------|---------------|
| Mappers | 100% | Data preservation, field mapping, round-trip |
| Repositories | 40-50% | HTTP error ranges, ID extraction, pagination |
| ViewModels | 30-40% | State transitions, random data flows |
| Validators | 60-80% | Input validation, boundary conditions |

## Turbine Flow Testing (MANDATORY for ViewModels)

### Basic ViewModel Testing

```kotlin
// features/pokemonlist/presentation/src/androidUnitTest/kotlin/.../PokemonListViewModelTest.kt
package com.example.features.pokemonlist.presentation

import app.cash.turbine.test
import arrow.core.Either
import com.example.features.pokemonlist.api.Pokemon
import com.example.features.pokemonlist.api.PokemonPage
import com.example.features.pokemonlist.api.PokemonListRepository
import com.example.features.pokemonlist.api.RepoError
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.TestScope

class PokemonListViewModelTest : StringSpec({
    lateinit var mockRepository: PokemonListRepository
    lateinit var testScope: TestScope
    lateinit var viewModel: PokemonListViewModel
    
    beforeTest {
        mockRepository = mockk()
        testScope = TestScope()
        viewModel = PokemonListViewModel(mockRepository, testScope)
    }
    
    "should start with Loading state" {
        viewModel.uiState.test {
            awaitItem() shouldBe PokemonListUiState.Loading
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    "should transition from Loading to Content on success" {
        val pokemons = listOf(
            Pokemon(1, "Bulbasaur", "https://example.com/1.png"),
            Pokemon(2, "Ivysaur", "https://example.com/2.png")
        )
        coEvery { mockRepository.loadPage() } returns Either.Right(
            PokemonPage(
                pokemons = pokemons.toImmutableList(),
                hasMore = true
            )
        )
        
        viewModel.uiState.test {
            awaitItem() shouldBe PokemonListUiState.Loading
            
            viewModel.start(mockk(relaxed = true))
            testScope.advanceUntilIdle()
            
            awaitItem().shouldBeInstanceOf<PokemonListUiState.Content>().let { state ->
                state.pokemons.size shouldBe 2
                state.pokemons.first().name shouldBe "Bulbasaur"
                state.isLoadingMore shouldBe false
                state.hasMore shouldBe true
            }
            
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    "should transition from Loading to Error on failure" {
        coEvery { mockRepository.loadPage() } returns Either.Left(RepoError.Network)
        
        viewModel.uiState.test {
            awaitItem() shouldBe PokemonListUiState.Loading
            
            viewModel.start(mockk(relaxed = true))
            testScope.advanceUntilIdle()
            
            awaitItem().shouldBeInstanceOf<PokemonListUiState.Error>().let { state ->
                state.message shouldBe "No internet connection"
            }
            
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    "should handle loadMore correctly" {
        // Setup initial state
        val initialPokemons = listOf(
            Pokemon(1, "Bulbasaur", "...")
        )
        coEvery { mockRepository.loadPage(offset = 0) } returns Either.Right(
            PokemonPage(initialPokemons.toImmutableList(), hasMore = true)
        )
        
        viewModel.start(mockk(relaxed = true))
        testScope.advanceUntilIdle()
        
        // Setup loadMore response
        val morePokemons = listOf(
            Pokemon(2, "Ivysaur", "...")
        )
        coEvery { mockRepository.loadPage(offset = 1) } returns Either.Right(
            PokemonPage(morePokemons.toImmutableList(), hasMore = false)
        )
        
        viewModel.uiState.test {
            skipItems(2)  // Skip Loading and initial Content
            
            viewModel.onUiEvent(PokemonListUiEvent.LoadMore)
            
            // First update: isLoadingMore = true
            awaitItem().shouldBeInstanceOf<PokemonListUiState.Content>().let { state ->
                state.isLoadingMore shouldBe true
                state.pokemons.size shouldBe 1
            }
            
            testScope.advanceUntilIdle()
            
            // Second update: new pokemon added
            awaitItem().shouldBeInstanceOf<PokemonListUiState.Content>().let { state ->
                state.pokemons.size shouldBe 2
                state.pokemons[1].name shouldBe "Ivysaur"
                state.isLoadingMore shouldBe false
                state.hasMore shouldBe false
            }
            
            cancelAndIgnoreRemainingEvents()
        }
    }
})
```

### Property-Based ViewModel Testing

```kotlin
"should transition Loading -> Content with any valid page" {
    checkAll(
        Arb.int(1..100),
        Arb.list(Arb.pokemon(), 1..50)
    ) { count, pokemons ->
        val testScope = TestScope()
        val mockRepo = mockk<PokemonListRepository>()
        val viewModel = PokemonListViewModel(mockRepo, testScope)
        
        coEvery { mockRepo.loadPage() } returns Either.Right(
            PokemonPage(
                pokemons = pokemons.toImmutableList(),
                hasMore = count > pokemons.size
            )
        )
        
        viewModel.uiState.test {
            awaitItem() shouldBe PokemonListUiState.Loading
            
            viewModel.start(mockk(relaxed = true))
            testScope.advanceUntilIdle()
            
            awaitItem().shouldBeInstanceOf<PokemonListUiState.Content>().let { state ->
                state.pokemons.size shouldBe pokemons.size
                state.hasMore shouldBe (count > pokemons.size)
            }
            
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

### Turbine API Reference

| Method | Use Case | Example |
|--------|----------|---------|
| `awaitItem()` | Get next emission (fails if none) | `val item = awaitItem()` |
| `skipItems(n)` | Skip n emissions | `skipItems(2)` |
| `expectNoEvents()` | Assert no emissions occurred | `expectNoEvents()` |
| `cancelAndIgnoreRemainingEvents()` | Clean teardown | Always call at end |
| `.test { }` | Turbine test block for flows | `flow.test { /* assertions */ }` |

### TestDispatcher Pattern

```kotlin
// ✅ CORRECT - Inject TestScope, no Dispatchers.setMain
beforeTest {
    testScope = TestScope()
    viewModel = MyViewModel(repository, testScope)  // Pass to constructor
}

// ❌ WRONG - Don't use Dispatchers.setMain when injecting scope
beforeTest {
    Dispatchers.setMain(StandardTestDispatcher())  // Unnecessary
    viewModel = MyViewModel(repository)
}
afterTest {
    Dispatchers.resetMain()  // Unnecessary
}
```

## Screenshot Testing (Roborazzi)

### Basic Screenshot Test

```kotlin
// features/pokemonlist/ui/src/androidUnitTest/kotlin/.../PokemonListScreenTest.kt
package com.example.features.pokemonlist.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.features.pokemonlist.api.Pokemon
import com.example.features.pokemonlist.presentation.PokemonListUiState
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PokemonListScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun pokemonListScreen_loading() {
        composeTestRule.setContent {
            PokemonListContent(
                uiState = PokemonListUiState.Loading,
                onLoadMore = {}
            )
        }
        
        composeTestRule.onRoot()
            .captureRoboImage("screenshots/PokemonListScreen_loading.png")
    }
    
    @Test
    fun pokemonListScreen_content() {
        composeTestRule.setContent {
            PokemonListContent(
                uiState = PokemonListUiState.Content(
                    pokemons = persistentListOf(
                        Pokemon(1, "Bulbasaur", "..."),
                        Pokemon(2, "Ivysaur", "..."),
                        Pokemon(3, "Venusaur", "...")
                    ),
                    hasMore = true,
                    isLoadingMore = false
                ),
                onLoadMore = {}
            )
        }
        
        composeTestRule.onRoot()
            .captureRoboImage("screenshots/PokemonListScreen_content.png")
    }
    
    @Test
    fun pokemonListScreen_error() {
        composeTestRule.setContent {
            PokemonListContent(
                uiState = PokemonListUiState.Error("Network error"),
                onLoadMore = {}
            )
        }
        
        composeTestRule.onRoot()
            .captureRoboImage("screenshots/PokemonListScreen_error.png")
    }
}
```

### Screenshot Test Commands

```bash
# Record baselines
./gradlew recordRoborazziDebug

# Verify against baselines
./gradlew verifyRoborazziDebug

# Compare screenshots
./gradlew compareRoborazziDebug
```

## Test Enforcement Rules

### Before Marking Code Complete

1. ✅ Test file created
2. ✅ Minimum coverage (success + all error paths)
3. ✅ Property tests added (30-40% of tests should be property-based)
4. ✅ Turbine used for all flow testing (NEVER Thread.sleep)
5. ✅ All tests pass
6. ✅ Preview added (for UI)
7. ✅ Remove redundant tests covered by property tests

### Redundant Test Elimination

Property tests often make concrete tests obsolete. **Remove concrete tests fully covered by property tests.**

```kotlin
// ❌ REDUNDANT - Property test covers all 4xx codes
"should return Http error for 400" { /* specific test */ }
"should return Http error for 404" { /* specific test */ }
"should return Http error for 429" { /* specific test */ }

// ✅ KEEP - One property test replaces all three
"should map all 4xx codes to Http error" {
    checkAll(Arb.int(400..499)) { code ->
        // Tests ALL 4xx codes (100 scenarios)
    }
}
```

### Decision Matrix for Removing Tests

1. Does a property test cover this scenario? → **Remove concrete test**
2. Is this an edge case not covered by properties? → **Keep concrete test**
3. Does this test document important behavior? → **Keep but add comment**
4. Is this test redundant with another concrete test? → **Merge or remove**

## Anti-Patterns to Avoid

### ❌ DON'T: Use Thread.sleep in Tests

```kotlin
// ❌ WRONG
"should update state" {
    viewModel.loadData()
    Thread.sleep(100)  // NEVER DO THIS
    viewModel.uiState.value.shouldBeInstanceOf<Content>()
}

// ✅ CORRECT
"should update state" {
    viewModel.uiState.test {
        awaitItem() shouldBe Loading
        viewModel.loadData()
        testScope.advanceUntilIdle()
        awaitItem().shouldBeInstanceOf<Content>()
        cancelAndIgnoreRemainingEvents()
    }
}
```

### ❌ DON'T: Manual Casting After Type Assertions

```kotlin
// ❌ WRONG - Manual cast after shouldBeInstanceOf
val result = repository.getById(1)
result.shouldBeInstanceOf<Either.Right<Pokemon>>()
val pokemon = (result as Either.Right).value  // Manual cast

// ✅ CORRECT - Use smart casting
val result = repository.getById(1)
result.shouldBeRight { pokemon ->
    // Smart cast - pokemon is Pokemon
    pokemon.id shouldBe 1
}
```

### ❌ DON'T: Test Implementation Details

```kotlin
// ❌ WRONG - Testing private methods
"should call private method" {
    // Can't and shouldn't test private methods
}

// ✅ CORRECT - Test public API behavior
"should load pokemons" {
    viewModel.start(lifecycle)
    // Test observable behavior through public API
}
```

## See Also

- `.junie/guides/tech/testing_strategy.md` — Complete testing strategy
- `.junie/guides/tech/kotest_smart_casting_quick_ref.md` — Smart casting patterns
- `.junie/guides/tech/koin_di_quick_ref.md` — DI in tests
- `patterns/error_handling_patterns.md` — Testing Either boundaries
- `patterns/viewmodel_patterns.md` — ViewModel testability
