# Compose Unstyled Reference Guide

**Last Updated:** December 20, 2025  
**Version:** 1.49.3

> Complete reference for Compose Unstyled implementation patterns in the Pokédex showcase app.

## Overview

Compose Unstyled is a renderless component library for Jetpack Compose and Compose Multiplatform. Components handle UX logic, state management, accessibility (ARIA standards), and keyboard interactions but render **no visual UI** by default.

**Philosophy**: "Renderless components give you complete control over styling while providing battle-tested interaction patterns."

## Installation

```kotlin
// gradle/libs.versions.toml
[versions]
compose-unstyled = "1.49.3"

[libraries]
composeunstyled-primitives = { module = "com.composables:composeunstyled-primitives", version.ref = "compose-unstyled" }
composeunstyled-platformtheme = { module = "com.composables:composeunstyled-platformtheme", version.ref = "compose-unstyled" }
composeunstyled-theming = { module = "com.composables:composeunstyled-theming", version.ref = "compose-unstyled" }
```

---

## Component Catalog

### Core Components

| Component | Purpose | Key Parameters |
|-----------|---------|---------------|
| **Text** | Renderless text with theme support | `text`, `fontSize`, `fontWeight`, `color`, `textAlign`, `lineHeight`, `maxLines`, `overflow` |
| **Button** | Accessible clickable component | `onClick`, `backgroundColor`, `contentColor`, `contentPadding`, `shape`, `borderColor`, `borderWidth`, `indication`, `enabled` |
| **Icon** | Renderless icon with tint support | `painter`/`imageVector`/`imageBitmap`, `tint`, `contentDescription`, `modifier` |
| **TextField** | Input field with styling | `value`, `onValueChange`, `contentColor`, `backgroundColor`, `borderColor`, `borderWidth`, `shape`, `textStyle`, `fontSize`, `singleLine`, `minLines`, `maxLines` |
| **ProgressIndicator** | Progress bar/spinner | `progress` (0-1f or null for indeterminate), `shape`, `backgroundColor`, `contentColor`, `content` |
| **ProgressBar** | Pre-styled progress bar | `shape`, `color` |

### Layout Components

| Component | Purpose | Key Parameters |
|-----------|---------|---------------|
| **Stack** | Vertical/Horizontal layout | `orientation` (Vertical/Horizontal), `spacing`, `alignment` |
| **Separators** | Dividers | `HorizontalSeparator`, `VerticalSeparator` |
| **Scroll Area** | Scrollable container | `modifier`, `content` |

### Overlay Components

| Component | Purpose | Key Parameters |
|-----------|---------|---------------|
| **Dialog** | Modal dialog | `Dialog.Panel` (backgroundColor, shape, contentPadding), `Dialog.Scrim` (scrimColor) |
| **Modal** | Full-screen modal | Similar to Dialog |
| **Bottom Sheet** | Bottom drawer | SheetDetent system (Hidden, Peek, FullyExpanded), DragIndication |
| **Tooltip** | Hover/focus tooltip | `content`, `modifier` |
| **Dropdown Menu** | Context menu | `items`, `onItemClick` |

### Form Components

| Component | Purpose | Key Parameters |
|-----------|---------|---------------|
| **Checkbox** | Binary/tristate checkbox | `checked`, `onCheckedChange`, `enabled` |
| **Radio Group** | Single-choice group | `options`, `selectedOption`, `onOptionSelected` |
| **Slider** | Range input | `value`, `track {}`, `thumb {}`, `valueLabel {}` |
| **Disclosure** | Collapsible section | `expanded`, `onExpandedChange` |
| **Tab Group** | Tabbed navigation | `tabs`, `selectedTab`, `onTabSelected` |

---

## Theming System

### buildTheme DSL

**Pattern**: Define themes using `buildTheme {}` DSL with `ThemeProperty` and `ThemeToken`.

