package com.minddistrict.multiplatformpoc

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.navigation3.ui.NavDisplay
import com.minddistrict.multiplatformpoc.core.designsystem.material.MaterialScope
import com.minddistrict.multiplatformpoc.core.designsystem.theme.PokemonTheme
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.UnstyledScope
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.UnstyledTheme
import com.minddistrict.multiplatformpoc.core.di.coreModule
import com.minddistrict.multiplatformpoc.core.diui.navigationUiModule
import com.minddistrict.multiplatformpoc.core.navigation.Navigator
import com.minddistrict.multiplatformpoc.features.pokemondetail.wiring.pokemonDetailModule
import com.minddistrict.multiplatformpoc.features.pokemondetail.wiring.ui.pokemonDetailNavigationModule
import com.minddistrict.multiplatformpoc.features.pokemondetail.wiring.ui.unstyled.pokemonDetailNavigationUnstyledModule
import com.minddistrict.multiplatformpoc.features.pokemonlist.wiring.pokemonListModule
import com.minddistrict.multiplatformpoc.features.pokemonlist.wiring.ui.material.pokemonListNavigationModule
import com.minddistrict.multiplatformpoc.features.pokemonlist.wiring.ui.unstyled.pokemonListNavigationUnstyledModule
import com.minddistrict.multiplatformpoc.icons.rememberInfo
import com.minddistrict.multiplatformpoc.icons.rememberSettings
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.compose.scope.KoinScope
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module

private val rootModule = module {
    viewModel {
        RootViewModel(savedStateHandle = SavedStateHandle())
    }
}

@Composable
fun App() {
    KoinApplication(
        configuration = koinConfiguration(
            declaration = {
                modules(
                    rootModule +
                        coreModule(baseUrl = "https://pokeapi.co/api/v2") +
                        pokemonListModule +
                        pokemonDetailModule +
                        // Load BOTH Material and Unstyled navigation modules
                        // Scoped to MaterialScope and UnstyledScope respectively
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

        var showIntroDialog by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            if (!rootViewModel.hasSeenIntro) {
                showIntroDialog = true
            }
        }

        if (showIntroDialog) {
            IntroDialog(
                onMaterialSelected = {
                    rootViewModel.setTheme(DesignSystemTheme.MATERIAL)
                    rootViewModel.markIntroSeen()
                    showIntroDialog = false
                },
                onUnstyledSelected = {
                    rootViewModel.setTheme(DesignSystemTheme.UNSTYLED)
                    rootViewModel.markIntroSeen()
                    showIntroDialog = false
                },
            )
        }

        NavigationSuiteScaffold(
            navigationSuiteItems = {
                item(
                    selected = currentTheme == DesignSystemTheme.MATERIAL,
                    onClick = {
                        rootViewModel.setTheme(DesignSystemTheme.MATERIAL)
                    },
                    icon = { Icon(rememberSettings(), contentDescription = "Material") },
                    label = { Text("Material") },
                )
                item(
                    selected = currentTheme == DesignSystemTheme.UNSTYLED,
                    onClick = {
                        rootViewModel.setTheme(DesignSystemTheme.UNSTYLED)
                    },
                    icon = { Icon(rememberInfo(), contentDescription = "Unstyled") },
                    label = { Text("Unstyled") },
                )
            },
        ) {
            Crossfade(
                targetState = currentTheme,
                animationSpec = tween(durationMillis = 300),
                modifier = Modifier.fillMaxSize(),
            ) { theme ->
                when (theme) {
                    DesignSystemTheme.MATERIAL -> MaterialWorld()
                    DesignSystemTheme.UNSTYLED -> UnstyledWorld()
                }
            }
        }
    }
}

@Composable
private fun IntroDialog(
    onMaterialSelected: () -> Unit,
    onUnstyledSelected: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                text = "Welcome to Pokédex!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                Text(
                    text = "This app showcases two design system implementations:",
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "• Material Design 3 Expressive",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "Google's latest design system with dynamic theming",
                    style = MaterialTheme.typography.bodySmall,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "• Compose Unstyled",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "Minimalist design with Pokémon-themed tokens",
                    style = MaterialTheme.typography.bodySmall,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Both share the same logic. Switch between them anytime!",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            Button(onClick = onMaterialSelected) {
                Text("Start with Material")
            }
        },
        dismissButton = {
            TextButton(onClick = onUnstyledSelected) {
                Text("Start with Unstyled")
            }
        },
    )
}

@Composable
private fun MaterialWorld() {
    PokemonTheme {
        val navigator: Navigator = koinInject()

        // Get entry provider (collects navigation entries from Material scope)
        KoinScope<MaterialScope>("materialScope") {
            val entryProvider = koinEntryProvider()

            NavDisplay(
                backStack = navigator.backStack,
                onBack = { navigator.goBack() },
                entryProvider = entryProvider,
            )
        }
    }
}

@Composable
private fun UnstyledWorld() {
    UnstyledTheme {
        val navigator: Navigator = koinInject()

        // Get entry provider (collects navigation entries from Unstyled scope)
        KoinScope<UnstyledScope>("unstyledScope") {
            val entryProvider = koinEntryProvider()

            NavDisplay(
                backStack = navigator.backStack,
                onBack = { navigator.goBack() },
                entryProvider = entryProvider,
            )
        }

    }
}
