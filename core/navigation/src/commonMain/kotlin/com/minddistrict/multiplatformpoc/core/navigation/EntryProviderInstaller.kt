package com.minddistrict.multiplatformpoc.core.navigation

import androidx.navigation3.runtime.EntryProviderScope

/**
 * Type alias for navigation entry provider installers.
 * Each feature module contributes an installer that registers its routes and composables
 * with the navigation graph.
 *
 * Pattern from Android nav3-recipes modular architecture.
 *
 * Example usage (Koin):
 * ```
 * // In :features:feature:wiring/androidMain
 * val featureNavigationModule = module {
 *     single<Set<EntryProviderInstaller>>(named("featureNavigationInstallers")) {
 *         setOf(
 *             {
 *                 entry<FeatureRoute> {
 *         PokemonListScreen(
 *             viewModel = viewModel,
 *             onPokemonClick = { id -> navigator.goTo(PokemonDetail(id)) }
 *         )
 *     }
 * }
 * ```
 */
typealias EntryProviderInstaller = EntryProviderScope<Any>.() -> Unit
