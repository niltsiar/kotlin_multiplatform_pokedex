# Coroutines & Concurrency Guidelines

Purpose: Ensure coroutine usage is testable, predictable, and aligned with platform lifecycles.

- Scopes
  - backgroundScope: Use a dedicated CoroutineScope with SupervisorJob() and Dispatchers.IO for repository/data work.
    - Inject the IO dispatcher (CoroutineDispatcher) for testability. In tests, use a TestDispatcher.
    - Example construction (DI): backgroundScope = CoroutineScope(SupervisorJob() + ioDispatcher)
  - ApplicationScope: For jobs that must outlive screens/features (e.g., warm caches, analytics upload, periodic sync), launch in an Application-level scope.
    - Provide this scope via DI. Tie its Job to app process lifetime.

- Dispatchers
  - Use Dispatchers.IO (injected) for blocking IO or network-bound work.
  - Confine CPU-heavy work to Dispatchers.Default (injected) when needed.
  - Avoid hardcoding Dispatchers in code; depend on abstractions (e.g., a DispatchersProvider) to improve testability.

- Repositories
  - Expose suspend functions and Flows. Perform IO using backgroundScope.
  - For long-running operations that should continue across screens, delegate to ApplicationScope.
  - Use withContext(ioDispatcher) around discrete IO when a new scope is not required.

- ViewModel scopes (KMP)
  - All ViewModels must extend `androidx.lifecycle.ViewModel`.
  - Do NOT store a `CoroutineScope` field. Instead, pass a custom scope to the `ViewModel` superclass constructor and use `viewModelScope` internally.
  - Recommended helper (place in `:core:util` commonMain):
    ```kotlin
    class CloseableCoroutineScope(
      context: CoroutineContext = SupervisorJob() + Dispatchers.Main.immediate
    ) : Closeable, CoroutineScope {
      override val coroutineContext: CoroutineContext = context
      override fun close() { coroutineContext.cancel() }
    }

    class MyViewModel(
      customScope: CloseableCoroutineScope = CloseableCoroutineScope()
    ) : ViewModel(customScope) {
      fun doSomething() = viewModelScope.launch { /* ... */ }
    }
    ```

- Structured Concurrency
  - Prefer structured concurrency; avoid GlobalScope.
  - Use SupervisorJob for scopes handling independent child coroutines so one failure doesn’t cancel siblings.

- Cancellation & Timeouts
  - Make network/database calls cancellable. Propagate coroutineContext to Ktor/SQL drivers.
  - Never catch and swallow `CancellationException`. If you need to wrap throwing code, prefer Arrow `Either.catch { ... }` which respects coroutine cancellation and avoids converting cancellations into recoverable failures.
  - Use timeouts judiciously (withTimeout) and map cancellations/timeouts to domain errors as needed.

- Testing
  - Inject dispatchers and scopes; in unit tests, use StandardTestDispatcher/UnconfinedTestDispatcher and TestScope.
  - Avoid real delays in tests; use TestCoroutineScheduler to advance time.

## Arrow patterns in suspend code
- At repository boundaries, wrap throwing blocks with `Either.catch { ... }` and map exceptions via `mapLeft { it.toRepoError() }`.
- Inside repositories or use cases that orchestrate multiple steps, prefer Arrow monad comprehensions for readability:
```kotlin
val result: Either<RepoError, Domain> = either {
  val a = repo.stepA().bind()
  val b = repo.stepB(a).bind()
  combine(a, b)
}
```
