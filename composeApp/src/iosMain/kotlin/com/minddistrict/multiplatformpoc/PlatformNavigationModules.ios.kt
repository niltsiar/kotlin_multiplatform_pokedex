package com.minddistrict.multiplatformpoc

import com.minddistrict.multiplatformpoc.features.pokemondetail.wiringui.pokemonDetailNavigationModule
import com.minddistrict.multiplatformpoc.features.pokemonlist.wiringui.pokemonListNavigationModule
import org.koin.core.module.Module

actual fun getPlatformNavigationModules(): List<Module> = listOf(
    pokemonListNavigationModule,
    pokemonDetailNavigationModule
)
