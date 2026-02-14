# Kotest Smart Casting Patterns

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
