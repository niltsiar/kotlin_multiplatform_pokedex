# Testing Strategy Guidelines (Kotest + MockK)

Purpose: Define a cohesive, multiplatform testing strategy using Kotest and MockK, with an emphasis on property-based testing where practical.

## Frameworks
- Kotest for test framework, assertions, and property-based testing.
- MockK for mocking/stubbing on JVM/Android; prefer fakes for Native targets.
 - Roborazzi for Android/JVM Compose UI screenshot testing (Robolectric-based) and optional Desktop tasks.
 - AssertK (JVM/Android) for fluent assertions; prefer in JVM tests. For commonMain/commonTest where AssertK is not available, use Kotest assertions.
 - Lifecycle ViewModel testing (KMP): use `lifecycle-viewmodel-testing` and `ViewModelScenario` for isolated ViewModel tests where appropriate.

## Gradle Setup (Multiplatform)
```kotlin
kotlin {
  sourceSets {
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation("io.kotest:kotest-assertions-core:<version>")
        implementation("io.kotest:kotest-framework-engine:<version>")
        implementation("io.kotest:kotest-property:<version>")
        // ViewModel testing (KMP)
        // testImplementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-testing:<version>")
        // Optional matcher modules (examples; confirm coordinates in libs.versions.toml)
        // implementation("io.kotest:kotest-assertions-json:<version>")
        // implementation("io.kotest:kotest-assertions-kotlinx-datetime:<version>")
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation("io.mockk:mockk:<version>")
        implementation("com.willowtreeapps.assertk:assertk-jvm:<version>")
        // Screenshot testing
        implementation("io.github.takahirom.roborazzi:roborazzi:<version>")
        implementation("io.github.takahirom.roborazzi:roborazzi-compose:<version>")
        testImplementation("io.github.takahirom.roborazzi:roborazzi-junit-rule:<version>")
      }
    }
    // Add platform-specific test deps as required.
  }
}
```

Notes
- Kotest on JVM uses JUnit Platform; ensure `useJUnitPlatform()` is configured on JVM `Test` tasks if needed.
- For Native targets, annotation-based project config is limited; use code-based configuration when necessary.

## Conventions
- Name test classes with `Spec` or `Test` suffix, mirroring production package structure.
- Use Given/When/Then comments or Kotest contexts (`context`, `should`) to structure scenarios.
- Prefer immutable test data and builder helpers.
- Prefer AssertK fluent assertions in JVM tests, e.g., `assertThat(value).isEqualTo(...)`. In common tests, use Kotest `shouldBe`, `should` matchers.

## Property-Based Testing
Use Kotest `checkAll`/`forAll` to validate invariants across many generated inputs.

Example (mapping inversion):
```kotlin
class JobMappingPropertySpec : StringSpec({
  "dto to domain preserves id and title" {
    checkAll(Arb.uuid(), Arb.string(minSize = 1..64)) { id, title ->
      val dto = JobResponse(id = id.toString(), title = title)
      val domain = dto.asDomain()
      domain.id shouldBe dto.id
      domain.title shouldBe dto.title
    }
  }
})
```

Example (value object laws):
```kotlin
class EmailValueObjectSpec : StringSpec({
  "toString of normalized email is lowercase" {
    checkAll(Arb.email()) { email ->
      Email(email).toString() shouldBe Email(email).toString().lowercase()
    }
  }
})
```

## Mocking Guidelines (MockK)
- Use MockK for JVM/Android tests to stub dependencies at the boundaries (e.g., API services).
- Prefer fakes for Native targets or design seams that eliminate mocks where feasible.
- Example:
```kotlin
class JobRepositorySpec : StringSpec({
  val api = mockk<JobApiService>()
  val dao = mockk<SavedJobDao>(relaxed = true)
  val repo = JobRepositoryImpl(api, dao)

  "saveJob returns Right(Unit) when api succeeds" {
    coEvery { api.saveJob(any()) } returns SaveJobResponse(success = true)

    repo.saveJob(sampleJob) shouldBe Right(Unit)
    coVerify { api.saveJob(SaveJobRequest(sampleJob.id)) }
  }
})
```

## Repositories and Arrow Either
- Assert on `Either` using Kotest matchers or pattern matching via `fold`.
- Prefer property tests for mapping and error-classification helpers.
 - Consider including Arrow-specific matcher helpers if available, or write small extension helpers in tests:
```kotlin
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
- If no tests exist for changed modules, at minimum run `./gradlew :composeApp:assembleDebug` as a compilation check.

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
