package com.minddistrict.multiplatformpoc

import com.minddistrict.multiplatformpoc.core.di.coreModule
import com.minddistrict.multiplatformpoc.features.pokemondetail.presentation.PokemonDetailViewModel
import com.minddistrict.multiplatformpoc.features.pokemonlist.wiring.pokemonListModule
import com.minddistrict.multiplatformpoc.features.pokemondetail.wiring.pokemonDetailModule
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListViewModel
import org.koin.core.context.startKoin
import org.koin.core.context.KoinContext
import org.koin.mp.KoinPlatform
import org.koin.core.parameter.parametersOf

/**
 * iOS-specific Koin initialization helper.
 * 
 * This function is called from Swift code to initialize Koin DI for iOS.
 * It configures only the modules needed for iOS (excludes Android/JVM navigation modules).
 * 
 * Usage in Swift:
 * ```swift
 * import Shared
 * 
 * @main
 * struct iOSApp: App {
 *     init() {
 *         KoinIosKt.doInitKoin(baseUrl: "https://pokeapi.co/api/v2")
 *     }
 * }
 * ```
 * 
 * @param baseUrl The base URL for API calls (e.g., "https://pokeapi.co/api/v2")
 */
fun initKoin(baseUrl: String) {
    startKoin {
        modules(
            coreModule(baseUrl),
            pokemonListModule,
            pokemonDetailModule
            // Note: Platform navigation modules (Android/JVM) are NOT included for iOS
            // iOS uses native SwiftUI NavigationStack instead of KMP Navigator
        )
    }
}

/**
 * Helper to get Koin instance from Swift.
 * 
 * Usage in Swift:
 * ```swift
 * let viewModel = KoinIosKt.getKoin().get(objCClass: PokemonListViewModel.self)
 * ```
 */
fun getKoin() = KoinPlatform.getKoin()

/**
 * Helper to get PokemonListViewModel from Koin for iOS.
 * This avoids dealing with Koin's Swift API complexity.
 * 
 * Usage in Swift:
 * ```swift
 * let viewModel = KoinIosKt.getPokemonListViewModel()
 * ```
 */
fun getPokemonListViewModel(): PokemonListViewModel {
    return KoinPlatform.getKoin().get()
}

/**
 * Helper to get PokemonDetailViewModel from Koin for iOS.
 * This avoids dealing with Koin's Swift API complexity.
 * 
 * Usage in Swift:
 * ```swift
 * let viewModel = KoinIosKt.getPokemonDetailViewModel(pokemonId: 25)
 * ```
 * 
 * @param pokemonId The ID of the Pokemon to load details for
 */
fun getPokemonDetailViewModel(pokemonId: Int): PokemonDetailViewModel {
    return KoinPlatform.getKoin().get { parametersOf(pokemonId) }
}
