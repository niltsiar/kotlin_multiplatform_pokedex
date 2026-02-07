---
name: kmp-testing-patterns
description: "Test implementation patterns for Kotlin Multiplatform with Kotest, MockK, Turbine, and property-based testing. Use when: (1) Writing tests for repositories, ViewModels, or mappers, (2) Implementing property-based tests with Kotest, (3) Testing Flow/StateFlow with Turbine, (4) Using MockK for mocking in tests, (5) Writing screenshot tests with Roborazzi"
---

# KMP Testing Patterns Skill

Test implementation patterns for Kotlin Multiplatform with Kotest, MockK, Turbine, and property-based testing.

## When to Use This Skill

**MANDATORY**: Load this skill when working on:
- Writing tests for repositories, ViewModels, or mappers
- Implementing property-based tests with Kotest
- Testing Flow/StateFlow with Turbine
- Using MockK for mocking in tests
- Writing screenshot tests with Roborazzi

**Do NOT use for**: Testing strategy decisions → use @kmp-testing-strategy, Architecture decisions → use @kmp-architecture

## Core Principle

**NO CODE WITHOUT TESTS** - Every production file MUST have a corresponding test file.

## Test Location Strategy

| Production Code | Test Location | Framework |
|----------------|---------------|-----------|
| Repository | androidUnitTest/ | Kotest + MockK + Turbine |
| ViewModel | androidUnitTest/ | Kotest + MockK + Turbine |
| Mapper | androidUnitTest/ | Kotest properties |
| Use Case | androidUnitTest/ | Kotest + MockK |
| API Service | androidUnitTest/ | Kotest + MockK |
| @Composable | Same file | @Preview + Roborazzi |
| Simple Utility | commonTest/ | kotlin-test |

## Essential Workflows

### Workflow 1: Write ViewModel Test with Turbine

1. Inject `SavedStateHandle()` and `testScope` in ViewModel constructor.
2. Use Turbine `.test { }` for flow assertions.
3. Advance time with `testDispatcher.scheduler.advanceUntilIdle()`.

```kotlin
"state transitions correctly" {
    viewModel.uiState.test {
        awaitItem() shouldBe UiState.Loading
        viewModel.onStart(owner)
        testScope.advanceUntilIdle()
        awaitItem().shouldBeInstanceOf<UiState.Content>()
        cancelAndIgnoreRemainingEvents()
    }
}
```

### Workflow 2: Write Repository Test with MockK

1. Mock the API service using `mockk()`.
2. Test both success (Right) and all error (Left) paths.
3. Verify DTO-to-domain mapping.

```kotlin
"returns Left on Network error" {
    coEvery { api.getData() } throws IOException()
    val result = repository.getData()
    result.shouldBeLeft { it shouldBe RepoError.Network }
}
```

### Workflow 3: Write Property-Based Test with Kotest

1. Use `checkAll` with `Arb` generators.
2. Define invariants (e.g., data preservation).

```kotlin
"property: mapper preserves all fields" {
    checkAll(Arb.dto()) { dto ->
        val domain = dto.toDomain()
        domain.id shouldBe dto.id
    }
}
```

### Workflow 4: Write Screenshot Test with Roborazzi

1. Add `@Preview` to your `@Composable`.
2. Establish baseline with `recordRoborazziDebug`.
3. Verify regressions with `verifyRoborazziDebug`.

```bash
./gradlew recordRoborazziDebug
./gradlew verifyRoborazziDebug
```

## Quick Reference

### Repository Test Pattern

```kotlin
class PokemonListRepositoryTest : StringSpec({
    lateinit var mockApi: PokemonListApiService
    lateinit var repository: PokemonListRepository

    beforeTest {
        mockApi = mockk()
        repository = PokemonListRepository(mockApi)
    }

    "should return Right on success" {
        coEvery { mockApi.getPokemonList(20, 0) } returns mockDto

        val result = repository.loadPage()

        result.shouldBeRight { page ->
            page.pokemons shouldHaveSize 2
        }
    }
})
```

