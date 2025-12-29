package com.minddistrict.multiplatformpoc.features.pokemondetail.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.minddistrict.multiplatformpoc.core.designsystem.theme.PokemonTheme
import com.minddistrict.multiplatformpoc.core.designsystem.theme.PokemonTypeColors
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.Ability
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.Stat
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.TypeOfPokemon
import com.minddistrict.multiplatformpoc.features.pokemondetail.presentation.PokemonDetailUiState
import com.minddistrict.multiplatformpoc.features.pokemondetail.presentation.PokemonDetailViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.snapshotFlow
import kotlinx.collections.immutable.persistentListOf

@Composable
fun PokemonDetailScreenMaterial(
    viewModel: PokemonDetailViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    PokemonDetailContent(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PokemonDetailContent(
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
                    Button(onClick = onBackClick) {
                        Text("← Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
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
                is PokemonDetailUiState.Loading -> LoadingContent()
                is PokemonDetailUiState.Content -> PokemonDetailBody(
                    pokemon = uiState.pokemon,
                    scrollPosition = restoredScrollIndex,
                    scrollOffset = restoredScrollOffset,
                    onScrollPositionChanged = onScrollPositionChanged,
                )
                is PokemonDetailUiState.Error -> ErrorContent(
                    message = uiState.message,
                    onRetry = onRetry
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
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

@Composable
private fun PokemonDetailBody(
    pokemon: PokemonDetail,
    scrollPosition: Int,
    scrollOffset: Int,
    onScrollPositionChanged: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val primaryType = pokemon.types.firstOrNull()?.name ?: "normal"
    val typeColor = PokemonTypeColors.getBackground(primaryType, isDark)
    
    // Restore scroll position from UiState
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = scrollPosition,
        initialFirstVisibleItemScrollOffset = scrollOffset
    )

    // Save scroll position as user scrolls (same pattern as PokemonListScreen)
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
            TypeBadgesSection(
                types = pokemon.types,
                isDark = isDark,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        // Physical Info
        item {
            PhysicalInfoSection(
                height = pokemon.height,
                weight = pokemon.weight,
                baseExperience = pokemon.baseExperience,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        // Abilities
        item {
            AbilitiesSection(
                abilities = pokemon.abilities,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        // Base Stats
        item {
            BaseStatsSection(
                stats = pokemon.stats,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeroSection(
    name: String,
    id: Int,
    imageUrl: String,
    typeColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        typeColor.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier.size(160.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "#${id.toString().padStart(3, '0')}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TypeBadgesSection(
    types: ImmutableList<TypeOfPokemon>,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        types.forEach { type ->
            TypeBadge(type = type.name, isDark = isDark)
        }
    }
}

@Composable
private fun TypeBadge(
    type: String,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = PokemonTypeColors.getBackground(type, isDark)
    val contentColor = PokemonTypeColors.getContent(type, isDark)
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        modifier = modifier
    ) {
        Text(
            text = type.replaceFirstChar { it.titlecase() },
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun PhysicalInfoSection(
    height: Int,
    weight: Int,
    baseExperience: Int,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        InfoCard(
            emoji = "📏",
            label = "Height",
            value = "${height / 10.0} m",
            modifier = Modifier.weight(1f)
        )
        InfoCard(
            emoji = "⚖️",
            label = "Weight",
            value = "${weight / 10.0} kg",
            modifier = Modifier.weight(1f)
        )
        InfoCard(
            emoji = "⭐",
            label = "Base XP",
            value = baseExperience.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InfoCard(
    emoji: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.displaySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AbilitiesSection(
    abilities: ImmutableList<Ability>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Abilities",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            abilities.forEach { ability ->
                AbilityRow(ability = ability)
                if (ability != abilities.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun AbilityRow(
    ability: Ability,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = ability.name.replace("-", " ").replaceFirstChar { it.titlecase() },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (ability.isHidden) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.secondary
            ) {
                Text(
                    text = "Hidden",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun BaseStatsSection(
    stats: ImmutableList<Stat>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Base Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            stats.forEachIndexed { index, stat ->
                StatBar(
                    stat = stat,
                    animationDelay = index * 100
                )
                if (stat != stats.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun StatBar(
    stat: Stat,
    animationDelay: Int,
    modifier: Modifier = Modifier
) {
    val maxStat = 255f
    val progress = (stat.baseStat / maxStat).coerceIn(0f, 1f)
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 1000,
            delayMillis = animationDelay
        ),
        label = "stat_animation"
    )
    
    val statColor = when {
        stat.baseStat < 50 -> Color(0xFFF44336) // Red
        stat.baseStat < 100 -> Color(0xFFFFEB3B) // Yellow
        else -> Color(0xFF4CAF50) // Green
    }
    
    val statName = stat.name.replace("-", " ")
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { char -> char.titlecase() } }
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = statName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(100.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = statColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            Text(
                text = stat.baseStat.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

// Previews

@Preview
@Composable
private fun PokemonDetailScreenLoadingPreview() {
    PokemonTheme {
        Surface {
            PokemonDetailContent(
                uiState = PokemonDetailUiState.Loading,
                onBackClick = {},
                onRetry = {},
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}

@Preview
@Composable
private fun PokemonDetailScreenContentPreview() {
    PokemonTheme {
        Surface {
            PokemonDetailContent(
                uiState = PokemonDetailUiState.Content(
                    pokemon = PokemonDetail(
                        id = 25,
                        name = "Pikachu",
                        height = 4,
                        weight = 60,
                        baseExperience = 112,
                        types = persistentListOf(
                            TypeOfPokemon(name = "electric", slot = 1)
                        ),
                        stats = persistentListOf(
                            Stat(name = "hp", baseStat = 35, effort = 0),
                            Stat(name = "attack", baseStat = 55, effort = 0),
                            Stat(name = "defense", baseStat = 40, effort = 0),
                            Stat(name = "special-attack", baseStat = 50, effort = 0),
                            Stat(name = "special-defense", baseStat = 50, effort = 0),
                            Stat(name = "speed", baseStat = 90, effort = 0)
                        ),
                        abilities = persistentListOf(
                            Ability(name = "static", isHidden = false, slot = 1),
                            Ability(name = "lightning-rod", isHidden = true, slot = 3)
                        ),
                        imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"
                    )
                ),
                onBackClick = {},
                onRetry = {},
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}

@Preview
@Composable
private fun PokemonDetailScreenErrorPreview() {
    PokemonTheme {
        Surface {
            PokemonDetailContent(
                uiState = PokemonDetailUiState.Error("Network error. Please check your connection."),
                onBackClick = {},
                onRetry = {},
                onScrollPositionChanged = { _, _ -> }
            )
        }
    }
}


