# Domain Exceptions

Domain exceptions for business rule enforcement and control flow.

## Philosophy

The project follows these principles for error handling:

1. **Either for expected errors**: Use `Either<RepoError, T>` for recoverable cases (network, not found, validation)
2. **Exceptions for exceptional cases**: Use exceptions for truly exceptional or control-flow dictated situations
3. **Explicit modeling**: Domain exceptions are explicitly defined and documented in PRD/user flows

## Standard Domain Exceptions

### UnauthenticatedException

Thrown when an operation requires a signed-in user.

```kotlin
// In :core:domain:api or shared exceptions package
class UnauthenticatedException(
    message: String = "Authentication required",
) : Exception(message)

// Usage in use case or repository
suspend fun deleteAccount(userId: UserId): Either<RepoError, Unit> = either {
    val currentUser = authRepo.currentUser()
        ?: throw UnauthenticatedException("Must be signed in to delete account")
    
    ensure(currentUser.id == userId) {
        RepoError.Forbidden("Can only delete own account")
    }
    
    userRepo.delete(userId).bind()
}
```

### PurchaseRequiredException

Thrown when an operation requires an active purchase or subscription.

```kotlin
// In :core:domain:api or shared exceptions package
class PurchaseRequiredException(
    message: String = "Purchase required",
    val productId: String? = null,
) : Exception(message)

// Usage
suspend fun exportData(): Either<RepoError, ExportFile> = either {
    val subscription = subscriptionRepo.current()
    
    if (!subscription?.hasFeature(Feature.EXPORT) == true) {
        throw PurchaseRequiredException(
            "Export requires Premium subscription",
            productId = "premium_monthly"
        )
    }
    
    exportRepo.generateExport().bind()
}
```

## When to Use Exceptions vs Either

| Scenario | Use | Example |
|----------|-----|---------|
| Network error | Either | `RepoError.Network(cause)` |
| Not found | Either | `RepoError.NotFound(id)` |
| Validation failure | Either | `RepoError.Validation(errors)` |
| Business rule violation | Either | `RepoError.Business("Insufficient funds")` |
| Missing authentication | Exception | `UnauthenticatedException` |
| Missing purchase | Exception | `PurchaseRequiredException` |
| Unexpected null | Exception | Standard `IllegalStateException` |
| Programming error | Exception | Standard `IllegalArgumentException` |

## Exception Hierarchy

```
Exception (Kotlin standard)
├── UnauthenticatedException      # Auth required
├── PurchaseRequiredException     # Payment/subscription required
└── (Project-specific exceptions)
    ├── RateLimitException        # Too many requests
    ├── FeatureDisabledException  # Feature flag off
    └── ...
```

## Handling Domain Exceptions

### In ViewModels

```kotlin
class ProfileViewModel(
    private val deleteAccountUseCase: DeleteAccountUseCase,
    scope: CoroutineScope,
) : ViewModel(scope) {
    fun deleteAccount() = scope.launch {
        _ui.value = ProfileUiState.Loading
        
        try {
            deleteAccountUseCase().fold(
                ifLeft = { error ->
                    _ui.value = ProfileUiState.Error(mapError(error))
                },
                ifRight = {
                    _events.emit(ProfileEvent.AccountDeleted)
                }
            )
        } catch (e: UnauthenticatedException) {
            // Handle auth exception - redirect to login
            _events.emit(ProfileEvent.NavigateToLogin)
        } catch (e: PurchaseRequiredException) {
            // Handle purchase exception - show paywall
            _events.emit(ProfileEvent.ShowPaywall(e.productId))
        }
    }
}
```

### In Repository Implementations

```kotlin
class PokemonRepositoryImpl(
    private val api: PokemonApi,
    private val authProvider: AuthProvider,
) : PokemonRepository {
    override suspend fun getPremiumPokemon(id: PokemonId): Either<RepoError, Pokemon> = try {
        val token = authProvider.token()
            ?: throw UnauthenticatedException()
        
        api.getPremiumPokemon(id.value, token)
            .map { it.toDomain() }
            .toEither { error ->
                when (error) {
                    is HttpException -> when (error.code()) {
                        401 -> RepoError.Unauthenticated
                        403 -> RepoError.PurchaseRequired
                        else -> RepoError.Http(error.code(), error.message())
                    }
                    else -> RepoError.Network(error)
                }
            }
    } catch (e: UnauthenticatedException) {
        RepoError.Unauthenticated.left()
    }
}
```

## Custom Domain Exceptions

Add new domain exceptions when PRD/user flows explicitly model control flow scenarios:

```kotlin
// Rate limiting
class RateLimitException(
    message: String = "Rate limit exceeded",
    val retryAfter: Duration? = null,
) : Exception(message)

// Feature flags
class FeatureDisabledException(
    message: String = "Feature is disabled",
    val featureKey: String,
) : Exception(message)

// Quota exceeded
class QuotaExceededException(
    message: String = "Quota exceeded",
    val limit: Int,
    val used: Int,
) : Exception(message)
```

## Testing Exception Cases

```kotlin
class DeleteAccountUseCaseTest : FunSpec({
    test("should throw UnauthenticatedException when no user") {
        val authRepo = FakeAuthRepository(currentUser = null)
        val useCase = DeleteAccountUseCase(authRepo, FakeUserRepository())
        
        shouldThrow<UnauthenticatedException> {
            useCase(UserId("123"))
        }
    }
    
    test("should throw PurchaseRequiredException for premium feature") {
        val subscriptionRepo = FakeSubscriptionRepository(
            subscription = Subscription.Free
        )
        val useCase = ExportDataUseCase(subscriptionRepo, FakeExportRepo())
        
        val exception = shouldThrow<PurchaseRequiredException> {
            useCase()
        }
        
        exception.productId shouldBe "premium_monthly"
    }
})
```

## Best Practices

### ✅ DO

- Document exceptions in PRD/user flows
- Provide clear error messages for debugging
- Include relevant context (product IDs, retry timing)
- Convert exceptions to `Either` at repository boundaries when appropriate
- Test exception cases explicitly

### ❌ DON'T

- Use exceptions for expected error conditions (use `Either`)
- Create exceptions for cases that can be handled with `Either` types
- Throw exceptions for validation errors that are part of normal flow
- Use generic exceptions when a specific one would be clearer

```kotlin
// ❌ Wrong: Using exception for validation
fun validateEmail(email: String) {
    if (!email.contains("@")) {
        throw IllegalArgumentException("Invalid email")  // Use Either instead
    }
}

// ✅ Correct: Using Either for validation
fun validateEmail(email: String): Either<ValidationError, String> =
    if (email.contains("@")) {
        email.right()
    } else {
        ValidationError.InvalidEmail.left()
    }

// ❌ Wrong: Generic exception for auth
throw Exception("Not logged in")  // Use UnauthenticatedException

// ✅ Correct: Specific domain exception
throw UnauthenticatedException("Must be signed in")
```

## Module Placement

Domain exceptions typically live in:

```
:core:domain:api/
  └── exceptions/
      └── UnauthenticatedException.kt
      └── PurchaseRequiredException.kt
      └── RateLimitException.kt
```

Or if feature-specific:

```
:features:payment:api/
  └── exceptions/
      └── PaymentFailedException.kt
      └── InsufficientFundsException.kt
```
