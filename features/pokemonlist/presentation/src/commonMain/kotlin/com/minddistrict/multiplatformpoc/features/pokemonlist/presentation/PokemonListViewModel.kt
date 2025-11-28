package com.minddistrict.multiplatformpoc.features.pokemonlist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minddistrict.multiplatformpoc.features.pokemonlist.PokemonListRepository
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.RepoError
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PokemonListViewModel(
    private val repository: PokemonListRepository,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : ViewModel(viewModelScope) {
    
    private val _uiState = MutableStateFlow<PokemonListUiState>(PokemonListUiState.Loading)
    val uiState: StateFlow<PokemonListUiState> = _uiState.asStateFlow()
    
    private val pageSize = 20
    private val allPokemons = mutableListOf<Pokemon>()
    
    fun loadInitialPage() {
        viewModelScope.launch {
            _uiState.value = PokemonListUiState.Loading
            loadPage(offset = 0)
        }
    }
    
    fun loadNextPage() {
        val currentState = _uiState.value
        if (currentState is PokemonListUiState.Content && 
            !currentState.isLoadingMore && 
            currentState.hasMore) {
            
            viewModelScope.launch {
                _uiState.value = currentState.copy(isLoadingMore = true)
                loadPage(offset = allPokemons.size)
            }
        }
    }
    
    private suspend fun loadPage(offset: Int) {
        repository.loadPage(limit = pageSize, offset = offset).fold(
            ifLeft = { error ->
                _uiState.value = PokemonListUiState.Error(error.toUiMessage())
            },
            ifRight = { page ->
                allPokemons.addAll(page.pokemons)
                _uiState.value = PokemonListUiState.Content(
                    pokemons = allPokemons.toImmutableList(),
                    isLoadingMore = false,
                    hasMore = page.hasMore
                )
            }
        )
    }
}

private fun RepoError.toUiMessage(): String = when (this) {
    is RepoError.Network -> "Network error. Please check your connection."
    is RepoError.Http -> "HTTP error $code: ${message ?: "Unknown error"}"
    is RepoError.Unknown -> "An unexpected error occurred."
}
