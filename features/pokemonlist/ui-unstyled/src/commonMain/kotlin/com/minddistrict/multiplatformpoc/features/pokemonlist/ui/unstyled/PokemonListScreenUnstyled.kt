package com.minddistrict.multiplatformpoc.features.pokemonlist.ui.unstyled

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.composeunstyled.ProgressBar
import com.composeunstyled.ProgressIndicator
import com.composeunstyled.Text
import com.composeunstyled.platformtheme.bright
import com.composeunstyled.platformtheme.indications
import com.composeunstyled.platformtheme.interactiveSize
import com.composeunstyled.platformtheme.interactiveSizes
import com.composeunstyled.platformtheme.roundedMedium
import com.composeunstyled.platformtheme.shapes
import com.composeunstyled.platformtheme.sizeDefault
import com.composeunstyled.platformtheme.text1
import com.composeunstyled.platformtheme.text3
import com.composeunstyled.platformtheme.textStyles
import com.composeunstyled.theme.Theme
import com.minddistrict.multiplatformpoc.core.designsystem.core.Elevation
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.UnstyledTheme
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.background
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.colors
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.error
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.onSurface
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.spacing
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.spacingMd
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.spacingSm
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.spacingXs
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.surface
import com.minddistrict.multiplatformpoc.features.pokemonlist.domain.Pokemon
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListUiState
import com.minddistrict.multiplatformpoc.features.pokemonlist.presentation.PokemonListViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PokemonListScreenUnstyled(
    viewModel: PokemonListViewModel,
    modifier: Modifier = Modifier,
    onPokemonClick: (Pokemon) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PokemonListContentUnstyled(
        uiState = uiState,
        restoredScrollIndex = viewModel.restoredScrollIndex,
        restoredScrollOffset = viewModel.restoredScrollOffset,
        onLoadMore = viewModel::loadNextPage,
        onPokemonClick = {
            viewModel.onPokemonSelected(it.id)
            onPokemonClick(it)
        },
        onScrollPositionChanged = viewModel::onScrollPositionChanged,
        modifier = modifier,
    )
}

@Composable
private fun PokemonListContentUnstyled(
    uiState: PokemonListUiState,
    restoredScrollIndex: Int,
    restoredScrollOffset: Int,
    onLoadMore: () -> Unit,
    onPokemonClick: (Pokemon) -> Unit = {},
    onScrollPositionChanged: (firstVisibleItemIndex: Int, firstVisibleItemScrollOffset: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is PokemonListUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                var progress by remember { mutableStateOf(0f) }
                LaunchedEffect(Unit) {
                    while (true) {
                        animate(
                            initialValue = 0f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse,
                            ),
                        ) { value, _ ->
                            progress = value
                        }
                    }
                }
                ProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    backgroundColor = Theme[colors][surface],
                    contentColor = Theme[colors][onSurface],
                ) {
                    ProgressBar()
                }
            }
        }

        is PokemonListUiState.Error -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(Theme[spacing][spacingMd]),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingMd]),
                ) {
                    Text(
                        text = uiState.message,
                        style = Theme[textStyles][text3],
                        color = Theme[colors][error],
                    )
                }
            }
        }

        is PokemonListUiState.Content -> {
            val gridState = rememberLazyGridState(
                initialFirstVisibleItemIndex = restoredScrollIndex,
                initialFirstVisibleItemScrollOffset = restoredScrollOffset,
            )

            LaunchedEffect(gridState) {
                snapshotFlow {
                    gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset
                }.collectLatest { (index, offset) ->
                    onScrollPositionChanged(index, offset)
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                contentPadding = PaddingValues(Theme[spacing][spacingMd]),
                horizontalArrangement = Arrangement.spacedBy(Theme[spacing][spacingMd]),
                verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingMd]),
                modifier = modifier
                    .fillMaxSize()
                    .background(Theme[colors][background]),
            ) {
                itemsIndexed(
                    items = uiState.pokemons,
                    key = { _, pokemon -> pokemon.id },
                ) { index, pokemon ->
                    PokemonCardUnstyled(
                        pokemon = pokemon,
                        onClick = { onPokemonClick(pokemon) },
                    )

                    // Load more when near end
                    LaunchedEffect(index, uiState.pokemons.size, uiState.isLoadingMore, uiState.hasMore) {
                        if (index >= uiState.pokemons.size - 4 && !uiState.isLoadingMore && uiState.hasMore) {
                            onLoadMore()
                        }
                    }
                }

                if (uiState.isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Theme[spacing][spacingMd]),
                            contentAlignment = Alignment.Center,
                        ) {
                            var progress by remember { mutableStateOf(0f) }
                            LaunchedEffect(Unit) {
                                while (true) {
                                    animate(
                                        initialValue = 0f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(2000, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse,
                                        ),
                                    ) { value, _ ->
                                        progress = value
                                    }
                                }
                            }
                            ProgressIndicator(
                                progress = progress,
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .height(6.dp),
                                shape = RoundedCornerShape(6.dp),
                                backgroundColor = Theme[colors][surface],
                                contentColor = Theme[colors][onSurface],
                            ) {
                                ProgressBar()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PokemonCardUnstyled(
    pokemon: Pokemon,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .interactiveSize(Theme[interactiveSizes][sizeDefault])  // Accessibility-friendly touch targets
            .shadow(
                elevation = Elevation.level2,
                shape = Theme[shapes][roundedMedium],
            )
            .clip(Theme[shapes][roundedMedium])
            .background(Theme[colors][surface])
            .border(
                width = 1.dp,
                color = Theme[colors][onSurface].copy(alpha = 0.12f),
                shape = Theme[shapes][roundedMedium],
            )
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = Theme[indications][bright],
                onClick = onClick,
            )
            .padding(Theme[spacing][spacingSm]),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingXs]),
        ) {
            // Pokemon Image
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.name,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
            )

            // Pokemon Number
            Text(
                text = "#${pokemon.id.toString().padStart(3, '0')}",
                style = Theme[textStyles][text1],
                color = Theme[colors][onSurface].copy(alpha = 0.6f),
            )

            // Pokemon Name
            Text(
                text = pokemon.name.replaceFirstChar { it.uppercase() },
                style = Theme[textStyles][text3],
                color = Theme[colors][onSurface],
            )
        }
    }
}

