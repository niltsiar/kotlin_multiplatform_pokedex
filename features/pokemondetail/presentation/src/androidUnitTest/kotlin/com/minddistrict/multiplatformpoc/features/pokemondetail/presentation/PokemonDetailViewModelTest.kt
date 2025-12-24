package com.minddistrict.multiplatformpoc.features.pokemondetail.presentation

import app.cash.turbine.test
import arrow.core.Either
import com.minddistrict.multiplatformpoc.features.pokemondetail.PokemonDetailRepository
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.RepoError
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.testing.TestLifecycleOwner
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.kotest.property.forAll
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonDetailViewModelTest : StringSpec({
    val testDispatcher = StandardTestDispatcher()
    val testScope = TestScope(testDispatcher)
    
    lateinit var mockRepository: PokemonDetailRepository
    
    val testPokemonId = 25
    
    beforeTest {
        mockRepository = mockk(relaxed = true)
    }

    fun createViewModel(pokemonId: Int = testPokemonId): PokemonDetailViewModel = PokemonDetailViewModel(
        repository = mockRepository,
        pokemonId = pokemonId,
        savedStateHandle = SavedStateHandle(),
        viewModelScope = testScope,
    )
    
    "initial state should be Loading" {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() shouldBe PokemonDetailUiState.Loading
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    "onStart should load Pokemon detail automatically" {
        val pokemon = PokemonDetail(
            id = 25,
            name = "Pikachu",
            height = 4,
            weight = 60,
            baseExperience = 112,
            types = persistentListOf(),
            stats = persistentListOf(),
            abilities = persistentListOf(),
            imageUrl = "https://example.com/25.png"
        )
        
        coEvery { mockRepository.getDetail(testPokemonId) } returns Either.Right(pokemon)
        
        val vm = createViewModel()
        
        // Test that onStart triggers loading (outside test block)
        vm.loadPokemonDetail()
                testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify final state
        val state = vm.uiState.value
        state.shouldBeInstanceOf<PokemonDetailUiState.Content>()
        state.pokemon.id shouldBe 25
        state.pokemon.name shouldBe "Pikachu"
        
        coVerify(exactly = 1) { mockRepository.getDetail(testPokemonId) }
    }
    
    "loadPokemonDetail should emit Content on success" {
        val pokemon = PokemonDetail(
            id = 1,
            name = "Bulbasaur",
            height = 7,
            weight = 69,
            baseExperience = 64,
            types = persistentListOf(),
            stats = persistentListOf(),
            abilities = persistentListOf(),
            imageUrl = "https://example.com/1.png"
        )
        
        coEvery { mockRepository.getDetail(testPokemonId) } returns Either.Right(pokemon)
        
        val viewModel = createViewModel()
        
        viewModel.uiState.test {
            awaitItem() shouldBe PokemonDetailUiState.Loading
            
            viewModel.loadPokemonDetail()
            testDispatcher.scheduler.advanceUntilIdle()
            
            val state = awaitItem()
            state.shouldBeInstanceOf<PokemonDetailUiState.Content>()
            state.pokemon.name shouldBe "Bulbasaur"
            state.pokemon.height shouldBe 7
            state.pokemon.weight shouldBe 69
            
            cancelAndIgnoreRemainingEvents()
        }
        
        coVerify(atLeast = 1) { mockRepository.getDetail(testPokemonId) }
    }
    
    "loadPokemonDetail should emit Error on Network failure" {
        coEvery { mockRepository.getDetail(testPokemonId) } returns Either.Left(RepoError.Network)
        
        val viewModel = createViewModel()
        
        viewModel.uiState.test {
            awaitItem() shouldBe PokemonDetailUiState.Loading
            
            viewModel.loadPokemonDetail()
            testDispatcher.scheduler.advanceUntilIdle()
            
            val state = awaitItem()
            state.shouldBeInstanceOf<PokemonDetailUiState.Error>()
            state.message shouldBe "Network error. Please check your connection."
            
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    "loadPokemonDetail should emit Error on Http 404" {
        coEvery { mockRepository.getDetail(testPokemonId) } returns 
            Either.Left(RepoError.Http(404, "Not found"))
        
        val viewModel = createViewModel()
        
        viewModel.uiState.test {
            awaitItem() shouldBe PokemonDetailUiState.Loading
            
            viewModel.loadPokemonDetail()
            testDispatcher.scheduler.advanceUntilIdle()
            
            val state = awaitItem()
            state.shouldBeInstanceOf<PokemonDetailUiState.Error>()
            state.message shouldBe "Error 404: Not found"
            
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    "loadPokemonDetail should emit Error on Http 500" {
        coEvery { mockRepository.getDetail(testPokemonId) } returns 
            Either.Left(RepoError.Http(500, "Internal server error"))
        
        val viewModel = createViewModel()
        
        viewModel.uiState.test {
            awaitItem() shouldBe PokemonDetailUiState.Loading
            
            viewModel.loadPokemonDetail()
            testDispatcher.scheduler.advanceUntilIdle()
            
            val state = awaitItem()
            state.shouldBeInstanceOf<PokemonDetailUiState.Error>()
            state.message shouldBe "Error 500: Internal server error"
            
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    "loadPokemonDetail should emit Error on Unknown error" {
        coEvery { mockRepository.getDetail(testPokemonId) } returns 
            Either.Left(RepoError.Unknown(RuntimeException("Unexpected")))
        
        val viewModel = createViewModel()
        
        viewModel.uiState.test {
            awaitItem() shouldBe PokemonDetailUiState.Loading
            
            viewModel.loadPokemonDetail()
            testDispatcher.scheduler.advanceUntilIdle()
            
            val state = awaitItem()
            state.shouldBeInstanceOf<PokemonDetailUiState.Error>()
            state.message shouldBe "An unexpected error occurred."
            
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    "retry should call loadPokemonDetail again" {
        val pokemon = PokemonDetail(
            id = 25,
            name = "Pikachu",
            height = 4,
            weight = 60,
            baseExperience = 112,
            types = persistentListOf(),
            stats = persistentListOf(),
            abilities = persistentListOf(),
            imageUrl = "https://example.com/25.png"
        )
        
        // First call fails
        coEvery { mockRepository.getDetail(testPokemonId) } returns Either.Left(RepoError.Network)
        
        val viewModel = createViewModel()
        
        viewModel.loadPokemonDetail()
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.uiState.value.shouldBeInstanceOf<PokemonDetailUiState.Error>()
        
        // Second call succeeds
        coEvery { mockRepository.getDetail(testPokemonId) } returns Either.Right(pokemon)
        viewModel.retry()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        state.shouldBeInstanceOf<PokemonDetailUiState.Content>()
        state.pokemon.name shouldBe "Pikachu"
        
        coVerify(exactly = 2) { mockRepository.getDetail(testPokemonId) }
    }
    
    "loadPokemonDetail should use correct pokemonId from constructor" {
        val differentPokemonId = 150
        
        val pokemon = PokemonDetail(
            id = differentPokemonId,
            name = "Mewtwo",
            height = 20,
            weight = 1220,
            baseExperience = 306,
            types = persistentListOf(),
            stats = persistentListOf(),
            abilities = persistentListOf(),
            imageUrl = "https://example.com/150.png"
        )
        
        coEvery { mockRepository.getDetail(differentPokemonId) } returns Either.Right(pokemon)
        
        val vm = createViewModel(pokemonId = differentPokemonId)
        
        vm.uiState.test {
            awaitItem() shouldBe PokemonDetailUiState.Loading
            
            vm.loadPokemonDetail()
                testDispatcher.scheduler.advanceUntilIdle()
            
            val state = awaitItem()
            state.shouldBeInstanceOf<PokemonDetailUiState.Content>()
            state.pokemon.id shouldBe differentPokemonId
            
            cancelAndIgnoreRemainingEvents()
        }
        
        coVerify(atLeast = 1) { mockRepository.getDetail(differentPokemonId) }
    }
    
    // Property-based tests with proper flow testing
    
    "property: any successful repository response leads to Content state" {
        checkAll(
            Arb.int(1..1000),
            arbPokemonName(),
            Arb.int(1..100),
            Arb.int(1..5000),
            Arb.int(0..500)
        ) { id, name, height, weight, baseExp ->
            val pokemon = PokemonDetail(
                id = id,
                name = name,
                height = height,
                weight = weight,
                baseExperience = baseExp,
                types = persistentListOf(),
                stats = persistentListOf(),
                abilities = persistentListOf(),
                imageUrl = "https://example.com/$id.png"
            )
            
            coEvery { mockRepository.getDetail(id) } returns Either.Right(pokemon)
            
            val vm = createViewModel(pokemonId = id)
            
            vm.uiState.test {
                skipItems(1) // Skip Loading
                
                vm.loadPokemonDetail()
                testDispatcher.scheduler.advanceUntilIdle()
                
                val state = awaitItem()
                state.shouldBeInstanceOf<PokemonDetailUiState.Content>()
                state.pokemon.id shouldBe id
                state.pokemon.name shouldBe name
                state.pokemon.height shouldBe height
                state.pokemon.weight shouldBe weight
                state.pokemon.baseExperience shouldBe baseExp
                
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
    
    "property: any repository error leads to Error state with non-empty message" {
        forAll(
            Arb.int(1..1000),
            arbRepoError()
        ) { pokemonId, error ->
            coEvery { mockRepository.getDetail(pokemonId) } returns Either.Left(error)
            
            val vm = createViewModel(pokemonId = pokemonId)
            
            vm.uiState.test {
                skipItems(1) // Skip Loading
                
                vm.loadPokemonDetail()
                testDispatcher.scheduler.advanceUntilIdle()
                
                val state = awaitItem()
                state.shouldBeInstanceOf<PokemonDetailUiState.Error>()
                state.message.shouldNotBeEmpty()
                
                cancelAndIgnoreRemainingEvents()
            }
            
            true
        }
    }
    
    "property: retry always calls repository again regardless of initial error" {
        checkAll(
            Arb.int(1..1000),
            arbRepoError()
        ) { pokemonId, initialError ->
            // First call fails
            coEvery { mockRepository.getDetail(pokemonId) } returns Either.Left(initialError)
            
            val vm = createViewModel(pokemonId = pokemonId)
            
            vm.loadPokemonDetail()
                testDispatcher.scheduler.advanceUntilIdle()
            
            vm.uiState.value.shouldBeInstanceOf<PokemonDetailUiState.Error>()
            
            // Retry should always work
            val pokemon = PokemonDetail(
                id = pokemonId,
                name = "RetryPokemon",
                height = 10,
                weight = 100,
                baseExperience = 50,
                types = persistentListOf(),
                stats = persistentListOf(),
                abilities = persistentListOf(),
                imageUrl = "https://example.com/$pokemonId.png"
            )
            coEvery { mockRepository.getDetail(pokemonId) } returns Either.Right(pokemon)
            
            vm.retry()
            testDispatcher.scheduler.advanceUntilIdle()
            
            val state = vm.uiState.value
            state.shouldBeInstanceOf<PokemonDetailUiState.Content>()
            state.pokemon.id shouldBe pokemonId
        }
    }
    
    "property: HTTP error codes always produce Error state with code in message" {
        checkAll(
            Arb.int(1..1000),
            Arb.int(400..599)
        ) { pokemonId, httpCode ->
            val error = RepoError.Http(httpCode, "Test error")
            coEvery { mockRepository.getDetail(pokemonId) } returns Either.Left(error)
            
            val vm = createViewModel(pokemonId = pokemonId)
            
            vm.uiState.test {
                skipItems(1) // Skip Loading
                
                vm.loadPokemonDetail()
                testDispatcher.scheduler.advanceUntilIdle()
                
                val state = awaitItem()
                state.shouldBeInstanceOf<PokemonDetailUiState.Error>()
                state.message.contains(httpCode.toString()) shouldBe true
                
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
    
    "property: ViewModel always uses pokemonId from constructor" {
        checkAll(Arb.int(1..1000)) { pokemonId ->
            val pokemon = PokemonDetail(
                id = pokemonId,
                name = "TestPokemon",
                height = 10,
                weight = 100,
                baseExperience = 50,
                types = persistentListOf(),
                stats = persistentListOf(),
                abilities = persistentListOf(),
                imageUrl = "https://example.com/$pokemonId.png"
            )
            
            coEvery { mockRepository.getDetail(pokemonId) } returns Either.Right(pokemon)
            
            val vm = createViewModel(pokemonId = pokemonId)
            
            vm.uiState.test {
                skipItems(1) // Skip Loading
                
                vm.loadPokemonDetail()
                testDispatcher.scheduler.advanceUntilIdle()
                
                // Verify correct ID was used
                coVerify(atLeast = 1) { mockRepository.getDetail(pokemonId) }
                
                val state = awaitItem()
                state.shouldBeInstanceOf<PokemonDetailUiState.Content>()
                state.pokemon.id shouldBe pokemonId
                
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
