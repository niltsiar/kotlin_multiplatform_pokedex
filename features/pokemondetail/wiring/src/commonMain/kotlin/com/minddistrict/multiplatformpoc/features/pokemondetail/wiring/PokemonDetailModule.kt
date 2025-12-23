package com.minddistrict.multiplatformpoc.features.pokemondetail.wiring

import com.minddistrict.multiplatformpoc.core.httpclient.createHttpClient
import com.minddistrict.multiplatformpoc.features.pokemondetail.PokemonDetailRepository
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.PokemonDetailApiService
import com.minddistrict.multiplatformpoc.features.pokemondetail.data.PokemonDetailRepository as createPokemonDetailRepository
import com.minddistrict.multiplatformpoc.features.pokemondetail.presentation.PokemonDetailViewModel
import io.ktor.client.HttpClient
import androidx.lifecycle.SavedStateHandle
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin DI module for Pokemon Detail feature.
 * 
 * Provides all dependencies needed for the Pokemon Detail feature:
 * - HttpClient (singleton, shared across all API services)
 * - PokemonDetailApiService
 * - PokemonDetailRepository
 * - PokemonDetailViewModel (parameterized by pokemonId)
 */
val pokemonDetailModule = module {
    /**
     * Provides a singleton HttpClient instance for making API requests.
     * Configured with content negotiation and logging.
     */
    single<HttpClient> { createHttpClient() }
    
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
     * Takes pokemonId as a parameter via Koin's parametersOf.
     * 
     * Note: On Desktop/JVM, SavedStateHandle is created inline since Koin's Android-specific
     * parameter resolution doesn't work on non-Android platforms.
     * 
     * Usage: val viewModel: PokemonDetailViewModel = koinViewModel(parameters = { parametersOf(pokemonId) })
     */
    viewModel { (pokemonId: Int) ->
        createPokemonDetailViewModel(
            repository = get(),
            pokemonId = pokemonId,
            savedStateHandle = SavedStateHandle(),
        )
    }
}

private fun createPokemonDetailViewModel(
    repository: PokemonDetailRepository,
    pokemonId: Int,
    savedStateHandle: SavedStateHandle,
): PokemonDetailViewModel = PokemonDetailViewModel(
    repository = repository,
    pokemonId = pokemonId,
    savedStateHandle = savedStateHandle,
)