### ViewModel Test with Turbine

```kotlin
class PokemonListViewModelTest : StringSpec({
    lateinit var mockRepository: PokemonListRepository
    lateinit var testScope: TestScope
    lateinit var viewModel: PokemonListViewModel

    beforeTest {
        mockRepository = mockk()
        testScope = TestScope()
        viewModel = PokemonListViewModel(mockRepository, testScope)
    }

    "should transition Loading to Content" {
        viewModel.uiState.test {
            awaitItem() shouldBe PokemonListUiState.Loading
            viewModel.onStart(TestLifecycleOwner())
            testScope.advanceUntilIdle()
            awaitItem().shouldBeInstanceOf<PokemonListUiState.Content>()
            cancelAndIgnoreRemainingEvents()
        }
    }
})
```

### Property-Based Test

```kotlin
"dto to domain preserves all fields" {
    checkAll(
        Arb.int(1..1000),
        Arb.string(1..50).filter { it.isNotBlank() }
    ) { id, name ->
        val dto = PokemonSummaryDto(name.lowercase(), "url/$id/")
        val domain = dto.toDomain()
        domain.id shouldBe id
        domain.name shouldBe name.lowercase().replaceFirstChar { it.uppercase() }
    }
}
```

## Reference Loading Guide

| Task | Reference | Load When |
|------|-----------|-----------|
| Kotest patterns & matchers | [kotest-patterns.md](references/kotest-patterns.md) | Writing Kotest tests |
| MockK mocking patterns | [mockk-patterns.md](references/mockk-patterns.md) | Using MockK |
| Property-based testing | [property-testing.md](references/property-testing.md) | Writing property tests |
| ViewModel testing | [vm-testing.md](references/vm-testing.md) | Testing ViewModels |
| Repository testing | [repo-testing.md](references/repo-testing.md) | Testing repositories |

## Critical Guardrails

1. NEVER skip testing error paths → test Network, Http (400-599), and Unknown RepoError cases.
2. NEVER use `runBlocking` in tests → use `runTest` with `TestScope` for deterministic behavior.
3. NEVER test implementation details → focus on public API behavior and UI state transitions.
4. NEVER skip Turbine for StateFlow testing → `awaitItem()` is essential for catching timing and emission issues.
5. NEVER mock domain models → use real domain objects/data classes; mock only external boundaries (API, DB).
6. NEVER skip property-based tests for mappers → aim for 100% property test coverage for DTO ↔ Domain mapping.
7. NEVER commit without running tests → execute `./gradlew test --continue` to ensure all 84+ tests pass.
8. NEVER use `GlobalScope` in tests → always use `TestScope` or the ViewModel's injected scope for control.

## Cross-References

### Skills (by Category)

**Architecture**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-architecture | Module structure, vertical slice organization | [SKILL.md](../kmp-architecture/SKILL.md) |
| @kmp-critical-patterns | Quick reference for 6 core patterns | [SKILL.md](../kmp-critical-patterns/SKILL.md) |

**Layer Implementation**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-presentation | ViewModels, UI state management | [SKILL.md](../kmp-presentation/SKILL.md) |
| @kmp-data-layer | Repository patterns with Either<RepoError,T> | [SKILL.md](../kmp-data-layer/SKILL.md) |

**Testing**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-testing-strategy | Testing philosophy, coverage guidelines | [SKILL.md](../kmp-testing-strategy/SKILL.md) |
| @kmp-testing-patterns | Kotest, MockK, Turbine, property tests | [SKILL.md](../kmp-testing-patterns/SKILL.md) |

### Documents

| Document | Purpose | Link |
| --- | --- | --- |
| Testing Strategy | Kotest, MockK, Turbine, property tests guide | [@kmp-testing-strategy skill](See @kmp-testing-strategy skill) |
| Conventions | Master architecture and testing reference | [conventions.md](See @kmp-architecture skill for architecture patterns) |
| Quick Reference | Essential commands and workflows | [QUICK_REFERENCE.md](../../docs/QUICK_REFERENCE.md) |

