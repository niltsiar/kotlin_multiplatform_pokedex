# Navigation Guidelines

Purpose: Define how navigation contracts are exposed and implemented in a vertical-slice, feature-modularized Compose Multiplatform app.

## Architecture
- Keep navigation feature‑local. Each feature owns its entry points and routes.
- Expose navigation contracts in `:features:<feature>:api` and keep implementations in `:features:<feature>:impl`.
- Avoid leaking implementation details (screens, view models) across module boundaries; expose only contracts.
- Standard: Compose Navigation 3 (supported in Compose Multiplatform 1.10.0‑beta02). Artifacts:
  - `org.jetbrains.androidx.navigation3:navigation3-ui`
  - `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3`
  - Optional: `org.jetbrains.compose.material3.adaptive:adaptive-navigation3`

## Contracts in api
```kotlin
// :features:profile:api
interface ProfileEntry {
  val route: String
  fun build(deeplink: String? = null): String // optional helpers
}
```

## Implementation in impl
```kotlin
// :features:profile:impl
internal class ProfileEntryImpl : ProfileEntry {
  override val route: String = "profile/{userId}"
  override fun build(deeplink: String?): String = "profile/${deeplink ?: "me"}"
}

// Screen composable kept internal to impl
@Composable internal fun ProfileScreen(modifier: Modifier = Modifier) { /* ... */ }
```

Bind via wiring (no annotations on the class)
```kotlin
// :features:profile:wiring/src/commonMain/.../ProfileNavigationWiring.kt
@Provides
fun provideProfileEntry(): ProfileEntry = ProfileEntryImpl()
```

## Composition Root
- Centralize route wiring in the app module and request feature entries via DI (Metro) as needed.
- Use multibinding (`Set<FeatureEntry>`) if you need to aggregate dynamic destinations.
- With Navigation 3, drive navigation by adding/removing items from a back stack list.

```kotlin
class NavRegistry(
  private val entries: Set<FeatureEntry>
) {
  fun allRoutes(): Set<String> = entries.mapTo(mutableSetOf()) { it.route }
}
```

## Wiring/Aggregation Modules for Navigation
- Prefer aggregating navigation entries in a wiring module to keep the app module thin and to avoid adding direct dependencies on each feature `impl`.
- Naming: `:features:<feature>:wiring` (feature-local) or `:wiring:navigation` (cross-feature aggregator).

Example (wiring module)
```kotlin
// :wiring:navigation/src/commonMain/.../NavigationWiring.kt
@Provides
fun provideFeatureEntries(
  profile: ProfileEntry,
  settings: SettingsEntry,
  home: HomeEntry
): Set<FeatureEntry> = setOf(profile, settings, home)

@Provides
fun provideNavRegistry(entries: Set<FeatureEntry>): NavRegistry = NavRegistry(entries)
```

Notes
- Keep wiring modules internal to the app; do not export them in the iOS umbrella. Only `api` modules should be exported.
- Feature `impl` modules remain private; wiring depends on them and exposes only contracts defined in `api`.

## Navigation 3 basics (examples)

Persistent back stack and entries
```kotlin
@Serializable data object Home : NavKey
@Serializable data class Profile(val userId: String) : NavKey

@Composable
fun AppNav(registry: NavRegistry) {
  val backStack = rememberNavBackStack(Home)
  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider = entryProvider {
      entry<Home> {
        HomeScreen(
          onNavigateProfile = { id -> backStack.add(Profile(id)) }
        )
      }
      entry<Profile> { key ->
        ProfileScreen(userId = key.userId)
      }
    }
  )
}
```

Result passing example (pop with result)
```kotlin
Button(onClick = {
  resultStore.setResult("updated", resultKey = "profile_action")
  backStack.removeLastOrNull()
}) { Text("Done") }
```

## Deep Links
- Define deep link parsing and mapping to feature routes in a single place; forward parameters to feature screens via the contract methods.
- Keep parsing logic independent of UI for testability.

## Testing
- Unit-test contract behavior (route building, parameter encoding/decoding) with Kotest, using property-based testing for round-trip invariants when applicable.
- UI navigation tests (Android) live under `:features:<feature>:presentation/src/commonTest/screentest` when implemented.
 - ViewModels used by destinations must extend `androidx.lifecycle.ViewModel` and can use `viewModelScope`; prefer constructing them via your DI/wiring or using factory methods aligned with Navigation 3 patterns.

## Notes
- Keep navigation state ephemeral and derived from the backstack where possible; avoid duplicating route state in view models.
- Align destination availability and restrictions with PRD and flows in `.junie/guides/project/user_flow.md`.
