# Use Case Guidelines

When to create use cases vs calling repositories directly from the presentation layer.

## The Golden Rule

**NEVER create pass-through use cases.** If a use case only forwards arguments to a repository and returns the result, delete it and call the repository directly.

```kotlin
// ❌ ANTI-PATTERN: Empty pass-through use case
class GetUserUseCase(private val repo: UserRepository) {
    suspend operator fun invoke(id: String) = repo.getUser(id)
}

// In ViewModel:
val user = getUserUseCase(id)  // Why not call repo directly?
```

## When to Create a Use Case

Create a use case only if at least one of these is true:

### 1. Orchestrates Multiple Repositories

```kotlin
class GetPokemonDetailUseCase(
    private val pokemonRepo: PokemonRepository,
    private val movesRepo: MovesRepository,
    private val evolutionsRepo: EvolutionRepository,
) {
    suspend operator fun invoke(id: PokemonId): Either<RepoError, PokemonDetail> = either {
        val pokemon = pokemonRepo.getPokemon(id).bind()
        val moves = movesRepo.getMovesForPokemon(id).bind()
        val evolutions = evolutionsRepo.getEvolutionChain(id).bind()
        PokemonDetail(pokemon, moves, evolutions)
    }
}
```

### 2. Enforces Business Rules / Authorization Gates

```kotlin
class DeleteAccountUseCase(
    private val authRepo: AuthRepository,
    private val userRepo: UserRepository,
) {
    suspend operator fun invoke(userId: UserId): Either<RepoError, Unit> = either {
        val currentUser = authRepo.currentUser()
            ?: raise(RepoError.Unauthenticated)
        
        ensure(currentUser.id == userId || currentUser.isAdmin) {
            RepoError.Forbidden("Can only delete own account")
        }
        
        userRepo.delete(userId).bind()
    }
}
```

### 3. Applies Cross-Cutting Policies

```kotlin
class SyncWithRetryUseCase(
    private val syncRepo: SyncRepository,
    private val retryPolicy: RetryPolicy,
) {
    suspend operator fun invoke(): Either<RepoError, SyncResult> =
        withRetry(policy = retryPolicy) {
            syncRepo.sync()
        }
}

class ValidateAndSaveAddressUseCase(
    private val addressValidator: AddressValidator,
    private val addressRepo: AddressRepository,
) {
    suspend operator fun invoke(address: Address): Either<RepoError, Address> = either {
        val errors = addressValidator.validate(address)
        ensure(errors.isEmpty()) { RepoError.Validation(errors) }
        addressRepo.save(address).bind()
    }
}
```

### 4. Coordinates Transactions

```kotlin
class TransferFundsUseCase(
    private val accountRepo: AccountRepository,
    private val transactionRepo: TransactionRepository,
) {
    suspend operator fun invoke(
        from: AccountId,
        to: AccountId,
        amount: Money,
    ): Either<RepoError, Transaction> = either {
        ensure(amount > Money(0)) { 
            RepoError.Validation("Amount must be positive") 
        }
        
        val fromAccount = accountRepo.get(from).bind()
        ensure(fromAccount.balance >= amount) {
            RepoError.Business("Insufficient funds")
        }
        
        // Atomic operation coordinated here
        accountRepo.debit(from, amount).bind()
        accountRepo.credit(to, amount).bind()
        transactionRepo.record(from, to, amount).bind()
    }
}
```

### 5. Complex Domain Transformations

```kotlin
class CalculateShippingUseCase(
    private val productRepo: ProductRepository,
    private val shippingRepo: ShippingRepository,
    private val locationService: LocationService,
) {
    suspend operator fun invoke(
        cart: Cart,
        destination: Address,
    ): Either<RepoError, ShippingQuote> = either {
        val products = cart.items
            .map { productRepo.get(it.productId).bind() }
        
        val totalWeight = products.sumOf { it.weight }
        val dimensions = products.fold(Dimensions.ZERO) { acc, p ->
            acc.combine(p.dimensions)
        }
        
        val origin = locationService.getNearestWarehouse(destination).bind()
        
        shippingRepo.calculate(
            from = origin,
            to = destination,
            weight = totalWeight,
            dimensions = dimensions,
        ).bind()
    }
}
```

## Preferred: Direct Repository Access

