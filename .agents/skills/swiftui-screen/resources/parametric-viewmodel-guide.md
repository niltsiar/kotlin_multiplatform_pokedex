# Parametric ViewModel Guide

Complete guide for ViewModels requiring constructor parameters (e.g., pokemonId, userId).

## Kotlin Setup

### 1. Create Koin Module with Parameters

```kotlin
val pokemonDetailModule = module {
    factory { params ->
        PokemonDetailViewModel(
            pokemonId = params.get(),
            repository = get()
        )
    }
}
```

### 2. Create iOS Helper Function

```kotlin
// shared/src/iosMain/kotlin/KoinIos.kt
fun getPokemonDetailViewModel(pokemonId: Int): PokemonDetailViewModel {
    return KoinPlatform.getKoin().get { parametersOf(pokemonId) }
}
```

## Swift Implementation

### 1. Initialize ViewModel with Parameters

```swift
import SwiftUI
import Shared

struct PokemonDetailView: View {
    let pokemonId: Int
    private var viewModel: PokemonDetailViewModel

    @State private var uiState: PokemonDetailUiState = PokemonDetailUiStateLoading()

    init(pokemonId: Int) {
        self.pokemonId = pokemonId
        // Cast Swift Int to Kotlin Int32
        viewModel = KoinIosKt.getPokemonDetailViewModel(pokemonId: Int32(pokemonId))
    }

    var body: some View {
        content
            .task {
                for await state in viewModel.uiState {
                    self.uiState = state
                }
            }
    }
}
```

## Type Conversions

| Kotlin | Swift | Conversion |
|--------|-------|------------|
| `Int` | `Int32` | `Int32(swiftInt)` |
| `Long` | `Int64` | `Int64(swiftInt)` |
| `String` | `String` | Direct |
| `Boolean` | `Bool` | Direct |

## Common Patterns

### Navigation with Parameters

```swift
// In parent view
NavigationLink(destination: PokemonDetailView(pokemonId: pokemon.id)) {
    PokemonCard(pokemon: pokemon)
}

// Or programmatically
NavigationLink(value: pokemon.id) { }
.navigationDestination(for: Int.self) { pokemonId in
    PokemonDetailView(pokemonId: pokemonId)
}
```

### Multiple Parameters

```kotlin
// Kotlin
factory { params ->
    UserProfileViewModel(
        userId = params.get(),
        isEditable = params.get(),
        repository = get()
    )
}

fun getUserProfileViewModel(userId: String, isEditable: Boolean): UserProfileViewModel {
    return KoinPlatform.getKoin().get { 
        parametersOf(userId, isEditable) 
    }
}
```

```swift
// Swift
init(userId: String, isEditable: Bool) {
    viewModel = KoinIosKt.getUserProfileViewModel(
        userId: userId,
        isEditable: isEditable
    )
}
```

## Testing Parametric ViewModels

```kotlin
class PokemonDetailViewModelSpec : StringSpec({
    "loads pokemon with correct id" {
        val pokemonId = 25
        coEvery { repository.getPokemon(pokemonId) } returns mockPokemon.right()
        
        val viewModel = PokemonDetailViewModel(pokemonId, repository)
        viewModel.loadPokemon()
        
        viewModel.uiState.value shouldBeInstanceOf<PokemonDetailUiState.Content>()
    }
})
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| `InvalidParameterException` | Check parameter order matches factory definition |
| Type mismatch | Ensure Kotlin `Int` → Swift `Int32`, Kotlin `Long` → Swift `Int64` |
| ViewModel not updating | Verify `.task` is used for StateFlow observation |
