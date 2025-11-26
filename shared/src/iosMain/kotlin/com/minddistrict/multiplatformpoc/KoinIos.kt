package com.minddistrict.multiplatformpoc

import com.minddistrict.multiplatformpoc.core.di.coreModule
import com.minddistrict.multiplatformpoc.features.pokemonlist.wiring.pokemonListModule
import org.koin.core.context.startKoin
import org.koin.core.context.KoinContext
import org.koin.mp.KoinPlatform

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
            pokemonListModule
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
fun getPokemonListViewModel(): com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListViewModel {
    return KoinPlatform.getKoin().get()
}