```kotlin
import com.composeunstyled.theme.*

// 1. Define Properties and Tokens
val colors = ThemeProperty<Color>("colors")
val primary = ThemeToken<Color>("primary")
val onPrimary = ThemeToken<Color>("onPrimary")
val background = ThemeToken<Color>("background")

val typography = ThemeProperty<TextStyle>("typography")
val titleLarge = ThemeToken<TextStyle>("titleLarge")
val bodyMedium = ThemeToken<TextStyle>("bodyMedium")

val shapes = ThemeProperty<Shape>("shapes")
val cardShape = ThemeToken<Shape>("cardShape")
val buttonShape = ThemeToken<Shape>("buttonShape")

// 2. Build Theme
val MyTheme = buildTheme {
    name = "MyTheme"
    
    // Set defaults
    defaultContentColor = Color.Black
    defaultTextStyle = TextStyle(fontSize = 14.sp, fontFamily = FontFamily.Default)
    defaultIndication = rememberColoredIndication(Color.Black, pressedAlpha = 0.1f)
    
    // Define properties
    properties[colors] = mapOf(
        primary to Color(0xFF6200EE),
        onPrimary to Color.White,
        background to Color.White
    )
    
    properties[typography] = mapOf(
        titleLarge to TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
        bodyMedium to TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal)
    )
    
    properties[shapes] = mapOf(
        cardShape to RoundedCornerShape(16.dp),
        buttonShape to RoundedCornerShape(8.dp)
    )
}

// 3. Use Theme
@Composable
fun App() {
    MyTheme {
        // Access theme tokens
        val primaryColor = Theme[colors][primary]
        val titleStyle = Theme[typography][titleLarge]
        
        Button(
            onClick = {},
            backgroundColor = Theme[colors][primary],
            contentColor = Theme[colors][onPrimary],
            shape = Theme[shapes][buttonShape]
        ) {
            Text("Hello", style = Theme[typography][bodyMedium])
        }
    }
}
```

### Platform Theme (buildPlatformTheme)

**Purpose**: Native look/feel with platform-specific fonts, sizes, indications.

```kotlin
import com.composeunstyled.platformtheme.*

val PlatformTheme = buildPlatformTheme(
    webFontOptions = WebFontOptions(
        supportedLanguages = listOf(
            SpokenLanguage.Korean,
            SpokenLanguage.Japanese,
            SpokenLanguage.ChineseSimplified
        ),
        emojiVariant = EmojiVariant.Colored
    )
) {
    // Customize platform theme
    properties[customProperty] = mapOf(customToken to customValue)
}
```

**Pre-defined Platform Tokens**:

| Token Type | Tokens | Description |
|------------|--------|-------------|
| **Typography** | `text1` - `text9`, `heading1` - `heading9` | Platform-specific font sizes |
| **Shapes** | `roundedNone`, `roundedSmall`, `roundedMedium`, `roundedLarge`, `roundedFull` | Platform-optimized corner radii |
| **Indications** | `bright`, `dimmed` | Platform-specific touch feedback (iOS: 0.25 alpha, Web: 0.08 alpha) |
| **Interactive Sizes** | `sizeDefault`, `sizeMinimum` | Touch target sizes (Android: 48dp, iOS: 44dp) |

---

## Styling Patterns

### 1. Explicit Parameter Styling

**Use when**: Order of styling matters (e.g., background before padding).

```kotlin
Button(
    onClick = {},
    backgroundColor = Color(0xFF6200EE),      // Background
    contentColor = Color.White,               // Text/Icon color
    contentPadding = PaddingValues(16.dp),   // Inner padding
    shape = RoundedCornerShape(8.dp),        // Clipping shape
    borderColor = Color.Gray,                // Border
    borderWidth = 1.dp
) {
    Icon(Lucide.Check, contentDescription = null)
    Spacer(Modifier.width(8.dp))
    Text("Submit")
}
```

### 2. Modifier Styling

**Use when**: Order doesn't matter or you need advanced modifiers.

