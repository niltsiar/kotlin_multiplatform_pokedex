# Dual-UI Pokédex Implementation Plan

**Created:** December 23, 2025  
**Goal:** Showcase Pokémon app with side-by-side Material Design 3 and Compose Unstyled implementations

**Status:** 🟡 Planning Complete — Ready for Implementation

---

## Overview

Build a dual-UI Pokédex that demonstrates both **Material 3 Expressive** and **Compose Unstyled** design systems in the same app. The theme switching UX approach will be determined by the UI/UX Design Agent (see Phase 1.1). All business logic (ViewModels, repositories, domain models) remains 100% shared — only UI layers differ.

**Key Principles:**
- Zero duplication of business logic
- Both design systems use identical ViewModels/repositories
- Use **Material 3 Expressive** theme (already implemented in project)
- Adaptive layouts work across mobile, tablet, desktop
- Theme switching UX designed by UI/UX specialist
- SwiftUI app (iosApp) remains **untouched** (native iOS UI)
- iOS Compose app (iosAppCompose) works like any other Compose app (uses both design systems)
- Screenshot testing deferred to future (not in initial implementation)

---

## Architecture Strategy

### Module Organization (Approach: Side-by-Side UI Modules)

```
:features:pokemonlist/
├── api/                  → Shared contracts (unchanged)
├── data/                 → Shared repositories (unchanged)
├── presentation/         → Shared ViewModels (unchanged)
├── ui-material/          → Material 3 screens (renamed from :ui)
├── ui-unstyled/          → Compose Unstyled screens (NEW)
├── wiring/               → Shared business DI (unchanged)
├── wiring-ui-material/   → Material nav registration (renamed from :wiring-ui)
└── wiring-ui-unstyled/   → Unstyled nav registration (NEW)

:features:pokemondetail/  → Same structure

:composeApp/              → Root app with theme switcher
:core:designsystem/       → Material 3 theme (existing)
:core:designsystem-unstyled/ → Compose Unstyled theme (NEW)
```

**Rationale:**
- Clear separation of UI concerns (Material vs Unstyled)
- Shared ViewModels enforce identical behavior across design systems
- Can run both UIs in same app with runtime switching
- Easy to add new features to both design systems

---

## Commit Strategy

**Goal:** Enable incremental review and rollback capability at each milestone.