## Quick Reference

### Test Checklist

- [ ] Test file created in correct source set
- [ ] Success + all error paths covered (repositories)
- [ ] State transitions covered with Turbine (ViewModels)
- [ ] Property tests added (30-40% of tests)
- [ ] Smart casting used (no manual casts)
- [ ] NO Thread.sleep - use testScope.advanceUntilIdle()

### Anti-Patterns to Avoid

| ❌ DON'T | ✅ DO |
|----------|-------|
| Manual cast after shouldBeInstanceOf | Use smart casting |
| Thread.sleep in tests | Use testScope.advanceUntilIdle() |
| Ignore shouldBeLeft return value | Use return value directly |
| Concrete tests covered by properties | Remove redundant tests |

## Kotest Smart Casting Patterns

**Purpose**: Guide for using Kotest matchers that provide smart casting through Kotlin compiler contracts. Avoid unnecessary manual casts in tests.

---

## Quick Summary

✅ **DO**: Use smart casting after type-checking matchers  
❌ **DON'T**: Manually cast after matchers that provide smart casting  
💡 **IDE Hint**: IntelliJ will highlight unnecessary casts with "Cast is never succeeds" or "Unnecessary cast" warning

---

## Matchers with Smart Casting Support

| Matcher | Smart Casting | Return Type | Package |
|---------|---------------|-------------|---------|
| `shouldBeInstanceOf<T>()` | ✅ Yes | `T` | `io.kotest.matchers.types` |
| `shouldBeLeft()` | ✅ Yes | `L` (left value) | `io.kotest.assertions.arrow.core` |
| `shouldBeRight()` | ✅ Yes | `R` (right value) | `io.kotest.assertions.arrow.core` |
| `shouldNotBeNull()` | ✅ Yes | Non-null type | `io.kotest.matchers` |
| `shouldBeTypeOf<T>()` | ✅ Yes | `T` | `io.kotest.matchers.types` |

---

## Pattern 1: `shouldBeInstanceOf<T>()`

### ✅ Correct Usage (Smart Cast)

```kotlin
val state = viewModel.uiState.value
state.shouldBeInstanceOf<PokemonListUiState.Content>()
// ✅ 'state' is smart-cast to PokemonListUiState.Content
state.pokemons.size shouldBe 1
state.pokemons[0].name shouldBe "Bulbasaur"
```

### ❌ Wrong Usage (Unnecessary Manual Cast)

```kotlin
val state = viewModel.uiState.value
state.shouldBeInstanceOf<PokemonListUiState.Content>()
val content = state as PokemonListUiState.Content  // ❌ UNNECESSARY
content.pokemons.size shouldBe 1
```

**Why wrong**: After `shouldBeInstanceOf`, the variable is already smart-cast by the compiler. Manual cast is redundant.

---

## Pattern 2: `shouldBeLeft()` / `shouldBeRight()` (Arrow Either)

### ✅ Correct Usage (Smart Cast)

```kotlin
val result = repository.getJobs()
val error = result.shouldBeLeft()
// ✅ 'error' is extracted and typed as the left value
error.shouldBeInstanceOf<RepoError.Http>()
// ✅ 'error' is smart-cast to RepoError.Http
error.code shouldBe 404
```

```kotlin
val result = repository.getJobs()
val page = result.shouldBeRight()
// ✅ 'page' is extracted and typed as the right value
page.pokemons.size shouldBe 20
```

### ❌ Wrong Usage (Manual Cast)

```kotlin
val result = repository.getJobs()
result.shouldBeLeft()
val error = result as Left<RepoError>  // ❌ UNNECESSARY - use shouldBeLeft() return value
```

