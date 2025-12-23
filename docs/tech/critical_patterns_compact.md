# Critical Patterns Compact (Token-Lean Cards)

Last Updated: December 20, 2025

Purpose: One-page pattern cards for low-context scenarios. Use this when packing context for agents or Copilot.
Full definitions remain in [critical_patterns_quick_ref.md](critical_patterns_quick_ref.md) and the pattern guides.

## How to Use

- Include this file when token budget is tight; follow the deep links for details.
- Keep references clickable to avoid duplication.

## Pattern Cards

| Pattern | Key Rule | Deep Dive | Example (click) |
| --- | --- | --- | --- |
| Impl + Factory (Koin) | Internal `Impl`, public factory; production classes are DI-agnostic | [critical_patterns_quick_ref.md#implfactory-pattern](critical_patterns_quick_ref.md#implfactory-pattern) • [di_patterns.md](../patterns/di_patterns.md) | [PokemonListRepositoryImpl.kt](../../features/pokemonlist/data/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/data/PokemonListRepositoryImpl.kt) • [PokemonListModule.kt](../../features/pokemonlist/wiring/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/wiring/PokemonListModule.kt) |
| Either Boundary | Repositories return `Either<RepoError, T>`; map errors, never throw | [critical_patterns_quick_ref.md#either-boundary-pattern](critical_patterns_quick_ref.md#either-boundary-pattern) • [error_handling_patterns.md](../patterns/error_handling_patterns.md) | [PokemonListRepositoryImpl.kt](../../features/pokemonlist/data/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/data/PokemonListRepositoryImpl.kt) |
| ViewModel Pattern | `DefaultLifecycleObserver` with `onStart()`, NO work in `init`, lifecycle-aware | [critical_patterns_quick_ref.md#viewmodel-pattern](critical_patterns_quick_ref.md#viewmodel-pattern) • [viewmodel_patterns.md](../patterns/viewmodel_patterns.md) | [PokemonListViewModel.kt](../../features/pokemonlist/presentation/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModel.kt) • [PokemonListViewModelTest.kt](../../features/pokemonlist/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModelTest.kt) |
| Navigation 3 Pattern | Route objects in `:api`; providers in wiring; install entries | [critical_patterns_quick_ref.md#navigation-3-pattern](critical_patterns_quick_ref.md#navigation-3-pattern) • [navigation_patterns.md](../patterns/navigation_patterns.md) | [PokemonListEntry.kt](../../features/pokemonlist/api/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/navigation/PokemonListEntry.kt) • [PokemonListNavigationProviders.kt](../../features/pokemonlist/wiring/src/androidMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/wiring/PokemonListNavigationProviders.kt) |
| Testing Pattern | NO CODE WITHOUT TESTS; property tests + Turbine on JVM | [critical_patterns_quick_ref.md#testing-pattern](critical_patterns_quick_ref.md#testing-pattern) • [testing_quick_ref.md](testing_quick_ref.md) • [testing_patterns.md](../patterns/testing_patterns.md) | [testing_strategy.md#repository-test-androidtest](testing_strategy.md#repository-test-androidtest) • [testing_strategy.md#flow-testing-with-turbine](testing_strategy.md#flow-testing-with-turbine) |

## Checklist (Pack This Instead of Full Docs)

- Use this compact file + [testing_quick_ref.md](testing_quick_ref.md) when tokens are tight
- Jump to pattern guides for rationale and edge cases
- Keep the feature examples above as the canonical “how it’s done here” references

## Related Quick Refs

- [testing_quick_ref.md](testing_quick_ref.md) — enforcement, locations, property/Turbine summary
- [kotest_smart_casting_quick_ref.md](kotest_smart_casting_quick_ref.md) — smart casting helpers to avoid manual casts
