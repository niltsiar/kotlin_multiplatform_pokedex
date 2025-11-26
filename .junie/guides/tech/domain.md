# Domain Layer Guidelines

Purpose: Make domain models and logic stable, pure, platform-agnostic, and easy to test. Align domain with vertical-slice Clean Architecture and feature `api/impl` modules.

- Location
  - Prefer feature-local domain types: place domain models and pure business logic inside each feature’s modules.
    - Public contracts that other features need live in `:features:<feature>:api`.
    - Private or implementation-only types live in `:features:<feature>:data` or `:features:<feature>:presentation`.
  - Shared, cross-feature domain types can live in a dedicated shared/core domain `api` module if necessary (e.g., `:core:domain:api`).
  - Keep domain free of platform-specific types (no Android/iOS classes, no Ktor, no SQL types).

- Models
  - Prefer immutable data classes with val properties.
  - Provide sensible default values for optional fields to simplify construction and testing. Do not add defaults that hide required invariants.
    - Example: data class User(id: UserId, name: String = "", avatarUrl: String? = null)
  - Keep models “pure domain”: no serialization annotations, no persistence concerns, no UI-specific fields.
  - Provide pure, side-effect-free helpers/derivations via functions or extension functions.

- Domain Exceptions (business rules)
  - Domain layer is allowed to throw the following when appropriate and explicitly modeled by PRD/user flows:
    - UnauthenticatedException — thrown when an operation requires a signed-in user.
    - PurchaseRequiredException — thrown when an operation requires an active purchase/subscription.
  - Define them in a common domain/exceptions package
   
- APIs and Boundaries
  - MVP-first minimalism: avoid overengineering. Do NOT add pass-through/empty use cases that only call a single repository method. Call repositories directly from the presentation layer when no domain policy/orchestration is needed. Avoid empty use cases at all costs.
  - Introduce a use case only when it adds value: aggregates multiple repositories, enforces business rules, coordinates transactions, applies throttling/debouncing, validation, or cross-cutting concerns.
  - Repository interfaces: With vertical slicing, define repository interfaces in the feature `api` module when cross-feature access is required; otherwise, keep repositories concrete and internal to the feature `impl`.
  - Implementations (network/database) live in feature `impl` (data) modules and depend on domain contracts from the feature `api` (or core domain `api`) — never the other way around.
  - Expose asynchronous data as suspend functions and/or Kotlin Flow/StateFlow.

- Mapping
  - Domain models are the source of truth inside the app. Map from DTOs/entities at the edges (data layer) into domain models.
  - Keep mapping logic close to the boundary (e.g., data.remote -> domain, data.local -> domain) and unit-test it.

- Error Handling
  - Convert transport/storage errors into meaningful domain errors at the repository boundary. Project-wide, repositories return Arrow `Either<RepoError, T>`.
  - Use sealed results for recoverable cases; reserve exceptions for truly exceptional or control-flow dictated cases as above.

- Testing
  - Domain should be unit-testable without Android/iOS. Use simple fakes for repository interfaces.
  - Cover invariants and exception cases (Unauthenticated, PurchaseRequired) with tests.
  - Prefer Kotest for property-based testing where appropriate (e.g., validation logic, value objects).

## Use Cases: When to Create Them (and When Not To)

Rules of thumb
- Do not create a use case that simply forwards arguments to a repository and returns the result.
- Create a use case only if at least one of these is true:
  - Orchestrates 2+ repositories or data sources
  - Enforces business rules/authorization gates (see Domain Exceptions)
  - Applies cross-cutting policies (rate limiting, retries, input validation, transactions)
  - Transforms/massages multiple inputs into a domain decision

Anti-pattern (don’t do this)
```kotlin
// Empty pass-through use case — avoid
class GetUserUseCase(private val repo: UserRepository) {
  suspend operator fun invoke(id: String) = repo.getUser(id)
}
```

Preferred
```kotlin
// Call repository directly from presentation when no domain policy is needed
class ProfileViewModel(
  private val repo: UserRepository,
  viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) : ViewModel(viewModelScope), UiStateHolder<ProfileUiState, ProfileUiEvent> {
  private val _ui = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
  override val uiState: StateFlow<ProfileUiState> = _ui
  override fun onUiEvent(event: ProfileUiEvent) { /* handle events */ }

  fun load(id: String) = viewModelScope.launch {
    repo.getUser(id).fold(
      ifLeft = { _ui.value = ProfileUiState.Error(mapError(it)) },
      ifRight = { user -> _ui.value = ProfileUiState.Content(user.toUi()) }
    )
  }
}
```

Value-adding example
```kotlin
// Aggregates multiple repositories and enforces a rule
class SubmitOrderUseCase(
  private val cartRepo: CartRepository,
  private val paymentRepo: PaymentRepository,
  private val inventoryRepo: InventoryRepository,
) {
  suspend operator fun invoke(): Either<RepoError, Receipt> = either {
    val cart = cartRepo.current().bind()
    ensure(cart.items.isNotEmpty()) { RepoError.Unknown(IllegalStateException("Empty cart")) }
    inventoryRepo.reserve(cart.items).bind()
    val receipt = paymentRepo.charge(cart.total).bind()
    receipt
  }
}
```

- Alignment with Product Docs
  - When modeling entities, constraints, and states, prefer the canonical definitions from .junie/guides/project/prd.md and .junie/guides/project/user_flow.md. If conflicts arise, follow PRD for data rules and user_flow for sequence/UX; call out discrepancies.
