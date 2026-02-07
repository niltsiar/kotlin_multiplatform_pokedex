# SavedStateHandle Patterns

State persistence across configuration changes and process death using SavedStateHandle.

## Modern Pattern (SavedState 1.4.0+)

Use `by saved` delegate for automatic state persistence. This is the current recommended approach.

### Basic Usage

```kotlin
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.serialization.saved
import kotlinx.serialization.Serializable

@Serializable
data class SearchPersistedState(
    val query: String = "",
    val lastResults: List<SearchResult> = emptyList()
)

class SearchViewModel(
    private val repository: SearchRepository,
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
) : ViewModel(viewModelScope),
    UiStateHolder<SearchUiState, SearchUiEvent> {

    // ✨ Single line - automatic persistence
    private var persistedState by savedStateHandle.saved { SearchPersistedState() }

    private val _uiState = MutableStateFlow<SearchUiState>(restoreUiState())
    override val uiState: StateFlow<SearchUiState> = _uiState

    private fun updateQuery(query: String) {
        persistedState = persistedState.copy(query = query)  // Auto-saved
        _uiState.value = SearchUiState.Idle(query)
    }

    private fun search() {
        val query = persistedState.query
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading(query)

            repository.search(query).fold(
                ifLeft = { error ->
                    _uiState.value = SearchUiState.Error(
                        query = query,
                        message = error.toUiMessage()
                    )
                },
                ifRight = { results ->
                    persistedState = persistedState.copy(
                        lastResults = results  // Auto-saved
                    )
                    _uiState.value = SearchUiState.Results(
                        query = query,
                        results = results.toImmutableList()
                    )
                }
            )
        }
    }

    private fun restoreUiState(): SearchUiState {
        return if (persistedState.lastResults.isNotEmpty()) {
            SearchUiState.Results(
                query = persistedState.query,
                results = persistedState.lastResults.toImmutableList()
            )
        } else {
            SearchUiState.Idle(persistedState.query)
        }
    }
}
```

### Benefits

| Benefit | Description |
|---------|-------------|
| Code reduction | 93% less code vs manual JSON |
| Automatic | State saved on every property write |
| Type-safe | Uses kotlinx.serialization |
| No manual calls | No `persistState()` functions |
| KMP compatible | Works in `commonMain` |

### Requirements

1. **Correct import**: `androidx.lifecycle.serialization.saved` (NOT `androidx.savedstate.serialization.saved`)
2. **Serializable state**: `@Serializable` annotation required
3. **Stable property names**: Renaming breaks restoration for existing users

## Navigation Arguments via SavedStateHandle

When navigating with arguments, SavedStateHandle receives them automatically:

```kotlin
class ProfileViewModel(
    private val repository: UserRepository,
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob())
) : ViewModel(viewModelScope),
    DefaultLifecycleObserver,
    UiStateHolder<ProfileUiState, ProfileUiEvent> {

    // Extract navigation argument
    private val userId: String = checkNotNull(savedStateHandle.get<String>("userId"))

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    override val uiState: StateFlow<ProfileUiState> = _uiState

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            repository.getUser(userId).fold(
                ifLeft = { _uiState.value = ProfileUiState.Error(it.toUiMessage()) },
                ifRight = { user ->
                    _uiState.value = ProfileUiState.Content(user.toUi())
                }
            )
        }
    }
}
```

### Navigation Route Definition

```kotlin
// Route with argument
@Serializable
data class Profile(val userId: String)

// Navigation registration
entry<Profile> { route ->
    ProfileScreen(userId = route.userId)
}
```

## Complex State Persistence

For forms and multi-field state:

