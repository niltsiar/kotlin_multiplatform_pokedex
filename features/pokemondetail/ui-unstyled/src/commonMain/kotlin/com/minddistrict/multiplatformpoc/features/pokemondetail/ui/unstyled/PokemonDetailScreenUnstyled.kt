package com.minddistrict.multiplatformpoc.features.pokemondetail.ui.unstyled

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.composeunstyled.ProgressBar
import com.composeunstyled.ProgressIndicator
import com.composeunstyled.Text
import com.composeunstyled.platformtheme.bright
import com.composeunstyled.platformtheme.indications
import com.composeunstyled.platformtheme.roundedMedium
import com.composeunstyled.platformtheme.shapes
import com.composeunstyled.platformtheme.text1
import com.composeunstyled.platformtheme.text2
import com.composeunstyled.platformtheme.text3
import com.composeunstyled.platformtheme.text5
import com.composeunstyled.platformtheme.text6
import com.composeunstyled.platformtheme.heading2
import com.composeunstyled.platformtheme.heading3
import com.composeunstyled.platformtheme.heading4
import com.composeunstyled.platformtheme.textStyles
import com.composeunstyled.theme.Theme
import com.minddistrict.multiplatformpoc.core.designsystem.core.Elevation
import com.minddistrict.multiplatformpoc.core.designsystem.core.PokemonTypeColors
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.UnstyledTheme
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.background
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.buildPlatformTheme
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.colors
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.error
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.onSurface
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.surface
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.spacing
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.spacingLg
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.spacingMd
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.spacingSm
import com.minddistrict.multiplatformpoc.core.designsystem.unstyled.theme.spacingXs
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.Ability
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.PokemonDetail
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.Stat
import com.minddistrict.multiplatformpoc.features.pokemondetail.domain.TypeOfPokemon
import com.minddistrict.multiplatformpoc.features.pokemondetail.presentation.PokemonDetailUiState
import com.minddistrict.multiplatformpoc.features.pokemondetail.presentation.PokemonDetailViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun PokemonDetailScreenUnstyled(
    viewModel: PokemonDetailViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    PokemonDetailContentUnstyled(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetry = viewModel::retry,
        modifier = modifier
    )
}

@Composable
private fun PokemonDetailContentUnstyled(
    uiState: PokemonDetailUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = Theme.currentTheme
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.colors.background)
    ) {
        // Top app bar (back button + title)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.colors.surface)
                .padding(theme.spacing.spacingMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(theme.spacing.spacingSm)
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(theme.shapes.roundedMedium)
                    .background(theme.colors.surface)
                    .border(
                        width = 1.dp,
                        color = theme.colors.onSurface.copy(alpha = 0.2f),
                        shape = theme.shapes.roundedMedium
                    )
                    .clickable(
                        onClick = onBackClick,
                        indication = theme.indications.bright,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "←",
                    style = theme.textStyles.text1,
                    color = theme.colors.onSurface
                )
            }
            
            // Title (only show when content loaded)
            if (uiState is PokemonDetailUiState.Content) {
                Text(
                    text = uiState.pokemon.name,
                    style = theme.textStyles.heading3,
                    color = theme.colors.onSurface
                )
            }
        }
        
        // Main content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (uiState) {
                is PokemonDetailUiState.Loading -> LoadingContentUnstyled()
                is PokemonDetailUiState.Content -> PokemonDetailBodyUnstyled(pokemon = uiState.pokemon)
                is PokemonDetailUiState.Error -> ErrorContentUnstyled(
                    message = uiState.message,
                    onRetry = onRetry
                )
            }
        }
    }
}

@Composable
private fun LoadingContentUnstyled(modifier: Modifier = Modifier) {
    val theme = Theme.currentTheme
    var angle by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        ) { value, _ ->
            angle = value
        }
    }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ProgressIndicator(
            size = 48.dp,
            color = theme.colors.onSurface
        )
    }
}

