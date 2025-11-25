package com.minddistrict.multiplatformpoc.core.di

import com.minddistrict.multiplatformpoc.features.pokemonlist.navigation.PokemonList
import com.minddistrict.multiplatformpoc.navigation.Navigator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

/**
 * Core navigation providers for the application.
 * Provides the Navigator instance that manages the back stack.
 */
@BindingContainer
@ContributesTo(AppScope::class)
interface NavigationProviders {
    
    companion object {
        /**
         * Provides the singleton Navigator instance.
         * Initialized with PokemonList as the start destination.
         */
        @Provides
        @SingleIn(AppScope::class)
        fun provideNavigator(): Navigator {
            return Navigator(startDestination = PokemonList)
        }
    }
}
