# Testing Strategy Guidelines (Kotest + MockK)

Purpose: Define a cohesive, multiplatform testing strategy using Kotest and MockK, with an emphasis on property-based testing where practical.

## Frameworks
- Kotest for test framework, assertions, and property-based testing.
- MockK for mocking/stubbing on JVM/Android; prefer fakes for Native targets.

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
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation("io.mockk:mockk:<version>")
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

## Running Tests (project guidelines)
- Shared unit tests: `./gradlew :composeApp:testDebugUnitTest` (or relevant target-specific tasks)
- Android UI tests on device (if any under `composeApp/src/commonTest/screentest`): `./gradlew :composeApp:connectedDebugAndroidTest`
- Do not run iOS tests by default; only if explicitly required for an issue.

## Test Data Generators
- Use Kotest `Arb` for generators (UUIDs, strings, emails, numerics).
- Create custom `Arb` for domain-specific values as needed.

## CI Hints
- Prefer the fastest relevant test tasks (module-scoped) for PRs.
- If no tests exist for changed modules, at minimum run `./gradlew :composeApp:assembleDebug` as a compilation check.

## Alignment with Architecture
- Tests should reflect vertical-slice boundaries: unit-test feature `impl` against `api` contracts.
- Use Metro DI sparingly in tests; prefer constructor injection and explicit fakes/mocks.