@Composable
private fun ErrorContentUnstyled(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = Theme.currentTheme
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(theme.spacing.spacingMd),
            modifier = Modifier.padding(theme.spacing.spacingLg)
        ) {
            Text(
                text = "⚠️",
                style = theme.textStyles.heading2,
                color = theme.colors.error
            )
            Text(
                text = message,
                style = theme.textStyles.text2,
                textAlign = TextAlign.Center,
                color = theme.colors.onSurface.copy(alpha = 0.7f)
            )
            Box(
                modifier = Modifier
                    .clip(theme.shapes.roundedMedium)
                    .background(theme.colors.error)
                    .clickable(
                        onClick = onRetry,
                        indication = theme.indications.bright,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    )
                    .padding(horizontal = theme.spacing.spacingMd, vertical = theme.spacing.spacingSm)
            ) {
                Text(
                    text = "Retry",
                    style = theme.textStyles.text3,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun PokemonDetailBodyUnstyled(
    pokemon: PokemonDetail,
    modifier: Modifier = Modifier
) {
    val theme = Theme.currentTheme
    val primaryType = pokemon.types.firstOrNull()?.name ?: "normal"
    val isDark = theme.colors.background.luminance() < 0.5f
    val typeColor = PokemonTypeColors.getBackground(primaryType, isDark)
    
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        // Hero Section (Image + Name + ID)
        item {
            HeroSectionUnstyled(
                name = pokemon.name,
                id = pokemon.id,
                imageUrl = pokemon.imageUrl,
                typeColor = typeColor
            )
        }
        
        // Type Badges
        item {
            TypeBadgesSectionUnstyled(
                types = pokemon.types,
                isDark = isDark,
                modifier = Modifier.padding(horizontal = theme.spacing.spacingMd, vertical = theme.spacing.spacingSm)
            )
        }
        
        // Physical Info (Height, Weight, Base XP)
        item {
            PhysicalInfoSectionUnstyled(
                height = pokemon.height,
                weight = pokemon.weight,
                baseExperience = pokemon.baseExperience,
                modifier = Modifier.padding(horizontal = theme.spacing.spacingMd, vertical = theme.spacing.spacingSm)
            )
        }
        
        // Abilities
        item {
            AbilitiesSectionUnstyled(
                abilities = pokemon.abilities,
                modifier = Modifier.padding(horizontal = theme.spacing.spacingMd, vertical = theme.spacing.spacingSm)
            )
        }
        
        // Base Stats
        item {
            BaseStatsSectionUnstyled(
                stats = pokemon.stats,
                modifier = Modifier.padding(horizontal = theme.spacing.spacingMd, vertical = theme.spacing.spacingSm)
            )
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(theme.spacing.spacingLg))
        }
    }
}

@Composable
private fun HeroSectionUnstyled(
    name: String,
    id: Int,
    imageUrl: String,
    typeColor: Color,
    modifier: Modifier = Modifier
) {
    val theme = Theme.currentTheme
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        typeColor.copy(alpha = 0.3f),
                        theme.colors.background
                    )
                )
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(theme.spacing.spacingLg)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier.size(160.dp)
            )
            Spacer(modifier = Modifier.height(theme.spacing.spacingMd))
            Text(
                text = name,
                style = theme.textStyles.heading2,
                fontWeight = FontWeight.Bold,
                color = theme.colors.onSurface
            )
            Text(
                text = "#${id.toString().padStart(3, '0')}",
                style = theme.textStyles.text2,
                color = theme.colors.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TypeBadgesSectionUnstyled(
    types: ImmutableList<TypeOfPokemon>,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val theme = Theme.currentTheme
    
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(theme.spacing.spacingSm),
        modifier = modifier
    ) {
        types.forEach { type ->
            TypeBadgeUnstyled(type = type.name, isDark = isDark)
        }
    }
}

@Composable
private fun TypeBadgeUnstyled(
    type: String,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val theme = Theme.currentTheme
    val backgroundColor = PokemonTypeColors.getBackground(type, isDark)
    val contentColor = PokemonTypeColors.getContent(type, isDark)
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = theme.spacing.spacingMd, vertical = 6.dp)
    ) {
        Text(
            text = type.replaceFirstChar { it.titlecase() },
            color = contentColor,
            style = theme.textStyles.text5
        )
    }
}

@Composable
private fun PhysicalInfoSectionUnstyled(
    height: Int,
    weight: Int,
    baseExperience: Int,
    modifier: Modifier = Modifier
) {
    val theme = Theme.currentTheme
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(theme.spacing.spacingSm),
        modifier = modifier.fillMaxWidth()
    ) {
        InfoCardUnstyled(
            emoji = "📏",
            label = "Height",
            value = "${height / 10.0} m",
            modifier = Modifier.weight(1f)
        )
        InfoCardUnstyled(
            emoji = "⚖️",
            label = "Weight",
            value = "${weight / 10.0} kg",
            modifier = Modifier.weight(1f)
        )
        InfoCardUnstyled(
            emoji = "⭐",
            label = "Base XP",
            value = baseExperience.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InfoCardUnstyled(
    emoji: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val theme = Theme.currentTheme
    
    Box(
        modifier = modifier
            .shadow(
                elevation = Elevation.low,
                shape = theme.shapes.roundedMedium,
                clip = false
            )
            .clip(theme.shapes.roundedMedium)
            .background(theme.colors.surface)
            .border(
                width = 1.dp,
                color = theme.colors.onSurface.copy(alpha = 0.1f),
                shape = theme.shapes.roundedMedium
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(theme.spacing.spacingMd)
        ) {
            Text(
                text = emoji,
                style = theme.textStyles.heading3
            )
            Spacer(modifier = Modifier.height(theme.spacing.spacingXs))
            Text(
                text = label,
                style = theme.textStyles.text6,
                color = theme.colors.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = theme.textStyles.text2,
                fontWeight = FontWeight.Bold,
                color = theme.colors.onSurface
            )
        }
    }
}

@Composable
private fun AbilitiesSectionUnstyled(
    abilities: ImmutableList<Ability>,
    modifier: Modifier = Modifier
) {
    val theme = Theme.currentTheme
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = Elevation.low,
                shape = theme.shapes.roundedMedium,
                clip = false
            )
            .clip(theme.shapes.roundedMedium)
            .background(theme.colors.surface)
            .border(
                width = 1.dp,
                color = theme.colors.onSurface.copy(alpha = 0.1f),
                shape = theme.shapes.roundedMedium
            )
    ) {
        Column(
            modifier = Modifier.padding(theme.spacing.spacingMd)
        ) {
            Text(
                text = "Abilities",
                style = theme.textStyles.heading4,
                fontWeight = FontWeight.Bold,
                color = theme.colors.onSurface
            )
            Spacer(modifier = Modifier.height(theme.spacing.spacingSm))
            abilities.forEach { ability ->
                AbilityRowUnstyled(ability = ability)
                if (ability != abilities.last()) {
                    Spacer(modifier = Modifier.height(theme.spacing.spacingSm))
                }
            }
        }
    }
}

