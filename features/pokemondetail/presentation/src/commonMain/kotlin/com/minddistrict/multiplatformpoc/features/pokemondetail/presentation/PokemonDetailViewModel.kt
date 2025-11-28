package com.minddistrict.multiplatformpoc.features.pokemondetail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minddistrict.multiplatformpoc.features.pokemondetail.PokemonDetailRepository
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.RepoError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PokemonDetailViewModel(
    private val repository: PokemonDetailRepository,
    private val pokemonId: Int,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : ViewModel(viewModelScope) {
    
    private val _uiState = MutableStateFlow<PokemonDetailUiState>(PokemonDetailUiState.Loading)
    val uiState: StateFlow<PokemonDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadPokemonDetail()
    }
    
    fun loadPokemonDetail() {
        viewModelScope.launch {
            _uiState.value = PokemonDetailUiState.Loading
            
            repository.getDetail(pokemonId).fold(
                ifLeft = { error ->
                    _uiState.value = PokemonDetailUiState.Error(error.toUiMessage())
                },
                ifRight = { pokemon ->
                    _uiState.value = PokemonDetailUiState.Content(pokemon = pokemon)
                }
            )
        }
    }
    
    fun retry() {
        loadPokemonDetail()
    }
}

private fun RepoError.toUiMessage(): String = when (this) {
    is RepoError.Network -> "Network error. Please check your connection."
    is RepoError.Http -> "Error $code: ${message ?: "Unknown error"}"
    is RepoError.Unknown -> "An unexpected error occurred."
}