**Why wrong**: `shouldBeLeft()` and `shouldBeRight()` **return the extracted value** from the `Either`. Use the return value directly.

---

## Pattern 3: `shouldNotBeNull()`

### ✅ Correct Usage (Smart Cast)

```kotlin
val name: String? = user.getName()
name.shouldNotBeNull()
// ✅ 'name' is smart-cast to String (non-null)
name.length shouldBe 10
name.uppercase() shouldBe "JOHN DOE"
```

### ❌ Wrong Usage (Unnecessary Safe Call or Cast)

```kotlin
val name: String? = user.getName()
name.shouldNotBeNull()
name?.length shouldBe 10  // ❌ UNNECESSARY - name is non-null after assertion
(name as String).length shouldBe 10  // ❌ UNNECESSARY - smart cast works
```

**Why wrong**: After `shouldNotBeNull()`, the compiler knows the variable is non-null. Safe calls (`?.`) and manual casts are redundant.

---

## Pattern 4: `shouldBeTypeOf<T>()`

### ✅ Correct Usage (Smart Cast)

```kotlin
val animal: Animal = getAnimal()
animal.shouldBeTypeOf<Dog>()
// ✅ 'animal' is smart-cast to Dog
animal.bark()
animal.breed shouldBe "Labrador"
```

### ❌ Wrong Usage (Unnecessary Manual Cast)

```kotlin
val animal: Animal = getAnimal()
animal.shouldBeTypeOf<Dog>()
val dog = animal as Dog  // ❌ UNNECESSARY
dog.bark()
```

**Why wrong**: `shouldBeTypeOf` provides smart casting just like `shouldBeInstanceOf`.

---

## Why Smart Casting Works

### Kotlin Compiler Contracts

Kotest matchers use Kotlin's `contract` feature to inform the compiler about type guarantees:

```kotlin
inline fun <reified T> Any?.shouldBeInstanceOf(): T {
    contract {
        returns() implies (this@shouldBeInstanceOf is T)
    }
    // assertion logic...
    return this as T
}
```

The `contract` tells the compiler: "If this function returns normally (doesn't throw), the receiver is guaranteed to be of type `T`."

### Smart Cast After Assertion

After a successful type-checking assertion, the Kotlin compiler **automatically narrows the type** of the variable. This is called "smart casting."

```kotlin
val value: Any = getSomeValue()
value.shouldBeInstanceOf<String>()
// Compiler now knows: value is String
// No manual cast needed!
value.length  // ✅ Works directly
```

---

## IDE Hints for Unnecessary Casts

IntelliJ IDEA will highlight unnecessary casts with warnings:

- **"Unnecessary cast"** - The cast is redundant after smart casting
- **"Cast never succeeds"** - Type is already guaranteed
- **Warning underline** - Yellow/gray squiggle under the cast expression

**Action**: If you see these warnings after `shouldBeInstanceOf` or similar matchers, remove the cast!

---

## Common Violations Found in Codebase

### Violation 1: Manual Cast After `shouldBeInstanceOf`

**File**: `PokemonListViewModelTest.kt` (Fixed)

**Before**:
```kotlin
val state = viewModel.uiState.value
state.shouldBeInstanceOf<PokemonListUiState.Content>()
val content = state as PokemonListUiState.Content  // ❌
content.pokemons.size shouldBe 1
```

**After**:
```kotlin
val state = viewModel.uiState.value
state.shouldBeInstanceOf<PokemonListUiState.Content>()
state.pokemons.size shouldBe 1  // ✅ Smart cast
```

### Violation 2: Ignoring Return Value of `shouldBeLeft`/`shouldBeRight`

**Wrong**:
```kotlin
result.shouldBeRight()
val value = result.getOrNull()  // ❌ Unnecessarily extracting again
```

**Correct**:
```kotlin
val value = result.shouldBeRight()  // ✅ Already extracted and typed
```

---

## Examples from Real Tests

### Repository Test (Correct Usage)

