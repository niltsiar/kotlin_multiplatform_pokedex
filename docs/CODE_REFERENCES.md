# Code Reference Index

**Last Updated:** December 31, 2025

**Purpose:** Central mapping document linking documentation patterns to actual implementation files. Use these canonical references instead of embedding code examples in documentation.

---

## ViewModel Pattern

### Standard ViewModel (Simple List)
- **Implementation**: [PokemonListViewModel.kt](../features/pokemonlist/presentation/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModel.kt)
- **Tests**: [PokemonListViewModelTest.kt](../features/pokemonlist/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModelTest.kt)
- **Demonstrates**:
  - DefaultLifecycleObserver implementation
  - SavedStateHandle with `by saved` delegate
  - Passing viewModelScope to constructor
  - NO work in init
  - onStart() lifecycle-aware initialization
  - ImmutableList in UI state
  - Pagination with infinite scroll

### Parametric ViewModel (Detail Screen)
- **Implementation**: [PokemonDetailViewModel.kt](../features/pokemondetail/presentation/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModel.kt)
- **Tests**: [PokemonDetailViewModelTest.kt](../features/pokemondetail/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModelTest.kt)
- **Demonstrates**:
  - Parametric ViewModel (accepts ID parameter)
  - Koin parametersOf usage
  - Nested DTO structures
  - Retry mechanism
  - SavedStateHandle delegation

---

## Repository Pattern (Either Boundary)

### Standard Repository
- **API Interface**: [PokemonListRepository.kt](../features/pokemonlist/api/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/PokemonListRepository.kt)
- **Implementation**: [PokemonListRepositoryImpl.kt](../features/pokemonlist/data/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/data/PokemonListRepositoryImpl.kt)
- **Tests**: [PokemonListRepositoryTest.kt](../features/pokemonlist/data/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/data/PokemonListRepositoryTest.kt)
- **Demonstrates**:
  - Either<RepoError, T> return types
  - Either.catch { } error handling
  - Sealed error hierarchy (Network, Http, Unknown)
  - DTO to domain mapping
  - Pagination parameters

### Parametric Repository
- **API Interface**: [PokemonDetailRepository.kt](../features/pokemondetail/api/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/PokemonDetailRepository.kt)
- **Implementation**: [PokemonDetailRepositoryImpl.kt](../features/pokemondetail/data/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/data/PokemonDetailRepositoryImpl.kt)
- **Tests**: [PokemonDetailRepositoryTest.kt](../features/pokemondetail/data/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/data/PokemonDetailRepositoryTest.kt)
- **Demonstrates**:
  - Complex nested DTOs
  - Error mapping
  - Type safety with sealed interfaces

---

## Testing Patterns

### Repository Tests
- **Example**: [PokemonListRepositoryTest.kt](../features/pokemonlist/data/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/data/PokemonListRepositoryTest.kt)
- **Demonstrates**:
  - Kotest StringSpec
  - MockK for API service mocking
  - Either shouldBeRight/shouldBeLeft assertions
  - Success + all error types covered
  - Property-based tests for HTTP codes
  - 18/18 tests passing

### ViewModel Tests (with Turbine)
- **Example**: [PokemonListViewModelTest.kt](../features/pokemonlist/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModelTest.kt)
- **Demonstrates**:
  - TestScope injection pattern
  - Turbine for StateFlow testing
  - testDispatcher.scheduler.advanceUntilIdle()
  - Property-based state transition tests
  - awaitItem() / cancelAndIgnoreRemainingEvents()
  - NO Dispatchers.setMain needed

### Parametric ViewModel Tests
- **Example**: [PokemonDetailViewModelTest.kt](../features/pokemondetail/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModelTest.kt)
- **Demonstrates**:
  - Koin parametersOf testing
  - Complex state verification
  - Retry mechanism testing
  - Lifecycle integration tests

