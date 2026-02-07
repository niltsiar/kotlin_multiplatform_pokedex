# Testing Quick Reference (Low-Token)

Last Updated: December 20, 2025

Purpose: Token-lean checklist for everyday test work. Use this file when you need enforcement and location rules without
loading the full strategy. For deep dives, see [@kmp-testing-strategy skill](@kmp-testing-strategy skill).

## When to Use This File

- Packing context for agents or Copilot with limited budget
- PR/task summaries where only enforcement and location decisions are needed
- Choosing the right source set/framework before writing tests

## Canonical Sources

- [@kmp-testing-strategy skill](@kmp-testing-strategy skill) — deep dive with rationale and playbooks
- [testing_patterns.md](See @kmp-testing-patterns skill) — concise pattern reminders
- [See @kmp-critical-patterns skill#testing-pattern](See @kmp-critical-patterns skill#testing-pattern) — canonical rules
- [See @kmp-critical-patterns skill](See @kmp-critical-patterns skill) — pattern cards view

## Test Enforcement (NO CODE WITHOUT TESTS)

| Production Code | Location | Framework | Key Rule |
| --- | --- | --- | --- |
| Repository | `androidUnitTest/` | Kotest + MockK | Success + all error paths |
| ViewModel | `androidUnitTest/` | Kotest + MockK + Turbine | Initial, Loading, Success, Error + events |
| Mapper | `androidUnitTest/` | Kotest properties | Property tests proving data preservation |
| `@Composable` | Same file | `@Preview` (+ Roborazzi where used) | Realistic preview + screenshot baseline |
| Simple Utility | `commonTest/` | kotlin-test | Pure functions only |

## Property-Based Testing Targets

- Mappers: **100%** property tests
- Repositories: **40–50%** property coverage (error mapping, pagination, paging sizes)
- ViewModels: **30–40%** property coverage (state transitions, random inputs)
- Use `checkAll` / `forAll` in **androidUnitTest/** for JVM + MockK support

## Flow Testing with Turbine

- Always test `Flow` / `StateFlow` / `SharedFlow` with Turbine
- Prefer injecting a `TestScope` via constructor; **NO** `Dispatchers.setMain/resetMain`
- Use `awaitItem()`, `skipItems()`, and `cancelAndIgnoreRemainingEvents()` with `advanceUntilIdle()`

## Smart Casting

- Use Kotest contracts: `shouldBeRight { }`, `shouldBeLeft { }`, `shouldBeInstanceOf<>()`
- Avoid manual casts after assertions
- See [kotest_smart_casting_quick_ref.md](kotest_smart_casting_quick_ref.md)

## Minimum Coverage Reminders

- Success + all error paths for repositories
- Initial/loading/success/error + events for ViewModels
- Data preservation for mappers
- Realistic `@Preview` for every `@Composable`
- Basic assertions for simple utilities (`commonTest/` only when dependency-free)

## Commands

- Primary: `./gradlew :composeApp:assembleDebug test --continue`
- Screenshots: `./gradlew recordRoborazziDebug` / `./gradlew verifyRoborazziDebug`
- All commands: [QUICK_REFERENCE.md](../QUICK_REFERENCE.md)

## Example Links

- Repository test: [@kmp-testing-strategy skill#repository-test-androidtest](@kmp-testing-strategy skill#repository-test-androidtest)
- Property tests: [@kmp-testing-strategy skill#property-based-testing-primary-strategy](@kmp-testing-strategy skill#property-based-testing-primary-strategy)
- Flow tests: [@kmp-testing-strategy skill#flow-testing-with-turbine](@kmp-testing-strategy skill#flow-testing-with-turbine)
- Screenshots: [@kmp-testing-strategy skill#screenshot-testing-roborazzi](@kmp-testing-strategy skill#screenshot-testing-roborazzi)