From `PokemonListRepositoryTest.kt`:

```kotlin
"should return Http error on ClientRequestException (4xx)" {
    coEvery { mockApi.getPokemonList(20, 0) } throws mockk<HttpResponse>(relaxed = true).let {
        ClientRequestException(it, "Not Found")
    }
    
    val result = repository.loadPage()
    
    val error = result.shouldBeLeft()  // ✅ Extracts and types left value
    error.shouldBeInstanceOf<RepoError.Http>()  // ✅ Assertion + smart cast
    error.code shouldBe 404  // ✅ Direct property access via smart cast
}
```

**Pattern**:
1. `shouldBeLeft()` extracts the error value
2. `shouldBeInstanceOf()` asserts type and enables smart cast
3. Direct property access without manual casting

### ViewModel Test (Corrected Usage)

From `PokemonListViewModelTest.kt` (after fixes):

```kotlin
"loadInitialPage should emit Content on success" {
    val pokemon = Pokemon(1, "Bulbasaur", "https://example.com/1.png")
    val page = PokemonPage(listOf(pokemon), hasMore = true)
    
    coEvery { mockRepository.loadPage(20, 0) } returns Either.Right(page)
    
    viewModel.loadInitialPage()
    Thread.sleep(100)
    
    val state = viewModel.uiState.value
    state.shouldBeInstanceOf<PokemonListUiState.Content>()  // ✅ Smart cast
    state.pokemons.size shouldBe 1  // ✅ Direct access
    state.pokemons[0].name shouldBe "Bulbasaur"
}
```

---

## Checklist for Code Reviews

When reviewing test code, check for:

- [ ] No manual casts after `shouldBeInstanceOf<T>()`
- [ ] No manual casts after `shouldBeTypeOf<T>()`
- [ ] Using return value of `shouldBeLeft()` / `shouldBeRight()`
- [ ] No safe calls (`?.`) after `shouldNotBeNull()`
- [ ] No manual casts highlighted by IDE
- [ ] Direct property/method access after type assertions

---

## Summary

**Golden Rule**: If a Kotest matcher provides a return value or enables smart casting through contracts, **use it directly**. Don't add manual casts.

| Scenario | Solution |
|----------|----------|
| Need to check type and access properties | Use `shouldBeInstanceOf<T>()` + smart cast |
| Need to extract Either left/right value | Use return value of `shouldBeLeft()`/`shouldBeRight()` |
| Need to assert non-null | Use `shouldNotBeNull()` + smart cast |
| IDE shows "Unnecessary cast" | Remove the cast - trust smart casting |

**References**:
- Kotest Type Matchers: https://kotest.io/docs/assertions/core-matchers.html#type-matchers
- Kotlin Contracts: https://kotlinlang.org/docs/whatsnew13.html#contracts
- Arrow Documentation: https://arrow-kt.io/learn/quickstart/

---

## Troubleshooting Common Testing Issues

### Tests Pass But Build Shows Failures

**Symptom:**
```
> Task :features:pokemonlist:wiring-ui-unstyled:compileDebugKotlinAndroid FAILED
BUILD SUCCESSFUL in 1m 23s
All 84 tests PASSED
```

**Cause:** `--continue` flag allows tests to run despite task failures.

**Interpretation:**
- Task failures shown are from earlier in build
- Tests actually passed (verify with explicit test run)
- Subsequent clean build resolves stale task states

**Solution:** Run explicit test verification:
```bash
./gradlew test --rerun-tasks
```

---

### Validation Commands

```bash
# Run all tests
./gradlew test --continue

# Record screenshots
./gradlew recordRoborazziDebug

# Verify screenshots
./gradlew verifyRoborazziDebug
```

### Property-Based Coverage Targets

| Code Type | Coverage Target |
|-----------|----------------|
| Mappers | 100% |
| Repositories | 40-50% |
| ViewModels | 30-40% |
| Validators | 60-80% |
