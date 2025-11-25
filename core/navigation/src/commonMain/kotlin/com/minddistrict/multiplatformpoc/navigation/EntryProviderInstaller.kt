package com.minddistrict.multiplatformpoc.navigation

import androidx.navigation3.runtime.EntryProviderScope

/**
 * Type alias for navigation entry provider installers.
 * Each feature module contributes an installer that registers its routes and composables
 * with the navigation graph.
 *
 * Pattern from Android nav3-recipes modular architecture.
 *
 * Example usage:
 * ```
 * @Provides
 * @IntoSet
 * fun providePokemonListNavigation(
 *     navigator: Navigator,
 *     viewModel: PokemonListViewModel
 * ): EntryProviderInstaller = {
 *     entry<PokemonList> {
 *         PokemonListScreen(
 *             viewModel = viewModel,
 *             onPokemonClick = { id -> navigator.goTo(PokemonDetail(id)) }
 *         )
 *     }
 * }
 * ```
 */
typealias EntryProviderInstaller = EntryProviderScope<Any>.() -> Unit
