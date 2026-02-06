# ViewModel Testing

Last Updated: February 6, 2026

Complete guide for testing ViewModels with Kotest, MockK, and Turbine in Kotlin Multiplatform.

## Quick Summary

ViewModel tests verify state transitions, UI events, and error handling. Use Turbine for Flow testing and TestScope for coroutine control.

---

## Test Structure

### Basic Setup

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
})
```

**Key Points:**
- Mock all repository dependencies
- Inject TestScope for coroutine control
- Don't use Dispatchers.setMain/resetMain
- Create fresh ViewModel for each test

---

## Turbine Flow Testing

### Basic Flow Testing

```kotlin
"should start with Loading state" {
    viewModel.uiState.test {
        awaitItem() shouldBe PokemonListUiState.Loading
        cancelAndIgnoreRemainingEvents()
    }
}
```

### State Transition Testing

```kotlin
"should transition Loading to Content on success" {
    val pokemons = listOf(
        Pokemon(1, "Bulbasaur", "url"),
        Pokemon(2, "Ivysaur", "url")
    )
    coEvery { mockRepository.loadPage() } returns Either.Right(
        PokemonPage(pokemons.toImmutableList(), hasMore = true)
    )

    viewModel.uiState.test {
        // Initial state
        awaitItem() shouldBe PokemonListUiState.Loading

        // Trigger state change
        viewModel.onStart(TestLifecycleOwner())
        testScope.advanceUntilIdle()

        // Verify transition
        val content = awaitItem().shouldBeInstanceOf<PokemonListUiState.Content>()
        content.pokemons.size shouldBe 2
        content.pokemons.first().name shouldBe "Bulbasaur"
        content.hasMore shouldBe true

        cancelAndIgnoreRemainingEvents()
    }
}
```

### Error State Testing

```kotlin
"should transition Loading to Error on failure" {
    coEvery { mockRepository.loadPage() } returns Either.Left(RepoError.Network)

    viewModel.uiState.test {
        awaitItem() shouldBe PokemonListUiState.Loading

        viewModel.onStart(TestLifecycleOwner())
        testScope.advanceUntilIdle()

        val error = awaitItem().shouldBeInstanceOf<PokemonListUiState.Error>()
        error.message shouldBe "No internet connection"

        cancelAndIgnoreRemainingEvents()
    }
}
```

### Skip Items Pattern

```kotlin
"should handle loadMore correctly" {
    // Setup initial state
    val initialPokemons = listOf(Pokemon(1, "Bulbasaur", "url"))
    coEvery { mockRepository.loadPage(offset = 0) } returns Either.Right(
        PokemonPage(initialPokemons.toImmutableList(), hasMore = true)
    )

    viewModel.onStart(TestLifecycleOwner())
    testScope.advanceUntilIdle()

    // Setup loadMore response
    val morePokemons = listOf(Pokemon(2, "Ivysaur", "url"))
    coEvery { mockRepository.loadPage(offset = 1) } returns Either.Right(
        PokemonPage(morePokemons.toImmutableList(), hasMore = false)
    )

    viewModel.uiState.test {
        skipItems(2)  // Skip Loading and initial Content

        // Trigger loadMore
        viewModel.onUiEvent(PokemonListUiEvent.LoadMore)

        // First update: isLoadingMore = true
        val loading = awaitItem().shouldBeInstanceOf<PokemonListUiState.Content>()
        loading.isLoadingMore shouldBe true
        loading.pokemons.size shouldBe 1

        testScope.advanceUntilIdle()

        // Second update: new pokemon added
        val content = awaitItem().shouldBeInstanceOf<PokemonListUiState.Content>()
        content.pokemons.size shouldBe 2
        content.pokemons[1].name shouldBe "Ivysaur"
        content.isLoadingMore shouldBe false
        content.hasMore shouldBe false

        cancelAndIgnoreRemainingEvents()
    }
}
```

### Multiple Events Testing

```kotlin
"should handle multiple refresh events" {
    val page1 = PokemonPage(listOf(pokemon1).toImmutableList(), true)
    val page2 = PokemonPage(listOf(pokemon2).toImmutableList(), true)

    coEvery { mockRepository.loadPage() } returnsMany listOf(page1, page2)

    viewModel.uiState.test {
        skipItems(1)  // Skip Loading

        // First refresh
        viewModel.onUiEvent(PokemonListUiEvent.Refresh)
        testScope.advanceUntilIdle()

        var state = awaitItem().shouldBeInstanceOf<PokemonListUiState.Content>()
        state.pokemons.first().name shouldBe "Pokemon1"

        // Second refresh
        viewModel.onUiEvent(PokemonListUiEvent.Refresh)
        testScope.advanceUntilIdle()

        state = awaitItem().shouldBeInstanceOf<PokemonListUiState.Content>()
        state.pokemons.first().name shouldBe "Pokemon2"

        cancelAndIgnoreRemainingEvents()
    }
}
```

---

## TestDispatcher Pattern

### ✅ CORRECT - Inject TestScope

```kotlin
beforeTest {
    testScope = TestScope()
    viewModel = MyViewModel(repository, testScope)
}
```

### ❌ WRONG - Don't use Dispatchers.setMain

```kotlin
beforeTest {
    Dispatchers.setMain(StandardTestDispatcher())  // Unnecessary
    viewModel = MyViewModel(repository)
}
afterTest {
    Dispatchers.resetMain()  // Unnecessary
}
```

**Why inject TestScope?**
- ViewModel receives scope via constructor
- No need to set Main dispatcher
- Cleaner setup/teardown
- More deterministic

---

## One-Time Events Testing

### EventChannel Testing

```kotlin
"should emit navigation event on item click" {
    viewModel.oneTimeEvents.test {
        viewModel.onUiEvent(PokemonListUiEvent.ItemClicked(1))

        val event = awaitItem()
        event.shouldBeInstanceOf<NavigationEvent.NavigateToDetail>()
            .let { it.pokemonId shouldBe 1 }

        cancelAndIgnoreRemainingEvents()
    }
}
```

### No Emission on Error

```kotlin
"should not emit event when no pokemon selected" {
    viewModel.oneTimeEvents.test {
        viewModel.onUiEvent(PokemonListUiEvent.ItemClicked(null))

        expectNoEvents()

        cancelAndIgnoreRemainingEvents()
    }
}
```

---

## Property-Based ViewModel Testing

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

            viewModel.onStart(TestLifecycleOwner())
            testScope.advanceUntilIdle()

            awaitItem().shouldBeInstanceOf<PokemonListUiState.Content> { state ->
                state.pokemons.size shouldBe pokemons.size
                state.hasMore shouldBe (count > pokemons.size)
            }

            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

---

## Lifecycle Testing

### TestLifecycleOwner Helper

```kotlin
// Helper class for lifecycle testing
class TestLifecycleOwner : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)

    init {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    fun start() {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    override val lifecycle: Lifecycle = lifecycleRegistry
}
```

### Lifecycle-Aware Testing

```kotlin
"should start loading when lifecycle starts" {
    coEvery { mockRepository.loadPage() } returns Either.Right(mockPage)

    viewModel.uiState.test {
        skipItems(1)  // Skip Loading

        val owner = TestLifecycleOwner()
        viewModel.onStart(owner)
        owner.start()
        testScope.advanceUntilIdle()

        awaitItem().shouldBeInstanceOf<PokemonListUiState.Content>()

        cancelAndIgnoreRemainingEvents()
    }

    coVerify { mockRepository.loadPage() }
}
```

---

## Turbine API Reference

| Method | Use Case | Example |
|--------|----------|---------|
| `awaitItem()` | Get next emission (fails if none) | `val item = awaitItem()` |
| `skipItems(n)` | Skip n emissions | `skipItems(2)` |
| `expectNoEvents()` | Assert no emissions occurred | `expectNoEvents()` |
| `cancelAndIgnoreRemainingEvents()` | Clean teardown | Always call at end |
| `.test { }` | Turbine test block for flows | `flow.test { /* assertions */ }` |

---

## Common Test Patterns

### Initial State Test

```kotlin
"should start with Loading state" {
    viewModel.uiState.test {
        awaitItem() shouldBe PokemonListUiState.Loading
        cancelAndIgnoreRemainingEvents()
    }
}
```

### Success Path Test

```kotlin
"should load pokemons on success" {
    coEvery { mockRepository.loadPage() } returns Either.Right(mockPage)

    viewModel.uiState.test {
        skipItems(1)

        viewModel.onStart(TestLifecycleOwner())
        testScope.advanceUntilIdle()

        awaitItem().shouldBeInstanceOf<PokemonListUiState.Content>()

        cancelAndIgnoreRemainingEvents()
    }
}
```

### Error Path Test

```kotlin
"should show error on network failure" {
    coEvery { mockRepository.loadPage() } returns Either.Left(RepoError.Network)

    viewModel.uiState.test {
        skipItems(1)

        viewModel.onStart(TestLifecycleOwner())
        testScope.advanceUntilIdle()

        awaitItem().shouldBeInstanceOf<PokemonListUiState.Error>()

        cancelAndIgnoreRemainingEvents()
    }
}
```

### Event Handling Test

```kotlin
"should handle Refresh event" {
    coEvery { mockRepository.loadPage() } returns Either.Right(mockPage)

    viewModel.uiState.test {
        skipItems(2)  // Skip Loading, initial Content

        viewModel.onUiEvent(PokemonListUiEvent.Refresh)
        testScope.advanceUntilIdle()

        awaitItem().shouldBeInstanceOf<PokemonListUiState.Content>()

        cancelAndIgnoreRemainingEvents()
    }

    coVerify(exactly = 2) { mockRepository.loadPage() }
}
```

### Loading State Test

```kotlin
"should show loading state during refresh" {
    coEvery { mockRepository.loadPage() } returns Either.Right(mockPage) andThenDelay(100)

    viewModel.uiState.test {
        skipItems(1)

        viewModel.onUiEvent(PokemonListUiEvent.Refresh)

        // Loading state
        val loading = awaitItem().shouldBeInstanceOf<PokemonListUiState.Content>()
        loading.isLoading shouldBe true

        testScope.advanceUntilIdle()

        // Content state
        val content = awaitItem().shouldBeInstanceOf<PokemonListUiState.Content>()
        content.isLoading shouldBe false

        cancelAndIgnoreRemainingEvents()
    }
}
```

---

## Best Practices

### 1. Always Use Turbine for Flows

```kotlin
// ❌ WRONG - Race conditions
viewModel.loadData()
Thread.sleep(100)  // Flaky!
viewModel.uiState.value.shouldBeInstanceOf<Content>()

// ✅ CORRECT - Deterministic
viewModel.uiState.test {
    viewModel.loadData()
    testScope.advanceUntilIdle()
    awaitItem().shouldBeInstanceOf<Content>()
    cancelAndIgnoreRemainingEvents()
}
```

### 2. Always Cancel at End

```kotlin
viewModel.uiState.test {
    awaitItem() shouldBe Loading

    // ... test code ...

    cancelAndIgnoreRemainingEvents()  // Always include this!
}
```

### 3. Use Smart Casting

```kotlin
// ✅ GOOD
awaitItem().shouldBeInstanceOf<Content>()
state.pokemons.size shouldBe 10

// ❌ BAD
val content = awaitItem() as Content
content.pokemons.size shouldBe 10
```

### 4. Verify Repository Calls

```kotlin
coVerify { mockRepository.loadPage() }
coVerify(exactly = 1) { mockRepository.loadPage() }
```

### 5. Use Property Tests for State Transitions

```kotlin
checkAll(Arb.list(Arb.pokemon(), 1..50)) { pokemons ->
    // Test state transition with any valid data
}
```

---

## Anti-Patterns

### ❌ Don't Test Private Methods

```kotlin
// ❌ WRONG - Can't access private methods
"should call private method" { ... }

// ✅ CORRECT - Test public API behavior
"should load pokemons" {
    viewModel.uiState.test {
        // Test observable behavior
    }
}
```

### ❌ Don't Test Implementation Details

```kotlin
// ❌ WRONG - Tests internal state
"should update _uiState" { ... }

// ✅ CORRECT - Tests observable behavior
"should emit Content state" {
    viewModel.uiState.test { ... }
}
```

### ❌ Don't Use Thread.sleep

```kotlin
// ❌ WRONG - Flaky and slow
Thread.sleep(100)

// ✅ CORRECT - Deterministic
testScope.advanceUntilIdle()
```

---

## Reference Implementations

- `features/pokemonlist/presentation/src/androidUnitTest/kotlin/.../PokemonListViewModelTest.kt`
- `features/pokemondetail/presentation/src/androidUnitTest/kotlin/.../PokemonDetailViewModelTest.kt`
