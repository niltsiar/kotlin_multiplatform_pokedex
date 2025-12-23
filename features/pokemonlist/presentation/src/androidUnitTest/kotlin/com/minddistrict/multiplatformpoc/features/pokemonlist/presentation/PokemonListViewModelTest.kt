package com.minddistrict.multiplatformpoc.features.pokemonlist.presentation

import app.cash.turbine.test
import arrow.core.Either
import com.minddistrict.multiplatformpoc.features.pokemonlist.PokemonListRepository
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.PokemonPage
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.RepoError
import androidx.lifecycle.SavedStateHandle
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import io.kotest.property.forAll
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonListViewModelTest : StringSpec({
    val testDispatcher = StandardTestDispatcher()
    val testScope = TestScope(testDispatcher)
    
    lateinit var mockRepository: PokemonListRepository
    lateinit var viewModel: PokemonListViewModel
    
    beforeTest {
        mockRepository = mockk(relaxed = true)
        viewModel = PokemonListViewModel(
            repository = mockRepository,
            savedStateHandle = SavedStateHandle(),
            viewModelScope = testScope,
        )
    }
    
    "initial state should be Loading" {
        viewModel.uiState.value shouldBe PokemonListUiState.Loading
    }
    
    "loadInitialPage should emit Content on success" {
        val pokemon = Pokemon(1, "Bulbasaur", "https://example.com/1.png")
        val page = PokemonPage(listOf(pokemon), hasMore = true)
        
        coEvery { mockRepository.loadPage(20, 0) } returns Either.Right(page)

        viewModel.uiState.test {
            awaitItem() shouldBe PokemonListUiState.Loading
            
            viewModel.loadInitialPage()
            testDispatcher.scheduler.advanceUntilIdle()
            
            val state = awaitItem()
            state.shouldBeInstanceOf<PokemonListUiState.Content>()
            state.pokemons.size shouldBe 1
            state.pokemons[0].name shouldBe "Bulbasaur"
            state.isLoadingMore shouldBe false
            state.hasMore shouldBe true
            
            cancelAndIgnoreRemainingEvents()
        }
        
        coVerify(exactly = 1) { mockRepository.loadPage(20, 0) }
    }
    
    "loadInitialPage should emit Error on Network failure" {
        coEvery { mockRepository.loadPage(20, 0) } returns Either.Left(RepoError.Network)
        
        viewModel.uiState.test {
            awaitItem() shouldBe PokemonListUiState.Loading
            
            viewModel.loadInitialPage()
            testDispatcher.scheduler.advanceUntilIdle()
            
            val state = awaitItem()
            state.shouldBeInstanceOf<PokemonListUiState.Error>()
            state.message shouldBe "Network error. Please check your connection."
            
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    "loadNextPage should not load when state is not Content" {
        viewModel.loadNextPage()
        testDispatcher.scheduler.advanceUntilIdle()
        
        coVerify(exactly = 0) { mockRepository.loadPage(any(), any()) }
    }
    
    "loadNextPage should append Pokemon to existing list" {
        val firstPage = PokemonPage(
            listOf(Pokemon(1, "Bulbasaur", "https://example.com/1.png")),
            hasMore = true
        )
        val secondPage = PokemonPage(
            listOf(Pokemon(2, "Ivysaur", "https://example.com/2.png")),
            hasMore = false
        )
        
        coEvery { mockRepository.loadPage(20, 0) } returns Either.Right(firstPage)
        coEvery { mockRepository.loadPage(20, 1) } returns Either.Right(secondPage) // offset = allPokemons.size = 1
        
        // Load first page
        viewModel.loadInitialPage()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val firstState = viewModel.uiState.value
        firstState.shouldBeInstanceOf<PokemonListUiState.Content>()
        firstState.pokemons.size shouldBe 1
        
        // Load next page
        viewModel.loadNextPage()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Check final state
        val finalState = viewModel.uiState.value
        finalState.shouldBeInstanceOf<PokemonListUiState.Content>()
        finalState.pokemons.size shouldBe 2
        finalState.pokemons[0].name shouldBe "Bulbasaur"
        finalState.pokemons[1].name shouldBe "Ivysaur"
        finalState.hasMore shouldBe false
        
        coVerify(exactly = 1) { mockRepository.loadPage(20, 0) }
        coVerify(exactly = 1) { mockRepository.loadPage(20, 1) }
    }
    
    // Property-based tests with proper flow testing
    
    "property: any successful repository response leads to Content state" {
        checkAll(
            Arb.list(Arb.int(1..1000), 0..100),
            Arb.boolean()
        ) { ids, hasMore ->
            val pokemons = ids.map { id ->
                Pokemon(id, "Pokemon$id", "https://example.com/$id.png")
            }
            val page = PokemonPage(pokemons, hasMore)
            
            coEvery { mockRepository.loadPage(any(), any()) } returns Either.Right(page)
            
            val vm = PokemonListViewModel(
                repository = mockRepository,
                savedStateHandle = SavedStateHandle(),
                viewModelScope = testScope,
            )
            
            vm.uiState.test {
                skipItems(1) // Skip Loading
                
                vm.loadInitialPage()
                testDispatcher.scheduler.advanceUntilIdle()
                
                val state = awaitItem()
                state.shouldBeInstanceOf<PokemonListUiState.Content>()
                state.pokemons.size shouldBe pokemons.size
                state.hasMore shouldBe hasMore
                
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
    
    "property: any repository error leads to Error state with non-empty message" {
        forAll(arbRepoError()) { error ->
            coEvery { mockRepository.loadPage(any(), any()) } returns Either.Left(error)
            
            val vm = PokemonListViewModel(
                repository = mockRepository,
                savedStateHandle = SavedStateHandle(),
                viewModelScope = testScope,
            )
            
            vm.uiState.test {
                skipItems(1) // Skip Loading
                
                vm.loadInitialPage()
                testDispatcher.scheduler.advanceUntilIdle()
                
                val state = awaitItem()
                state.shouldBeInstanceOf<PokemonListUiState.Error>()
                state.message.shouldNotBeEmpty()
                
                cancelAndIgnoreRemainingEvents()
            }
            
            true
        }
    }
    
    "property: Content state preserves all Pokemon data from repository" {
        checkAll(
            Arb.list(Arb.int(1..1000), 1..50),
            Arb.list(arbPokemonName(), 1..50)
        ) { ids, names ->
            val pokemons = ids.zip(names).map { (id, name) ->
                Pokemon(id, name, "https://example.com/$id.png")
            }
            val page = PokemonPage(pokemons, hasMore = true)
            
            coEvery { mockRepository.loadPage(any(), any()) } returns Either.Right(page)
            
            val vm = PokemonListViewModel(
                repository = mockRepository,
                savedStateHandle = SavedStateHandle(),
                viewModelScope = testScope,
            )
            
            vm.uiState.test {
                skipItems(1) // Skip Loading
                
                vm.loadInitialPage()
                testDispatcher.scheduler.advanceUntilIdle()
                
                val state = awaitItem()
                state.shouldBeInstanceOf<PokemonListUiState.Content>()
                
                // Verify all data preserved
                state.pokemons.forEachIndexed { index, pokemon ->
                    pokemon.id shouldBe pokemons[index].id
                    pokemon.name shouldBe pokemons[index].name
                    pokemon.imageUrl shouldBe pokemons[index].imageUrl
                }
                
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
    
    "property: HTTP error codes always produce Error state with code in message" {
        checkAll(Arb.int(400..599)) { httpCode ->
            val error = RepoError.Http(httpCode, "Test error")
            coEvery { mockRepository.loadPage(any(), any()) } returns Either.Left(error)
            
            val vm = PokemonListViewModel(
                repository = mockRepository,
                savedStateHandle = SavedStateHandle(),
                viewModelScope = testScope,
            )
            
            vm.uiState.test {
                skipItems(1) // Skip Loading
                
                vm.loadInitialPage()
                testDispatcher.scheduler.advanceUntilIdle()
                
                val state = awaitItem()
                state.shouldBeInstanceOf<PokemonListUiState.Error>()
                state.message.contains(httpCode.toString()) shouldBe true
                
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
})

// Helper Arb generators for property-based testing

private fun arbRepoError(): Arb<RepoError> = 
    Arb.choice(
        Arb.constant(RepoError.Network),
        Arb.int(400..599).map { code -> RepoError.Http(code, "Error $code") },
        Arb.constant(RepoError.Unknown(RuntimeException("Test error")))
    )

private fun arbPokemonName(): Arb<String> = 
    Arb.string(3..20)
        .filter { it.all { c -> c.isLetterOrDigit() || c == ' ' || c == '-' } }
        .filter { it.isNotBlank() }
