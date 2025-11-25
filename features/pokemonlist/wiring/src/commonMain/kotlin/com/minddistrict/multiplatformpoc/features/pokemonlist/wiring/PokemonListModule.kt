package com.minddistrict.multiplatformpoc.features.pokemonlist.wiring

import com.minddistrict.multiplatformpoc.core.httpclient.createHttpClient
import com.minddistrict.multiplatformpoc.features.pokemonlist.PokemonListRepository
import com.minddistrict.multiplatformpoc.features.pokemonlist.data.PokemonListApiService
import com.minddistrict.multiplatformpoc.features.pokemonlist.data.PokemonListRepository as createPokemonListRepository
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListViewModel
import io.ktor.client.HttpClient

// Manual DI (Metro integration later)
object PokemonListModule {
    
    private var httpClientInstance: HttpClient? = null
    
    fun provideHttpClient(): HttpClient {
        return httpClientInstance ?: createHttpClient().also {
            httpClientInstance = it
        }
    }
    
    fun providePokemonListApiService(): PokemonListApiService {
        return PokemonListApiService(provideHttpClient())
    }
    
    fun providePokemonListRepository(): PokemonListRepository {
        return createPokemonListRepository(providePokemonListApiService())
    }
    
    fun providePokemonListViewModel(): PokemonListViewModel {
        return PokemonListViewModel(providePokemonListRepository())
    }
}
