package com.minddistrict.multiplatformpoc.features.pokemondetail.wiring

import com.minddistrict.multiplatformpoc.features.pokemondetail.PokemonDetailRepository
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.PokemonDetailApiService
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.PokemonDetailRepository as createPokemonDetailRepository
import com.minddistrict.multiplatformpoc.features.pokemondetail.presentation.PokemonDetailViewModel
import androidx.lifecycle.SavedStateHandle
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin DI module for Pokemon Detail feature.
 * 
 * Provides all dependencies needed for the Pokemon Detail feature:
     * - PokemonDetailApiService
     * - PokemonDetailRepository
     * - PokemonDetailViewModel (parameterized by pokemonUrl)
     */
val pokemonDetailModule = module {
    /**
     * Provides the API service for Pokemon Detail endpoints.
     */
    factory {
        PokemonDetailApiService(
            httpClient = get()
        )
    }
    
    /**
     * Provides the repository for Pokemon Detail data.
     */
    factory<PokemonDetailRepository> {
        createPokemonDetailRepository(
            apiService = get()
        )
    }
    
    /**
     * Provides the ViewModel for Pokemon Detail screen.
     * Takes pokemonUrl as a parameter via Koin's parametersOf.
     * 
     * Note: On Desktop/JVM, SavedStateHandle is created inline since Koin's Android-specific
     * parameter resolution doesn't work on non-Android platforms.
     * 
     * Usage: val viewModel: PokemonDetailViewModel = koinViewModel(parameters = { parametersOf(pokemonUrl) })
     */
    viewModel { (pokemonUrl: String) ->
        createPokemonDetailViewModel(
            repository = get(),
            pokemonUrl = pokemonUrl,
            savedStateHandle = SavedStateHandle(),
        )
    }
}

private fun createPokemonDetailViewModel(
    repository: PokemonDetailRepository,
    pokemonUrl: String,
    savedStateHandle: SavedStateHandle,
): PokemonDetailViewModel = PokemonDetailViewModel(
    repository = repository,
    pokemonUrl = pokemonUrl,
    savedStateHandle = savedStateHandle,
)
