# Testing Strategy Guidelines: Mobile-First Approach

**Purpose**: Define a mobile-first testing strategy that maximizes testing capabilities within Kotlin Multiplatform framework limitations.

## ⚠️ CRITICAL: Test Enforcement

**NO CODE WITHOUT TESTS**

See `.junie/test-enforcement-agent.md` for complete enforcement rules.

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
        androidTest.dependencies {
            implementation(libs.kotest.assertions)
            implementation(libs.kotest.framework)
            implementation(libs.kotest.property)
            implementation(libs.mockk)
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
./gradlew :features:pokemonlist:impl:allTests

# Run specific test class
./gradlew :features:pokemonlist:impl:testDebugUnitTest --tests "PokemonListRepositoryTest"

# Verify test coverage
./gradlew :features:pokemonlist:impl:testDebugUnitTest --info | grep "tests completed"

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

See [kotest-smart-casting-quick-ref.md](./kotest-smart-casting-quick-ref.md) for complete documentation.

## Complete Testing Examples

### Repository Test (androidTest/)

```kotlin
// features/pokemonlist/impl/src/androidTest/kotlin/.../PokemonRepositoryTest.kt
class PokemonRepositoryTest : StringSpec({
    lateinit var mockApi: PokemonListApiService
    lateinit var repository: PokemonListRepository
    
    beforeTest {
        mockApi = mockk()
        repository = PokemonListRepository(mockApi)
    }
    
    "should return Right with pokemon list on success" {
        // Given
        val mockDto = PokemonListDto(
            count = 1292,
            next = "https://pokeapi.co/api/v2/pokemon/?offset=20&limit=20",
            previous = null,
            results = listOf(
                PokemonSummaryDto("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"),
                PokemonSummaryDto("ivysaur", "https://pokeapi.co/api/v2/pokemon/2/")
            )
        )
        coEvery { mockApi.getPokemonList(20, 0) } returns mockDto
        
        // When
        val result = repository.loadPage(limit = 20, offset = 0)
        
        // Then
        result.shouldBeRight { page ->
            page.pokemons shouldHaveSize 2
            page.pokemons[0].name shouldBe "Bulbasaur"  // Capitalized
            page.pokemons[0].id shouldBe 1
            page.hasMore shouldBe true
        }
    }
    
    "should return Network error on timeout" {
        coEvery { mockApi.getPokemonList(any(), any()) } throws 
            ConnectTimeoutException("Connection timeout")
        
        val result = repository.loadPage()
        
        result.shouldBeLeft { error ->
            error shouldBe RepoError.Network
        }
    }
    
    "should return Http error on 404" {
        coEvery { mockApi.getPokemonList(any(), any()) } throws
            ClientRequestException(mockk(relaxed = true), "Not found")
        
        val result = repository.loadPage()
        
        result.shouldBeLeft { error ->
            error shouldBeInstanceOf<RepoError.Http>()
        }
    }
})

// Helper extensions for Either testing
fun <L, R> Either<L, R>.shouldBeRight(assertion: (R) -> Unit = {}) {
    this.fold(
        ifLeft = { fail("Expected Right but was Left($it)") },
        ifRight = { assertion(it) }
    )
}

fun <L, R> Either<L, R>.shouldBeLeft(assertion: (L) -> Unit = {}) {
    this.fold(
        ifLeft = { assertion(it) },
        ifRight = { fail("Expected Left but was Right($it)") }
    )
}
```

### Property-Based Mapper Test (androidTest/)

```kotlin
// features/pokemonlist/impl/src/androidTest/kotlin/.../PokemonMappersTest.kt
class PokemonMappersTest : StringSpec({
    "property: DTO to domain preserves id and name" {
        checkAll(
            Arb.int(1..1000),
            Arb.string(1..30)
        ) { id, name ->
            val dto = PokemonSummaryDto(
                name = name.lowercase(),
                url = "https://pokeapi.co/api/v2/pokemon/$id/"
            )
            
            val domain = dto.toDomain()
            
            domain.id shouldBe id
            domain.name.lowercase() shouldBe name.lowercase()
            domain.name.first().isUpperCase() shouldBe true  // Always capitalized
        }
    }
    
    "property: domain to DTO round-trip preserves data" {
        checkAll(
            Arb.int(1..1000),
            Arb.string(1..30).map { it.replaceFirstChar { c -> c.uppercaseChar() } }
        ) { id, name ->
            val original = Pokemon(
                id = id,
                name = name,
                imageUrl = "https://example.com/$id.png"
            )
            
            // In real scenario, if you have toDto()
            // val dto = original.toDto()
            // val restored = dto.toDomain()
            // restored shouldBe original
        }
    }
})
```

### ViewModel Test (androidTest/)

```kotlin
// features/pokemonlist/impl/src/androidTest/kotlin/.../PokemonViewModelTest.kt
class PokemonListViewModelTest : StringSpec({
    lateinit var mockRepository: PokemonListRepository
    lateinit var viewModel: PokemonListViewModel
    val testDispatcher = StandardTestDispatcher()
    
    beforeTest {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk()
        viewModel = PokemonListViewModel(mockRepository)
    }
    
    afterTest {
        Dispatchers.resetMain()
    }
    
    "should start with Loading state" {
        viewModel.uiState.value shouldBe PokemonListUiState.Loading
    }
    
    "should emit Content state on successful load" {
        // Given
        val mockPage = PokemonPage(
            pokemons = listOf(
                Pokemon(1, "Bulbasaur", "url1"),
                Pokemon(2, "Ivysaur", "url2")
            ).toImmutableList(),
            hasMore = true
        )
        coEvery { mockRepository.loadPage(any(), any()) } returns Either.Right(mockPage)
        
        // When
        viewModel.loadInitialPage()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.uiState.value
        state shouldBeInstanceOf<PokemonListUiState.Content>()
        (state as PokemonListUiState.Content).pokemons shouldHaveSize 2
        state.hasMore shouldBe true
        state.isLoadingMore shouldBe false
    }
    
    "should emit Error state on repository failure" {
        coEvery { mockRepository.loadPage(any(), any()) } returns 
            Either.Left(RepoError.Network)
        
        viewModel.loadInitialPage()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        state shouldBeInstanceOf<PokemonListUiState.Error>()
        (state as PokemonListUiState.Error).message shouldContain "network"
    }
})
```

### Simple Utility Test (commonTest/)

```kotlin
// features/pokemonlist/impl/src/commonTest/kotlin/.../UrlUtilsTest.kt
class UrlUtilsTest {
    @Test
    fun extractIdFromUrl_validUrl_returnsId() {
        val url = "https://pokeapi.co/api/v2/pokemon/25/"
        val id = extractPokemonId(url)
        assertEquals(25, id)
    }
    
    @Test
    fun extractIdFromUrl_invalidUrl_returnsNull() {
        val url = "https://pokeapi.co/api/v2/pokemon/"
        val id = extractPokemonId(url)
        assertNull(id)
    }
}

fun extractPokemonId(url: String): Int? {
    return url.trimEnd('/').substringAfterLast('/').toIntOrNull()
}
```

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
- Shared unit tests: run the most relevant module task, e.g. `./gradlew :features:<feature>:presentation:jvmTest` or `:features:<feature>:impl:allTests` as applicable.
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
- Use Metro DI sparingly in tests; prefer constructor injection and explicit fakes/mocks.

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