```kotlin
Text(
    text = "Pokemon",
    modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth()
        .background(Color.White, RoundedCornerShape(8.dp)),
    fontSize = 18.sp,
    fontWeight = FontWeight.Bold,
    color = Color.Black
)
```

### 3. Theme Token Styling

**Use when**: Maintaining consistency across the app.

```kotlin
Button(
    onClick = {},
    backgroundColor = Theme[colors][primary],
    contentColor = Theme[colors][onPrimary],
    contentPadding = Theme[spacing][medium],
    shape = Theme[shapes][buttonShape],
    indication = Theme[indications][bright]
) {
    Text("Click Me", style = Theme[typography][labelLarge])
}
```

### 4. Composition Locals (Cascading Styles)

**Use when**: Providing default colors/styles to child components.

```kotlin
import com.composeunstyled.LocalContentColor
import com.composeunstyled.LocalTextStyle
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.ProvideTextStyle

@Composable
fun Card(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
    ) {
        ProvideContentColor(Color.Black) {
            ProvideTextStyle(TextStyle(fontSize = 14.sp)) {
                content()  // Text and Icon inside will inherit color/style
            }
        }
    }
}
```

---

## Indication System

**Purpose**: Visual feedback for user interactions (hover, press, focus, drag).

### ColoredIndication

```kotlin
import com.composeunstyled.theme.rememberColoredIndication

val indication = rememberColoredIndication(
    color = Color.Black,
    hoveredColor = Color.Gray,     // Optional override
    pressedColor = Color.DarkGray, // Optional override
    focusedColor = Color.Blue,     // Optional override
    draggedColor = Color.Red,      // Optional override
    
    // Alpha values (0-1f)
    hoveredAlpha = 0.06f,
    pressedAlpha = 0.1f,
    focusedAlpha = 0.12f,
    draggedAlpha = 0.16f,
    
    // Animation specs
    showAnimationSpec = tween(100),
    hideAnimationSpec = tween(100)
)

Button(
    onClick = {},
    indication = indication
) { Text("Interactive") }
```

### Platform-Specific Indications

| Platform | Hover | Press | Focus | Notes |
|----------|-------|-------|-------|-------|
| **Android** | N/A | Ripple | N/A | Uses Material ripple |
| **iOS** | N/A | 0.25 alpha | N/A | Snap animations (instant) |
| **Web** | 0.06 alpha | 0.08 alpha | 0.12 alpha | Smooth transitions |
| **JVM** | 0.06 alpha | 0.25 alpha | 0.12 alpha | Desktop behavior |

---

## Progress Indicator Patterns

### Animated Horizontal Progress Bar (Recommended)

```kotlin
var progress by remember { mutableStateOf(0f) }
LaunchedEffect(Unit) {
    while (true) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        ) { value, _ ->
            progress = value
        }
    }
}

ProgressIndicator(
    progress = progress,
    modifier = Modifier
        .fillMaxWidth(0.8f)  // 80% width
        .height(8.dp),
    shape = RoundedCornerShape(8.dp),
    backgroundColor = Theme[colors][surface],
    contentColor = Theme[colors][onSurface],
) {
    Box(
        Modifier
            .fillMaxWidth(progress)
            .fillMaxSize()
            .background(contentColor, shape)
    )
}
```

### Linear Progress Bar (Static)

```kotlin
ProgressIndicator(
    progress = 0.5f,  // 0-1f for determinate
    modifier = Modifier
        .width(400.dp)
        .height(24.dp)
        .shadow(4.dp, RoundedCornerShape(100)),
    shape = RoundedCornerShape(100),
    backgroundColor = Color(0xFF176153),
    contentColor = Color(0xFFB6EABB)
) {
    // Custom fill rendering
    Box(
        Modifier
            .fillMaxWidth(0.5f)
            .fillMaxHeight()
            .background(contentColor, shape)
    )
}
```

### Pre-styled ProgressBar

