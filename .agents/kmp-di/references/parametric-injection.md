# Parametric Injection with parametersOf

## When to Use

ViewModels or services requiring runtime parameters (IDs, config values).

## ViewModel with Parameters

```kotlin
class PokemonDetailViewModel(
    private val pokemonId: Int,
    private val repository: PokemonDetailRepository,
    viewModelScope: CoroutineScope = ...
) : ViewModel(viewModelScope) { ... }

// Koin module
val pokemonDetailModule = module {
    factory { params ->
        PokemonDetailViewModel(
            pokemonId = params.get(),
            repository = get()
        )
    }
}
```

## Compose Injection

```kotlin
@Composable
fun PokemonDetailScreen(pokemonId: Int) {
    val viewModel: PokemonDetailViewModel = koinInject { parametersOf(pokemonId) }
    // Use viewModel...
}
```

## Parameter Extraction

```kotlin
factory { params ->
    // By type (first match)
    val id: Int = params.get()
    
    // By type with index
    val id: Int = params.get<Int>(0)
    val name: String = params.get<String>(1)
    
    // By name
    val id: Int = params.get<Int>("pokemonId")
}
```

## iOS Integration

```kotlin
// shared/src/iosMain/kotlin/KoinIos.kt
fun getPokemonDetailViewModel(pokemonId: Int): PokemonDetailViewModel {
    return KoinPlatform.getKoin().get { parametersOf(pokemonId) }
}
```

```swift
// Swift wrapper
@MainActor
class PokemonDetailViewModelWrapper: ObservableObject {
    private let viewModel: PokemonDetailViewModel
    
    init(pokemonId: Int) {
        self.viewModel = KoinIosKt.getPokemonDetailViewModel(pokemonId: Int32(pokemonId))
    }
}
```

## Alternative: Factory Function

```kotlin
// ViewModel with explicit factory
fun PokemonDetailViewModel(
    pokemonId: Int,
    repository: PokemonDetailRepository
): PokemonDetailViewModel = PokemonDetailViewModel(pokemonId, repository)

// In Compose
@Composable
fun PokemonDetailScreen(pokemonId: Int) {
    val repository: PokemonDetailRepository = koinInject()
    val viewModel = remember(pokemonId, repository) {
        PokemonDetailViewModel(pokemonId, repository)
    }
}
```

## When to Use parametersOf

| Use Case | Recommendation |
|----------|---------------|
| ViewModels with IDs | ✅ parametersOf |
| Repositories with config | ❌ Use named dependencies |
| Use cases with runtime data | ✅ parametersOf |
| Static configuration | ❌ Use named dependencies |
