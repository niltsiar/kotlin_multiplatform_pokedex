# Presentation Layer Guidelines

Purpose: Establish consistent patterns for UI architecture, state management, and component design in Compose Multiplatform projects.

## Location and Structure
- All presentation code under `composeApp/src/commonMain/kotlin/com/<org>/<app>/presentation/`
- Organize by feature: `presentation/screens/<feature>/` (e.g., `home/`, `paywall/`, `onboarding/`)
- Shared components: `presentation/components/` with sub-packages for related components
- Screen-level navigation: `presentation/screens/<feature>/<Feature>ScreenRoute.kt`

## Screen Architecture Pattern

### UiStateHolder Pattern
Use UiStateHolder as a viewmodel for complex screens with business logic. Repositories return Arrow `Either`, so handle both Left and Right paths near the boundary and map to UI state.

```kotlin
class HomeUiStateHolder(
    private val repository: Repository,
    private val applicationScope: ApplicationScope
) : UiStateHolder() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        // Example: initial load
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            val result = repository.loadItems()
            _uiState.value = result.fold(
                ifLeft = { err -> HomeUiState.Error(mapError(err)) },
                ifRight = { items -> HomeUiState.Content(items.map(::toUi)) }
            )
        }
    }

    fun onUiEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.Refresh -> refresh()
            is HomeUiEvent.ItemClicked -> {/* navigate */}
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            repository.loadItems().fold(
                ifLeft = { _uiState.value = HomeUiState.Error(mapError(it)) },
                ifRight = { _uiState.value = HomeUiState.Content(it.map(::toUi)) }
            )
        }
    }
}
```

### Screen Composables
Follow function overloading pattern for flexibility:

```kotlin
// Main entry point with StateHolder
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiStateHolder: HomeUiStateHolder,
    onNavigate: (destination) -> Unit
) {
    val uiState by uiStateHolder.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        modifier = modifier,
        uiState = uiState,
        onUiEvent = uiStateHolder::onUiEvent,
        onNavigate = onNavigate
    )
}

// Pure composable for testing and previews
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onUiEvent: (HomeUiEvent) -> Unit,
    onNavigate: (destination) -> Unit
) {
    when (uiState) {
        is HomeUiState.Loading -> Loading()
        is HomeUiState.Error -> ErrorView(message = uiState.message) { onUiEvent(HomeUiState.DefaultRetryEvent) }
        is HomeUiState.Content -> ContentList(uiState.items, onUiEvent)
    }
}
```

## State Management

### UI State Classes
- Use immutable data classes for UI state
- Provide meaningful defaults for easy construction
- Group related state in nested data classes when appropriate

```kotlin
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Error(val message: String) : HomeUiState
    data class Content(val items: List<ItemUiState>) : HomeUiState

    companion object {
        val DefaultRetryEvent = HomeUiEvent.Refresh
    }
}
```

### UI Events
Use sealed classes/interfaces for user interactions:

```kotlin
sealed interface HomeUiEvent {
    data object Refresh : HomeUiEvent
    data class ItemClicked(val id: String) : HomeUiEvent
    data class FilterChanged(val filter: FilterType) : HomeUiEvent
}
```

## Component Design

### Reusable Components
- Keep components small and focused on single responsibility
- Use `modifier: Modifier = Modifier` parameter for all custom composables
- Provide meaningful parameter names, avoid generic names like `onClick`

```kotlin
@Composable
fun VideoCardItem(
    title: String,
    thumbnailUrl: String?,
    onPlayClick: () -> Unit,
    onMoreOptionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Implementation
}
```

### Component Organization
- Group related components in dedicated packages:
  - `presentation/components/video/` - video-related components
  - `presentation/components/premium/` - premium/subscription components
  - `presentation/components/ads/` - advertisement components

### Design System Integration
- Use design system components from `designsystem` module when available
- Follow design system naming conventions (e.g., `AppButton`, `AppCard`)
- Keep presentation components generic and parameterized

## Vertical Slices & Module Boundaries
- Presentation for each feature lives in that feature’s modules. Only expose what other features need via `:features:<feature>:api`.
- Keep navigation contracts (routes, deep links, entry points) in `api` and implementations in `impl`.

## Screen Wrapper Patterns

### Common Screen Structure
Use consistent screen wrapper pattern:

```kotlin
@Composable
fun FeatureScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    ScreenWithToolbar(
        title = stringResource(Res.string.feature_title),
        onNavigationClick = onBackClick
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues
        ) {
            // Screen content
        }
    }
}
```


### User Feedback
- Use proper loading indicators for async operations
- Show meaningful error messages with retry options
- Provide haptic feedback for important actions


## Performance Considerations

### State Management
- Use `remember` for expensive calculations
- Implement proper `key` parameters for LazyColumn/LazyRow items
- Avoid unnecessary recomposition with stable parameters


## Alignment with Product Docs
- Map UI states to user flows from `.junie/guides/project/user_flow.md`
- Implement copy and content from `.junie/guides/project/prd.md`
- Follow UX specifications from `.junie/guides/project/ui_ux.md`
- Handle premium states according to `.junie/guides/project/paywall.md`
