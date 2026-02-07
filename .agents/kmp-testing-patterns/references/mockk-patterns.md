# MockK Patterns

Last Updated: February 6, 2026

Complete guide for MockK mocking patterns in Kotlin Multiplatform tests.

## Quick Summary

MockK is the mocking framework for JVM-based tests. Use it for mocking repositories, API services, and external dependencies.

---

## Basic Mocking

### Creating Mocks

```kotlin
// Simple mock
val mockApi = mockk<MyApiService>()

// Relaxed mock (returns default values)
val mockApi = mockk<MyApiService>(relaxed = true)

// Mock with default answer
val mockApi = mockk<MyApiService>(relaxUnitFun = true)
```

### Mocking Functions

```kotlin
// Mock regular function
val mockApi = mockk<MyApiService>()
every { mockApi.getData() } returns "data"

// Mock suspend function
coEvery { mockApi.fetchData() } returns "data"

// Mock function with parameters
every { mockApi.getUser(1) } returns User(1, "John")

// Mock any parameter
every { mockApi.getUser(any()) } returns User(0, "Default")
```

---

## Answering Function Calls

### Return Values

```kotlin
// Return specific value
every { mockApi.getData() } returns "result"

// Return different values on multiple calls
every { mockApi.getData() } returnsMany listOf("first", "second", "third")

// Return based on arguments
every { mockApi.getUser(any()) } answers { firstArg<Int>() }
every { mockApi.getUser(1) } returns User(1, "John")
every { mockApi.getUser(2) } returns User(2, "Jane")
```

### Throwing Exceptions

```kotlin
// Throw exception
every { mockApi.getData() } throws IOException("Network error")

// Throw exception for suspend function
coEvery { mockApi.fetchData() } throws TimeoutCancellationException()

// Throw exception for specific arguments
every { mockApi.getUser(999) } throws NotFoundException()
```

### Complex Answers

```kotlin
// Answer with lambda
every { mockApi.getUser(any()) } answers {
    val id = firstArg<Int>()
    User(id, "User$id")
}

// Access call information
every { mockApi.log(any(), any()) } answers {
    val (message, level) = listOf(firstArg<String>(), secondArg<String>())
    // Process arguments
}
```

---

## Verification

### Verify Function Calls

```kotlin
// Verify function was called
verify { mockApi.getData() }

// Verify was called exactly once
verify(exactly = 1) { mockApi.getData() }

// Verify was never called
verify(exactly = 0) { mockApi.getData() }

// Verify was called at least/at most n times
verify(atLeast = 2) { mockApi.getData() }
verify(atMost = 5) { mockApi.getData() }

// Verify order of calls
verifyOrder {
    mockApi.initialize()
    mockApi.loadData()
}
```

### Verify with Arguments

```kotlin
// Verify specific argument
verify { mockApi.getUser(1) }

// Verify any argument
verify { mockApi.getUser(any()) }

// Verify multiple arguments
verify { mockApi.search(any(), any(), eq("exact")) }

// Verify with matching
verify { mockApi.search(match { it.length > 3 }, any()) }
```

### Capture Arguments

```kotlin
val slot = slot<User>()
every { mockApi.saveUser(capture(slot)) } returns Unit

mockApi.saveUser(User(1, "John"))

verify { mockApi.saveUser(any()) }
slot.captured.id shouldBe 1
slot.captured.name shouldBe "John"
```

---

## Capturing Verification Order

```kotlin
val order = mutableListOf<String>()

every { mockApi.method1() } answers { order.add("method1") }
every { mockApi.method2() } answers { order.add("method2") }

// ... call methods ...

order shouldBe listOf("method1", "method2")
```

---

## Mocking Properties

```kotlin
// Mock property getter
every { mockApi.token } returns "abc123"

// Mock property setter
every { mockApi.token = any() } just Runs

// Verify property access
verify { mockApi.token }
verify { mockApi.token = any() }
```

---

## Mocking Suspend Functions

```kotlin
class RepositoryTest : StringSpec({
    lateinit var mockApi: MyApiService
    lateinit var repository: MyRepository

    beforeTest {
        mockApi = mockk()
        repository = MyRepository(mockApi)
    }

    "should fetch data" {
        // Mock suspend function
        coEvery { mockApi.fetchData() } returns "data"

        // Call suspend function in test
        val result = repository.getData()

        result shouldBe "data"

        // Verify suspend function was called
        coVerify { mockApi.fetchData() }
    }

    "should handle timeout" {
        // Mock suspend function to throw
        coEvery { mockApi.fetchData() } throws TimeoutCancellationException()

        val result = repository.getData()

        result.shouldBeNull()

        // Verify call with timeout
        coVerify(timeout = 1000) { mockApi.fetchData() }
    }
})
```

---

## Mocking Objects and Singletons

```kotlin
// MockkObject - mock methods on a real object
object Logger {
    fun log(message: String) { /* real implementation */ }
}

mockkObject(Logger)
every { Logger.log(any()) } just Runs

// Reset after test
unmockkObject(Logger)
```

---

## Relaxed Mocks

