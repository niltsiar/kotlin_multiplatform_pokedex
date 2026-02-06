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

## Related Skills

| Skill | Use For |
|-------|---------|
| @kmp-testing-strategy | Testing strategy and philosophy |
| @kmp-presentation | ViewModel patterns |
| @kmp-data-layer | Repository patterns |

## Documentation Sources

| Document | Purpose | Tokens |
|----------|---------|--------|
| [testing_patterns.md](../../../docs/patterns/testing_patterns.md) | Complete testing patterns | ~6000 |
| [kotest_smart_casting_quick_ref.md](../../../docs/tech/kotest_smart_casting_quick_ref.md) | Smart casting patterns | ~3000 |
| [testing_quick_ref.md](../../../docs/tech/testing_quick_ref.md) | Quick reference | ~2000 |

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
