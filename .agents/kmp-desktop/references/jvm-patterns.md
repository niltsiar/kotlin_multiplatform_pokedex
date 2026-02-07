# JVM-Specific Patterns Reference

## Desktop/JVM Overview

The Desktop/JVM target in Kotlin Multiplatform shares much of its codebase with Android but requires specific configuration for ViewModel lifecycle and DI integration.

## Platform Differences

### ViewModel CreationExtras

| Aspect | Android | Desktop/JVM |
|--------|---------|-------------|
| CreationExtras population | Automatic via ComponentActivity | Manual via ProvideDesktopLifecycle |
| SavedStateHandle resolution | Automatic | Explicit via parametersOf |
| Lifecycle owner | Activity-based | Custom DesktopLifecycleOwner |

### Lifecycle Setup

Desktop requires explicit lifecycle setup using `ProvideDesktopLifecycle`:

```kotlin
import androidx.compose.ui.window.application
import androidx.lifecycle.compose.DesktopLifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.ProvideDesktopLifecycle

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        ProvideDesktopLifecycle {
            // Your Compose content here
            App()
        }
    }
}
```

## Desktop ViewModel Wiring Pattern

### Common Module (ViewModel Definition)

```kotlin
// :features:<feature>:presentation
class PokemonDetailViewModel(
    private val pokemonId: Int,
    private val repository: PokemonDetailRepository,
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob())
) : ViewModel(viewModelScope), UiStateHolder<...> {
    // Implementation
}
```

### Koin Wiring (commonMain)

```kotlin
// :features:<feature>:wiring
val pokemonDetailModule = module {
    viewModel { (pokemonId: Int, savedStateHandle: SavedStateHandle) ->
        PokemonDetailViewModel(
            pokemonId = pokemonId,
            repository = get(),
            savedStateHandle = savedStateHandle,
        )
    }
}
```

### Desktop-Specific Usage (jvmMain)

```kotlin
// :features:<feature>:wiring-ui/jvmMain
entry<PokemonDetail> { route ->
    val navigator: Navigator = koinInject()
    // Create SavedStateHandle explicitly for Desktop
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

## Common Issues and Solutions

### Issue: CreationExtras Missing SAVED_STATE_REGISTRY_OWNER_KEY

**Solution**: Pass SavedStateHandle explicitly via `parametersOf`

```kotlin
val savedStateHandle = SavedStateHandle()
val viewModel = koinViewModel {
    parametersOf(pokemonId, savedStateHandle)
}
```

### Issue: ViewModel Not Surviving Configuration Changes

**Solution**: Ensure `ProvideDesktopLifecycle` wraps your content

```kotlin
ProvideDesktopLifecycle {
    // DesktopLifecycleOwner is available here
    MyScreen()
}
```

## Related Files

- `composeApp/src/jvmMain/kotlin/main.kt` - Desktop app entry point
- `docs/tech/desktop_viewmodel_savedstate.md` - Detailed SavedStateHandle guide
- `features/<feature>/wiring-ui/jvmMain/` - Desktop-specific navigation