```kotlin
// Relaxed mock returns default values
val mockApi = mockk<MyApiService>(relaxed = true)

// Functions return:
// - String -> ""
// - Int -> 0
// - Boolean -> false
// - List -> emptyList()
// - Custom objects -> mocked instance
```

---

## Partial Mocks

```kotlin
// Spy - real implementation for some methods
val api = spyk(MyApiService())
every { api.getToken() } returns "test-token"

// getToken() is mocked
api.getToken() shouldBe "test-token"

// Other methods use real implementation
api.realMethod()
```

---

## Clearing Mocks

```kotlin
// Clear specific mock
clearMocks(mockApi)

// Clear all mocks
clearAllMocks()

// Clear specific answers
clearMocks(mockApi, answers = false, calls = true)
```

---

## Common Patterns

### Repository Test Pattern

```kotlin
class RepositoryTest : StringSpec({
    lateinit var mockApi: MyApiService
    lateinit var repository: MyRepository

    beforeTest {
        mockApi = mockk()
        repository = MyRepository(mockApi)
    }

    "should return data on success" {
        // Arrange
        val dto = MyDto(1, "data")
        coEvery { mockApi.fetch(1) } returns dto

        // Act
        val result = repository.getById(1)

        // Assert
        result.shouldBeRight { data ->
            data.id shouldBe 1
            data.name shouldBe "data"
        }

        // Verify
        coVerify { mockApi.fetch(1) }
    }

    "should handle error" {
        // Arrange
        coEvery { mockApi.fetch(999) } throws ClientRequestException(...)

        // Act
        val result = repository.getById(999)

        // Assert
        result.shouldBeLeft(RepoError.NotFound)

        // Verify
        coVerify { mockApi.fetch(999) }
    }
})
```

### ViewModel Test Pattern

```kotlin
class ViewModelTest : StringSpec({
    lateinit var mockRepository: MyRepository
    lateinit var testScope: TestScope
    lateinit var viewModel: MyViewModel

    beforeTest {
        mockRepository = mockk()
        testScope = TestScope()
        viewModel = MyViewModel(mockRepository, testScope)
    }

    "should load data on start" {
        // Arrange
        val data = listOf(Item(1, "item"))
        coEvery { mockRepository.getItems() } returns Either.Right(data)

        // Act
        viewModel.uiState.test {
            awaitItem() shouldBe Loading

            viewModel.onStart(TestLifecycleOwner())
            testScope.advanceUntilIdle()

            // Assert
            awaitItem().shouldBeInstanceOf<Content> { state ->
                state.items.size shouldBe 1
            }

            cancelAndIgnoreRemainingEvents()
        }

        // Verify
        coVerify { mockRepository.getItems() }
    }
})
```

---

## Best Practices

### 1. Use coEvery for Suspend Functions

```kotlin
// ✅ CORRECT
coEvery { mockApi.fetchData() } returns "data"
coVerify { mockApi.fetchData() }

// ❌ WRONG
every { mockApi.fetchData() } returns "data"
verify { mockApi.fetchData() }
```

### 2. Be Specific with Verification

```kotlin
// ✅ GOOD - Verify specific call
coVerify { mockApi.getUser(1) }

// ❌ BAD - Verify any call
coVerify { mockApi.getUser(any()) }
```

### 3. Use Relaxed Mocks for Unimportant Dependencies

```kotlin
// ✅ GOOD - Logger behavior not important for test
val mockLogger = mockk<Logger>(relaxed = true)
val repository = MyRepository(mockApi, mockLogger)
```

### 4. Capture Arguments for Complex Assertions

```kotlin
val capturedUser = slot<User>()
every { mockApi.saveUser(capture(capturedUser)) } returns Unit

repository.createUser("John")

capturedUser.captured.name shouldBe "John"
capturedUser.captured.id shouldNotBe 0 // Generated ID
```

### 5. Don't Over-Mock

```kotlin
// ❌ BAD - Testing implementation details
coVerify(exactly = 1) { repository.cache.get(1) }
coVerify(exactly = 2) { repository.cache.put(1, any()) }

// ✅ GOOD - Test observable behavior
result.shouldBeRight { it.id shouldBe 1 }
```

---

## Common Pitfalls

### Don't Mock Final Classes

```kotlin
// ❌ WRONG
class MyService { /* ... */ }
val mock = mockk<MyService>() // Error!

// ✅ CORRECT - Use interface
interface MyService { /* ... */ }
val mock = mockk<MyService>()
```

### Don't Mock Value Classes

```kotlin
// ❌ WRONG - Value classes don't work well with mocks
@JvmInline
value class UserId(val value: Int)

// ✅ CORRECT - Mock the containing class
every { mockApi.getUser(UserId(1)) } returns user
```

### Don't Use Mocks for Testing Pure Functions

```kotlin
// ❌ WRONG - Don't mock pure functions
val mockCalculator = mockk<Calculator>()
every { mockCalculator.add(1, 2) } returns 3

// ✅ CORRECT - Test pure functions directly
Calculator.add(1, 2) shouldBe 3
```

---

## Reference Implementations

- `features/pokemonlist/data/src/androidUnitTest/kotlin/.../PokemonListRepositoryTest.kt`
- `features/pokemonlist/presentation/src/androidUnitTest/kotlin/.../PokemonListViewModelTest.kt`