**Commit Format:** Follow [Conventional Commits](https://www.conventionalcommits.org/) as documented in [guidelines.md](../../.junie/guidelines.md)

**Key Principles:**
- Commit after each completed task (not phase)
- Each commit must build successfully: `./gradlew :composeApp:assembleDebug test --continue`
- Keep commits atomic (one logical change per commit)
- Write descriptive commit messages with context
- Use appropriate commit types: `feat`, `refactor`, `docs`, `test`, `build`

**Example Commit Messages:**
```bash
git commit -m "refactor(features): rename :ui to :ui-material for dual-UI support"
git commit -m "build(deps): add Compose Unstyled 1.49.3 dependencies"
git commit -m "feat(designsystem): create Compose Unstyled theme matching Pokémon branding"
git commit -m "feat(pokemonlist): implement unstyled UI variant"
git commit -m "docs(architecture): document dual-UI module organization"
```

**Review Points:**
- Each phase ends with a comprehensive commit
- PRs can be created per phase or per major feature
- Tag reviewers for architecture decisions (Phase 1.1, Phase 2)

---

## Implementation Phases

### Phase 1: Architecture & Navigation Foundation

**Owner:** UI/UX Design Agent + KMP Mobile Expert Agent + Product Design Agent  
**Duration:** 2-3 days  
**Dependencies:** None

#### Tasks:

**1.1 CRITICAL: Design theme switching UX approach (UI/UX Design Agent)**
   - **Problem:** Bottom bar destinations approach causes inconsistency (scaffold theme != screen theme)
   - **Constraints:**
     - Must support adaptive navigation (bottom bar → rail → drawer)
     - Follow Material 3 Adaptive best practices: [Compose Material3 Adaptive](https://developer.android.com/jetpack/androidx/releases/compose-material3-adaptive)
     - Both Material 3 Expressive and Compose Unstyled must feel native
     - User must understand they're comparing design systems
     - State persistence across app restarts
   - **Adaptive Resources:**
     - [Android: Build Adaptive Apps](https://developer.android.com/develop/ui/compose/build-adaptive-apps)
     - [Kotlin: Compose Adaptive Layouts](https://kotlinlang.org/docs/multiplatform/compose-adaptive-layouts.html)
   - **Options to evaluate:**
     - Option A: Top-level theme wrapper (entire app switches, including scaffold)
     - Option B: Separate navigation graphs with transition animation between worlds
     - Option C: Split-screen/side-by-side comparison (desktop/tablet only)
     - Option D: Theme selector screen before entering main app
     - Option E: Other creative solutions
   - **Deliverable:** UX design document with wireframes, interaction flows, recommendation
   - References: [ui_ux.md](../project/ui_ux.md), Material 3 Expressive guidelines

1.2 **Define navigation structure based on UX design**
   - Create root-level `DesignSystem` enum (MATERIAL, UNSTYLED)
   - Implement navigation approach from Phase 1.1 decision
   - Architect adaptive nav scaffold (bottom bar → rail → drawer transitions)
   - References: [conventions.md](../tech/conventions.md), [navigation.md](../tech/navigation.md)

2. **Refactor existing module structure**
   - Rename `:features:pokemonlist:ui` → `:features:pokemonlist:ui-material`
   - Rename `:features:pokemonlist:wiring-ui` → `:features:pokemonlist:wiring-ui-material`
   - Apply same pattern to `:features:pokemondetail`
   - Update build.gradle.kts dependencies in [composeApp](../../composeApp/build.gradle.kts)
   - Preserves existing Material implementation as one "world"

3. **Create new unstyled module structure**
   - Add `:features:pokemonlist:ui-unstyled/build.gradle.kts` using `ConventionFeatureUiPlugin`
   - Add `:features:pokemonlist:wiring-ui-unstyled/build.gradle.kts` using `ConventionFeatureWiringUiPlugin`
   - Repeat for `:features:pokemondetail`
   - Convention plugins automatically configure KMP targets + Compose dependencies

4. **Add Compose Unstyled dependencies**
   - Update [gradle/libs.versions.toml](../../gradle/libs.versions.toml):
     ```toml
     [versions]
     composeUnstyled = "1.49.3"
     
     [libraries]
     composeunstyled = { module = "com.composables:composeunstyled", version.ref = "composeUnstyled" }
     composeunstyled-theming = { module = "com.composables:composeunstyled-theming", version.ref = "composeUnstyled" }
     composeunstyled-primitives = { module = "com.composables:composeunstyled-primitives", version.ref = "composeUnstyled" }
     ```
   - Run `./gradlew dependencyUpdates` to check for latest version
   - Add to `:core:designsystem-unstyled` module dependencies

5. **Update root app for dual-UI support**
   - Modify [composeApp/build.gradle.kts](../../composeApp/build.gradle.kts):
     ```kotlin
     commonMain.dependencies {
         // Both design systems available at runtime
         implementation(projects.features.pokemonlist.uiMaterial)
         implementation(projects.features.pokemonlist.uiUnstyled)
         implementation(projects.features.pokemonlist.wiringUiMaterial)
         implementation(projects.features.pokemonlist.wiringUiUnstyled)
         // Same for pokemondetail
     }
     ```

**Acceptance Criteria:**
- [x] Module structure refactored (ui → ui-material, wiring-ui → wiring-ui-material)
- [x] New unstyled modules created with correct convention plugins
- [x] Compose Unstyled dependencies added to version catalog
- [x] App builds successfully with both module sets
- [x] Existing Material UI works unchanged

**Commit Checkpoints:**
```bash
# After Phase 1.1 (UX Design)
git commit -m "docs(ux): design theme switching UX approach for dual-UI

- Evaluate 5 options for theme switching consistency
- Provide wireframes and interaction flows
- Recommend solution with rationale
- Document implementation guidance for Phase 5"

# After Task 1.2 (Module Refactoring)
git commit -m "refactor(features): rename :ui to :ui-material and :wiring-ui to :wiring-ui-material

- Rename pokemonlist UI modules for dual-UI architecture
- Rename pokemondetail UI modules for dual-UI architecture
- Update composeApp dependencies to reference new module names
- All existing tests pass (84/84)"

# After Task 1.3 (New Modules)
git commit -m "build(features): create :ui-unstyled and :wiring-ui-unstyled modules

- Add pokemonlist:ui-unstyled with ConventionFeatureUiPlugin
- Add pokemonlist:wiring-ui-unstyled with ConventionFeatureWiringUiPlugin
- Add pokemondetail:ui-unstyled with ConventionFeatureUiPlugin
- Add pokemondetail:wiring-ui-unstyled with ConventionFeatureWiringUiPlugin
- Modules build successfully with KMP targets configured"

# After Task 1.4 (Dependencies)
git commit -m "build(deps): add Compose Unstyled 1.49.3 dependencies

- Add composeunstyled, composeunstyled-theming, composeunstyled-primitives to version catalog
- Configure dependencies for :core:designsystem-unstyled module
- Verify latest version with dependencyUpdates"

# After Task 1.5 (App Configuration)
git commit -m "build(composeApp): configure dual-UI module dependencies

- Add both :ui-material and :ui-unstyled modules to composeApp
- Add both :wiring-ui-material and :wiring-ui-unstyled modules
- Enable runtime design system selection
- Build succeeds: ./gradlew :composeApp:assembleDebug test --continue"
```

---

### Phase 2: Design System & Theme Architecture

**Owner:** UI/UX Design Agent + KMP Mobile Expert Agent  
**Duration:** 2-3 days  
**Dependencies:** Phase 1 (module structure)

#### Tasks:
1. **Design Compose Unstyled theme matching Pokémon branding**
   - Create `:core:designsystem-unstyled` module
   - Use `buildTheme { }` DSL to define custom theme
   - Port Pokémon type colors from [Color.kt](../../core/designsystem/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/core/designsystem/theme/Color.kt)
   - Port typography system from [Typography.kt](../../core/designsystem/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/core/designsystem/theme/Typography.kt) as token-based styles
   - **Note:** Material side uses **Material 3 Expressive** theme (already implemented in `:core:designsystem`)
   - Define theme properties:
     ```kotlin
     val PokemonUnstyledTheme = buildTheme {
         name = "PokemonUnstyled"
         defaultTextStyle = TextStyle(fontFamily = GoogleSans, fontSize = 16.sp)
         defaultContentColor = Color(0xFF1C1B1F)
         defaultComponentInteractiveSize = ComponentInteractiveSize(
             touchInteractionSize = 48.dp,
             nonTouchInteractionSize = 32.dp
         )
         properties {
             set(TypeColorTokens.Fire, Color(0xFFFF4422))
             set(TypeColorTokens.Water, Color(0xFF3399FF))
             // ... other Pokémon types
         }
     }
     ```

2. **Create unstyled component library**
   - Build styled wrappers in `:core:designsystem-unstyled/src/commonMain/kotlin/components/`:
     - `PokemonButton`: Unstyled `Button` + custom shape/colors/padding
     - `PokemonCard`: Unstyled `Box` + border/shadow/corner radius
     - `PokemonText`: Unstyled `Text` + theme token integration
     - `TypeBadge`: Custom component with Pokémon type colors
     - `StatBar`: Custom progress indicator for Pokémon stats
   - Ensure WCAG AA contrast ratios (4.5:1 minimum)
   - Reference: [ui_ux.md](ui_ux.md) design guidelines

3. **Define adaptive breakpoints for responsive layouts**
   - **Use Material 3 Adaptive WindowSizeClass** (built-in standard):
     ```kotlin
     @Composable
     fun AdaptiveLayout() {
         val windowSizeClass = calculateWindowSizeClass()
         when (windowSizeClass.widthSizeClass) {
             WindowWidthSizeClass.Compact -> // < 600dp (phone portrait)
             WindowWidthSizeClass.Medium -> // 600-840dp (phone landscape, small tablet)
             WindowWidthSizeClass.Expanded -> // >= 840dp (large tablet, desktop)
         }
     }
     ```
   - **Follow official breakpoint guidelines:**
     - Compact: 0-599dp (optimize for single-column layouts)
     - Medium: 600-839dp (two-column layouts, navigation rail)
     - Expanded: 840dp+ (multi-column layouts, navigation drawer)
   - Create layout utilities:
     - `gridColumns(windowSizeClass)`: Returns 2/3/4 based on width class
     - `adaptiveSpacing(windowSizeClass)`: Returns 8dp/12dp/16dp
     - `adaptiveNavigationType(windowSizeClass)`: Returns NavigationSuiteType
   - Build `AdaptiveNavigationScaffold` using **NavigationSuiteScaffold**
   - **Resources:**
     - [Material 3 Adaptive Navigation Suite](https://developer.android.com/develop/ui/compose/layouts/adaptive/navigation-suite-scaffold)
     - [WindowSizeClass Guide](https://developer.android.com/develop/ui/compose/layouts/adaptive#window-size-classes)

4. **Document styling patterns**
   - Create `:features:pokemonlist:ui-unstyled/README.md`:
     - Component usage examples with code snippets
     - Theme customization guide (how to override tokens)
     - Responsive layout patterns (grid, adaptive spacing)
     - Comparison table: Material component → Unstyled equivalent
   - Enables consistency when adding new features to unstyled variant

**Acceptance Criteria:**
- [x] Compose Unstyled theme defined with Pokémon branding
- [x] Component library created (Button, Card, Text, TypeBadge, StatBar)
- [x] Adaptive breakpoints configured and tested
- [x] Documentation written with usage examples
- [x] Theme applies correctly on Android, Desktop, iOS

**Deliverables:**
- `:core:designsystem-unstyled` module with theme + components
- README with styling patterns
- @Preview functions demonstrating components in light/dark modes

**Commit Checkpoints:**
```bash
# After Task 2.1 (Theme Creation)
git commit -m "feat(designsystem): create Compose Unstyled theme matching Pokémon branding

- Create :core:designsystem-unstyled module
- Implement buildTheme DSL with Pokémon type colors
- Port typography system as token-based styles
- Define ComponentInteractiveSize for touch/non-touch targets
- Material 3 Expressive theme remains in :core:designsystem"

# After Task 2.2 (Component Library)
git commit -m "feat(designsystem): build unstyled component library

- Create PokemonButton (styled wrapper around unstyled Button)
- Create PokemonCard (Box with border/shadow/corner radius)
- Create PokemonText (theme token integration)
- Create TypeBadge (custom component with Pokémon type colors)
- Create StatBar (custom progress indicator for stats)
- All components meet WCAG AA contrast ratios (4.5:1)"

# After Task 2.3 (Adaptive Breakpoints)
git commit -m "feat(designsystem): define adaptive breakpoints and layout utilities

- Use Material 3 WindowSizeClass (Compact/Medium/Expanded)
- Follow official breakpoint standards (0-599dp, 600-839dp, 840dp+)
- Create gridColumns() utility (2/3/4 based on width class)
- Create adaptiveSpacing() utility (8dp/12dp/16dp)
- Create adaptiveNavigationType() utility returns NavigationSuiteType
- Build AdaptiveNavigationScaffold using NavigationSuiteScaffold
- Reference: Material 3 Adaptive best practices"

# After Task 2.4 (Documentation)
git commit -m "docs(designsystem): document unstyled styling patterns

- Create :ui-unstyled/README.md with component usage examples
- Document theme customization guide
- Document responsive layout patterns
- Add comparison table: Material component → Unstyled equivalent"
```

---

### Phase 3: Pokemon List Screen Migration

**Owner:** Screen Implementation Agent (Compose)  
**Duration:** 2-3 days  
**Dependencies:** Phase 2 (theme + components)

#### Tasks:
1. **Implement PokemonListScreen in ui-unstyled**
   - Create `features/pokemonlist/ui-unstyled/src/commonMain/kotlin/.../PokemonListScreen.kt`
   - Replicate logic from [PokemonListScreen.kt](../../features/pokemonlist/ui/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/ui/PokemonListScreen.kt)
   - Replace Material components:
     - `LazyVerticalGrid` (keep same) with unstyled `PokemonCard` items
     - `CircularProgressIndicator` → custom loading spinner
     - `Button` (retry) → `PokemonButton` from designsystem-unstyled
   - Apply custom theme tokens for spacing, colors, typography
   - ViewModel integration remains **identical** (no changes to [PokemonListViewModel.kt](../../features/pokemonlist/presentation/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModel.kt))

2. **Create navigation entry in wiring-ui-unstyled**
   - Create `features/pokemonlist/wiring-ui-unstyled/src/androidMain/kotlin/.../PokemonListNavigationProviders.kt`
   - Use Koin Navigation 3 DSL pattern from [PokemonListNavigationProviders.kt](../../features/pokemonlist/wiring-ui/src/androidMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/wiring/PokemonListNavigationProviders.kt):
     ```kotlin
     val pokemonListUnstyledNavigationModule = module {
         navigation<PokemonList> { route ->
             val navigator: Navigator = koinInject()
             val viewModel = koinViewModel<PokemonListViewModel>()
             val lifecycleOwner = LocalLifecycleOwner.current
             
             DisposableEffect(Unit) {
                 lifecycleOwner.lifecycle.addObserver(viewModel)
                 onDispose { lifecycleOwner.lifecycle.removeObserver(viewModel) }
             }
             
             PokemonListScreen(
                 viewModel = viewModel,
                 onPokemonClick = { pokemon -> navigator.goTo(PokemonDetail(pokemon.id)) }
             )
         }
     }
     ```
   - Preserve lifecycle registration pattern (ViewModel implements `DefaultLifecycleObserver`)

3. **Add responsive grid layout**
   - **Use WindowSizeClass to determine columns** (Material 3 Adaptive pattern):
     ```kotlin
     val windowSizeClass = calculateWindowSizeClass()
     val columns = when (windowSizeClass.widthSizeClass) {
         WindowWidthSizeClass.Compact -> 2   // < 600dp (phone)
         WindowWidthSizeClass.Medium -> 3    // 600-839dp (tablet portrait)
         WindowWidthSizeClass.Expanded -> 4  // >= 840dp (tablet landscape, desktop)
         else -> 2
     }
     ```
   - **Implement adaptive padding** following Material guidelines:
     - Compact: 8dp (maximize content on small screens)
     - Medium: 16dp (comfortable spacing)
     - Expanded: 24dp (generous spacing for large screens)
   - **Test across all window size classes:**
     - Android phone portrait/landscape (Compact/Medium)
     - Android tablet split-screen (responsive resize)
     - Android tablet free-form windows (all size classes)
     - Desktop with window resize (840dp → 1920dp+)
     - iOS (compact/regular size classes map to WindowSizeClass)
   - **Resources:**
     - [Adaptive Grid Layouts](https://developer.android.com/develop/ui/compose/layouts/adaptive/grid-lists)

4. **Create @Preview functions with multiple states**
   - Add to PokemonListScreen.kt:
     ```kotlin
     @Preview
     @Composable
     private fun PokemonListScreenLoadingPreview() {
         PokemonUnstyledTheme {
             PokemonListScreen(
                 uiState = PokemonListUiState.Loading,
                 onUiEvent = {},
                 onNavigate = {}
             )
         }
     }
     
     @Preview
     @Composable
     private fun PokemonListScreenContentPreview() {
         PokemonUnstyledTheme {
             PokemonListScreen(
                 uiState = PokemonListUiState.Content(
                     pokemons = samplePokemons(),
                     hasMore = true
                 ),
                 onUiEvent = {},
                 onNavigate = {}
             )
         }
     }
     
     @Preview
     @Composable
     private fun PokemonListScreenErrorPreview() {
         PokemonUnstyledTheme {
             PokemonListScreen(
                 uiState = PokemonListUiState.Error("Network error"),
                 onUiEvent = {},
                 onNavigate = {}
             )
         }
     }
     ```
   - Required by [testing_strategy.md](../tech/testing_strategy.md#screenshot-testing-roborazzi)

**Acceptance Criteria:**
- [x] PokemonListScreen implemented with unstyled components
- [x] Grid layout adapts to screen size (2/3/4 columns)
- [x] Loading, content, error states render correctly
- [x] @Preview functions added for all states
- [x] Navigation entry registered with Koin
- [x] ViewModel integration unchanged (shared with Material)
- [x] Infinite scroll pagination works identically to Material version

**Test Commands:**
```bash
./gradlew :composeApp:assembleDebug test --continue
```

**Commit Checkpoints:**
```bash
# After Task 3.1 (Screen Implementation)
git commit -m "feat(pokemonlist): implement PokemonListScreen with unstyled components

- Create ui-unstyled/PokemonListScreen.kt
- Replace Material components with PokemonCard, PokemonButton
- Apply custom theme tokens for spacing/colors/typography
- ViewModel integration unchanged (shared with Material variant)
- LazyVerticalGrid logic preserved"

# After Task 3.2 (Navigation Registration)
git commit -m "feat(pokemonlist): register unstyled navigation entry

- Create wiring-ui-unstyled/PokemonListNavigationProviders.kt
- Use Koin Navigation 3 DSL pattern
- Wire PokemonListScreen composable
- Preserve lifecycle registration with DisposableEffect
- Same ViewModel as Material variant"

# After Task 3.3 (Responsive Layout)
git commit -m "feat(pokemonlist): add responsive grid layout to unstyled variant

- Use WindowSizeClass to adjust columns (2/3/4)
- Implement adaptive padding based on screen size
- Test on Android phone portrait/landscape
- Test on Android tablet split-screen + free-form
- Test on Desktop with window resize"

# After Task 3.4 (Previews)
git commit -m "feat(pokemonlist): add @Preview functions for unstyled screen

- Add loading state preview
- Add content state preview with sample data
- Add error state preview
- All previews use PokemonUnstyledTheme"
```

---

### Phase 4: Pokemon Detail Screen Migration

**Owner:** Screen Implementation Agent (Compose)  
**Duration:** 3-4 days  
**Dependencies:** Phase 3 (list screen + components)

#### Tasks:
1. **Implement PokemonDetailScreen in ui-unstyled**
   - Create `features/pokemondetail/ui-unstyled/src/commonMain/kotlin/.../PokemonDetailScreen.kt`
   - Replicate [PokemonDetailScreen.kt](../../features/pokemondetail/ui/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/ui/PokemonDetailScreen.kt) structure:
     - Header: Large Pokémon image + name + ID
     - Type badges: Use `TypeBadge` component with Pokémon colors
     - Physical attributes: Height/weight in styled cards
     - Abilities: List with hidden ability indicator
     - Base stats: Custom `StatBar` component (horizontal progress bars)
   - Replace Material components:
     - `Card` → `PokemonCard` (custom Box + styling)
     - `LinearProgressIndicator` → `StatBar` (custom painted canvas)
     - `AssistChip` (type badges) → `TypeBadge` component
   - Maintain parametric ViewModel integration (no changes to [PokemonDetailViewModel.kt](../../features/pokemondetail/presentation/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/presentation/PokemonDetailViewModel.kt))

2. **Create navigation entry with animations**
   - Create `features/pokemondetail/wiring-ui-unstyled/src/androidMain/kotlin/.../PokemonDetailNavigationProviders.kt`
   - Register with metadata-based transitions:
     ```kotlin
     val pokemonDetailUnstyledNavigationModule = module {
         navigation<PokemonDetail>(
             metadata = NavDisplay.transitionSpec(
                 slideInHorizontally(initialOffsetX = { it }) + fadeIn(tween(300))
             ) + NavDisplay.popTransitionSpec(
                 slideOutHorizontally(targetOffsetX = { it }) + fadeOut(tween(300))
             )
         ) { route ->
             val navigator: Navigator = koinInject()
             val viewModel = koinViewModel<PokemonDetailViewModel>(
                 key = "pokemon_detail_unstyled_${route.id}"
             ) { parametersOf(route.id) }
             val lifecycleOwner = LocalLifecycleOwner.current
             
             DisposableEffect(route.id) {
                 lifecycleOwner.lifecycle.addObserver(viewModel)
                 onDispose { lifecycleOwner.lifecycle.removeObserver(viewModel) }
             }
             
             PokemonDetailScreen(
                 pokemonId = route.id,
                 viewModel = viewModel,
                 onBack = { navigator.goBack() }
             )
         }
     }
     ```
   - Scope ViewModel with unique key per Pokémon ID as per [Navigation 3 pattern](../tech/critical_patterns_quick_ref.md#navigation-3-pattern)
   - Preserve lifecycle registration scoped to route instance

3. **Implement adaptive detail layout**
   - **Responsive width using WindowSizeClass:**
     ```kotlin
     val windowSizeClass = calculateWindowSizeClass()
     val detailWidth = when (windowSizeClass.widthSizeClass) {
         WindowWidthSizeClass.Compact -> Modifier.fillMaxWidth()        // < 600dp: Full width
         WindowWidthSizeClass.Medium -> Modifier.fillMaxWidth(0.75f)    // 600-839dp: 75% width
         WindowWidthSizeClass.Expanded -> Modifier.widthIn(max = 800.dp) // >= 840dp: Max 800dp centered
         else -> Modifier.fillMaxWidth()
     }
     ```
   - **Stat visualization adapts** (canonical pattern):
     - Compact: Vertical list of horizontal stat bars (optimize vertical space)
     - Medium: Two-column grid with horizontal bars (utilize horizontal space)
     - Expanded: Two-column grid with enhanced spacing
   - **Center content on large screens** with `Modifier.widthIn()` + horizontal padding
   - **Follow list-detail pattern** for future multi-pane layouts
   - **Resources:**
     - [Adaptive Panes and Content Layouts](https://developer.android.com/develop/ui/compose/layouts/adaptive/panes)

4. **Add @Preview variations**
   - Loading state preview (skeleton UI)
   - Success state with realistic Pokémon data
   - Error state with retry button
   - Enables Roborazzi screenshot testing per [testing_strategy.md](../tech/testing_strategy.md)

**Acceptance Criteria:**
- [x] PokemonDetailScreen implemented with unstyled components
- [x] Type badges use official Pokémon colors
- [x] Stat bars visualize values correctly (max 255)
- [x] Layout adapts to screen size (full-width → centered)
- [x] Navigation animations match Material version
- [x] @Preview functions added for all states
- [x] Navigation entry registered with Koin
- [x] ViewModel integration unchanged (parametric pattern preserved)

**Test Commands:**
```bash
./gradlew :features:pokemondetail:ui-unstyled:check
./gradlew :composeApp:assembleDebug test --continue
```

**Commit Checkpoints:**
```bash
# After Task 4.1 (Screen Implementation)
git commit -m "feat(pokemondetail): implement PokemonDetailScreen with unstyled components

- Create ui-unstyled/PokemonDetailScreen.kt
- Implement header (image + name + ID)
- Implement TypeBadge components with Pokémon colors
- Implement physical attributes cards
- Implement abilities list with hidden ability indicator
- Implement StatBar components for base stats
- ViewModel integration unchanged (parametric pattern preserved)"

# After Task 4.2 (Navigation Registration)
git commit -m "feat(pokemondetail): register unstyled navigation entry with animations

- Create wiring-ui-unstyled/PokemonDetailNavigationProviders.kt
- Register navigation<PokemonDetail> with metadata-based transitions
- Scope ViewModel with unique key (pokemon_detail_unstyled_${route.id})
- Preserve lifecycle registration with DisposableEffect(route.id)
- Navigation animations: slideInHorizontally + fadeIn"

# After Task 4.3 (Adaptive Layout)
git commit -m "feat(pokemondetail): implement adaptive detail layout for unstyled variant

- Responsive width (full-width mobile, 75% tablet, 800dp desktop)
- Stat visualization adapts (vertical list → two-column grid)
- Center content on large screens with max-width constraint
- Test on multiple screen sizes"

# After Task 4.4 (Previews)
git commit -m "feat(pokemondetail): add @Preview variations for unstyled screen

- Add loading state preview (skeleton UI)
- Add success state preview with realistic Pokémon data
- Add error state preview with retry button
- All previews use PokemonUnstyledTheme"
```

---

### Phase 5: Root Navigation & Theme Switcher

**Owner:** Screen Implementation Agent (Compose) + Product Design Agent  
**Duration:** 2-3 days  
**Dependencies:** Phase 3 & 4 (both screens migrated), Phase 1.1 (UX design complete)

#### Tasks:
1. **Implement UX design from Phase 1.1 in App.kt**
   - Update [composeApp/src/commonMain/kotlin/App.kt](../../composeApp/src/commonMain/kotlin/App.kt):
     ```kotlin
     @Composable
     fun App() {
         val windowSizeClass = calculateWindowSizeClass()
         val navType = when (windowSizeClass.widthSizeClass) {
             WindowWidthSizeClass.Compact -> NavigationSuiteType.NavigationBar
             WindowWidthSizeClass.Medium -> NavigationSuiteType.NavigationRail
             WindowWidthSizeClass.Expanded -> NavigationSuiteType.NavigationDrawer
             else -> NavigationSuiteType.NavigationBar
         }
         
         var selectedDesignSystem by rememberSaveable { mutableStateOf(DesignSystem.MATERIAL) }
         
         NavigationSuiteScaffold(
             navigationSuiteItems = {
                 item(
                     selected = selectedDesignSystem == DesignSystem.MATERIAL,
                     onClick = { selectedDesignSystem = DesignSystem.MATERIAL },
                     icon = { Icon(Icons.Default.Palette, "Material World") },
                     label = { Text("Material") }
                 )
                 item(
                     selected = selectedDesignSystem == DesignSystem.UNSTYLED,
                     onClick = { selectedDesignSystem = DesignSystem.UNSTYLED },
                     icon = { Icon(Icons.Default.BrushOutlined, "Unstyled World") },
                     label = { Text("Unstyled") }
                 )
             },
             layoutType = navType
         ) {
             when (selectedDesignSystem) {
                 DesignSystem.MATERIAL -> MaterialWorld()
                 DesignSystem.UNSTYLED -> UnstyledWorld()
             }
         }
     }
     ```
   - Define two destinations (Material World, Unstyled World)
   - Icons: Material (Palette), Unstyled (Brush/Design)

2. **Create theme switcher state management**
   - Create `DesignSystem` enum:
     ```kotlin
     enum class DesignSystem {
         MATERIAL,
         UNSTYLED
     }
     ```
   - Use `rememberSaveable` for runtime state
   - Persist preference across app restarts using SavedStateHandle in app-level ViewModel:
     ```kotlin
     class AppViewModel(
         private val savedStateHandle: SavedStateHandle
     ) : ViewModel() {
         var designSystem by savedStateHandle.saved { DesignSystem.MATERIAL }
     }
     ```
   - References: [ViewModel Pattern](../tech/critical_patterns_quick_ref.md#viewmodel-pattern), [di_patterns.md](../patterns/di_patterns.md#savedstatehandle-in-viewmodels)

3. **Wire separate navigation graphs per design system**
   - Create `MaterialWorld()` composable:
     ```kotlin
     @Composable
     fun MaterialWorld() {
         val navigator: Navigator = koinInject()
         val materialEntryProvider = koinEntryProvider(
             modules = listOf(
                 pokemonListMaterialNavigationModule,
                 pokemonDetailMaterialNavigationModule
             )
         )
         
         MaterialTheme {
             NavDisplay(
                 backStack = navigator.backStack,
                 onBack = { navigator.goBack() },
                 entryProvider = materialEntryProvider
             )
         }
     }
     ```
   - Create `UnstyledWorld()` composable (similar structure with unstyled modules + `PokemonUnstyledTheme`)
   - Both use same `Navigator` instance (shared back stack abstraction)
   - Same route objects (`PokemonList`, `PokemonDetail(id)`) work in both worlds

4. **Implement smooth transitions between design system switches**
   - Crossfade animation when switching worlds:
     ```kotlin
     Crossfade(
         targetState = selectedDesignSystem,
         animationSpec = tween(300)
     ) { designSystem ->
         when (designSystem) {
             DesignSystem.MATERIAL -> MaterialWorld()
             DesignSystem.UNSTYLED -> UnstyledWorld()
         }
     }
     ```
   - Preserve scroll position/state: Use SavedStateHandle in ViewModels (already implemented)
   - Handle deep links: Extract Pokémon ID from URI, navigate to detail in chosen design system

**Acceptance Criteria:**
- [x] Adaptive navigation scaffold implemented (bottom bar → rail → drawer)
- [x] Two navigation destinations created (Material World, Unstyled World)
- [x] Design system selection persists across app restarts
- [x] Separate navigation graphs wired per design system
- [x] Crossfade animation between worlds
- [x] Scroll position preserved when returning to list
- [x] Both worlds share same Navigator and route objects

**Test Scenarios:**
- Mobile (Compact): Theme switching works as designed in Phase 1.1
- Tablet (Medium): Adaptive navigation transitions properly
- Desktop (Expanded): Theme switching and navigation work together
- Switch between design systems: Smooth transitions, state preserved
- Navigate to detail in Material → switch to Unstyled → verify behavior

**Commit Checkpoints:**
```bash
# After Task 5.1 (UX Implementation)
git commit -m "feat(app): implement theme switching UX from Phase 1.1 design

- Implement UX approach designed by UI/UX agent
- Create DesignSystem enum (MATERIAL, UNSTYLED)
- Add adaptive navigation scaffold (bottom bar → rail → drawer)
- Configure navigation suite items per design
- Test theme switching interaction flow"

# After Task 5.2 (State Management)
git commit -m "feat(app): implement theme switcher state management

- Create AppViewModel with SavedStateHandle
- Persist design system preference across restarts
- Use rememberSaveable for runtime state
- Implement state restoration on app restart"

# After Task 5.3 (Navigation Graphs)
git commit -m "feat(app): wire separate navigation graphs per design system

- Create MaterialWorld() composable with Material modules
- Create UnstyledWorld() composable with Unstyled modules
- Both use same Navigator instance (shared back stack)
- Same route objects work in both worlds (PokemonList, PokemonDetail)
- Verify navigation consistency"

# After Task 5.4 (Transitions)
git commit -m "feat(app): implement smooth transitions between design systems

- Add Crossfade animation when switching worlds (300ms)
- Preserve scroll position via SavedStateHandle in ViewModels
- Handle deep links to specific screens in chosen design system
- Test state preservation during theme switches"
```

---

### Phase 6: Testing & Validation

**Owner:** Testing Strategy Agent  
**Duration:** 1 day  
**Dependencies:** Phase 5 (complete app)

**Note:** Screenshot testing deferred to Phase 8 (Future Enhancements). Focus on functional validation.

#### Tasks:
1. **Verify existing test suite (no screenshot tests yet)**
   - Create `composeApp/src/test/kotlin/.../screenshots/UnstyledScreenshotTests.kt`:
     ```kotlin
     @RunWith(RobolectricTestRunner::class)
     class UnstyledScreenshotTests {
         @get:Rule
         val composeTestRule = createComposeRule()
         
         @Test
         fun pokemonListScreen_unstyled_loading() {
             composeTestRule.setContent {
                 PokemonUnstyledTheme {
                     PokemonListScreen(
                         uiState = PokemonListUiState.Loading,
                         onUiEvent = {},
                         onNavigate = {}
                     )
                 }
             }
             composeTestRule.captureRoboImage("unstyled/pokemon_list_loading.png")
         }
         
         // Similar tests for content, error states, detail screen
     }
     ```
   - Record baselines: `./gradlew recordRoborazziDebug`
   - Covers:
     - List screen: Loading, content (2/3/4 column variants), error
     - Detail screen: Loading, success, error with retry
     - Navigation scaffold: Bottom bar, rail, drawer

2. **Verify shared logic remains unchanged**
   - Run existing test suite: `./gradlew :composeApp:assembleDebug test --continue`
   - Confirm all 84 tests pass (34 property + 50 concrete)
   - Verify:
     - Repository tests unaffected ([PokemonListRepositoryTest.kt](../../features/pokemonlist/data/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/data/PokemonListRepositoryTest.kt))
     - ViewModel tests unaffected ([PokemonListViewModelTest.kt](../../features/pokemonlist/presentation/src/androidUnitTest/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModelTest.kt))
     - Property tests still pass (mappers, HTTP error codes, state transitions)
   - No regressions in shared business logic

3. **Test adaptive layouts across sizes**
   - Android phone (portrait/landscape): Bottom navigation bar, 2 columns
   - Android tablet (10"): Navigation rail, 3 columns
   - Android tablet split-screen: Adapts to compact width class
   - Android tablet free-form windows: Tests all size classes
   - Desktop 1200×800: Navigation rail, 3 columns
   - Desktop 1920×1080: Navigation drawer, 4 columns
   - Desktop window resize: Live adaptation as width changes
   - Verify:
     - Navigation type changes smoothly (no crashes)
     - Grid columns adjust correctly
     - Content reflows without clipping

4. **iOS Strategy**
   - **SwiftUI app (iosApp)**: **UNTOUCHED** — continues using native SwiftUI views
     - Uses shared ViewModels via :shared framework (no changes needed)
     - No dual-UI implementation (native iOS stays native)
   - **iOS Compose app (iosAppCompose)**: Works like any other Compose app
     - Uses both `:ui-material` and `:ui-unstyled` modules
     - Theme switching works identically to Android/Desktop
     - Test both design systems on iOS Compose when available
   - **Validation approach**: Test on Android + Desktop during development, iOS Compose as final verification

**Acceptance Criteria:**
- [x] All existing tests pass (84/84 - ViewModels, repositories, mappers)
- [x] Adaptive layouts validated on multiple screen sizes (Android, Desktop)
- [x] No regressions in shared logic (ViewModels/repositories)
- [x] Both design systems work identically (behavior parity, UI differences only)
- [x] iOS Compose app builds successfully (manual verification)
- [x] SwiftUI app unaffected (no changes)

**Commands:**
```bash
# Primary validation
./gradlew :composeApp:assembleDebug test --continue

# iOS (only if needed)
open iosApp/iosApp.xcodeproj
open iosAppCompose/iosAppCompose.xcodeproj
```

**Commit Checkpoints:**
```bash
# After Task 6.1 (Test Suite Verification)
git commit -m "test: verify existing test suite passes with dual-UI changes

- All 84 tests pass (34 property + 50 concrete)
- Repository tests unaffected
- ViewModel tests unaffected
- Property tests (mappers, HTTP codes, state transitions) pass
- No regressions in shared business logic"

# After Task 6.2 (Adaptive Layout Testing)
git commit -m "test: validate adaptive layouts across screen sizes

- Android phone (portrait/landscape): Verified
- Android tablet (10\"): Navigation adapts correctly
- Android tablet split-screen: Verified
- Android tablet free-form windows: Tested all size classes
- Desktop 1200×800: Verified
- Desktop 1920×1080: Verified
- Desktop window resize: Live adaptation works"

# After Task 6.3 (Behavior Parity)
git commit -m "test: verify behavior parity between Material and Unstyled

- List screen: Pagination works identically
- Detail screen: Data rendering identical
- Navigation: Same flow in both design systems
- State management: Same behavior (SavedStateHandle)
- Error handling: Same error states displayed"

# After Task 6.4 (iOS Verification)
git commit -m "test: verify iOS apps with dual-UI changes

- SwiftUI app (iosApp): Unaffected, builds successfully
- iOS Compose app (iosAppCompose): Both design systems work
- Manual verification completed
- No iOS-specific issues found"
```

---

### Phase 7: Documentation & Iteration

**Owner:** Documentation Agent  
**Duration:** 1-2 days  
**Dependencies:** Phase 6 (testing complete)

#### Tasks:
1. **Create comprehensive guide**
   - Write `docs/project/dual_ui_architecture.md`:
     - Overview: Why dual-UI, benefits, architecture strategy
     - Module organization: Diagram + rationale for side-by-side UI modules
     - Theme switching mechanism: Code examples, state management
     - Adding features to both design systems: Step-by-step checklist
     - Decision matrix: When to use Material vs Unstyled
     - Comparison table: Component mapping (Material → Unstyled)
     - Performance considerations: Build times, app size impact
     - Future considerations: Adding more design systems (e.g., Cupertino)

2. **Update agent routing**
   - Add to [docs/agent-prompts/README.md](../agent-prompts/README.md):
     ```markdown
     | 🎨 Compose Unstyled | Screen (Unstyled) | ui_ux_system_agent_for_unstyled_screen_DELTA.md | Implement Compose Unstyled screens |
     ```
   - Create `ui_ux_system_agent_for_unstyled_screen_DELTA.md`:
     - Includes base agent prompt + canonical links
     - Delta: Focus on unstyled component usage, theme tokens, manual styling
     - References: `designsystem-unstyled` component library, theme builder patterns
   - Update [AGENTS.md](../../AGENTS.md) routing table
   - Sync changes to [.github/copilot-instructions.md](../../.github/copilot-instructions.md) and [.junie/guidelines.md](../../.junie/guidelines.md)

3. **Add Compose Unstyled patterns**
   - Create `docs/patterns/unstyled_patterns.md`:
     - Theme builder usage: `buildTheme { }` DSL, custom properties
     - Unstyled component wrapping: Creating styled variants
     - Responsive layout utilities: WindowSizeClass, adaptive spacing
     - Accessibility: ARIA semantics, keyboard navigation
     - Contrast with Material patterns: Side-by-side examples
   - Link from [critical_patterns_quick_ref.md](../tech/critical_patterns_quick_ref.md):
     ```markdown
     | **Unstyled UI** | Use theme tokens, manual styling, composable primitives | unstyled_patterns.md |
     ```

4. **Update PRD with dual-UI goals**
   - Modify [prd.md](prd.md):
     - Add section: "Design System Showcase Feature"
     - User value proposition: Learn design systems, compare implementations
     - Acceptance criteria: Both design systems work identically, adaptive nav
     - Update UI guidelines: Add Compose Unstyled references
     - Add comparison screenshots (Material vs Unstyled side-by-side)

**Acceptance Criteria:**
- [x] Dual-UI architecture guide written (comprehensive)
- [x] Agent routing updated (new Compose Unstyled agent)
- [x] Unstyled patterns documented with examples
- [x] PRD updated with showcase feature goals
- [x] All entrypoints synced (AGENTS.md, Copilot, Junie)

**Deliverables:**
- `docs/project/dual_ui_architecture.md` (new)
- `docs/agent-prompts/ui_ux_system_agent_for_unstyled_screen_DELTA.md` (new)
- `docs/patterns/unstyled_patterns.md` (new)
- Updated: `prd.md`, `AGENTS.md`, `copilot-instructions.md`, `guidelines.md`

**Commit Checkpoints:**
```bash
# After Task 7.1 (Architecture Guide)
git commit -m "docs(architecture): create comprehensive dual-UI architecture guide

- Document why dual-UI, benefits, architecture strategy
- Explain module organization with diagrams
- Document theme switching mechanism with code examples
- Provide step-by-step checklist for adding features
- Add decision matrix: When to use Material vs Unstyled
- Include component mapping table
- Document performance considerations"

# After Task 7.2 (Agent Routing)
git commit -m "docs(agents): add Compose Unstyled screen agent to routing

- Create ui_ux_system_agent_for_unstyled_screen_DELTA.md
- Update agent routing table in docs/agent-prompts/README.md
- Sync changes to AGENTS.md
- Sync changes to .github/copilot-instructions.md
- Sync changes to .junie/guidelines.md
- Add unstyled component usage patterns to agent prompt"

# After Task 7.3 (Pattern Documentation)
git commit -m "docs(patterns): document Compose Unstyled patterns

- Create docs/patterns/unstyled_patterns.md
- Document buildTheme DSL usage
- Document unstyled component wrapping patterns
- Document responsive layout utilities
- Document accessibility (ARIA semantics, keyboard nav)
- Add side-by-side comparison with Material patterns
- Link from critical_patterns_quick_ref.md"

# After Task 7.4 (PRD Update)
git commit -m "docs(prd): update PRD with dual-UI showcase feature

- Add \"Design System Showcase Feature\" section
- Document user value proposition
- Define acceptance criteria
- Update UI guidelines with Compose Unstyled references
- Add comparison screenshots placeholder"
```

---

## Further Considerations

### 1. Module Naming Convention
**Question:** Current proposal uses `:ui-material` / `:ui-unstyled`. Alternative: `:ui-m3` / `:ui-unstyled` for brevity?

**Impact:** Affects 8 modules (2 features × 2 UI variants × 2 layers: ui + wiring-ui)

**Recommendation:** Use `:ui-material` / `:ui-unstyled` for clarity. "Material" is more recognizable than "M3" for new developers.

**Decision Matrix:**
| Option | Pros | Cons |
|--------|------|------|
| `:ui-material` | Clear, explicit, self-documenting | Slightly longer |
| `:ui-m3` | Shorter, follows Google naming | Requires context (what is M3?) |
| `:ui-material3` | Most explicit | Longest, redundant (Material implies 3) |

**Action:** Document decision in `dual_ui_architecture.md`.

---

### 2. Theme Switching UX Approach
**Status:** ✅ **RESOLVED** — Delegated to UI/UX Design Agent in Phase 1.1

**Problem:** Bottom bar destinations approach creates inconsistency (scaffold theme != screen theme)

**Solution:** UI/UX Design Agent will evaluate options and provide UX design document with:
- Wireframes and interaction flows
- Evaluation of consistency vs discoverability trade-offs
- Recommendation with rationale
- Implementation guidance for Phase 5

**Options under consideration:**
- Top-level theme wrapper (entire app switches)
- Separate navigation graphs with transition animation
- Split-screen comparison (desktop/tablet)
- Theme selector screen before main app
- Other creative solutions

**Action:** Phase 1.1 must complete before Phase 5 implementation.

---

### 3. Compose Unstyled Component Parity
**Question:** Material has ~40 components, Unstyled has ~15 primitives. Which Material components need unstyled equivalents?

**Current Features Use:**
- **Pokemon List**: Card, Button (retry), CircularProgressIndicator
- **Pokemon Detail**: Card, AssistChip (type badges), LinearProgressIndicator (stats), Button (retry)

**Priority Components for Unstyled Library:**
1. ✅ **Card** → `PokemonCard` (Box + border/shadow/radius)
2. ✅ **Button** → `PokemonButton` (Unstyled Button + styling)
3. ✅ **TypeBadge** → Custom (Chip alternative)
4. ✅ **StatBar** → Custom (LinearProgressIndicator alternative)
5. ⚠️ **LoadingSpinner** → Custom (CircularProgressIndicator alternative)

**Defer to Future:**
- BottomSheet (not in current features)
- Dialog/AlertDialog (not in current features)
- TextField/SearchBar (not in current features)

**Action:** Phase 2 builds priority components only. Add others as features require.

---

### 4. iOS Strategy for Dual-UI
**Question:** Should iOS apps showcase both design systems?

**Current Setup:**
- **SwiftUI app (iosApp)**: Production app, uses native SwiftUI views with Direct Integration pattern
- **iOS Compose app (iosAppCompose)**: Experimental, uses Compose UI from `:ui` modules

**Options:**
- **A) SwiftUI app stays native** (unaffected by dual-UI)
- **B) iOS Compose app showcases both** (Material + Unstyled)
- **C) Both iOS apps support dual-UI** (native + Compose variants)

**Recommendation:** 
- Phase 1-6: Focus on Android + Desktop (faster iteration)
- Phase 7: iOS Compose app (iosAppCompose) showcases both design systems
- SwiftUI app (iosApp) continues using native Material-inspired views

**Rationale:**
- iOS Compose valuable for KMP advocacy (shows Compose Multiplatform capabilities)
- SwiftUI app stays production-ready (no experimental features)
- Adds iOS-specific testing complexity — defer unless stakeholder requests

**Action:** Include in Phase 7 documentation as optional enhancement.

---

### Phase 8: Future Enhancements (Deferred)

**Owner:** Testing Strategy Agent  
**Duration:** 1-2 days  
**Dependencies:** Phase 1-7 complete

**Status:** 🔵 Future Work — Not in Initial Implementation

#### Tasks:
1. **Add screenshot tests for both design systems**
   - Set up Roborazzi for Material 3 Expressive screens
   - Set up Roborazzi for Compose Unstyled screens
   - Record baselines for list screen (loading, content, error)
   - Record baselines for detail screen (loading, success, error)
   - Record baselines for navigation scaffold (bottom bar, rail, drawer)
   - Cover responsive layouts (2/3/4 column grid variants)
   - Run: `./gradlew recordRoborazziDebug`
   - Verify: `./gradlew verifyRoborazziDebug`

2. **Add visual regression testing to CI/CD**
   - Configure GitHub Actions to run screenshot tests on PRs
   - Fail builds on visual regressions
   - Allow baseline updates with approval

**Rationale for Deferral:**
- Functional validation sufficient for initial implementation
- Screenshot testing adds complexity without immediate value
- Can be added incrementally after core features stabilize
- Allows faster iteration on UX design (Phase 1.1 decision)

---

### 5. Adaptive Layout Best Practices
**Status:** ✅ **APPLY** — Follow official Material 3 Adaptive guidelines throughout implementation

**Key Principles:**
1. **Use WindowSizeClass** — Not custom breakpoints (ensures consistency)
2. **NavigationSuiteScaffold** — Handles adaptive nav automatically (bar/rail/drawer)
3. **List-Detail Pattern** — Plan for future multi-pane layouts on large screens
4. **Test on Real Devices** — Emulators don't capture all adaptive behaviors
5. **Responsive Typography** — Consider font scaling (accessibility)

**Official Documentation:**
- [Build Adaptive Apps (Android)](https://developer.android.com/develop/ui/compose/build-adaptive-apps)
- [Compose Adaptive Layouts (Kotlin)](https://kotlinlang.org/docs/multiplatform/compose-adaptive-layouts.html)
- [Material 3 Adaptive Navigation](https://developer.android.com/develop/ui/compose/layouts/adaptive/navigation-suite-scaffold)
- [Window Size Classes](https://developer.android.com/develop/ui/compose/layouts/adaptive#window-size-classes)
- [Adaptive Grid/List Layouts](https://developer.android.com/develop/ui/compose/layouts/adaptive/grid-lists)
- [Adaptive Panes](https://developer.android.com/develop/ui/compose/layouts/adaptive/panes)

**Testing Matrix:**
| Device Type | Width Class | Navigation | Grid Columns | Detail Width |
|-------------|-------------|------------|--------------|-------------|
| Phone Portrait | Compact | Bottom Bar | 2 | Full width |
| Phone Landscape | Medium | Rail | 3 | 75% width |
| Tablet Portrait | Medium | Rail | 3 | 75% width |
| Tablet Landscape | Expanded | Drawer | 4 | 800dp max |
| Desktop < 840dp | Medium | Rail | 3 | 75% width |
| Desktop >= 840dp | Expanded | Drawer | 4 | 800dp max |
| Desktop >= 1240dp | Expanded | Drawer | 4-6 | Consider list-detail |

**Action:** Reference these docs in Phase 2 (design), Phase 3-4 (implementation), Phase 6 (testing).

---

### 6. Desktop Window Sizing Defaults
**Question:** What's optimal starting window size for side-by-side design system comparison?

**Test Matrix:**
| Resolution | Width Class | Nav Type | Grid Columns | Notes |
|------------|------------|----------|--------------|-------|
| 1200×800 | Medium | Rail | 3 | Good balance |
| 1400×900 | Medium | Rail | 3 | More comfortable |
| 1920×1080 | Expanded | Drawer | 4 | Full experience |
| 2560×1440 | Expanded | Drawer | 4 | High-DPI displays |

**Recommendation:** Default to 1400×900 (shows rail + 3 columns, comfortable for development).

**Implementation:**
```kotlin
// composeApp/src/jvmMain/kotlin/main.kt
fun main() = application {
    Window(
        title = "Pokédex - Material vs Unstyled",
        state = rememberWindowState(
            width = 1400.dp,
            height = 900.dp,
            position = WindowPosition(Alignment.Center)
        )
    ) {
        App()
    }
}
```

**Action:** Configure in Phase 5, document in `dual_ui_architecture.md`.

---

## Implementation Tracking

### Phase Checklist

- [ ] **Phase 1:** Architecture & Navigation Foundation (2-3 days)
  - [ ] **Phase 1.1:** Theme Switching UX Design (UI/UX Design Agent) — CRITICAL
  - [ ] Phase 1.2-1.5: Module refactoring, dependencies, navigation structure
- [ ] **Phase 2:** Design System & Theme Architecture (2-3 days)
- [ ] **Phase 3:** Pokemon List Screen Migration (2-3 days)
- [ ] **Phase 4:** Pokemon Detail Screen Migration (3-4 days)
- [ ] **Phase 5:** Root Navigation & Theme Switcher (2-3 days)
- [ ] **Phase 6:** Testing & Validation (1 day) — No screenshot testing
- [ ] **Phase 7:** Documentation & Iteration (1-2 days)
- [ ] **Phase 8:** Future Enhancements (1-2 days) — Screenshot testing (deferred)

**Total Estimated Duration (Phases 1-7):** 13-19 days (2.6-3.8 weeks)  
**Phase 8 (Future):** +1-2 days when prioritized

---

## Specialized Agents Required

| Agent | Phases | Responsibilities |
|-------|--------|-----------------|
| **Product Design Agent** | 1, 5, 7 | Navigation structure, switcher UX, PRD updates |
| **UI/UX Design Agent** | 2 | Theme design, component library, breakpoints |
| **Screen Implementation Agent (Compose)** | 3, 4, 5 | List migration, detail migration, root navigation |
| **KMP Mobile Expert Agent** | 1, 2, 5 | Module refactoring, architecture decisions, graph wiring |
| **Testing Strategy Agent** | 6 | Screenshot tests, validation strategy, coverage |
| **Documentation Agent** | 7 | Comprehensive docs, pattern updates, agent routing |

---

## Success Criteria

**Technical:**
- [x] Both design systems use identical ViewModels/repositories (100% shared logic)
- [x] Material 3 Expressive theme used for Material UI
- [x] Adaptive navigation works across all screen sizes (mobile, tablet, desktop)
- [x] All existing tests pass (84/84, no regressions)
- [x] Build succeeds: `./gradlew :composeApp:assembleDebug test --continue`
- [x] SwiftUI app (iosApp) remains untouched and functional
- [x] iOS Compose app (iosAppCompose) works with both design systems

**User Experience:**
- [x] Smooth navigation between Material and Unstyled worlds
- [x] Design system preference persists across app restarts
- [x] Scroll position/state preserved when switching worlds
- [x] Both UIs look polished (not "demo quality")
- [x] Adaptive layouts feel native on each platform (mobile vs desktop)

**Documentation:**
- [x] Comprehensive dual-UI architecture guide written
- [x] Compose Unstyled patterns documented with examples
- [x] Agent routing updated for new specialized agents
- [x] PRD updated with showcase feature goals

---

## Next Steps

1. **Review plan with team/stakeholders** — gather feedback on approach, priorities
2. **Clarify open questions** (see "Further Considerations" section)
3. **Assign phases to specialized agents** — use agent routing table above
4. **Start Phase 1** — KMP Mobile Expert + Product Design agents

---

**Document Status:** ✅ Ready for Implementation  
**Last Updated:** December 23, 2025  
**Next Review:** After Phase 1 completion (architecture refactored)