```kotlin
ProgressBar(
    shape = RoundedCornerShape(8.dp),
    color = Color(0xFF6200EE)
)
```

---

## TextField Styling

```kotlin
TextField(
    value = state.value,
    onValueChange = { state.value = it },
    placeholder = { Text("Enter name...") },
    
    // Colors
    contentColor = Color.Black,
    disabledColor = Color.Gray,
    backgroundColor = Color.White,
    borderColor = Color.Gray,
    borderWidth = 1.dp,
    
    // Typography
    textStyle = Theme[typography][bodyMedium],
    fontSize = 16.sp,
    fontWeight = FontWeight.Normal,
    fontFamily = FontFamily.SansSerif,
    
    // Behavior
    singleLine = true,
    minLines = 1,
    maxLines = 1,
    keyboardOptions = KeyboardOptions.Default,
    
    // Shape and padding
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier.padding(16.dp)
)
```

---

## Icon Usage

### Vector Icons (from compose-icons)

```kotlin
import com.composables.icons.lucide.*

Icon(
    imageVector = Lucide.Check,        // Vector icon
    contentDescription = "Success",
    tint = Theme[colors][primary],     // Or LocalContentColor.current
    modifier = Modifier.size(24.dp)
)
```

### Painter Icons

```kotlin
Icon(
    painter = painterResource("ic_pokemon.xml"),
    contentDescription = "Pokemon",
    tint = Color.Unspecified  // No tint (use original colors)
)
```

### Bitmap Icons

```kotlin
Icon(
    imageBitmap = ImageBitmap.imageResource("pokemon.png"),
    contentDescription = "Pokemon sprite"
)
```

---

## Example: Complete Screen Implementation

```kotlin
@Composable
fun PokemonListScreenUnstyled(
    viewModel: PokemonListViewModel,
    onNavigate: (Route) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Theme[colors][background])
    ) {
        // Header
        ProvideContentColor(Theme[colors][onBackground]) {
            ProvideTextStyle(Theme[typography][headlineLarge]) {
                Text(
                    text = "Pokédex",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        when (uiState) {
            is PokemonListUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    var progress by remember { mutableStateOf(0f) }
                    LaunchedEffect(Unit) {
                        while (true) {
                            animate(
                                initialValue = 0f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(2000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart,
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
                        Box(
                            Modifier
                                .fillMaxWidth(progress)
                                .fillMaxSize()
                                .background(contentColor, shape)
                        )
                    }
                }
            }
            
            is PokemonListUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = (uiState as PokemonListUiState.Error).message,
                        color = Theme[colors][error],
                        fontSize = Theme[typography][bodyLarge].fontSize
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Button(
                        onClick = { viewModel.onUiEvent(PokemonListUiEvent.Retry) },
                        backgroundColor = Theme[colors][primary],
                        contentColor = Theme[colors][onPrimary],
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                        shape = Theme[shapes][buttonShape],
                        indication = Theme[indications][bright]
                    ) {
                        Icon(
                            imageVector = Lucide.RotateCw,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Retry", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            
            is PokemonListUiState.Content -> {
                val content = uiState as PokemonListUiState.Content
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(content.pokemons) { pokemon ->
                        PokemonCard(pokemon, onClick = {
                            onNavigate(PokemonDetail(pokemon.id))
                        })
                    }
                    
                    if (content.hasMore) {
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                ProgressBar(
                                    shape = Theme[shapes][roundedSmall],
                                    color = Theme[colors][primary]
                                )
                            }
                        }
                    }
                }
                
                LaunchedEffect(content.pokemons.size) {
                    if (content.hasMore) {
                        viewModel.onUiEvent(PokemonListUiEvent.LoadMore)
                    }
                }
            }
        }
    }
}

@Composable
private fun PokemonCard(pokemon: Pokemon, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        backgroundColor = Theme[colors][surface],
        contentColor = Theme[colors][onSurface],
        contentPadding = PaddingValues(12.dp),
        shape = Theme[shapes][cardShape],
        indication = Theme[indications][bright],
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.name,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = pokemon.name.capitalize(),
                fontSize = Theme[typography][titleMedium].fontSize,
                fontWeight = FontWeight.Bold,
                color = Theme[colors][onSurface]
            )
            
            Text(
                text = "#${pokemon.id.toString().padStart(3, '0')}",
                fontSize = Theme[typography][bodySmall].fontSize,
                color = Theme[colors][onSurfaceVariant]
            )
        }
    }
}
```