### Property-Based Tests
- **Repository Example**: [PokemonListRepositoryTest.kt#L91-L113](../features/pokemonlist/data/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/data/PokemonListRepositoryTest.kt#L91-L113)
- **ViewModel Example**: [PokemonListViewModelTest.kt#L235-L260](../features/pokemonlist/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModelTest.kt#L235-L260)
- **Demonstrates**:
  - Kotest checkAll with Arb generators
  - HTTP code range testing (400-599)
  - Random data validation
  - 1000x coverage multiplier

---

## Mapper Patterns

### DTO to Domain Mapping
- **Implementation**: [PokemonMappers.kt](../features/pokemonlist/data/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/data/PokemonMappers.kt)
- **Tests**: [PokemonMappersTest.kt](../features/pokemonlist/data/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/data/PokemonMappersTest.kt)
- **Demonstrates**:
  - Extension function mappers
  - Property-based testing for data preservation
  - ID extraction from URLs
  - Name capitalization

---

## Navigation Patterns
### Scoped Navigation (Multi-Theme Support)

#### Material Navigation Provider
- **Route**: [PokemonList](../features/pokemonlist/api/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/navigation/PokemonListEntry.kt)
- **Provider**: [PokemonListMaterialNavigationProviders.kt](../features/pokemonlist/wiring-ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/wiring/ui/material/PokemonListMaterialNavigationProviders.kt)
- **Demonstrates**:
  - scope<MaterialScope> { } for theme separation
  - navigation<Route> DSL within scope
  - Material Design 3 screen wiring

#### Unstyled Navigation Provider
- **Route**: [PokemonList](../features/pokemonlist/api/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/navigation/PokemonListEntry.kt)
- **Provider**: [PokemonListUnstyledNavigationProviders.kt](../features/pokemonlist/wiring-ui-unstyled/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/wiring/ui/unstyled/PokemonListUnstyledNavigationProviders.kt)
- **Demonstrates**:
  - scope<UnstyledScope> { } for theme separation
  - UnstyledTheme { } wrapper
  - Shared navigation with different UI
### Simple Route (No Parameters)
- **Route**: [PokemonListEntry.kt](../features/pokemonlist/api/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/navigation/PokemonListEntry.kt)
- **Provider**: [PokemonListNavigationProviders.kt](../features/pokemonlist/wiring-ui/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/wiringui/PokemonListNavigationProviders.kt)
- **Demonstrates**:
  - Plain data class route
  - navigation<Route> DSL
  - koinInject() for dependencies
  - koinViewModel() for ViewModels

### Parametric Route (With ID)
- **Route**: [PokemonDetail.kt](../features/pokemondetail/api/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/navigation/PokemonDetail.kt)
- **Provider**: [PokemonDetailNavigationProviders.kt](../features/pokemondetail/wiring-ui/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/wiringui/PokemonDetailNavigationProviders.kt)
- **Demonstrates**:
  - Route with parameter
  - ViewModel scoping with key
  - parametersOf for DI
  - Navigation 3 animations (slideInHorizontally + fadeIn)
  - DisposableEffect for lifecycle registration

---

## DI Patterns (Koin)

### Data Module (Repositories)
- **Example**: [PokemonListModule.kt](../features/pokemonlist/wiring/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/wiring/PokemonListModule.kt)
- **Demonstrates**:
  - module { } DSL
  - factory<Interface> { }
  - Factory function pattern
  - Platform-specific source sets

### Presentation Module (ViewModels)
- **Example**: [PokemonListModule.kt](../features/pokemonlist/wiring/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/wiring/PokemonListModule.kt)
- **Demonstrates**:
  - viewModel<T> { } DSL
  - SavedStateHandle injection
  - parametersOf for parametric ViewModels

### iOS Helpers
- **Example**: [KoinIos.kt](../features/pokemonlist/wiring/src/iosMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/wiring/KoinIos.kt)
- **Demonstrates**:
  - KoinPlatform.getKoin() wrapper functions
  - Int32 parameter conversion
  - iosMain source set usage

---

## iOS Integration Patterns

### SwiftUI View (Direct Integration)
- **List View**: [PokemonListView.swift](../iosApp/iosApp/Views/PokemonListView.swift)
- **Demonstrates**:
  - KoinIosKt helper usage
  - StateFlow → AsyncSequence with SKIE
  - .task { for await ... } pattern
  - Direct lifecycle calls (onStart/onStop)

### SwiftUI View (Parametric)
- **Detail View**: [PokemonDetailView.swift](../iosApp/iosApp/Views/PokemonDetailView.swift)
- **Demonstrates**:
  - Parametric ViewModel initialization
  - Int32 conversion for Int parameters
  - SKIE type renames (Type → Type_)
  - Sealed interface handling with is/as

---

## UI Patterns (Compose)

### Material Design 3 Screens

#### Material List Screen
- **Implementation**: [PokemonListMaterialScreen.kt](../features/pokemonlist/ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/ui/material/PokemonListMaterialScreen.kt)
- **Components**: [PokemonListCard.kt](../features/pokemonlist/ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/ui/material/components/PokemonListCard.kt), [PokemonListGrid.kt](../features/pokemonlist/ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/ui/material/components/PokemonListGrid.kt)
- **Demonstrates**:
  - Material 3 Card with elevation states
  - Staggered entrance animations
  - Adaptive grid (2/3/4 columns)
  - Material Symbols icons
  - Shimmer loading skeleton

#### Material Detail Screen
- **Implementation**: [PokemonDetailMaterialScreen.kt](../features/pokemondetail/ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/ui/material/PokemonDetailMaterialScreen.kt)
- **Components**: [HeroSection.kt](../features/pokemondetail/ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/ui/material/components/HeroSection.kt), [TypeBadgeRow.kt](../features/pokemondetail/ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/ui/material/components/TypeBadgeRow.kt), [AnimatedStatBar.kt](../features/pokemondetail/ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/ui/material/components/BaseStatsSection.kt)
- **Demonstrates**:
  - 256dp hero image with gradient
  - Badge animations (25ms stagger)
  - Material Symbols (height, weight, star)
  - Animated stat bars (50ms stagger)
  - Multiple preview states

### Unstyled (Minimal) Screens

#### Unstyled List Screen
- **Implementation**: [PokemonListUnstyledScreen.kt](../features/pokemonlist/ui-unstyled/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/ui/unstyled/PokemonListUnstyledScreen.kt)
- **Components**: [PokemonListCardUnstyled.kt](../features/pokemonlist/ui-unstyled/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/ui/unstyled/components/PokemonListCardUnstyled.kt), [PokemonListGridUnstyled.kt](../features/pokemonlist/ui-unstyled/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/ui/unstyled/components/PokemonListGridUnstyled.kt)
- **Demonstrates**:
  - Border-only cards (no fill)
  - Flat elevation (1dp)
  - Enhanced hover effects (brightness 1.15, border 0.5 alpha, scale 1.02)
  - .clickable() with interaction tracking
  - Theme[property][token] syntax
  - Clean minimal spacing

#### Unstyled Detail Screen
- **Implementation**: [PokemonDetailUnstyledScreen.kt](../features/pokemondetail/ui-unstyled/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/ui/unstyled/PokemonDetailUnstyledScreen.kt)
- **Components**: [HeroSectionUnstyled.kt](../features/pokemondetail/ui-unstyled/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/ui/unstyled/components/HeroSectionUnstyled.kt), [TypeBadgeRowUnstyled.kt](../features/pokemondetail/ui-unstyled/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/ui/unstyled/components/TypeBadgeRowUnstyled.kt), [BaseStatsSectionUnstyled.kt](../features/pokemondetail/ui-unstyled/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/ui/unstyled/components/BaseStatsSectionUnstyled.kt)
- **Demonstrates**:
  - 256dp flat hero image
  - Border-only type badges
  - Flat physical attribute cards
  - Monochrome stat bars
  - Linear motion (no emphasized easing)

### Legacy List Screen (Pre-Redesign)
- **Implementation**: [PokemonListScreen.kt](../features/pokemonlist/ui/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/ui/PokemonListScreen.kt)
- **Demonstrates**:
  - LazyVerticalGrid layout
  - collectAsStateWithLifecycle()
  - Loading/Content/Error state handling
  - Infinite scroll with onBottomReached
  - @Preview with realistic data

### Detail Screen
- **Implementation**: [PokemonDetailScreen.kt](../features/pokemondetail/ui/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/ui/PokemonDetailScreen.kt)
- **Demonstrates**:
  - Scaffold with TopAppBar
  - Stateful vs Stateless composables
  - Type badges with colors
  - Stats with progress bars
  - Multiple @Preview variations

---

## Error Handling Patterns

### Sealed Error Hierarchy
- **Example**: [RepoError.kt](../features/pokemonlist/api/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/RepoError.kt)
- **Demonstrates**:
  - Sealed interface design
  - Network, Http, Unknown variants
  - UI message mapping

### Error Mapping
- **Example**: [PokemonListRepositoryImpl.kt#L25-L35](../features/pokemonlist/data/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/data/PokemonListRepositoryImpl.kt#L25-L35)
- **Demonstrates**:
  - Either.catch { } pattern
  - .mapLeft { it.toRepoError() }
  - CancellationException respect

---

## Usage Guidelines

### For Documentation Authors

When writing guides, **link to this index** instead of embedding code:

```markdown
See [ViewModel Pattern](CODE_REFERENCES.md#viewmodel-pattern) for implementation examples.
```

### For LLM Agents

When explaining patterns:
1. Link to this index for concrete examples
2. Keep documentation focused on rules and rationale
3. Let the actual code be the source of truth

### Maintenance

**This file is curated** - each link has been verified and represents canonical patterns. When patterns evolve:
1. Update implementation files first
2. Update this index if file paths change
3. Documentation automatically stays current (links point to living code)

---

**Token Efficiency:** This single reference file (~400 lines) replaces 160+ embedded code blocks across documentation, saving ~10,000+ tokens across all agent prompts while improving freshness.