// ================================================================================================
// Previews
// ================================================================================================

@Preview
@Composable
private fun PreviewLoadingState() {
    UnstyledTheme {
        PokemonListContentUnstyled(
            uiState = PokemonListUiState.Loading,
            restoredScrollIndex = 0,
            restoredScrollOffset = 0,
            onLoadMore = {},
            onPokemonClick = {},
            onScrollPositionChanged = { _, _ -> },
        )
    }
}

@Preview
@Composable
private fun PreviewErrorState() {
    UnstyledTheme {
        PokemonListContentUnstyled(
            uiState = PokemonListUiState.Error("Failed to load Pokemon"),
            restoredScrollIndex = 0,
            restoredScrollOffset = 0,
            onLoadMore = {},
            onPokemonClick = {},
            onScrollPositionChanged = { _, _ -> },
        )
    }
}

@Preview
@Composable
private fun PreviewContentState() {
    UnstyledTheme {
        PokemonListContentUnstyled(
            uiState = PokemonListUiState.Content(
                pokemons = kotlinx.collections.immutable.persistentListOf(
                    Pokemon(id = 1, name = "Bulbasaur", imageUrl = ""),
                    Pokemon(id = 4, name = "Charmander", imageUrl = ""),
                    Pokemon(id = 7, name = "Squirtle", imageUrl = ""),
                    Pokemon(id = 25, name = "Pikachu", imageUrl = ""),
                ),
                hasMore = true,
                isLoadingMore = false,
            ),
            restoredScrollIndex = 0,
            restoredScrollOffset = 0,
            onLoadMore = {},
            onPokemonClick = {},
            onScrollPositionChanged = { _, _ -> },
        )
    }
}

@Preview
@Composable
private fun PreviewLoadingMoreState() {
    UnstyledTheme {
        PokemonListContentUnstyled(
            uiState = PokemonListUiState.Content(
                pokemons = kotlinx.collections.immutable.persistentListOf(
                    Pokemon(id = 1, name = "Bulbasaur", imageUrl = ""),
                    Pokemon(id = 4, name = "Charmander", imageUrl = ""),
                ),
                hasMore = true,
                isLoadingMore = true,
            ),
            restoredScrollIndex = 0,
            restoredScrollOffset = 0,
            onLoadMore = {},
            onPokemonClick = {},
            onScrollPositionChanged = { _, _ -> },
        )
    }
}

@Preview
@Composable
private fun PreviewPokemonCard() {
    UnstyledTheme {
        PokemonCardUnstyled(
            pokemon = Pokemon(id = 25, name = "Pikachu", imageUrl = ""),
            onClick = {},
        )
    }
}
