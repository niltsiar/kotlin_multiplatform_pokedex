# Component Token Customization Example

Last Updated: December 30, 2025

## Architecture

MaterialComponentTokens now follows the same CompositionLocal pattern as MaterialDesignTokens, enabling full theme customization.

### Pattern Structure

```
MaterialTheme.componentTokens       ← Extension property (@Composable @ReadOnlyComposable)
    ↓
LocalMaterialComponentTokens        ← CompositionLocal
    ↓
DefaultMaterialComponentTokens      ← Default implementation (internal class)
    ↓
MaterialComponentTokens interface   ← Customizable contract
```

## Default Usage (No Customization)

```kotlin
@Composable
fun MyScreen() {
    PokemonTheme {
        // Uses DefaultMaterialComponentTokens automatically
        Card(
            tokens = MaterialTheme.componentTokens.card(),
            content = { Text("Hello") }
        )
    }
}
```

## Custom Component Tokens

```kotlin
// 1. Create custom implementation
class CustomMaterialComponentTokens : MaterialComponentTokens {
    override val card: @Composable () -> CardTokens = {
        object : CardTokens {
            override val shape = RoundedCornerShape(16.dp)  // Less expressive than default 28dp
            override val elevation = 1.dp                    // Flatter than default 2.dp
            override val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
            override val contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            override val pressedScale = 0.95f               // More dramatic scale
        }
    }
    
    override val badge: @Composable () -> BadgeTokens = {
        object : BadgeTokens {
            override val shape = RoundedCornerShape(8.dp)  // Square-ish instead of pill
            override val borderWidth = 2.dp                // Add border (default has none)
            override val fillAlpha = 0.8f                  // Semi-transparent
            override val textColor = Color.Black
        }
    }
    
    override val progressBar: @Composable () -> ProgressBarTokens = {
        object : ProgressBarTokens {
            override val height = 4.dp                     // Thinner than default 8dp
            override val shape = RectangleShape            // Sharp corners
            override val backgroundColor = Color.LightGray
            override val foregroundColor = Color.Blue
            override val animationSpec = tween(
                durationMillis = 150,                      // Faster than default 400ms
                easing = LinearEasing                      // Different curve
            )
        }
    }
}

// 2. Provide custom tokens
@Composable
fun MyCustomTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(/* ... */)
    val typography = Typography(/* ... */)
    
    CompositionLocalProvider(
        LocalMaterialTokens provides DefaultMaterialTokens(),
        LocalMaterialComponentTokens provides CustomMaterialComponentTokens()  // 🎯 Custom tokens
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

// 3. Use custom theme
@Composable
fun App() {
    MyCustomTheme {
        // All cards, badges, and progress bars now use custom tokens
        PokemonListScreen()
    }
}
```

## Benefits

✅ **Customizable**: Apps can override component appearance without forking component code  
✅ **Theme-scoped**: Different parts of the app can use different token implementations  
✅ **Type-safe**: Interface contract ensures all required tokens are provided  
✅ **Consistent pattern**: Follows same CompositionLocal architecture as MaterialTheme.tokens  
✅ **Future-proof**: Easy to add new component types to the interface

## Comparison: Before vs After

### Before (Object - Not Customizable)
```kotlin
object MaterialComponentTokens {
    val card: @Composable () -> CardTokens = { /* hardcoded */ }
}

// ❌ Can't override without forking the object
```

### After (CompositionLocal - Fully Customizable)
```kotlin
interface MaterialComponentTokens {
    val card: @Composable () -> CardTokens
}

val LocalMaterialComponentTokens = staticCompositionLocalOf<MaterialComponentTokens> { /* ... */ }

// ✅ Can provide custom implementation via CompositionLocal
CompositionLocalProvider(
    LocalMaterialComponentTokens provides MyCustomTokens()
) { /* ... */ }
```

## Real-World Use Cases

1. **White-label apps**: Different brands need different component styling
2. **A/B testing**: Test card elevation variants without code changes
3. **Accessibility modes**: Provide high-contrast component tokens
4. **Platform differences**: iOS-specific vs Android-specific component tokens
5. **Feature flags**: Gradually roll out new component designs

