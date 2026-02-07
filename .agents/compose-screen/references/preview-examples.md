# Preview Examples

Complete @Preview annotation examples for Compose screens.

## Basic Component Preview

```kotlin
@Composable
fun PokemonCard(
    pokemon: Pokemon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(
            defaultElevation = MaterialTheme.tokens.elevation.level2
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.tokens.spacing.medium)
        ) {
            Text(text = pokemon.name)
        }
    }
}

@Preview(name = "Default")
@Composable
private fun PokemonCardPreview() {
    PokemonTheme {
        Surface {
            PokemonCard(
                pokemon = Pokemon(name = "Bulbasaur", detailUrl = ""),
                onClick = {}
            )
        }
    }
}
```

## Screen-Level Previews with Mock Data

```kotlin
// PokemonListMaterialScreenPreviews.kt

@Preview(name = "Loading")
@Composable
private fun PokemonListLoadingPreview() {
    PokemonTheme {
        Surface {
            PokemonListMaterialContent(
                uiState = PokemonListUiState.Loading,
                onLoadMore = {},
                onPokemonClick = {},
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}

@Preview(name = "Error")
@Composable
private fun PokemonListErrorPreview() {
    PokemonTheme {
        Surface {
            PokemonListMaterialContent(
                uiState = PokemonListUiState.Error("Network error"),
                onLoadMore = {},
                onPokemonClick = {},
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}

@Preview(name = "Content - Few Items")
@Composable
private fun PokemonListContentFewPreview() {
    PokemonTheme {
        Surface {
            PokemonListMaterialContent(
                uiState = PokemonListUiState.Content(
                    pokemons = persistentListOf(
                        Pokemon(name = "Bulbasaur", detailUrl = ""),
                        Pokemon(name = "Charmander", detailUrl = "")
                    ),
                    isLoadingMore = false,
                    hasMore = true
                ),
                restoredScrollIndex = 0,
                restoredScrollOffset = 0,
                onLoadMore = {},
                onPokemonClick = {},
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}

@Preview(name = "Content - Loading More")
@Composable
private fun PokemonListLoadingMorePreview() {
    PokemonTheme {
        Surface {
            PokemonListMaterialContent(
                uiState = PokemonListUiState.Content(
                    pokemons = persistentListOf(
                        Pokemon(name = "Bulbasaur", detailUrl = ""),
                        Pokemon(name = "Charmander", detailUrl = "")
                    ),
                    isLoadingMore = true,
                    hasMore = true
                ),
                restoredScrollIndex = 0,
                restoredScrollOffset = 0,
                onLoadMore = {},
                onPokemonClick = {},
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}

@Preview(name = "Content - End of List")
@Composable
private fun PokemonListEndOfListPreview() {
    PokemonTheme {
        Surface {
            PokemonListMaterialContent(
                uiState = PokemonListUiState.Content(
                    pokemons = persistentListOf(
                        Pokemon(name = "Bulbasaur", detailUrl = ""),
                        Pokemon(name = "Charmander", detailUrl = "")
                    ),
                    isLoadingMore = false,
                    hasMore = false
                ),
                restoredScrollIndex = 0,
                restoredScrollOffset = 0,
                onLoadMore = {},
                onPokemonClick = {},
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}
```

## Multi-Preview Annotation

```kotlin
@Preview(
    name = "Light Theme",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark Theme",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    name = "Large Font",
    fontScale = 1.5f
)
@Composable
private fun PokemonCardMultiPreview() {
    PokemonTheme {
        Surface {
            PokemonCard(
                pokemon = Pokemon(name = "Bulbasaur", detailUrl = ""),
                onClick = {}
            )
        }
    }
}
```

## Preview with Different Screen Sizes

```kotlin
@Preview(
    name = "Phone",
    device = "spec:width=411dp,height=891dp,dpi=420"
)
@Preview(
    name = "Tablet",
    device = "spec:width=1280dp,height=800dp,dpi=240"
)
@Composable
private fun PokemonListResponsivePreview() {
    PokemonTheme {
        PokemonListMaterialContent(
            uiState = PokemonListUiState.Content(
                pokemons = persistentListOf(
                    Pokemon(name = "Bulbasaur", detailUrl = ""),
                    Pokemon(name = "Charmander", detailUrl = "")
                ),
                isLoadingMore = false,
                hasMore = true
            ),
            restoredScrollIndex = 0,
            restoredScrollOffset = 0,
            onLoadMore = {},
            onPokemonClick = {},
            onScrollPositionChanged = { _, _ -> }
        )
    }
}
```

## Unstyled Component Preview

```kotlin
@Preview(name = "Unstyled Card")
@Composable
private fun PokemonCardUnstyledPreview() {
    PokemonThemeUnstyled {
        PokemonCardUnstyled(
            pokemon = Pokemon(name = "Bulbasaur", detailUrl = ""),
            onClick = {}
        )
    }
}
```

## Preview Template

Copy-paste template for new previews:

```kotlin
@Preview(name = "<State Name>")
@Composable
private fun <Feature><State>Preview() {
    PokemonTheme {
        Surface {
            <Feature>MaterialContent(
                uiState = <Feature>UiState.<State>(
                    // mock data
                ),
                // callbacks
            )
        }
    }
}
```
