package com.minddistrict.multiplatformpoc

import com.minddistrict.multiplatformpoc.features.pokemondetail.wiring.pokemonDetailNavigationModule
import com.minddistrict.multiplatformpoc.features.pokemonlist.wiring.pokemonListNavigationModule
import org.koin.core.module.Module

actual fun getPlatformNavigationModules(): List<Module> = listOf(
    pokemonListNavigationModule,
    pokemonDetailNavigationModule
)
