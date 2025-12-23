# Desktop/JVM ViewModel with SavedStateHandle

## Problem: CreationExtras Missing SAVED_STATE_REGISTRY_OWNER_KEY

When using Koin's `koinViewModel()` function on Desktop/JVM in Compose Multiplatform, you may encounter:

```
CreationExtras must have a value by `SAVED_STATE_REGISTRY_OWNER_KEY`
```

This occurs when your ViewModel requires `SavedStateHandle` but `koinViewModel()` cannot automatically provide it.

## Why This Happens

Koin's `org.koin.compose.viewmodel.koinViewModel()` internally:

1. Reads `LocalViewModelStoreOwner` and `LocalSavedStateRegistryOwner` from composition
2. Creates `CreationExtras` to pass metadata to the `ViewModelProvider`
3. Expects the `SavedStateRegistryOwner` to be available in `CreationExtras`

On **Android**, `ComponentActivity` automatically provides fully-populated `CreationExtras` including the `SavedStateRegistryOwner`.

On **Desktop/JVM**, even though we manually provide the composition locals via `ProvideDesktopLifecycle`, the `CreationExtras` population logic differs, causing the error.

## Solution: Explicitly Pass SavedStateHandle

Instead of relying on Koin to automatically extract `SavedStateHandle` from `CreationExtras`, **explicitly create and pass it** via Koin's `parametersOf`:

### Before (Causes Error)

```kotlin
// ❌ This fails on Desktop - SavedStateHandle cannot be resolved
val viewModel: PokemonDetailViewModel = koinViewModel(
    parameters = { parametersOf(pokemonId) }
)
```

### After (Works Correctly)

```kotlin
import androidx.lifecycle.SavedStateHandle

// ✅ Create SavedStateHandle explicitly and pass it
val savedStateHandle = SavedStateHandle()
val viewModel: PokemonDetailViewModel = koinViewModel(
    parameters = { parametersOf(pokemonId, savedStateHandle) }
)
```

## Full Example

### Koin Module Definition

```kotlin
// :features:pokemondetail:wiring
val pokemonDetailModule = module {
    viewModel { (pokemonId: Int, savedStateHandle: SavedStateHandle) ->
        PokemonDetailViewModel(
            repository = get(),
            pokemonId = pokemonId,
            savedStateHandle = savedStateHandle,
        )
    }
}
```

### Desktop/JVM Usage

```kotlin
// :features:pokemondetail:wiring-ui/jvmMain
entry<PokemonDetail> { route ->
    val navigator: Navigator = koinInject()
    // Create SavedStateHandle explicitly for Desktop/JVM
    val savedStateHandle = SavedStateHandle()
    val viewModel: PokemonDetailViewModel = koinViewModel(
        parameters = { parametersOf(route.id, savedStateHandle) },
    )
    
    PokemonDetailScreen(
        viewModel = viewModel,
        onBackClick = { navigator.goBack() },
    )
}
```

### Android Usage (Automatic)

On Android, you can omit the SavedStateHandle creation because `ComponentActivity` provides proper `CreationExtras`:

```kotlin
// :features:pokemondetail:wiring-ui/androidMain
entry<PokemonDetail> { route ->
    val navigator: Navigator = koinInject()
    // Android automatically provides SavedStateHandle via CreationExtras
    val viewModel: PokemonDetailViewModel = koinViewModel(
        parameters = { parametersOf(route.id) },
    )
    
    PokemonDetailScreen(
        viewModel = viewModel,
        onBackClick = { navigator.goBack() },
    )
}
```

## Why This Works

1. **Koin's `parametersOf` mechanism**: When you pass values via `parametersOf`, Koin injects them directly into your ViewModel factory, bypassing the `CreationExtras` mechanism entirely.

2. **ViewModel definition matches**: Your Koin module expects `(pokemonId, savedStateHandle)`, and you provide exactly that.

3. **No Android-specific APIs**: `SavedStateHandle()` constructor is KMP-compatible and works on all platforms.

## Pattern Comparison: iOS vs Desktop

Both iOS and Desktop use the same pattern of **explicit SavedStateHandle creation**:

### iOS (shared/src/iosMain)

```kotlin
fun getPokemonDetailViewModel(pokemonId: Int, key: String): PokemonDetailViewModel {
    val owner = getViewModelStoreOwner(key)
    val savedStateHandle = getOrCreateSavedStateHandle(key)
    return owner.koinViewModel<PokemonDetailViewModel> { 
        parametersOf(pokemonId, savedStateHandle) 
    }
}
```

### Desktop/JVM (features/.../wiring-ui/jvmMain)

```kotlin
entry<PokemonDetail> { route ->
    val savedStateHandle = SavedStateHandle()
    val viewModel: PokemonDetailViewModel = koinViewModel(
        parameters = { parametersOf(route.id, savedStateHandle) }
    )
}
```

## Alternative: Custom ViewModelProvider.Factory

If you prefer not to pass `SavedStateHandle` explicitly, you can create a custom `ViewModelProvider.Factory` that populates `CreationExtras` correctly:

```kotlin
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.compose.LocalSavedStateRegistryOwner

@Composable
fun <VM : ViewModel> koinViewModelWithExtras(
    parameters: ParametersDefinition? = null,
): VM {
    val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current
    val viewModelStoreOwner = LocalViewModelStoreOwner.current
    
    val factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(
            modelClass: KClass<T>,
            extras: CreationExtras
        ): T {
            // Populate extras with SavedStateRegistryOwner
            val mutableExtras = MutableCreationExtras(extras)
            mutableExtras[SAVED_STATE_REGISTRY_OWNER_KEY] = savedStateRegistryOwner
            
            return if (parameters == null) {
                KoinPlatform.getKoin().get<VM>()
            } else {
                KoinPlatform.getKoin().get<VM>(parameters = parameters)
            }
        }
    }
    
    return ViewModelProvider.create(viewModelStoreOwner, factory)[VM::class]
}
```

However, **this approach is more complex** and still doesn't provide automatic `SavedStateHandle` injection. The explicit `parametersOf` approach is simpler and more aligned with your iOS pattern.

## Key Takeaways

1. **Desktop/JVM requires explicit SavedStateHandle creation** when using Koin's `koinViewModel()`
2. **Pass SavedStateHandle via `parametersOf`** to bypass CreationExtras resolution
3. **Pattern is consistent with iOS** implementation
4. **Android works automatically** due to ComponentActivity's CreationExtras support
5. **SavedStateHandle() constructor is KMP-compatible** and safe to use on all platforms

## Related Files

- [`composeApp/src/jvmMain/kotlin/main.kt`](../../composeApp/src/jvmMain/kotlin/com/minddistrict/multiplatformpoc/main.kt) - ProvideDesktopLifecycle setup
- [`features/pokemondetail/wiring-ui/src/jvmMain/`](../../features/pokemondetail/wiring-ui/src/jvmMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/wiringui/PokemonDetailNavigationProviders.kt) - Desktop ViewModel usage
- [`shared/src/iosMain/kotlin/KoinIos.kt`](../../shared/src/iosMain/kotlin/com/minddistrict/multiplatformpoc/KoinIos.kt) - iOS ViewModel pattern