---

## Theme Definition Template

```kotlin
import com.composeunstyled.theme.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. Define Properties
val colors = ThemeProperty<Color>("colors")
val typography = ThemeProperty<TextStyle>("typography")
val shapes = ThemeProperty<Shape>("shapes")
val spacing = ThemeProperty<Dp>("spacing")
val indications = ThemeProperty<Indication>("indications")

// 2. Define Tokens
// Colors
val primary = ThemeToken<Color>("primary")
val onPrimary = ThemeToken<Color>("onPrimary")
val background = ThemeToken<Color>("background")
val onBackground = ThemeToken<Color>("onBackground")
val surface = ThemeToken<Color>("surface")
val onSurface = ThemeToken<Color>("onSurface")
val error = ThemeToken<Color>("error")
val onError = ThemeToken<Color>("onError")

// Typography
val headlineLarge = ThemeToken<TextStyle>("headlineLarge")
val titleLarge = ThemeToken<TextStyle>("titleLarge")
val titleMedium = ThemeToken<TextStyle>("titleMedium")
val bodyLarge = ThemeToken<TextStyle>("bodyLarge")
val bodyMedium = ThemeToken<TextStyle>("bodyMedium")
val labelLarge = ThemeToken<TextStyle>("labelLarge")

// Shapes
val cardShape = ThemeToken<Shape>("cardShape")
val buttonShape = ThemeToken<Shape>("buttonShape")

// Spacing
val xs = ThemeToken<Dp>("xs")
val sm = ThemeToken<Dp>("sm")
val md = ThemeToken<Dp>("md")
val lg = ThemeToken<Dp>("lg")
val xl = ThemeToken<Dp>("xl")

// Indications
val bright = ThemeToken<Indication>("bright")
val dimmed = ThemeToken<Indication>("dimmed")

// 3. Build Theme
val UnstyledTheme = buildTheme {
    name = "UnstyledTheme"
    
    defaultContentColor = Color(0xFF1C1B1F)
    defaultTextStyle = TextStyle(fontSize = 14.sp, fontFamily = FontFamily.SansSerif)
    defaultIndication = rememberColoredIndication(
        color = Color.Black,
        pressedAlpha = 0.1f,
        hoveredAlpha = 0.06f
    )
    
    properties[colors] = mapOf(
        primary to Color(0xFF6200EE),
        onPrimary to Color.White,
        background to Color(0xFFFFFBFE),
        onBackground to Color(0xFF1C1B1F),
        surface to Color(0xFFECE6F0),
        onSurface to Color(0xFF1C1B1F),
        error to Color(0xFFB3261E),
        onError to Color.White
    )
    
    properties[typography] = mapOf(
        headlineLarge to TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
        titleLarge to TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
        titleMedium to TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
        bodyLarge to TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
        bodyMedium to TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
        labelLarge to TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
    )
    
    properties[shapes] = mapOf(
        cardShape to RoundedCornerShape(16.dp),
        buttonShape to RoundedCornerShape(8.dp)
    )
    
    properties[spacing] = mapOf(
        xs to 4.dp,
        sm to 8.dp,
        md to 16.dp,
        lg to 24.dp,
        xl to 32.dp
    )
    
    properties[indications] = mapOf(
        bright to rememberColoredIndication(Color.White, pressedAlpha = 0.25f),
        dimmed to rememberColoredIndication(Color.Black, pressedAlpha = 0.1f)
    )
}
```

---