When no domain policy is needed, call repositories directly from ViewModels:

```kotlin
class ProfileViewModel(
    private val repo: UserRepository,
    scope: CoroutineScope,
) : ViewModel(scope), UiStateHolder<ProfileUiState, ProfileUiEvent> {
    private val _ui = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    override val uiState: StateFlow<ProfileUiState> = _ui
    
    fun load(id: String) = scope.launch {
        repo.getUser(id).fold(
            ifLeft = { error ->
                _ui.value = ProfileUiState.Error(mapError(error))
            },
            ifRight = { user ->
                _ui.value = ProfileUiState.Content(user.toUi())
            }
        )
    }
    
    fun refresh() = scope.launch {
        _ui.update { state ->
            if (state is ProfileUiState.Content) {
                state.copy(isRefreshing = true)
            } else state
        }
        
        repo.refresh().fold(
            ifLeft = { /* handle error */ },
            ifRight = { /* update content */ }
        )
    }
}
```

## Decision Flowchart

```
Start: Need to load/transform data?
├── Is it a simple repository call?
│   ├── Yes → Call repo directly from ViewModel
│   └── No → Continue...
├── Does it need 2+ repositories?
│   ├── Yes → Create use case
│   └── No → Continue...
├── Does it enforce business rules?
│   ├── Yes → Create use case
│   └── No → Continue...
├── Does it need cross-cutting concerns?
│   ├── Yes → Create use case
│   └── No → Continue...
├── Can the logic live in ViewModel?
│   ├── Yes → Do it in ViewModel
│   └── No → Create use case
```

## MVP-First Minimalism

Avoid overengineering. The project follows YAGNI (You Aren't Gonna Need It):

1. Start with direct repository calls
2. Extract a use case only when the logic grows complex
3. Refactor when you see duplication or violation of single responsibility

## Common Anti-Patterns

### ❌ The Pass-Through

```kotlin
class GetPokemonUseCase(private val repo: PokemonRepository) {
    suspend operator fun invoke(id: PokemonId) = repo.getPokemon(id)
}
```

**Problem**: Adds no value. Just adds indirection.

### ❌ The Premature Abstraction

```kotlin
// Created "just in case" we need to add logic later
class GetUserUseCase(private val repo: UserRepository) {
    suspend operator fun invoke(id: String) = repo.getUser(id)
}
```

**Problem**: Don't add layers you don't need. Add them when you actually need the logic.

### ❌ The "Use Case for Everything"

```kotlin
class GetCurrentTimeUseCase {
    operator fun invoke() = Clock.System.now()
}
```

**Problem**: Not every operation needs a use case. Simple utilities can be called directly.

## Testing Use Cases

Use cases should be pure and testable:

```kotlin
class SubmitOrderUseCaseTest : FunSpec({
    lateinit var cartRepo: FakeCartRepository
    lateinit var paymentRepo: FakePaymentRepository
    lateinit var inventoryRepo: FakeInventoryRepository
    lateinit var useCase: SubmitOrderUseCase
    
    beforeEach {
        cartRepo = FakeCartRepository()
        paymentRepo = FakePaymentRepository()
        inventoryRepo = FakeInventoryRepository()
        useCase = SubmitOrderUseCase(cartRepo, paymentRepo, inventoryRepo)
    }
    
    test("should fail with empty cart") {
        cartRepo.setCart(Cart.empty())
        
        val result = useCase()
        
        result.shouldBeLeft()
    }
    
    test("should reserve inventory before charging") {
        val item = CartItem.testItem()
        cartRepo.setCart(Cart(listOf(item)))
        
        useCase()
        
        inventoryRepo.reservedItems.shouldContain(item)
    }
})
```

## File Naming Convention

```
[Action][Entity]UseCase.kt

Examples:
- SubmitOrderUseCase.kt
- DeleteAccountUseCase.kt
- GetPokemonDetailUseCase.kt
- SyncWithRetryUseCase.kt
```

## Module Placement

Use cases typically live in the `:presentation` module if they're specific to a feature's presentation layer, or in a `:domain` module if they're shared:

```
:features:checkout:presentation/
  └── SubmitOrderUseCase.kt
  └── CalculateTaxUseCase.kt
  └── ValidateCouponUseCase.kt
```
