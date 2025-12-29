package com.minddistrict.multiplatformpoc.features.pokemondetail.presentation

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.serialization.saved
import com.minddistrict.multiplatformpoc.features.pokemondetail.PokemonDetailRepository
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.RepoError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PokemonDetailViewModel(
    private val repository: PokemonDetailRepository,
    private val pokemonId: Int,
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : ViewModel(viewModelScope), DefaultLifecycleObserver {
    
    private var persistedState by savedStateHandle.saved { PokemonDetailPersistedState(pokemonId = pokemonId) }

    private val _uiState = MutableStateFlow<PokemonDetailUiState>(restoreUiState())
    val uiState: StateFlow<PokemonDetailUiState> = _uiState.asStateFlow()
    
    // Expose scroll position for UI restoration (same pattern as PokemonListViewModel)
    val restoredScrollIndex: Int get() = persistedState.scrollPosition
    val restoredScrollOffset: Int get() = persistedState.scrollOffset
    
    override fun onStart(owner: LifecycleOwner) {
        // Only load if we have no restored content or error.
        if (_uiState.value is PokemonDetailUiState.Loading) {
            loadPokemonDetail()
        }
    }
    
    fun loadPokemonDetail() {
        viewModelScope.launch {
            _uiState.update { PokemonDetailUiState.Loading }
            
            repository.getDetail(pokemonId).fold(
                ifLeft = { error ->
                    val message = error.toUiMessage()
                    persistedState = persistedState.copy(
                        lastErrorMessage = message,
                        pokemon = null,
                    )
                    _uiState.update { PokemonDetailUiState.Error(message) }
                },
                ifRight = { pokemon ->
                    persistedState = persistedState.copy(
                        lastErrorMessage = null,
                        pokemon = pokemon.asSnapshot(),
                    )
                    _uiState.update {
                        PokemonDetailUiState.Content(
                            pokemon = pokemon,
                        )
                    }
                }
            )
        }
    }
    
    fun retry() {
        loadPokemonDetail()
    }

    /**
     * Save scroll position to persist across theme switches and process death.
     */
    fun saveScrollPosition(firstVisibleItemIndex: Int, firstVisibleItemScrollOffset: Int) {
        persistedState = persistedState.copy(
            scrollPosition = firstVisibleItemIndex,
            scrollOffset = firstVisibleItemScrollOffset,
        )
    }

    private fun restoreUiState(): PokemonDetailUiState {
        return when {
            persistedState.pokemon != null -> PokemonDetailUiState.Content(
                pokemon = persistedState.pokemon!!.asDomain(),
            )
            persistedState.lastErrorMessage != null -> PokemonDetailUiState.Error(persistedState.lastErrorMessage!!)
            else -> PokemonDetailUiState.Loading
        }
    }

}

private fun RepoError.toUiMessage(): String = when (this) {
    is RepoError.Network -> "Network error. Please check your connection."
    is RepoError.Http -> "Error $code: ${message ?: "Unknown error"}"
    is RepoError.Unknown -> "An unexpected error occurred."
}