## Migration from Material 3

| Material 3 | Compose Unstyled | Notes |
|------------|------------------|-------|
| `Text()` | `Text()` | Add explicit `fontSize`, `fontWeight`, `color` |
| `Button()` | `Button()` | Add `backgroundColor`, `contentColor`, `contentPadding`, `shape` |
| `Icon()` | `Icon()` | Add `tint` parameter |
| `CircularProgressIndicator()` | Custom or keep Material | No built-in circular variant |
| `MaterialTheme.typography.titleMedium` | `Theme[typography][titleMedium]` | Define in theme |
| `MaterialTheme.colorScheme.primary` | `Theme[colors][primary]` | Define in theme |
| `Modifier.clickable` | `Button { }` | Use Button for accessibility |

---

## Testing Considerations

### Preview with Theme

**IMPORTANT**: All preview functions must be annotated with `@Preview` to be visible in Android Studio.

```kotlin
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PokemonCardPreview() {
    UnstyledTheme {
        PokemonCard(
            pokemon = Pokemon(25, "Pikachu", "https://..."),
            onClick = {}
        )
    }
}
```

### Test Theme Access

```kotlin
@Test
fun themeTokensResolveCorrectly() = runComposeUiTest {
    setContent {
        UnstyledTheme {
            val primaryColor = Theme[colors][primary]
            primaryColor shouldBe Color(0xFF6200EE)
        }
    }
}
```

---

## Common Patterns

### Loading State (Animated)
```kotlin
Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    var progress by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            ) { value, _ ->
                progress = value
            }
        }
    }
    ProgressIndicator(
        progress = progress,
        modifier = Modifier.fillMaxWidth(0.8f).height(8.dp),
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Theme[colors][surface],
        contentColor = Theme[colors][onSurface],
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress)
                .fillMaxSize()
                .background(contentColor, shape)
        )
    }
}
```

### Error State
```kotlin
Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(errorMessage, color = Theme[colors][error])
    Spacer(Modifier.height(16.dp))
    Button(onClick = onRetry, ...) { Text("Retry") }
}
```

### Card Pattern
```kotlin
Button(
    onClick = onClick,
    backgroundColor = Theme[colors][surface],
    contentColor = Theme[colors][onSurface],
    shape = Theme[shapes][cardShape],
    contentPadding = PaddingValues(16.dp)
) {
    Column { /* Card content */ }
}
```

---

## Resources

- **Official Docs**: https://composables.com/docs/compose-unstyled
- **GitHub Repo**: https://github.com/composablehorizons/compose-unstyled
- **Demo Apps**: See repo `demo/` folder for 10+ theme examples
- **Compose Icons**: https://github.com/composablehorizons/compose-icons (17,000+ icons)

---

## Key Differences vs Material 3

1. **NO default styling**: Must provide backgroundColor, contentColor, shape for every component
2. **NO built-in circular progress**: Use custom Canvas or keep Material's CircularProgressIndicator
3. **Explicit typography**: Can't rely on MaterialTheme cascading, must pass fontSize/fontWeight
4. **Button replaces clickable**: Use Button wrapper for accessibility instead of Modifier.clickable
5. **Theme token access**: `Theme[property][token]` instead of MaterialTheme.colorScheme.primary

---

## Best Practices

1. ✅ **Define complete themes** - colors, typography, shapes, spacing, indications
2. ✅ **Use ProvideContentColor/ProvideTextStyle** for cascading defaults
3. ✅ **Leverage Platform Themes** for native look/feel
4. ✅ **Use Button over Modifier.clickable** for interactive elements (accessibility)
5. ✅ **Keep Material's CircularProgressIndicator** for loading states (acceptable compromise)
6. ✅ **Test themes with @Preview** to verify visual consistency
7. ❌ **DON'T mix Material 3 and Compose Unstyled** in same UI hierarchy (creates confusion)
8. ❌ **DON'T forget contentDescription** on Icons (accessibility)
