# Testing Strategy Guidelines: Mobile-First Approach

**Last Updated:** December 22, 2025

**Purpose**: Define a mobile-first testing strategy that maximizes testing capabilities within Kotlin Multiplatform framework limitations.

## ⚠️ CRITICAL: Test Enforcement

**NO CODE WITHOUT TESTS**

### Core Rule

Every production code file MUST have a corresponding test file. Tests are not optional—they are part of the feature implementation.

**Enforcement Table:**

| Production Code Type | Test Required | Test Location | Framework |
|---------------------|---------------|---------------|-----------|
| Repository | ✅ MANDATORY | androidUnitTest/ | Kotest + MockK |
| ViewModel | ✅ MANDATORY | androidUnitTest/ | Kotest + MockK |
| Mapper (DTO ↔ Domain) | ✅ MANDATORY | androidUnitTest/ | Kotest properties |
| Use Case | ✅ MANDATORY | androidUnitTest/ | Kotest + MockK |
| API Service | ✅ MANDATORY | androidUnitTest/ | Kotest + MockK |
| @Composable UI | ✅ MANDATORY | @Preview + Screenshot | Roborazzi |
| Simple Utility | ✅ MANDATORY | commonTest/ | kotlin-test |
| Platform-specific | ✅ MANDATORY | iosTest/androidUnitTest | kotlin-test |

**Minimum Coverage Requirements:**
- Repositories: Success + all error types (Network, Http, Unknown)
- ViewModels: Initial, Loading, Success, Error states + Events
- Mappers: Property-based tests proving data preservation
- @Composable: At least one realistic @Preview

**Automatic Rejection:**
- ❌ Repository without tests
- ❌ ViewModel without tests
- ❌ Mapper without property-based tests
- ❌ @Composable without @Preview
- ❌ Modified code without updated tests

## 🎯 Property-Based Testing: Primary Strategy

**CRITICAL PRINCIPLE: Favor property-based tests over concrete examples**

### Why Property-Based Testing?

1. **1000x More Coverage**: One property test = 1000 concrete examples (default iterations)
2. **Finds Edge Cases**: Discovers bugs concrete tests miss
3. **Self-Documenting**: Properties express invariants clearly
4. **Less Maintenance**: One property test replaces dozens of concrete tests
5. **Regression Protection**: Random data catches future breaking changes

### Project Metrics (Real Implementation)

**Current Property Test Coverage:**
- **Total Property Tests**: 34 tests across ViewModel and Repository layers
- **Generated Scenarios**: 34,000+ test cases per test run (34 tests × ~1000 iterations)
- **Coverage Ratio**: ~40% property tests, ~60% concrete tests (documentation + edge cases)
- **Coverage Multiplier**: 1000× more scenarios than concrete tests

