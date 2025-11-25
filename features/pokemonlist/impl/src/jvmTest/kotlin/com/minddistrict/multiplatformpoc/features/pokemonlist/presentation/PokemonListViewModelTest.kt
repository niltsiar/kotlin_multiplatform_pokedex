package com.minddistrict.multiplatformpoc.features.pokemonlist.presentation

import arrow.core.Either
import com.minddistrict.multiplatformpoc.features.pokemonlist.PokemonListRepository
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.PokemonPage
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.RepoError
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class PokemonListViewModelTest : StringSpec({
    lateinit var mockRepository: PokemonListRepository
    lateinit var viewModel: PokemonListViewModel
    
    beforeTest {
        mockRepository = mockk(relaxed = true)
        viewModel = PokemonListViewModel(
            mockRepository, 
            CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        )
    }
    
    "initial state should be Loading" {
        viewModel.uiState.value shouldBe PokemonListUiState.Loading
    }
    
    "loadInitialPage should emit Content on success" {
        val pokemon = Pokemon(1, "Bulbasaur", "https://example.com/1.png")
        val page = PokemonPage(listOf(pokemon), hasMore = true)
        
        coEvery { mockRepository.loadPage(20, 0) } returns Either.Right(page)
        
        viewModel.loadInitialPage()
        Thread.sleep(100) // Give coroutine time to complete
        
        val state = viewModel.uiState.value
        state.shouldBeInstanceOf<PokemonListUiState.Content>()
        val content = state as PokemonListUiState.Content
        content.pokemons.size shouldBe 1
        content.pokemons[0].name shouldBe "Bulbasaur"
        
        coVerify(exactly = 1) { mockRepository.loadPage(20, 0) }
    }
    
    "loadInitialPage should emit Error on Network failure" {
        coEvery { mockRepository.loadPage(20, 0) } returns Either.Left(RepoError.Network)
        
        viewModel.loadInitialPage()
        Thread.sleep(100)
        
        val state = viewModel.uiState.value
        state.shouldBeInstanceOf<PokemonListUiState.Error>()
        val error = state as PokemonListUiState.Error
        error.message shouldBe "Network error. Please check your connection."
    }
    
    "loadNextPage should not load when state is not Content" {
        viewModel.loadNextPage()
        Thread.sleep(100)
        
        coVerify(exactly = 0) { mockRepository.loadPage(any(), any()) }
    }
})
