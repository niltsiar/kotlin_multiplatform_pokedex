# Quick Reference Guide (API References)

Last Updated: February 7, 2026

> Fast lookup for API references, system status, and test requirements.

## 🎨 Phase 2 Redesign Status

**Completed (Steps 1-8):**
- ✅ Step 1: NavigationProvider naming + Core token foundation
- ✅ Step 2: Theme token systems with delegation  
- ✅ Step 3: Google Sans Flex typography
- ✅ Step 4: Shared component abstraction layer
- ✅ Step 5: Motion preference + predictive back
- ✅ Step 6: Material screens redesign (8 components)
- ✅ Step 7: Unstyled screens redesign (8 components + navigation + hover fixes)
- ✅ Step 8: SwiftUI design system with theme tokens

**Next: Step 9** - Comprehensive unit tests

## Test Enforcement Matrix

| Production Code | Test Location | Framework | Property Tests Required |
|----------------|---------------|-----------|------------------------|
| Repository | androidUnitTest/ | Kotest + MockK + Turbine | HTTP error ranges, ID preservation |
| ViewModel | androidUnitTest/ | Kotest + MockK + Turbine | State transitions with random data |
| Mapper | androidUnitTest/ | Kotest properties | Data preservation invariants |
| Use Case | androidUnitTest/ | Kotest + MockK | Business rule validation |
| API Service | androidUnitTest/ | Kotest + MockK | HTTP mocking |
| @Composable | Same file | @Preview + Roborazzi | N/A |
| Simple Utility | commonTest/ | kotlin-test | Input/output validation |

## API Quick Reference

### SavedStateHandle (Persistence)
Use the `saved` delegate for automatic persistence across configuration changes.
```kotlin
import androidx.lifecycle.serialization.saved

// Single line - automatic persistence (State must be @Serializable)
private var state by savedStateHandle.saved { MyState() }
```

### Turbine (Flow Testing)
| Method | Use Case |
|--------|----------|
| `awaitItem()` | Get next emission (fails if none) |
| `skipItems(n)` | Skip n emissions |
| `expectNoEvents()` | Assert no emissions occurred |
| `cancelAndIgnoreRemainingEvents()` | Clean teardown (always call at end) |

### Library Resources
Enable public access in library `build.gradle.kts`:
```kotlin
compose.resources { publicResClass = true }
android { namespace = "com.minddistrict.multiplatformpoc.core.designsystem.core" }
```
**Import:** `multiplatformpoc.core.designsystem_core.generated.resources.Res`

## Minimum Test Coverage (Per File)

- **Repositories**: Success path (Right) + All Error paths (Network, Http, Unknown)
- **ViewModels**: Initial state + Loading → Success/Error flows + Event handling
- **Mappers**: 100% Property-based tests (data preservation) + Edge cases
- **@Composable**: At least one `@Preview` with realistic data

## Version Catalog (gradle/libs.versions.toml)

```toml
[versions]
arrow = "1.2.0"
[libraries]
arrow-core = { module = "io.arrow-kt:arrow-core", version.ref = "arrow" }
[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
```

**See Also:** [AGENTS.md](../AGENTS.md) for full skill routing.
