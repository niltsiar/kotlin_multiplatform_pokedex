package com.minddistrict.multiplatformpoc.features.pokemondetail.ui.material

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.minddistrict.multiplatformpoc.core.designsystem.material.theme.PokemonTypeColors
import com.minddistrict.multiplatformpoc.core.designsystem.material.tokens.tokens
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemondetail.presentation.PokemonDetailUiState
import com.minddistrict.multiplatformpoc.features.pokemondetail.presentation.PokemonDetailViewModel
import com.minddistrict.multiplatformpoc.features.pokemondetail.ui.material.components.AbilitiesSection
import com.minddistrict.multiplatformpoc.features.pokemondetail.ui.material.components.BaseStatsSection
import com.minddistrict.multiplatformpoc.features.pokemondetail.ui.material.components.HeroSection
import com.minddistrict.multiplatformpoc.features.pokemondetail.ui.material.components.PhysicalAttributesCard
import com.minddistrict.multiplatformpoc.features.pokemondetail.ui.material.components.TypeBadgeRow
import kotlinx.coroutines.flow.collectLatest

/**
 * Material Design 3 Pokémon detail screen.
 * 
 * Main entry point for the Pokémon detail feature with Material styling.
 * Coordinates ViewModel state with composable UI components.
 * 
 * @param viewModel ViewModel managing detail state and scroll position
 * @param onBackClick Callback for back navigation
 * @param modifier Modifier for the screen container
 */
@Composable
fun PokemonDetailMaterialScreen(
    viewModel: PokemonDetailViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    PokemonDetailMaterialContent(
        uiState = uiState,
        restoredScrollIndex = viewModel.restoredScrollIndex,
        restoredScrollOffset = viewModel.restoredScrollOffset,
        onBackClick = onBackClick,
        onRetry = { viewModel.retry() },
        onScrollPositionChanged = { position, offset ->
            viewModel.saveScrollPosition(position, offset)
        },
        modifier = modifier
    )
}

/**
 * Material Design 3 Pokémon detail content.
 * 
 * Renders different states (Loading, Error, Content) using focused components.
 * 
 * @param uiState Current UI state
 * @param restoredScrollIndex Restored scroll position index
 * @param restoredScrollOffset Restored scroll position offset
 * @param onBackClick Callback for back navigation
 * @param onRetry Callback to retry loading
 * @param onScrollPositionChanged Callback to save scroll position
 * @param modifier Modifier for the content container
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PokemonDetailMaterialContent(
    uiState: PokemonDetailUiState,
    restoredScrollIndex: Int = 0,
    restoredScrollOffset: Int = 0,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onScrollPositionChanged: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (uiState is PokemonDetailUiState.Content) {
                        Text(uiState.pokemon.name)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text(
                            text = "←",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is PokemonDetailUiState.Loading -> LoadingState()
                is PokemonDetailUiState.Content -> ContentState(
                    pokemon = uiState.pokemon,
                    scrollPosition = restoredScrollIndex,
                    scrollOffset = restoredScrollOffset,
                    onScrollPositionChanged = onScrollPositionChanged,
                )
                is PokemonDetailUiState.Error -> ErrorState(
                    message = uiState.message,
                    onRetry = onRetry
                )
            }
        }
    }
}

/**
 * Loading state with centered progress indicator.
 */
@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Error state with message and retry button.
 */
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.tokens.spacing.medium),
            modifier = Modifier.padding(MaterialTheme.tokens.spacing.xl)
        ) {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

/**
 * Content state displaying Pokémon details using component library.
 */
@Composable
private fun ContentState(
    pokemon: PokemonDetail,
    scrollPosition: Int,
    scrollOffset: Int,
    onScrollPositionChanged: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val primaryType = pokemon.types.firstOrNull()?.name ?: "normal"
    val typeColor = PokemonTypeColors.getBackground(primaryType, isDark)
    
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = scrollPosition,
        initialFirstVisibleItemScrollOffset = scrollOffset
    )

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }.collectLatest { (index, offset) ->
            onScrollPositionChanged(index, offset)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
    ) {
        // Hero Section
        item {
            HeroSection(
                name = pokemon.name,
                id = pokemon.id,
                imageUrl = pokemon.imageUrl,
                typeColor = typeColor
            )
        }
        
        // Type Badges
        item {
            TypeBadgeRow(
                types = pokemon.types,
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.tokens.spacing.medium,
                    vertical = MaterialTheme.tokens.spacing.xs
                )
            )
        }
        
        // Physical Attributes
        item {
            PhysicalAttributesCard(
                height = pokemon.height,
                weight = pokemon.weight,
                baseExperience = pokemon.baseExperience,
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.tokens.spacing.medium,
                    vertical = MaterialTheme.tokens.spacing.xs
                )
            )
        }
        
        // Abilities
        item {
            AbilitiesSection(
                abilities = pokemon.abilities,
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.tokens.spacing.medium,
                    vertical = MaterialTheme.tokens.spacing.xs
                )
            )
        }
        
        // Base Stats
        item {
            BaseStatsSection(
                stats = pokemon.stats,
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.tokens.spacing.medium,
                    vertical = MaterialTheme.tokens.spacing.xs
                )
            )
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(MaterialTheme.tokens.spacing.xl))
        }
    }
}
