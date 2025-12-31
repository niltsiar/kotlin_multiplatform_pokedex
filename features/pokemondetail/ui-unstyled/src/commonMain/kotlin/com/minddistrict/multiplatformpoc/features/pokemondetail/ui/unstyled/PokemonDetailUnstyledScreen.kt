package com.minddistrict.multiplatformpoc.features.pokemondetail.ui.unstyled

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeunstyled.Text
import com.composeunstyled.platformtheme.shapes
import com.composeunstyled.platformtheme.indications
import com.composeunstyled.platformtheme.sizeDefault
import com.composeunstyled.platformtheme.interactiveSizes
import com.composeunstyled.theme.Theme
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.*
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemondetail.presentation.PokemonDetailUiState
import com.minddistrict.multiplatformpoc.features.pokemondetail.presentation.PokemonDetailViewModel
import com.minddistrict.multiplatformpoc.features.pokemondetail.ui.unstyled.components.*
import kotlinx.coroutines.flow.collectLatest

/**
 * Unstyled Pokémon detail screen.
 * 
 * Displays detailed Pokémon information with minimalist design:
 * - Flat background (no gradients)
 * - Border-only badges and cards
 * - Monochrome stat bars
 * - Minimal spacing and elevation
 * 
 * @param viewModel ViewModel managing detail state and scroll position
 * @param onBackClick Callback for back navigation
 * @param modifier Modifier for the screen container
 */
@Composable
fun PokemonDetailUnstyledScreen(
    viewModel: PokemonDetailViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PokemonDetailContentUnstyled(
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

@Composable
internal fun PokemonDetailContentUnstyled(
    uiState: PokemonDetailUiState,
    restoredScrollIndex: Int = 0,
    restoredScrollOffset: Int = 0,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onScrollPositionChanged: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Theme[colors][background])
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        // Top app bar (back button + title)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Theme[colors][surface])
                .padding(Theme[spacing][spacingMd]),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Theme[spacing][spacingSm]),
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(Theme[shapes][shapeMedium])
                    .border(
                        width = 1.dp,
                        color = Theme[colors][onSurface].copy(alpha = 0.3f),
                        shape = Theme[shapes][shapeMedium]
                    )
                    .clickable(onClick = onBackClick)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "←",
                    style = Theme[typography][bodyMedium],
                    color = Theme[colors][onSurface]
                )
            }
            
            // Title
            Text(
                text = "Pokémon Detail",
                style = Theme[typography][titleLarge],
                color = Theme[colors][onSurface]
            )
        }
        
        // Content
        when (uiState) {
            is PokemonDetailUiState.Loading -> {
                LoadingStateUnstyledDetail()
            }
            
            is PokemonDetailUiState.Error -> {
                ErrorStateUnstyledDetail(
                    message = uiState.message,
                    onRetry = onRetry
                )
            }
            
            is PokemonDetailUiState.Content -> {
                val listState = rememberLazyListState(
                    initialFirstVisibleItemIndex = restoredScrollIndex,
                    initialFirstVisibleItemScrollOffset = restoredScrollOffset
                )
                
                LaunchedEffect(listState) {
                    snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
                        .collectLatest { (index, offset) ->
                            onScrollPositionChanged(index, offset)
                        }
                }
                
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        HeroSectionUnstyled(
                            imageUrl = uiState.pokemon.imageUrl,
                            id = uiState.pokemon.id,
                            name = uiState.pokemon.name
                        )
                    }
                    
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Theme[spacing][spacingLg]),
                            verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingLg])
                        ) {
                            // Types
                            TypeBadgeRowUnstyled(types = uiState.pokemon.types)
                            
                            // Physical attributes
                            PhysicalAttributesCardUnstyled(
                                height = uiState.pokemon.height,
                                weight = uiState.pokemon.weight,
                                baseExperience = uiState.pokemon.baseExperience
                            )
                            
                            // Abilities
                            AbilitiesSectionUnstyled(abilities = uiState.pokemon.abilities)
                            
                            // Base stats
                            BaseStatsSectionUnstyled(stats = uiState.pokemon.stats)
                            
                            Spacer(modifier = Modifier.height(Theme[spacing][spacingLg]))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingStateUnstyledDetail(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Loading...",
            style = Theme[typography][bodyMedium],
            color = Theme[colors][onSurface].copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ErrorStateUnstyledDetail(
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
            verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingMd]),
            modifier = Modifier.padding(Theme[spacing][spacingLg])
        ) {
            Text(
                text = message,
                style = Theme[typography][bodyMedium],
                color = Theme[colors][error],
                textAlign = TextAlign.Center
            )
            
            Box(
                modifier = Modifier
                    .clip(Theme[shapes][shapeMedium])
                    .border(
                        width = 1.dp,
                        color = Theme[colors][onSurface].copy(alpha = 0.5f),
                        shape = Theme[shapes][shapeMedium]
                    )
                    .clickable(onClick = onRetry)
                    .padding(
                        horizontal = Theme[spacing][spacingLg],
                        vertical = Theme[spacing][spacingSm]
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Retry",
                    style = Theme[typography][labelLarge],
                    color = Theme[colors][onSurface]
                )
            }
        }
    }
}
