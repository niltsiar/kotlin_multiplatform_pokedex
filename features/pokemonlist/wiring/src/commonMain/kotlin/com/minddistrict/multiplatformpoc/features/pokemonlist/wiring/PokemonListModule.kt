package com.minddistrict.multiplatformpoc.features.pokemonlist.wiring

import com.minddistrict.multiplatformpoc.core.designsystem.navigation.AppDestination
import com.minddistrict.multiplatformpoc.core.httpclient.createHttpClient
import com.minddistrict.multiplatformpoc.features.pokemonlist.PokemonListRepository
import com.minddistrict.multiplatformpoc.features.pokemonlist.data.PokemonListApiService
import com.minddistrict.multiplatformpoc.features.pokemonlist.data.PokemonListRepository as createPokemonListRepository
import com.minddistrict.multiplatformpoc.features.pokemonlist.navigation.PokemonListDestination
import com.minddistrict.multiplatformpoc.features.pokemonlist.navigation.PokemonListEntry
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient

/**
 * Metro DI module for Pokemon List feature.
 * 
 * Provides all dependencies needed for the Pokemon List feature:
 * - HttpClient (singleton, shared across all API services)
 * - PokemonListApiService
 * - PokemonListRepository
 * - PokemonListViewModel
 * - Navigation destination and entry point
 */
@BindingContainer
@ContributesTo(AppScope::class)
interface PokemonListProviders {
    
    companion object {
        /**
         * Provides a singleton HttpClient instance for making API requests.
         * Configured with content negotiation and logging.
         */
        @Provides
        @SingleIn(AppScope::class)
        fun provideHttpClient(): HttpClient {
            return createHttpClient()
        }
        
        /**
         * Provides the API service for Pokemon List endpoints.
         * 
         * @param httpClient The HTTP client for making requests
         * @param baseUrl The base API URL (injected at graph creation)
         */
        @Provides
        fun providePokemonListApiService(
            httpClient: HttpClient,
            @Provides baseUrl: String
        ): PokemonListApiService {
            return PokemonListApiService(httpClient)
        }
        
        /**
         * Provides the repository for Pokemon List data.
         * 
         * @param apiService The API service for fetching data
         */
        @Provides
        fun providePokemonListRepository(
            apiService: PokemonListApiService
        ): PokemonListRepository {
            return createPokemonListRepository(apiService)
        }
        
        /**
         * Provides the ViewModel for Pokemon List screen.
         * 
         * @param repository The repository for Pokemon data
         */
        @Provides
        fun providePokemonListViewModel(
            repository: PokemonListRepository
        ): PokemonListViewModel {
            return PokemonListViewModel(repository)
        }
        
        /**
         * Contributes the Pokemon List destination to the app's navigation set.
         * Used by NavigationSuiteScaffold to configure adaptive navigation.
         */
        @Provides
        @IntoSet
        fun providePokemonListDestination(): AppDestination {
            return PokemonListDestination
        }
        
        /**
         * Provides the navigation entry point for Pokemon List feature.
         * Used for building navigation routes.
         */
        @Provides
        fun providePokemonListEntry(): PokemonListEntry {
            return object : PokemonListEntry {
                override val destination: AppDestination = PokemonListDestination
                override fun buildRoute(): String = PokemonListDestination.route
            }
        }
    }
}
