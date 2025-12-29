package com.minddistrict.multiplatformpoc

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.SavedStateHandle
import androidx.navigation3.ui.NavDisplay
import com.minddistrict.multiplatformpoc.core.designsystem.theme.PokemonTheme
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.UnstyledTheme
import com.minddistrict.multiplatformpoc.icons.rememberInfo
import com.minddistrict.multiplatformpoc.icons.rememberSettings
import com.minddistrict.multiplatformpoc.core.di.coreModule
import com.minddistrict.multiplatformpoc.core.diui.navigationUiModule
import com.minddistrict.multiplatformpoc.core.navigation.Navigator
import com.minddistrict.multiplatformpoc.features.pokemondetail.wiring.pokemonDetailModule
import com.minddistrict.multiplatformpoc.features.pokemondetail.wiringui.pokemonDetailNavigationModule
import com.minddistrict.multiplatformpoc.features.pokemondetail.wiringui.unstyled.pokemonDetailNavigationUnstyledModule
import com.minddistrict.multiplatformpoc.features.pokemonlist.wiring.pokemonListModule
import com.minddistrict.multiplatformpoc.features.pokemonlist.wiring.ui.material.pokemonListNavigationModule
import com.minddistrict.multiplatformpoc.features.pokemonlist.wiringui.unstyled.pokemonListNavigationUnstyledModule
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.KoinApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module

/**
 * Root-level Koin module for app-wide dependencies.
 * Defines RootViewModel for theme switching state management.
 */
private val rootModule = module {
    viewModel {
        RootViewModel(
            savedStateHandle = SavedStateHandle()
        )
    }
}

/**
 * Root app composable with dual-UI theme switching.
 * 
 * Phase 5 Implementation:
 * - Two design system worlds: Material and Unstyled
 * - Theme selection persisted via RootViewModel + SavedStateHandle
 * - Smooth Crossfade animation between design systems
 * - Both worlds share same Navigator and route objects
 */
@Composable
@Preview
fun App() {
    KoinApplication(
        configuration = koinConfiguration(
            declaration = fun KoinApplication.() {
                modules(
                    rootModule +
                        coreModule(baseUrl = "https://pokeapi.co/api/v2") +
                        pokemonListModule +
                        pokemonDetailModule +
                        pokemonListNavigationModule +
                        pokemonDetailNavigationModule +
                        pokemonListNavigationUnstyledModule +
                        pokemonDetailNavigationUnstyledModule +
                        navigationUiModule,
                )
            },
        ),
    ) {
        val rootViewModel: RootViewModel = koinViewModel()
        val currentTheme by rootViewModel.currentTheme.collectAsState()
        
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentTheme == DesignSystemTheme.MATERIAL,
                        onClick = { rootViewModel.setTheme(DesignSystemTheme.MATERIAL) },
                        icon = { Icon(rememberSettings(), "Material World") },
                        label = { Text("Material") }
                    )
                    NavigationBarItem(
                        selected = currentTheme == DesignSystemTheme.UNSTYLED,
                        onClick = { rootViewModel.setTheme(DesignSystemTheme.UNSTYLED) },
                        icon = { Icon(rememberInfo(), "Unstyled World") },
                        label = { Text("Unstyled") }
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Crossfade(
                    targetState = currentTheme,
                    animationSpec = tween(300),
                    modifier = Modifier.fillMaxSize()
                ) { theme ->
                    when (theme) {
                        DesignSystemTheme.MATERIAL -> MaterialWorld()
                        DesignSystemTheme.UNSTYLED -> UnstyledWorld()
                    }
                }
            }
        }
    }
}

/**
 * Material Design 3 world with Material-themed navigation.
 * Uses shared Navigator and route objects.
 */
@Composable
fun MaterialWorld() {
    val navigator: Navigator = koinInject()
    val materialEntryProvider = koinEntryProvider()
    
    PokemonTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavDisplay(
                backStack = navigator.backStack,
                onBack = { navigator.goBack() },
                entryProvider = materialEntryProvider,
            )
        }
    }
}

/**
 * Compose Unstyled world with Unstyled-themed navigation.
 * Uses shared Navigator and route objects.
 */
@Composable
fun UnstyledWorld() {
    val navigator: Navigator = koinInject()
    val unstyledEntryProvider = koinEntryProvider()
    
    UnstyledTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavDisplay(
                backStack = navigator.backStack,
                onBack = { navigator.goBack() },
                entryProvider = unstyledEntryProvider,
            )
        }
    }
}
