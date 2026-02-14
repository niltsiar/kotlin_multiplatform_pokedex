# Factory Functions Pattern (Impl + Factory)

## Keep Classes DI-Agnostic

**DO NOT annotate production classes with DI framework specifics.**

```kotlin
// api/ProfileRepository.kt
interface ProfileRepository {
    suspend fun getProfile(): Either<RepoError, Profile>
}

// data/ProfileRepositoryImpl.kt
internal class ProfileRepositoryImpl(
    private val api: ProfileApiService
) : ProfileRepository

// data/ProfileRepositoryFactory.kt
fun ProfileRepository(api: ProfileApiService): ProfileRepository =
    ProfileRepositoryImpl(api)

// wiring/ProfileModule.kt
val profileModule = module {
    factory<ProfileRepository> {
        ProfileRepository(api = get())
    }
}
```

## Benefits

- Classes remain pure Kotlin (no DI annotations)
- Implementations are internal/private
- Easy to test without Koin
- Clear factory function signatures

## Complete Feature Module Example

```kotlin
// features/pokemonlist/wiring/src/commonMain/.../PokemonListModule.kt
val pokemonListModule = module {
    // API service factory
    factory<PokemonListApiService> {
        PokemonListApiService(
            client = get(),
            baseUrl = get(named("baseUrl"))
        )
    }
    
    // Repository factory (calls factory function)
    factory<PokemonListRepository> {
        PokemonListRepository(apiService = get())
    }
    
    // ViewModel factory
    factory<PokemonListViewModel> {
        PokemonListViewModel(repository = get())
    }
}
```

## Module Definition Pattern

```kotlin
// Define module as val with explicit type
val featureModule: Module = module {
    factory<Interface> { Implementation(get()) }
}
```