**Example Implementations:**
- [PokemonListViewModelTest.kt#L235-240](../../features/pokemonlist/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModelTest.kt#L235-240) — HTTP error code property test (400-599 range)
- [PokemonDetailViewModelTest.kt#L272-310](../../features/pokemondetail/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModelTest.kt#L272-310) — Success state property tests
- [PokemonDetailViewModelTest.kt#L315-375](../../features/pokemondetail/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModelTest.kt#L315-375) — Error handling property tests
- [PokemonDetailViewModelTest.kt#L404-420](../../features/pokemondetail/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModelTest.kt#L404-420) — ViewModel ID scoping property test

### When to Use Property-Based Tests

**ALWAYS use property tests for:**
- ✅ **Mappers** (DTO ↔ Domain): Data preservation, transformations
- ✅ **Repositories**: HTTP error codes (400-599), pagination parameters
- ✅ **ViewModels**: State transitions, event handling across ranges
- ✅ **Parsers/Validators**: URL parsing, ID extraction, format validation
- ✅ **JSON serialization**: Round-trip consistency
- ✅ **Math/String utilities**: Commutative, associative, identity properties

**Use concrete tests ONLY for:**
- 📚 **Documentation examples**: Clear, specific scenarios for developers
- 🎯 **Edge cases**: Specific failure modes that need explicit demonstration
- 🔧 **Complex setups**: When property test setup is more complex than the test itself

### Property Test Examples

**See real implementations:**
- [PokemonListViewModelTest.kt#L235-240](../../features/pokemonlist/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModelTest.kt#L235-240) — HTTP error property test
- [PokemonDetailViewModelTest.kt#L272-310](../../features/pokemondetail/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModelTest.kt#L272-310) — Success state properties
- [PokemonDetailViewModelTest.kt#L315-375](../../features/pokemondetail/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModelTest.kt#L315-375) — Error handling properties

### Kotest Property Testing Basics

**Core API:**
- `checkAll(Arb.type())` — Runs assertions, fails on first failure
- `forAll(Arb.type())` — Returns boolean for flexible composition
- Common generators: `Arb.int(range)`, `Arb.string(length)`, `Arb.list()`, `.orNull()`

**See complete examples:**
- [PokemonDetailViewModelTest.kt](../../features/pokemondetail/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModelTest.kt) — Multiple property test patterns
- [Kotest Property Testing Docs](https://kotest.io/docs/proptest/property-based-testing.html)

### Guidelines for Removing Redundant Tests

**Before removing a concrete test, verify:**

1. ✅ Property test covers the SAME scenario with broader range
2. ✅ Property test runs 1000+ iterations (default)
3. ✅ Property test assertions are equivalent or stronger
4. ✅ No unique setup/edge case in concrete test
5. ✅ Documentation value is captured in property test name

**Keep concrete tests if:**
- 📚 Provides clear documentation for developers
- 🎯 Tests specific edge case not covered by property range
- 🔧 Setup complexity makes property test impractical

**Example: What to Keep vs Remove**

**Keep:** Edge cases, documentation examples with clear setup
**Remove:** Tests fully covered by property test ranges

**See decision in practice:**
- [PokemonDetailViewModelTest.kt](../../features/pokemondetail/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModelTest.kt) — Mix of property tests (HTTP codes 400-599) + concrete edge cases (specific failure modes)

### Measuring Property Test Coverage

**Target Metrics:**
- 🎯 **30-40% of tests** should be property-based
- 🎯 **60-70% of tests** should be concrete (documentation/edge cases)
- 🎯 **15+ redundant tests removed** per feature module cleanup

**Example Project Stats:**
```
Total Tests: 84
Property Tests: 34 (40%)
Concrete Tests: 50 (60%)
Scenarios per run: 34,000+ (34 property tests × 1000 iterations)
```

## 🌊 Flow Testing with Turbine

**CRITICAL: Use Turbine for testing StateFlow/SharedFlow/Flow**

### Why Turbine?

1. **Deterministic**: Works with TestDispatcher for controlled time
2. **Expressive**: `awaitItem()`, `skipItems()`, `cancelAndIgnoreRemainingEvents()`
3. **No Thread.sleep()**: Fast, predictable tests
4. **Flow-specific**: Built for Kotlin coroutines Flow testing

### Setup

**Dependencies:**
- See [gradle/libs.versions.toml](../../gradle/libs.versions.toml) for current Turbine version
- Add to `androidUnitTest.dependencies` in feature module `build.gradle.kts`

### ViewModel Flow Testing Pattern

**Key Pattern:** Inject `testScope` into ViewModel, use Turbine `.test { }` for flow assertions, advance time with `testDispatcher.scheduler.advanceUntilIdle()`.

**Complete implementations:**
- [PokemonListViewModelTest.kt](../../features/pokemonlist/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModelTest.kt) — Standard flow testing with state transitions
- [PokemonDetailViewModelTest.kt](../../features/pokemondetail/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModelTest.kt) — Parametric ViewModel + property tests

### Turbine API Essentials

**Core Methods:**
- `awaitItem()` — Get next emission (fails if none)
- `skipItems(n)` — Skip n emissions  
- `expectNoEvents()` — Assert no emissions
- `cancelAndIgnoreRemainingEvents()` — Clean teardown (always call at end)

**Pattern:** `flow.test { /* assertions */ }`

**See in practice:** [PokemonListViewModelTest.kt](../../features/pokemonlist/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModelTest.kt#L61-82)

### ⚠️ Forbidden: Thread.sleep() in Tests

```kotlin
// ❌ NEVER DO THIS
"test with delay" {
    viewModel.load()
    Thread.sleep(1000)  // ❌ Slow, flaky, bad practice
    viewModel.uiState.value shouldBe expected
}

// ✅ USE TURBINE + TEST DISPATCHER
"test with flow" {
    viewModel.uiState.test {
        viewModel.load()
        testDispatcher.scheduler.advanceUntilIdle()  // ✅ Fast, deterministic
        awaitItem() shouldBe expected
        cancelAndIgnoreRemainingEvents()
    }
}
```

### TestDispatcher Setup (No Dispatchers.setMain needed)

```kotlin
// ✅ CORRECT: Inject test scope into ViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest : StringSpec({
    val testDispatcher = StandardTestDispatcher()
    val testScope = TestScope(testDispatcher)
    
    beforeTest {
        mockRepository = mockk(relaxed = true)
    }
    // NO afterTest needed - no Dispatchers.setMain/resetMain
    
    "test" {
        // Pass testScope to ViewModel constructor
        val vm = MyViewModel(mockRepository, testScope)
        
        vm.uiState.test {
            vm.doSomething()
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() shouldBe expected
            cancelAndIgnoreRemainingEvents()
        }
    }
})

// ViewModel must accept CoroutineScope parameter
class MyViewModel(
    private val repository: MyRepository,
    viewModelScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )
) : ViewModel(viewModelScope) {
    // ViewModel uses injected scope
}
```

**Why no Dispatchers.setMain?**
- ✅ ViewModel receives test scope via constructor
- ✅ ViewModel uses injected scope, not Dispatchers.Main
- ✅ Cleaner test setup (no beforeTest/afterTest boilerplate)
- ✅ Better encapsulation (ViewModel doesn't depend on global state)

## Strategic Decision: Mobile-First Testing

**Primary Testing Location: `androidUnitTest/` source sets**

### Framework Limitations
- ❌ **Kotest**: Does NOT support iOS/Native targets (JVM only)
- ❌ **MockK**: Does NOT support iOS/Native targets (JVM only)
- ✅ **kotlin-test**: Multiplatform support (basic assertions only)

### Mobile-First Rationale
1. **Android/iOS = Primary mobile targets** - Core product focus
2. **iOS shares identical Kotlin code** - Type safety guarantees compatibility
3. **Testing on Android validates ALL shared logic** - Same code runs on iOS
4. **Fast feedback** - Android unit tests run on JVM in seconds
5. **Full framework support** - Kotest + MockK available

### Trade-off Analysis

| Aspect | androidUnitTest/ | commonTest/ | iosTest/ |
|--------|-------------|-------------|---------|
| **Test Framework** | ✅ Full Kotest | ⚠️ kotlin-test only | ⚠️ kotlin-test only |
| **Mocking** | ✅ MockK | ❌ None | ❌ None (use fakes) |
| **Primary Use** | ✅ Business logic | ⚠️ Simple utilities | ⚠️ Platform code |
| **Speed** | ✅ Fast (JVM) | ✅ Fast | ❌ Slow (Native) |
| **Coverage** | ✅ Complete | ⚠️ Partial | ⚠️ Platform-specific |
| **iOS Validation** | ✅ Type safety | ✅ Type safety | ✅ Direct |

**Conclusion**: Place ALL business logic tests in `androidUnitTest/` for maximum testing power.

## Frameworks

### Primary (androidUnitTest/)
- **Kotest** - Full framework, specs, assertions, property-based testing
- **MockK** - Powerful mocking and stubbing
- **Roborazzi** - Compose UI screenshot testing (Robolectric-based)
- **kotlinx-coroutines-test** - Coroutine testing utilities

### Minimal (commonTest/)
- **kotlin-test** - Basic assertions only
- **kotlinx-coroutines-test** - Test dispatchers
- Use for: Simple utilities with NO dependencies

### Rare (iosTest/)
- **kotlin-test** - Basic assertions
- Use for: Platform-specific code (expect/actual implementations only)
- Use fakes instead of mocks

## Gradle Setup (Mobile-First)

### Feature Module Structure
```kotlin
// features/pokemonlist/impl/build.gradle.kts
kotlin {
    androidTarget()
    jvm()
    iosArm64()
    iosSimulatorArm64()
    
    sourceSets {
        // Common: Basic testing only
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        
        // Android: PRIMARY testing location for business logic
        androidUnitTest.dependencies {
            implementation(libs.kotest.assertions)
            implementation(libs.kotest.framework)
            implementation(libs.kotest.property)
            implementation(libs.mockk)
            implementation(libs.turbine)  // Flow testing
            implementation(libs.kotlinx.coroutines.test)
        }
        
        // JVM: Full capabilities (Desktop testing)
        jvmTest.dependencies {
            implementation(libs.kotest.assertions)
            implementation(libs.kotest.framework)
            implementation(libs.kotest.property)
            implementation(libs.mockk)
            // Screenshot testing
            implementation(libs.roborazzi)
            implementation(libs.roborazzi.compose)
        }
        
        // iOS: Platform-specific code only
        iosTest.dependencies {
            // Only kotlin-test, use fakes
        }
    }
}
```

### Notes
- **Kotest requires JUnit Platform** - Ensure `useJUnitPlatform()` is configured for Android/JVM test tasks
- **MockK is JVM-only** - Not available for Native/iOS tests
- **Roborazzi for screenshots** - JVM-based, works on Android tests

## Testing by Source Set

### androidUnitTest/ - PRIMARY (Business Logic)

**What to test here:**
- ✅ Repositories
- ✅ ViewModels
- ✅ Mappers (DTO ↔ Domain)
- ✅ Use cases (if complex orchestration)
- ✅ API services (with mocked responses)

**Directory structure:**
```
features/pokemonlist/data/src/androidUnitTest/kotlin/
├── data/
│   ├── PokemonRepositoryTest.kt      // Repository with mocked API
│   ├── PokemonMappersTest.kt         // Property-based mapper tests
│   └── PokemonApiServiceTest.kt      // API service tests
└── presentation/
    └── PokemonViewModelTest.kt       // ViewModel state/events
```

**Available tools:**
- ✅ Full Kotest (all specs, assertions, property testing)
- ✅ MockK for powerful mocking
- ✅ Coroutines test utilities
- ✅ Fast feedback (JVM-based)

### commonTest/ - MINIMAL (Simple Utilities)

**What to test here:**
- ✅ Pure functions with NO dependencies
- ✅ Extension functions on primitives
- ✅ URL parsing, string manipulation
- ✅ Math utilities, formatters

**Directory structure:**
```
features/pokemonlist/impl/src/commonTest/kotlin/
└── utils/
    ├── StringUtilsTest.kt           // String extensions
    └── UrlUtilsTest.kt              // URL parsing
```

**Available tools:**
- ✅ kotlin-test only (basic assertions)
- ❌ No Kotest
- ❌ No MockK

**Rule**: If it needs mocking or complex assertions, put it in `androidUnitTest/`

### iosTest/ - RARE (Platform-Specific)

**What to test here:**
- ✅ Platform-specific implementations (expect/actual)
- ✅ iOS-specific APIs
- ✅ Native interop code

**Directory structure:**
```
features/pokemonlist/impl/src/iosTest/kotlin/
└── IOSPlatformTest.kt               // Platform implementations
```

**Available tools:**
- ✅ kotlin-test only
- ❌ No Kotest
- ❌ No MockK (use fakes)

## Enforcement Workflow

### When Creating New Code

1. **Before starting**: Plan both production and test files
2. **During development**: Write test alongside production code (TDD encouraged)
3. **Before PR**: Verify all production files have tests
4. **PR Review**: Tests are reviewed with same scrutiny as production code

### When Modifying Existing Code

1. **Check for tests**: Verify test file exists
2. **Update tests**: Modify tests to reflect changes
3. **Add missing tests**: If tests don't exist, add them NOW
4. **Run tests**: Ensure all tests pass before committing

### Exceptions to Test Requirement

**Only the following do NOT require tests:**

1. **Data classes** (no logic, just structure)
2. **Constants** (no behavior to test)
3. **Simple enums** (unless complex logic)
4. **Sealed interfaces** (contracts only)

**Everything else REQUIRES tests.**

### Test Quality Standards

Tests must:
- ✅ Be clear and readable (Given/When/Then or descriptive names)
- ✅ Test one thing per test case
- ✅ Use realistic test data (not empty/null unless testing that case)
- ✅ Verify actual behavior, not implementation details
- ✅ Be independent (no test order dependencies)
- ✅ Clean up resources (use beforeTest/afterTest)

**Forbidden Test Practices:**

❌ **DO NOT**:
- Skip tests with @Ignore without issue reference
- Use Thread.sleep() (use test dispatchers)
- Test private methods directly
- Mock everything (prefer fakes for simple cases)
- Write tests that always pass
- Copy-paste tests without understanding

## Test Execution & Caching

### Forced Test Execution: KMP Architecture

**Convention plugins automatically disable test caching for ALL test types** (JVM, KMP, Android, iOS):

```kotlin
// Applied in ALL KMP convention plugins
// 1. Configure AbstractTestTask - covers ALL test types
tasks.withType<AbstractTestTask>().configureEach {
    outputs.upToDateWhen { false }  // Force test re-execution
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
    }
}

// 2. Configure Test - JUnit Platform for standard JVM/Android tests
tasks.withType<Test>().configureEach {
    useJUnitPlatform()              // Enable Kotest runner
}
```

**Why Dual Configuration?**
- **AbstractTestTask**: Base class for ALL Gradle test tasks (standard JVM `Test`, KMP-generated `jvmTest`, `iosX64Test`, Android `testDebugUnitTest`)
- **Test**: Specific subclass for standard JVM test tasks that need JUnit Platform configuration
- KMP projects generate test tasks extending `AbstractTestTask` (not `Test`), so must configure both

**Task Type Hierarchy**:
```
AbstractTestTask (org.gradle.api.tasks.testing.AbstractTestTask)
├── Test (org.gradle.api.tasks.testing.Test)
│   └── Standard JVM test tasks
└── KotlinTest (KMP-generated, not public API)
    └── jvmTest, iosX64Test, etc.
```

**Why**: Tests should run on every invocation to catch regressions, even when source files haven't changed. Environmental factors, flaky tests, or external dependencies may cause failures that caching would hide.

**Additional Safety Net**: `gradle.properties` includes `org.gradle.caching.tests=false` as a global safeguard.

### Validation Commands

```bash
# PRIMARY: Build + ALL tests (always run together)
./gradlew :composeApp:assembleDebug test --continue

# Run all Android tests for a feature module
./gradlew :features:pokemonlist:impl:testDebugUnitTest

# Run common tests (utilities)
# Run all tests (if module has multiplatform test targets)
./gradlew :features:pokemonlist:data:allTests

# Run specific test class
./gradlew :features:pokemonlist:data:testDebugUnitTest --tests "PokemonListRepositoryTest"

# Verify test coverage
# Run with info logging
./gradlew :features:pokemonlist:data:testDebugUnitTest --info | grep "tests completed"

# Record screenshots
./gradlew recordRoborazziDebug

# Verify screenshots
./gradlew verifyRoborazziDebug

# Force re-run with Gradle flag (redundant with our config, but available)
./gradlew test --rerun-tasks
```

**Note**: With `outputs.upToDateWhen { false }` configured on `AbstractTestTask`, tests will NEVER show "UP-TO-DATE" status. They execute on every invocation across all platforms (JVM, Android, iOS).

## Conventions
- Name test classes with `Test` or `Spec` suffix
- Use package structure mirroring production code
- Use Kotest specs in androidTest: `StringSpec`, `BehaviorSpec`, `FunSpec`
- Use Given/When/Then comments or Kotest contexts for structure

## Smart Casting with Kotest Matchers

Kotest matchers provide smart casting through Kotlin compiler contracts. Never manually cast after type-checking assertions.

See [kotest_smart_casting_quick_ref.md](./kotest_smart_casting_quick_ref.md) for complete documentation.

## Complete Testing Examples

### Repository Test (androidTest/)

**Pattern:** Mock API service, test Either-based error handling, verify DTO-to-domain mapping.

**Complete implementation:**
- [PokemonListRepositoryTest.kt](../../features/pokemonlist/data/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/data/PokemonListRepositoryTest.kt) — Success/Network/Http error paths
- [PokemonDetailRepositoryTest.kt](../../features/pokemondetail/data/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/data/PokemonDetailRepositoryTest.kt) — Parametric repository with nested DTOs

**Key Patterns:**
- Use MockK for API services: `mockApi = mockk()`
- Test Either paths: `result.shouldBeRight { }` and `result.shouldBeLeft { }`
- Cover all error types: Network, Http (with codes), Unknown

### Property-Based Mapper Test (androidTest/)

**Pattern:** Use `checkAll()` with `Arb` generators to test data preservation invariants across 1000+ random inputs.

**Complete implementations:**
- [PokemonDetailRepositoryTest.kt#L77-95](../../features/pokemondetail/data/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/data/PokemonDetailRepositoryTest.kt#L77-95) — Property test for ID extraction from URLs
- [PokemonDetailViewModelTest.kt#L272-420](../../features/pokemondetail/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModelTest.kt#L272-420) — Multiple property tests for state transitions

**Key Principles:**
- 100% property test coverage for mappers (NO concrete tests for simple transformations)
- Test invariants: ID preservation, name capitalization, data completeness
- Use appropriate Arb generators: `Arb.int(range)`, `Arb.string(length)`

### ViewModel Test with Turbine (androidUnitTest/)

**Critical:** Always inject `SavedStateHandle()` in tests.

**Pattern:** Inject `testScope`, use Turbine for flow assertions, verify state transitions and event handling.

**Complete implementations:**
- [PokemonListViewModelTest.kt](../../features/pokemonlist/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModelTest.kt) — Pagination, loading states, error handling
- [PokemonDetailViewModelTest.kt](../../features/pokemondetail/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModelTest.kt) — Parametric ViewModel, lifecycle integration, property tests

**Key Principles:**
- Inject `SavedStateHandle()` in ViewModel constructor
- Inject `testScope` for deterministic coroutine testing
- Use Turbine `.test { }` for flow assertions
- Test: Initial state, Loading → Content/Error transitions, event handling
- NO `Dispatchers.setMain/resetMain` needed (testScope pattern)

### Simple Utility Test (commonTest/)

**Pattern:** Use `kotlin-test` for pure functions with no dependencies. Only for truly generic utilities.

**Rule:** If it needs mocking or Kotest features → use `androidUnitTest/` instead.

**Example structure:** Basic assertions with `assertEquals`, `assertNull`, `assertTrue`.

**Most tests belong in androidUnitTest/** — see Repository/ViewModel examples above for primary pattern.

## Property-Based Testing Guidelines

Use Kotest `checkAll`/`forAll` in **androidTest/** to validate invariants across generated inputs.

### Mapper Invariants
```kotlin
"property: mapper preserves all fields" {
    checkAll(Arb.pokemonDto()) { dto ->
        val domain = dto.toDomain()
        domain.id shouldBe dto.extractId()
        domain.name.lowercase() shouldBe dto.name.lowercase()
    }
}
```

### Value Object Laws
```kotlin
"property: capitalization is consistent" {
    checkAll(Arb.string(1..50)) { name ->
        val pokemon = Pokemon(1, name, "url")
        pokemon.name.first().isUpperCase() shouldBe true
    }
}
```

### Round-Trip Tests
```kotlin
"property: JSON round-trip preserves data" {
    checkAll(Arb.pokemon()) { pokemon ->
        val json = Json.encodeToString(pokemon)
        val decoded = Json.decodeFromString<Pokemon>(json)
        decoded shouldBe pokemon
    }
}
```

## Mocking Guidelines (MockK in androidTest/)

**Use MockK for:**
- ✅ API services
- ✅ Repositories (when testing ViewModels)
- ✅ Database DAOs
- ✅ External dependencies

**Example: Mocking API Service**
```kotlin
class PokemonApiServiceTest : StringSpec({
    lateinit var mockClient: HttpClient
    lateinit var apiService: PokemonListApiService
    
    beforeTest {
        mockClient = mockk()
        apiService = PokemonListApiService(mockClient)
    }
    
    "should make correct API request" {
        val mockResponse = mockk<HttpResponse>(relaxed = true)
        coEvery { 
            mockClient.get(any<String>()) { any() }
        } returns mockResponse
        
        apiService.getPokemonList(limit = 20, offset = 0)
        
        coVerify { 
            mockClient.get(
                urlString = withArg { 
                    it shouldContain "pokemon"
                    it shouldContain "limit=20"
                    it shouldContain "offset=0"
                }
            )
        }
    }
})
```

**MockK Relaxed Mode:**
```kotlin
val mockApi = mockk<PokemonListApiService>(relaxed = true)
// Returns default values, useful for testing flows
```

## Repositories and Arrow Either

Test Either returns using helper extensions:

```kotlin
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight

"repository returns Right on success" {
    coEvery { mockApi.getPokemonList(any(), any()) } returns mockDto
    
    val result = repository.loadPage()
    
    // Returns unwrapped PokemonPage - no casting needed
    val page = result.shouldBeRight()
    page.pokemons shouldNotBeEmpty()
}

"repository returns Left on error" {
    coEvery { mockApi.getPokemonList(any(), any()) } throws IOException()
    
    val result = repository.loadPage()
    
    // Returns unwrapped RepoError - no casting needed
    val error = result.shouldBeLeft()
    error shouldBe RepoError.Network
}
```
 - Consider including Arrow-specific matcher helpers from Kotest Arrow extensions:
```kotlin
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight

// ✅ PREFERRED: Kotest Arrow extensions (returns unwrapped value)
val user = result.shouldBeRight()
val error = result.shouldBeLeft()

// ❌ DEPRECATED: Custom extensions (old pattern)
fun <L, R> Either<L, R>.shouldBeRight(): R = this.getOrNull() ?: fail("Expected Right but was $this")
fun <L, R> Either<L, R>.shouldBeLeft(): L = this.swap().getOrNull() ?: fail("Expected Left but was $this")
```

## Screenshot Testing (Roborazzi)

Purpose
- Catch UI regressions by diffing rendered Compose UI against committed baselines.
- Runs on JVM using Robolectric (fast, device-free). Optional Desktop (Compose Desktop) targets are supported.

Setup
- Add dependencies shown in Gradle Setup above.
- Optionally configure the Roborazzi Gradle extension if generating tests from @Preview:
```kotlin
roborazzi {
  generateComposePreviewRobolectricTests {
    enable = true
  }
}
```

Running locally
```bash
# Record baselines (writes to build/outputs/roborazzi by default)
./gradlew recordRoborazziDebug

# Compare current vs baseline (generates diffs)
./gradlew compareRoborazziDebug

# Verify (fails build on diff)
./gradlew verifyRoborazziDebug

# Alternatively trigger through unit tests with properties:
./gradlew testDebugUnitTest -Proborazzi.test.record=true
./gradlew testDebugUnitTest -Proborazzi.test.compare=true
./gradlew testDebugUnitTest -Proborazzi.test.verify=true
```

Compose example (Robolectric)
```kotlin
@RunWith(AndroidJUnit4::class)
class HomeScreenScreenshotTest {
  @get:Rule val compose = createComposeRule()

  @Test fun recordHomeScreen() {
    compose.setContent {
      HomeScreen(
        uiState = HomeUiState.Content(items = sampleItems()),
        onUiEvent = {},
        onNavigate = {}
      )
    }
    // Capture, compare, or verify depending on -P flags
    captureRoboImage("home/HomeScreen_content.png")
  }
}
```

Determinism tips
- Use fixed fonts, locale, and time (inject a clock) to reduce diffs.
- Disable animations and ensure consistent sizes/densities.
- Isolate network/IO; render from deterministic sample UI state.

Desktop tasks (optional)
```bash
./gradlew recordRoborazziDesktop
./gradlew compareRoborazziDesktop
./gradlew verifyRoborazziDesktop
```

Scope and CI
- Android/JVM only by default (fast checks). Do not run iOS tasks unless an issue explicitly requires it.
- Store baselines under `composeApp/src/test/snapshots` (or a repo-level `snapshots/`).
- In CI, run `verifyRoborazziDebug` on PRs; allow updating baselines only behind an explicit flag (e.g., `-Proborazzi.test.record=true`).

## Running Tests (project guidelines)
- Shared unit tests: run the most relevant module task, e.g. `./gradlew :features:<feature>:presentation:testDebugUnitTest` or `:features:<feature>:data:testDebugUnitTest` as applicable.
- Android UI tests on device (if any under `:features:<feature>:presentation/src/androidTest`): `./gradlew :features:<feature>:presentation:connectedDebugAndroidTest`
- Do not run iOS tests by default; only if explicitly required for an issue.

Note: For feature presentation modules, place UI tests under `:features:<feature>:presentation/src/jvmTest` or `src/androidTest` as appropriate. Roborazzi tests typically run in JVM (`jvmTest`).

## Test Data Generators
- Use Kotest `Arb` for generators (UUIDs, strings, emails, numerics).
- Create custom `Arb` for domain-specific values as needed.

## CI Hints
- Prefer the fastest relevant test tasks (module-scoped) for PRs.
- If no tests exist for changed modules, at minimum run `./gradlew :composeApp:assembleDebug test --continue` as a compilation + test check.

## Alignment with Architecture
- Tests should reflect vertical-slice boundaries: unit-test feature `impl` against `api` contracts.
- Use Koin DI sparingly in tests; prefer constructor injection and explicit fakes/mocks.

## JSON round‑trip tests (recommended)
Purpose: Validate that JSON adapters are symmetric and stable over time.

Example (Kotlinx Serialization)
```kotlin
@Serializable data class UserDto(@SerialName("id") val id: String, @SerialName("name") val name: String)

class UserJsonRoundTripSpec : StringSpec({
  val json = Json { ignoreUnknownKeys = true }

  "json -> object -> json is stable" {
    val source = """{"id":"1","name":"Jane"}"""
    val obj = json.decodeFromString<UserDto>(source)
    val out = json.encodeToString(obj)
    // AssertK (JVM):
    // assertThat(Json.parseToJsonElement(out)).isEqualTo(Json.parseToJsonElement(source))
    // Or Kotest JSON matcher:
    out shouldContainJsonKeyValue "id" to "1"
  }

  "object -> json -> object is equal" {
    val obj = UserDto(id = "1", name = "Jane")
    val back = json.decodeFromString<UserDto>(json.encodeToString(obj))
    back shouldBe obj
  }
})
```

## Property-Based Testing Best Practices

### Effective Property Test Design

**1. Choose the Right Properties**

**Good:** Test invariants (data preservation), transformation rules (capitalization), mathematical properties
**Bad:** Specific concrete values (ID 25 = Pikachu), conditional logic not universal

**See examples:** [PokemonDetailViewModelTest.kt#L272-420](../../features/pokemondetail/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModelTest.kt#L272-420)

**2. Use Appropriate Generators**

**Built-in:** `Arb.int(range)`, `Arb.string(length)`, `Arb.list()`, `.orNull()`, `.filter { }`
**Custom:** Create `Arb<YourType>` using `arbitrary { }` builder

**See usage:** [PokemonDetailViewModelTest.kt#L272-310](../../features/pokemondetail/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModelTest.kt#L272-310) — Uses Arb.int ranges for Pokemon IDs

**3. Balance Property vs Concrete Tests**

```
📊 Target Distribution:
- 30-40% Property tests (broad coverage)
- 60-70% Concrete tests (documentation + specific edge cases)

🎯 Property Tests For:
- Mappers (data preservation)
- Repositories (HTTP codes, pagination)
- ViewModels (state transitions)
- Parsers/Validators
- JSON round-trips

📚 Concrete Tests For:
- Happy path examples (documentation)
- Specific edge cases (empty lists, null values)
- Error scenarios (specific failure modes)
- Complex setup scenarios
```

**4. Naming Conventions**

```kotlin
// ✅ GOOD: Starts with "property:"
"property: HTTP error codes always produce Error state"
"property: mapper preserves ID regardless of name"
"property: round-trip maintains data integrity"

// ❌ BAD: Looks like concrete test
"repository maps DTO to domain"
"ViewModel handles errors"
```

**5. Common Mistakes to Avoid**

**❌ DON'T:**
- Custom iteration counts: `checkAll(iterations = 10000)` — Slow!
- `Thread.sleep()` or `delay()` — Use Turbine + TestDispatcher
- Skip test dispatcher advancement — Always call `advanceUntilIdle()`

**✅ DO:**
- Use default 1000 iterations
- Inject `testScope` into ViewModels
- Use Turbine `.test { }` for flow testing

**See correct pattern:** [PokemonDetailViewModelTest.kt](../../features/pokemondetail/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModelTest.kt) — Property tests with Turbine + TestScope

### When to Remove Redundant Concrete Tests

**Decision Matrix:**

| Scenario | Property Test Exists? | Keep Concrete? | Reason |
|----------|---------------------|----------------|--------|
| HTTP 404 error | ✅ Yes (400-599) | ❌ Remove | Covered by property |
| Network timeout | ❌ No | ✅ Keep | Specific error type |
| ID extraction for ID=25 | ✅ Yes (1-10000) | ❌ Remove | Covered by property |
| Empty list handling | ❌ No | ✅ Keep | Specific edge case |
| Name capitalization for "pikachu" | ✅ Yes (all strings) | ❌ Remove | Covered by property |
| Invalid URL format | ❌ No | ✅ Keep | Specific failure mode |
| hasMore=true when next!=null | ✅ Yes (all scenarios) | ❌ Remove | Covered by property |
| Complex setup example | ❌ No | ✅ Keep | Documentation value |

**Cleanup Process:**

1. **Identify property tests**: List all property tests and their coverage
2. **Map concrete tests**: For each concrete test, check if property test covers it
3. **Evaluate documentation value**: Does concrete test explain something clearly?
4. **Remove redundant tests**: Delete tests fully covered by properties with no doc value
5. **Verify test count**: Aim for 30-40% property tests after cleanup

**Example Cleanup:**

**Before:** 15 tests with redundant concrete tests for HTTP 404, 500, 503
**After:** 12 tests — one property test covering HTTP 400-599 range replaces 3+ concrete tests
**Result:** Same coverage (actually better — 200 HTTP codes vs 3), less maintenance

**See real cleanup:** [PokemonDetailViewModelTest.kt#L315-375](../../features/pokemondetail/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModelTest.kt#L315-375) — Property test for error handling replaced multiple concrete error tests

## AI Agent Enforcement

### For Copilot

When generating code, Copilot MUST:

1. ✅ Generate test file alongside production code
2. ✅ Place tests in correct source set (androidTest/ for business logic)
3. ✅ Use appropriate testing framework (Kotest + MockK)
4. ✅ Add @Preview to all @Composable functions
5. ✅ Include minimum test coverage (success + error paths)
6. ✅ Run tests before marking code complete

### For Junie

When implementing features, Junie MUST:

1. ✅ Create test files before marking feature complete
2. ✅ Verify test files exist for all production files
3. ✅ Run tests and ensure they pass
4. ✅ Include test file paths in completion summary
5. ✅ Report any missing tests as BLOCKERS
6. ✅ Refuse to complete without tests

### Example: Complete Feature with Tests

```
Feature: Pokemon List - COMPLETION CHECKLIST

Production Files Created:
✅ features/pokemonlist/api/src/commonMain/.../PokemonListRepository.kt
✅ features/pokemonlist/impl/src/commonMain/.../data/PokemonListRepositoryImpl.kt
✅ features/pokemonlist/impl/src/commonMain/.../data/PokemonMappers.kt
✅ features/pokemonlist/impl/src/commonMain/.../presentation/PokemonListViewModel.kt
✅ features/pokemonlist/impl/src/commonMain/.../presentation/PokemonListScreen.kt

Test Files Created:
✅ features/pokemonlist/impl/src/androidTest/.../data/PokemonListRepositoryTest.kt
✅ features/pokemonlist/impl/src/androidTest/.../data/PokemonMappersTest.kt
✅ features/pokemonlist/impl/src/androidTest/.../presentation/PokemonListViewModelTest.kt
✅ PokemonListScreen.kt includes @Preview functions

Verification:
✅ All tests pass: ./gradlew :features:pokemonlist:impl:testDebugUnitTest
✅ Build + ALL tests succeed: ./gradlew :composeApp:assembleDebug test --continue
✅ Test coverage: 100% of production files have tests

STATUS: ✅ COMPLETE - ALL TESTS PRESENT AND PASSING
```

## Summary

**NO CODE IS COMPLETE WITHOUT TESTS**

- ✅ Every production file has a test file
- ✅ Tests are in correct location (androidTest/ for business logic)
- ✅ Tests use appropriate frameworks (Kotest + MockK)
- ✅ Tests cover minimum scenarios (success + errors)
- ✅ @Composable functions have @Preview
- ✅ Tests pass before PR

**This is not optional. Tests are part of the feature.**