@Composable
private fun AbilityRowUnstyled(
    ability: Ability,
    modifier: Modifier = Modifier
) {
    val theme = Theme.currentTheme
    
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = ability.name.replace("-", " ").replaceFirstChar { it.titlecase() },
            style = theme.textStyles.text2,
            color = theme.colors.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (ability.isHidden) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(theme.colors.onSurface.copy(alpha = 0.2f))
                    .padding(horizontal = theme.spacing.spacingSm, vertical = 4.dp)
            ) {
                Text(
                    text = "Hidden",
                    style = theme.textStyles.text6,
                    color = theme.colors.onSurface
                )
            }
        }
    }
}

@Composable
private fun BaseStatsSectionUnstyled(
    stats: ImmutableList<Stat>,
    modifier: Modifier = Modifier
) {
    val theme = Theme.currentTheme
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = Elevation.low,
                shape = theme.shapes.roundedMedium,
                clip = false
            )
            .clip(theme.shapes.roundedMedium)
            .background(theme.colors.surface)
            .border(
                width = 1.dp,
                color = theme.colors.onSurface.copy(alpha = 0.1f),
                shape = theme.shapes.roundedMedium
            )
    ) {
        Column(
            modifier = Modifier.padding(theme.spacing.spacingMd)
        ) {
            Text(
                text = "Base Stats",
                style = theme.textStyles.heading4,
                fontWeight = FontWeight.Bold,
                color = theme.colors.onSurface
            )
            Spacer(modifier = Modifier.height(theme.spacing.spacingMd))
            stats.forEachIndexed { index, stat ->
                StatBarUnstyled(
                    stat = stat,
                    animationDelay = index * 100
                )
                if (stat != stats.last()) {
                    Spacer(modifier = Modifier.height(theme.spacing.spacingSm))
                }
            }
        }
    }
}

@Composable
private fun StatBarUnstyled(
    stat: Stat,
    animationDelay: Int,
    modifier: Modifier = Modifier
) {
    val theme = Theme.currentTheme
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
                style = theme.textStyles.text3,
                color = theme.colors.onSurface,
                modifier = Modifier.width(100.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = theme.spacing.spacingSm)
            ) {
                ProgressBar(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = statColor,
                    trackColor = theme.colors.onSurface.copy(alpha = 0.1f)
                )
            }
            Text(
                text = stat.baseStat.toString(),
                style = theme.textStyles.text3,
                fontWeight = FontWeight.Bold,
                color = theme.colors.onSurface,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

// Color extension for luminance calculation
private fun Color.luminance(): Float {
    return (0.299f * red + 0.587f * green + 0.114f * blue)
}

// Previews

@Preview
@Composable
private fun PokemonDetailScreenUnstyledLoadingPreview() {
    UnstyledTheme(platformTheme = buildPlatformTheme()) {
        Box(modifier = Modifier.fillMaxSize().background(Theme.currentTheme.colors.background)) {
            PokemonDetailContentUnstyled(
                uiState = PokemonDetailUiState.Loading,
                onBackClick = {},
                onRetry = {}
            )
        }
    }
}

@Preview
@Composable
private fun PokemonDetailScreenUnstyledContentPreview() {
    UnstyledTheme(platformTheme = buildPlatformTheme()) {
        Box(modifier = Modifier.fillMaxSize().background(Theme.currentTheme.colors.background)) {
            PokemonDetailContentUnstyled(
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
                onRetry = {}
            )
        }
    }
}

@Preview
@Composable
private fun PokemonDetailScreenUnstyledErrorPreview() {
    UnstyledTheme(platformTheme = buildPlatformTheme()) {
        Box(modifier = Modifier.fillMaxSize().background(Theme.currentTheme.colors.background)) {
            PokemonDetailContentUnstyled(
                uiState = PokemonDetailUiState.Error("Network error. Please check your connection."),
                onBackClick = {},
                onRetry = {}
            )
        }
    }
}