```kotlin
@Serializable
data class FormPersistedState(
    val title: String = "",
    val description: String = "",
    val selectedCategoryId: String? = null,
    val tags: List<String> = emptyList(),
    val isPublished: Boolean = false,
    val lastSavedAt: Long? = null
)

class EditPostViewModel(
    private val repository: PostRepository,
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob())
) : ViewModel(viewModelScope),
    UiStateHolder<EditPostUiState, EditPostUiEvent> {

    private var persistedState by savedStateHandle.saved { FormPersistedState() }

    private val _uiState = MutableStateFlow<EditPostUiState>(restoreState())
    override val uiState: StateFlow<EditPostUiState> = _uiState

    override fun onUiEvent(event: EditPostUiEvent) {
        when (event) {
            is EditPostUiEvent.TitleChanged -> updateTitle(event.title)
            is EditPostUiEvent.DescriptionChanged -> updateDescription(event.description)
            is EditPostUiEvent.CategorySelected -> updateCategory(event.categoryId)
            is EditPostUiEvent.TagAdded -> addTag(event.tag)
            is EditPostUiEvent.TagRemoved -> removeTag(event.tag)
            is EditPostUiEvent.PublishToggle -> togglePublish()
            is EditPostUiEvent.Save -> save()
        }
    }

    private fun updateTitle(title: String) {
        persistedState = persistedState.copy(title = title)
        updateUiState()
    }

    private fun updateDescription(description: String) {
        persistedState = persistedState.copy(description = description)
        updateUiState()
    }

    private fun updateCategory(categoryId: String) {
        persistedState = persistedState.copy(selectedCategoryId = categoryId)
        updateUiState()
    }

    private fun addTag(tag: String) {
        if (tag !in persistedState.tags) {
            persistedState = persistedState.copy(
                tags = persistedState.tags + tag
            )
            updateUiState()
        }
    }

    private fun removeTag(tag: String) {
        persistedState = persistedState.copy(
            tags = persistedState.tags - tag
        )
        updateUiState()
    }

    private fun togglePublish() {
        persistedState = persistedState.copy(isPublished = !persistedState.isPublished)
        updateUiState()
    }

    private fun updateUiState() {
        _uiState.value = EditPostUiState.Form(
            title = persistedState.title,
            description = persistedState.description,
            selectedCategoryId = persistedState.selectedCategoryId,
            tags = persistedState.tags.toImmutableList(),
            isPublished = persistedState.isPublished,
            canSave = persistedState.title.isNotBlank() &&
                     persistedState.description.isNotBlank(),
            lastSavedAt = persistedState.lastSavedAt
        )
    }

    private fun save() {
        viewModelScope.launch {
            _uiState.value = EditPostUiState.Saving

            repository.savePost(
                title = persistedState.title,
                description = persistedState.description,
                categoryId = checkNotNull(persistedState.selectedCategoryId),
                tags = persistedState.tags,
                isPublished = persistedState.isPublished
            ).fold(
                ifLeft = { error ->
                    _uiState.value = EditPostUiState.Error(error.toUiMessage())
                },
                ifRight = { postId ->
                    persistedState = persistedState.copy(lastSavedAt = System.currentTimeMillis())
                    _uiState.value = EditPostUiState.Saved(postId)
                }
            )
        }
    }

    private fun restoreState(): EditPostUiState {
        return EditPostUiState.Form(
            title = persistedState.title,
            description = persistedState.description,
            selectedCategoryId = persistedState.selectedCategoryId,
            tags = persistedState.tags.toImmutableList(),
            isPublished = persistedState.isPublished,
            canSave = persistedState.title.isNotBlank(),
            lastSavedAt = persistedState.lastSavedAt
        )
    }
}
```

## Desktop ViewModel + SavedStateHandle

Desktop requires additional setup for SavedStateHandle:

```kotlin
// Desktop-specific ViewModel creation
class DesktopViewModelProvider {
    fun createSearchViewModel(): SearchViewModel {
        // Create SavedStateHandle with desktop persistence
        val savedStateHandle = SavedStateHandle.createHandle(
            restoredState = loadSavedState(),  // Custom persistence
            defaultArgs = null
        )

        return SearchViewModel(
            repository = get(),
            savedStateHandle = savedStateHandle
        )
    }

    private fun loadSavedState(): Bundle? {
        // Load from file or preferences
        return null
    }
}
```

See [desktop_viewmodel_savedstate.md](See @kmp-desktop skill) for complete setup.

## Common Patterns

### Simple Search Persistence

```kotlin
@Serializable
data class SearchState(val query: String = "")

class SimpleSearchViewModel(
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob())
) : ViewModel(viewModelScope) {

    private var state by savedStateHandle.saved { SearchState() }

    val query: String
        get() = state.query

    fun updateQuery(query: String) {
        state = state.copy(query = query)
    }
}
```

### Scroll Position Persistence

```kotlin
@Serializable
data class ListState(
    val firstVisibleItemIndex: Int = 0,
    val firstVisibleItemScrollOffset: Int = 0
)

class PersistedListViewModel(
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob())
) : ViewModel(viewModelScope) {

    private var listState by savedStateHandle.saved { ListState() }

    fun saveScrollPosition(index: Int, offset: Int) {
        listState = listState.copy(
            firstVisibleItemIndex = index,
            firstVisibleItemScrollOffset = offset
        )
    }

    fun restoreScrollPosition(): Pair<Int, Int> {
        return listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
    }
}
```

## Testing with SavedStateHandle

```kotlin
class SearchViewModelTest {

    private lateinit var viewModel: SearchViewModel
    private lateinit var savedStateHandle: SavedStateHandle

    @BeforeTest
    fun setup() {
        savedStateHandle = SavedStateHandle()
        viewModel = SearchViewModel(
            repository = mockRepository,
            savedStateHandle = savedStateHandle,
            viewModelScope = TestScope(UnconfinedTestDispatcher())
        )
    }

    @Test
    fun `state is restored from saved state handle`() = runTest {
        // Pre-populate saved state
        val initialState = SearchPersistedState(
            query = "pikachu",
            lastResults = listOf(SearchResult("Pikachu", 25))
        )
        savedStateHandle["SearchPersistedState"] = Json.encodeToString(initialState)

        // Create new ViewModel instance
        val restoredViewModel = SearchViewModel(
            repository = mockRepository,
            savedStateHandle = savedStateHandle,
            viewModelScope = TestScope(UnconfinedTestDispatcher())
        )

        // Verify state restored
        restoredViewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is SearchUiState.Results)
            assertEquals("pikachu", state.query)
        }
    }
}
```

## Reference Implementations

- `features/pokemonlist/presentation/PokemonListViewModel.kt` — Uses SavedStateHandle
- `features/pokemondetail/presentation/PokemonDetailViewModel.kt` — Navigation args

## Documentation Sources

- [presentation_layer.md](See @kmp-presentation skill) — Navigation integration
- [viewmodel_patterns.md](See @kmp-presentation skill) — Extended examples
- [desktop_viewmodel_savedstate.md](See @kmp-desktop skill) — Desktop-specific
