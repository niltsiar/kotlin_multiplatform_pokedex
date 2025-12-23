package com.minddistrict.multiplatformpoc.features.pokemonlist.wiring

import com.minddistrict.multiplatformpoc.core.httpclient.createHttpClient
import com.minddistrict.multiplatformpoc.features.pokemonlist.PokemonListRepository
import com.minddistrict.multiplatformpoc.features.pokemonlist.data.PokemonListApiService
import com.minddistrict.multiplatformpoc.features.pokemonlist.data.PokemonListRepository as createPokemonListRepository
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListViewModel
import io.ktor.client.HttpClient
import androidx.lifecycle.SavedStateHandle
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin DI module for Pokemon List feature.
 * 
 * Provides all dependencies needed for the Pokemon List feature:
 * - HttpClient (singleton, shared across all API services)
 * - PokemonListApiService
 * - PokemonListRepository
 * - PokemonListViewModel
 */
val pokemonListModule = module {
    /**
     * Provides a singleton HttpClient instance for making API requests.
     * Configured with content negotiation and logging.
     */
    single<HttpClient> { createHttpClient() }
    
    /**
     * Provides the API service for Pokemon List endpoints.
     */
    factory {
        PokemonListApiService(
            httpClient = get()
        )
    }
    
    /**
     * Provides the repository for Pokemon List data.
     */
    factory<PokemonListRepository> {
        createPokemonListRepository(
            apiService = get()
        )
    }
    
    /**
     * Provides the ViewModel for Pokemon List screen.
     * 
     * Note: On Desktop/JVM, SavedStateHandle is created inline since Koin's Android-specific
     * parameter resolution doesn't work on non-Android platforms.
     */
    viewModel {
        createPokemonListViewModel(
            repository = get(),
            savedStateHandle = SavedStateHandle(),
        )
    }
}

private fun createPokemonListViewModel(
    repository: PokemonListRepository,
    savedStateHandle: SavedStateHandle,
): PokemonListViewModel = PokemonListViewModel(
    repository = repository,
    savedStateHandle = savedStateHandle,
)
