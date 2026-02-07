# Coroutines & Concurrency Guidelines

> **MIGRATED**: This documentation has been migrated to the **@kmp-presentation** skill.

Please use `@kmp-presentation` for the latest coroutine and concurrency patterns.

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
val result: Either<Error, Domain> = either {
  val a = repo.stepA().bind()
  val b = repo.stepB(a).bind()
  combine(a, b)
}
```
