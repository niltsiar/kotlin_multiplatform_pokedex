package com.minddistrict.multiplatformpoc.features.pokemonlist.presentation

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.serialization.saved
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PokemonListViewModel(
    private val repository: PokemonListRepository,
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : ViewModel(viewModelScope), DefaultLifecycleObserver {
    
    private var persistedState by savedStateHandle.saved { PokemonListPersistedState() }

    private val allPokemons = persistedState.pokemons.map { it.asDomain() }.toMutableList()

    private val _uiState = MutableStateFlow<PokemonListUiState>(restoreUiState())
    val uiState: StateFlow<PokemonListUiState> = _uiState.asStateFlow()
    
    val restoredScrollIndex: Int get() = persistedState.scrollIndex
    val restoredScrollOffset: Int get() = persistedState.scrollOffset
    val restoredScrollAnchorPokemonId: Int? get() = persistedState.scrollAnchorPokemonId
    val restoredLastSelectedPokemonId: Int? get() = persistedState.lastSelectedPokemonId

    private val pageSize: Int get() = persistedState.pageSize
    private val offset: Int get() = persistedState.offset

    override fun onStart(owner: LifecycleOwner) {
        // Avoid re-loading if we already have restored content.
        if (_uiState.value is PokemonListUiState.Content) return
        if (allPokemons.isNotEmpty()) {
            _uiState.update {
                PokemonListUiState.Content(
                    pokemons = allPokemons.toImmutableList(),
                    isLoadingMore = false,
                    hasMore = persistedState.hasMore,
                )
            }
            return
        }

        loadInitialPage()
    }
    
    fun loadInitialPage() {
        viewModelScope.launch {
            allPokemons.clear()
            persistedState = persistedState.copy(
                offset = 0,
                hasMore = true,
                pokemons = emptyList(),
                lastErrorMessage = null,
            )

            _uiState.update { PokemonListUiState.Loading }
            loadPage(offset = 0)
        }
    }
    
    fun loadNextPage() {
        val currentState = _uiState.value
        if (currentState is PokemonListUiState.Content && 
            !currentState.isLoadingMore && 
            currentState.hasMore) {
            
            viewModelScope.launch {
                _uiState.update { currentState.copy(isLoadingMore = true) }
                loadPage(offset = offset)
            }
        }
    }

    fun onScrollPositionChanged(
        firstVisibleItemIndex: Int,
        firstVisibleItemScrollOffset: Int,
        anchorPokemonId: Int? = null,
    ) {
        persistedState = persistedState.copy(
            scrollIndex = firstVisibleItemIndex,
            scrollOffset = firstVisibleItemScrollOffset,
            scrollAnchorPokemonId = anchorPokemonId ?: persistedState.scrollAnchorPokemonId,
        )
    }

    /**
     * iOS (SwiftUI) helper: persist an anchor Pokémon ID without touching the Compose scroll index/offset.
     */
    fun onScrollAnchorPokemonIdChanged(pokemonId: Int) {
        persistedState = persistedState.copy(scrollAnchorPokemonId = pokemonId)
    }

    fun onPokemonSelected(pokemonId: Int) {
        persistedState = persistedState.copy(lastSelectedPokemonId = pokemonId)
    }
    
    private suspend fun loadPage(offset: Int) {
        repository.loadPage(limit = pageSize, offset = offset).fold(
            ifLeft = { error ->
                val message = error.toUiMessage()
                persistedState = persistedState.copy(lastErrorMessage = message)
                _uiState.update { PokemonListUiState.Error(message) }
            },
            ifRight = { page ->
                allPokemons.addAll(page.pokemons)
                persistedState = persistedState.copy(
                    offset = allPokemons.size,
                    hasMore = page.hasMore,
                    pokemons = allPokemons.map { it.asSnapshot() },
                    lastErrorMessage = null,
                )
                _uiState.update {
                    PokemonListUiState.Content(
                        pokemons = allPokemons.toImmutableList(),
                        isLoadingMore = false,
                        hasMore = page.hasMore,
                    )
                }
            }
        )
    }

    private fun restoreUiState(): PokemonListUiState {
        return when {
            allPokemons.isNotEmpty() -> PokemonListUiState.Content(
                pokemons = allPokemons.toImmutableList(),
                isLoadingMore = false,
                hasMore = persistedState.hasMore,
            )
            persistedState.lastErrorMessage != null -> PokemonListUiState.Error(persistedState.lastErrorMessage!!)
            else -> PokemonListUiState.Loading
        }
    }
}

private fun RepoError.toUiMessage(): String = when (this) {
    is RepoError.Network -> "Network error. Please check your connection."
    is RepoError.Http -> "HTTP error $code: ${message ?: "Unknown error"}"
    is RepoError.Unknown -> "An unexpected error occurred."
}
