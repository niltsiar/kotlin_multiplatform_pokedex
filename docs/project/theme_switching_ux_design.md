# Theme Switching UX Design

**Last Updated:** December 20, 2025  
**Owner:** UI/UX Design Agent  
**Related:** [DUAL_UI_IMPLEMENTATION_PLAN.md](DUAL_UI_IMPLEMENTATION_PLAN.md)

## Executive Summary

**Problem:** Bottom bar destinations for "Material World" vs "Unstyled World" create visual inconsistency — the navigation scaffold stays in one theme while screen content switches to another.

**Recommendation:** **Option A (Top-Level Theme Wrapper)** with Modal Theme Selector enhancement

**Key Decision:** Entire app switches design systems atomically, including scaffold. Theme state persisted. First-run modal educates users.

**Why:** Best visual consistency, simplest implementation, most adaptive-friendly, aligns with Material 3 Adaptive best practices, clear educational value.

---

## Problem Statement

### User Story
> "As a developer exploring design systems, I want to compare Material 3 Expressive vs Compose Unstyled implementations side-by-side, so I can understand their differences in styling, composition, and behavior."

### Technical Challenge
Using bottom navigation destinations (e.g., "Material World" tab vs "Unstyled World" tab) causes:
- **Visual Inconsistency:** NavigationSuiteScaffold (bottom bar/rail/drawer) themed with one design system, but screen content uses another
- **Adaptive Complications:** WindowSizeClass transitions (Compact → Medium → Expanded) behave differently per design system
- **State Management:** Need to coordinate theme state between scaffold and screens
- **User Confusion:** Unclear when/where design system boundary occurs

### Constraints
1. ✅ **Adaptive Navigation:** Must support Material 3 Adaptive patterns (bottom bar → rail → drawer)
2. ✅ **Native Feel:** Each design system must feel cohesive, not hybrid
3. ✅ **Educational Value:** Users must understand they're comparing design systems
4. ✅ **State Persistence:** Theme choice survives app restart, process death
5. ✅ **Platform Support:** Works on Android, Desktop, iOS Compose (SwiftUI untouched)
6. ✅ **Maintainability:** Simple to implement, test, and extend

---

## Options Evaluation

### Scoring Criteria (1-5 scale)
- **Visual Consistency** (higher = better): How cohesive is the UI across scaffold and screens?
- **Educational Value** (higher = better): How clearly does the UX convey design system comparison?
- **Implementation Complexity** (lower = better): How much code/state management required?
- **User Experience** (higher = better): How intuitive and enjoyable is the interaction?
- **Adaptive Compatibility** (higher = better): How well does it work with Material 3 Adaptive navigation?

| Option | Visual Consistency | Educational Value | Implementation (inverse) | User Experience | Adaptive Compatibility | **Total** |
|--------|-------------------|-------------------|--------------------------|-----------------|------------------------|-----------|
| **A: Top-Level Wrapper** | 5 | 4 | 5 | 5 | 5 | **24/25** ✅ |
| B: Separate Nav Graphs | 5 | 5 | 3 | 3 | 4 | 20/25 |
| C: Split-Screen | 3 | 5 | 3 | 3 | 2 | 16/25 |
| D: Theme Selector Screen | 4 | 3 | 4 | 2 | 5 | 18/25 |
| E: Modal Switcher | 4 | 4 | 4 | 4 | 5 | 21/25 |
| F: Gesture-Based Toggle | 2 | 2 | 2 | 3 | 4 | 13/25 |

---

## Option A: Top-Level Theme Wrapper (RECOMMENDED ✅)

### Description
Entire app (scaffold + screens) switches design systems atomically via a global theme state. User selects theme via:
1. **First-run modal** explaining comparison (onboarding)
2. **Persistent FAB/button** in top bar for quick switching

### Visual Flow

```
[First Launch]
┌─────────────────────────────────┐
│  🎨 Design System Showcase      │
│                                 │
│  Compare two design approaches: │
│                                 │
│  • Material 3 Expressive        │
│    (Opinionated, themed)        │
│                                 │
│  • Compose Unstyled             │
│    (Minimal, raw primitives)    │
│                                 │
│  [Start with Material]          │
│  [Start with Unstyled]          │
└─────────────────────────────────┘

[Subsequent Launches]
┌─────────────────────────────────┐
│  Pokédex ║ [🎨 Switch Theme]   │ ← Top bar with theme toggle
├─────────────────────────────────┤
│                                 │
│  [Material 3 Expressive Theme]  │
│                                 │
│  • Pokemon List Screen          │
│  • (Grid layout, themed cards)  │
│                                 │
└─────────────────────────────────┘
      Nav: [List] [Favorites] [Settings]  ← NavigationSuiteScaffold (adaptive)
```

### Interaction Flow (All Window Size Classes)

**Compact (< 600dp) - Phone:**
```
User taps "🎨 Switch Theme" FAB → 
  Animated theme transition (300ms crossfade) →
  Entire app recomposes with new theme →
  NavigationBar switches from Material → Unstyled (or vice versa) →
  Screen content switches atomically →
  Preference saved to SavedStateHandle
```

**Medium (600-839dp) - Tablet Portrait:**
```
User taps "🎨 Switch Theme" button in top app bar →
  Animated theme transition (300ms crossfade) →
  NavigationRail switches theme →
  Screen content switches atomically →
  Preference saved
```

**Expanded (≥ 840dp) - Tablet Landscape / Desktop:**
```
User taps "🎨 Switch Theme" button in top app bar →
  Animated theme transition (300ms crossfade) →
  PermanentNavigationDrawer switches theme →
  Screen content switches atomically →
  Preference saved
```

### Implementation Guidance

**State Management:**
```kotlin
// Root state (SavedStateHandle-backed)
enum class DesignSystemTheme { MATERIAL, UNSTYLED }

@Composable
fun App() {
    val viewModel: RootViewModel = koinViewModel()
    val currentTheme by viewModel.currentTheme.collectAsState()
    
    // Top-level theme wrapper
    when (currentTheme) {
        MATERIAL -> PokemonTheme { AppContent(viewModel) }
        UNSTYLED -> UnstyledPokemonTheme { AppContent(viewModel) }
    }
}
```

**Theme Toggle Component:**
```kotlin
@Composable
fun ThemeToggleFab(
    currentTheme: DesignSystemTheme,
    onToggle: () -> Unit,
    windowSizeClass: WindowSizeClass
) {
    val icon = if (currentTheme == MATERIAL) Icons.Outlined.Palette else Icons.Outlined.ColorLens
    val label = if (currentTheme == MATERIAL) "Switch to Unstyled" else "Switch to Material"
    
    when (windowSizeClass.widthSizeClass) {
        Compact -> FloatingActionButton(onClick = onToggle) { Icon(icon, label) }
        Medium, Expanded -> IconButton(onClick = onToggle) { Icon(icon, label) }
    }
}
```

**First-Run Modal:**
```kotlin
@Composable
fun DesignSystemIntroDialog(
    onDismiss: (DesignSystemTheme) -> Unit
) {
    AlertDialog(
        title = { Text("🎨 Design System Showcase") },
        text = {
            Column {
                Text("Compare two design approaches:")
                Spacer(Modifier.height(16.dp))
                Text("• Material 3 Expressive\n  (Opinionated, themed)")
                Text("• Compose Unstyled\n  (Minimal, raw primitives)")
            }
        },
        confirmButton = { TextButton(onClick = { onDismiss(MATERIAL) }) { Text("Material") } },
        dismissButton = { TextButton(onClick = { onDismiss(UNSTYLED) }) { Text("Unstyled") } }
    )
}
```

**Navigation 3 Integration:**
```kotlin
@Composable
fun AppContent(viewModel: RootViewModel) {
    val windowSizeClass = calculateWindowSizeClass()
    val navigator: Navigator = koinInject()
    val entryProvider = koinEntryProvider()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pokédex") },
                actions = { ThemeToggleFab(viewModel.currentTheme.value, viewModel::toggleTheme, windowSizeClass) }
            )
        }
    ) { padding ->
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                item(selected = ..., onClick = ..., icon = ..., label = ...)
            }
        ) {
            NavDisplay(
                backStack = navigator.backStack,
                onBack = { navigator.goBack() },
                entryProvider = entryProvider,
                modifier = Modifier.padding(padding)
            )
        }
    }
}
```

### Pros ✅
1. **Perfect Visual Consistency:** No hybrid states, always cohesive
2. **Simple State:** Single `DesignSystemTheme` enum, SavedStateHandle persistence
3. **Adaptive-Friendly:** NavigationSuiteScaffold naturally adapts per WindowSizeClass
4. **Clear Educational Value:** Users understand they're switching entire system
5. **Easy to Test:** Mock theme state, verify recomposition
6. **Performance:** Minimal recomposition (only theme-dependent nodes)

### Cons ⚠️
1. **No Side-by-Side:** Can't compare both themes simultaneously (addressed by Split-Screen variant for desktop)
2. **Requires Toggle Action:** Users must explicitly switch (mitigated by first-run modal)

### Edge Cases
1. **Deep Linking:** Load saved theme, then navigate to requested screen
2. **Process Death:** SavedStateHandle restores theme preference
3. **Back Navigation:** Theme persists across back stack
4. **Orientation Change:** Theme survives configuration changes
5. **Multi-Window (Desktop):** Each window has independent theme state

---

## Option B: Separate Navigation Graphs

### Description
Two complete navigation graphs (MaterialNavGraph, UnstyledNavGraph) with a root selector screen. User picks world, navigates within it, can transition to other world with animation.

### Pros ✅
- Perfect isolation between design systems
- Clear "world" metaphor (enter Material world or Unstyled world)
- Could have unique features per world (easter eggs)

### Cons ⚠️
- **High Complexity:** Duplicate navigation structure for each theme
- **Poor UX:** Extra step to enter app, no quick comparison
- **State Duplication:** Need to sync shared state (favorites, settings) across graphs
- **Adaptive Complexity:** Two NavigationSuiteScaffolds to maintain

**Score:** 20/25 (Good isolation, but too complex)

---

## Option C: Split-Screen Comparison

### Description
Desktop/Tablet landscape only: Show Material on left, Unstyled on right, synchronized scrolling.

### Pros ✅
- **Ultimate Educational Value:** Direct side-by-side comparison
- **No Switching:** Both visible simultaneously

### Cons ⚠️
- **Compact Incompatible:** Doesn't work on phones (majority use case)
- **Poor Mobile UX:** Forces choice between full-screen or no comparison
- **Complexity:** Synchronization logic for scroll/state/navigation

**Score:** 16/25 (Desktop-only, breaks mobile-first principle)

---

## Option D: Theme Selector Screen

### Description
Splash screen asks "Material or Unstyled?" before entering app. User must pick, then entire session uses that theme.

### Pros ✅
- Simple implementation
- Clear initial choice

### Cons ⚠️
- **Poor UX:** Extra friction on every app launch
- **No Quick Switching:** Must restart app to compare
- **Reduced Educational Value:** Can't easily see differences

**Score:** 18/25 (Simple but limiting)

---

## Option E: Hybrid Modal Switcher

### Description
Similar to Option A, but theme toggle opens a modal with previews of both themes before switching.

### Pros ✅
- **Enhanced Educational Value:** User sees preview before committing
- **Smooth Transition:** Modal animates to new theme

### Cons ⚠️
- **Extra Step:** Modal adds friction (but could be skipped after first time)
- **More Complexity:** Need preview rendering logic

**Score:** 21/25 (Good balance, but slightly over-engineered for this use case)

---

## Option F: Gesture-Based Toggle

### Description
Swipe gesture (e.g., two-finger swipe) toggles theme. Hidden Easter egg mechanic.

### Pros ✅
- Fun discovery
- No UI clutter

### Cons ⚠️
- **Discoverability:** Users won't find it without documentation
- **Accessibility:** Gesture-only interaction excludes users
- **Inconsistent Feel:** Not aligned with Material/Unstyled conventions

**Score:** 13/25 (Novel but impractical)

---

## Recommendation: Option A + First-Run Modal

### Decision Rationale

**Why Option A wins:**
1. ✅ **Visual Consistency (5/5):** Entire app (scaffold + screens) always cohesive
2. ✅ **Educational Value (4/5):** Clear system boundary, first-run modal explains purpose
3. ✅ **Implementation (5/5):** Simple state management (SavedStateHandle), no duplication
4. ✅ **User Experience (5/5):** Intuitive toggle, smooth transitions, no friction
5. ✅ **Adaptive Compatibility (5/5):** NavigationSuiteScaffold works naturally across all WindowSizeClass sizes

**Enhancement: First-Run Modal** (adds +1 Educational Value):
- Show modal on first launch explaining comparison purpose
- Let user pick starting theme
- Never show again (preference saved)
- Add "About Design Systems" in settings to re-show modal

### User Journey

**First Launch:**
```
1. App opens → DesignSystemIntroDialog appears
2. User reads: "Compare Material 3 Expressive vs Compose Unstyled"
3. User picks "Material" or "Unstyled"
4. Dialog dismisses, app loads in chosen theme
5. Preference saved: never show dialog again
6. User sees persistent theme toggle (FAB on Compact, button on Medium/Expanded)
```

**Subsequent Launches:**
```
1. App opens directly in last-used theme
2. User browses Pokémon list
3. User taps theme toggle (FAB/button)
4. 300ms crossfade animation
5. Entire app (scaffold + screens) switches theme atomically
6. User continues browsing, sees design system differences
7. User can toggle back instantly
```

**Cross-Platform Experience:**
```
Phone (Compact):
  - NavigationBar at bottom
  - FAB in bottom-right for theme toggle
  - Full-screen content

Tablet Portrait (Medium):
  - NavigationRail on left
  - IconButton in top app bar for theme toggle
  - Content area optimized

Tablet Landscape / Desktop (Expanded):
  - PermanentNavigationDrawer on left
  - IconButton in top app bar for theme toggle
  - Multi-column layouts where appropriate
```

---

## Implementation Checklist (Phase 5)

### 5.1 Root ViewModel with Theme State
- [ ] Create `RootViewModel` with `StateFlow<DesignSystemTheme>`
- [ ] Inject `SavedStateHandle`, use `by saved` delegate for persistence
- [ ] Implement `toggleTheme()` function
- [ ] Expose `currentTheme: StateFlow<DesignSystemTheme>`

### 5.2 Top-Level Theme Wrapper
- [ ] Modify `composeApp/src/commonMain/.../App.kt`
- [ ] Add `when (currentTheme)` branching to wrap entire app
- [ ] Ensure `PokemonTheme` vs `UnstyledPokemonTheme` wraps `AppContent`

### 5.3 First-Run Modal
- [ ] Create `DesignSystemIntroDialog` composable
- [ ] Show on first launch (check preference)
- [ ] Save choice to `SavedStateHandle`
- [ ] Add "About Design Systems" screen in settings to re-show modal

### 5.4 Theme Toggle Component
- [ ] Create `ThemeToggleFab` composable
- [ ] Adapt to WindowSizeClass (FAB on Compact, IconButton on Medium/Expanded)
- [ ] Add to TopAppBar in `AppContent`
- [ ] Animate transition (300ms crossfade)

### 5.5 Adaptive Scaffold Integration
- [ ] Update `NavigationSuiteScaffold` to respect current theme
- [ ] Verify bottom bar → rail → drawer transitions work in both themes
- [ ] Test WindowSizeClass breakpoints (600dp, 840dp)

### 5.6 Testing
- [ ] Unit test: `RootViewModel.toggleTheme()` updates state
- [ ] Unit test: SavedStateHandle persists theme preference
- [ ] Integration test: Theme switches recompose entire tree
- [ ] Screenshot test: Both themes render correctly (Phase 8)
- [ ] Accessibility test: Theme toggle has proper content description

---

## Acceptance Criteria

### Must Have ✅
1. ✅ Entire app switches theme atomically (scaffold + screens)
2. ✅ Theme toggle accessible on all WindowSizeClass sizes
3. ✅ First-run modal explains comparison purpose
4. ✅ Theme preference persists across app restarts
5. ✅ NavigationSuiteScaffold adapts correctly in both themes (bar → rail → drawer)
6. ✅ Smooth transition animation (300ms crossfade)
7. ✅ No hybrid states (scaffold and screen always match theme)

### Should Have 🎯
1. 🎯 Theme toggle uses adaptive positioning (FAB on Compact, button on Medium/Expanded)
2. 🎯 First-run modal can be re-opened from settings
3. 🎯 Theme toggle has haptic feedback on Android
4. 🎯 Transition animation respects reduced motion accessibility setting

### Could Have 💡
1. 💡 Theme toggle shows preview of other theme (brief flash)
2. 💡 "About Design Systems" screen with educational content
3. 💡 Analytics event when user toggles theme
4. 💡 Desktop: Keyboard shortcut (Cmd/Ctrl+T) to toggle theme

---

## Future Enhancements (Post-MVP)

### Phase 8+: Split-Screen Desktop Mode
If desktop users need side-by-side comparison:
- Add "Split-Screen Mode" toggle (desktop/tablet landscape only)
- Show Material on left, Unstyled on right
- Synchronize scroll and navigation between panes
- Add to settings, not default behavior

### Gesture-Based Easter Egg
Add hidden gesture (e.g., triple-tap Poké Ball logo) to toggle theme as fun discovery.

### Theme Comparison Guide
In-app educational content showing specific differences:
- Typography comparison (Google Sans Flex vs raw tokens)
- Color scheme comparison (WCAG contrast ratios)
- Component anatomy (Material button vs Unstyled primitives)

---

## Wireframes

### Compact (Phone) - Material 3 Expressive
```
┌─────────────────────────────────┐
│ Pokédex          [🎨]          │ ← Top bar with theme toggle button
├─────────────────────────────────┤
│                                 │
│  [Bulbasaur]    [Ivysaur]      │
│  #001           #002            │
│                                 │
│  [Venusaur]     [Charmander]   │
│  #003           #004            │
│                                 │
│  ...                            │
│                                 │
└─────────────────────────────────┘
  [List] [Favorites] [Settings]    ← NavigationBar (Material styled)

                         [🎨]      ← FAB for quick theme toggle
```

### Compact (Phone) - Compose Unstyled
```
┌─────────────────────────────────┐
│ Pokédex          [🎨]          │ ← Top bar (unstyled)
├─────────────────────────────────┤
│                                 │
│  Bulbasaur      Ivysaur        │
│  #001           #002            │
│                                 │
│  Venusaur       Charmander     │
│  #003           #004            │
│                                 │
│  ...                            │
│                                 │
└─────────────────────────────────┘
  List | Favorites | Settings      ← NavigationBar (unstyled primitives)

                         [🎨]      ← FAB (unstyled button)
```

### Medium (Tablet Portrait) - Material 3 Expressive
```
┌─────────────────────────────────────┐
│ [☰]  Pokédex              [🎨]     │ ← Top bar, IconButton for toggle
├─────┬───────────────────────────────┤
│  ≡  │                               │
│  ⬜  │  [Bulbasaur]  [Ivysaur]      │
│List │  #001         #002            │
│     │                               │
│  ☆  │  [Venusaur]   [Charmander]   │
│Fav  │  #003         #004            │
│     │                               │
│  ⚙  │  ...                          │
│Set  │                               │
│     │                               │
└─────┴───────────────────────────────┘
  ↑ NavigationRail (Material styled)
```

### Expanded (Desktop) - Material 3 Expressive
```
┌────────────────────────────────────────────────────┐
│ [☰]  Pokédex                           [🎨]       │ ← Top bar
├──────────────┬─────────────────────────────────────┤
│              │                                     │
│  [⬜] List   │  [Bulbasaur]  [Ivysaur]  [Venusaur]│
│              │  #001         #002       #003      │
│  [☆] Fav    │                                     │
│              │  [Charmander] [Charmeleon] ...     │
│  [⚙] Set    │  #004         #005                 │
│              │                                     │
│              │  ...                                │
│              │                                     │
│              │                                     │
└──────────────┴─────────────────────────────────────┘
  ↑ PermanentNavigationDrawer (Material styled)
```

### First-Run Modal (All Sizes)
```
┌─────────────────────────────────────┐
│                                     │
│  🎨 Design System Showcase          │
│                                     │
│  This app compares two design      │
│  approaches for Compose UI:         │
│                                     │
│  • Material 3 Expressive            │
│    Opinionated theme with Pokémon  │
│    branding, expressive motion,    │
│    and Google Sans Flex font        │
│                                     │
│  • Compose Unstyled                 │
│    Minimal primitives, raw tokens, │
│    no default styling               │
│                                     │
│  Pick a starting theme:             │
│                                     │
│  [Start with Material]              │
│  [Start with Unstyled]              │
│                                     │
│  You can switch themes anytime via  │
│  the 🎨 button in the top bar.     │
│                                     │
└─────────────────────────────────────┘
```

---

## Animation Specifications

### Theme Transition Animation
```kotlin
val themeTransitionSpec = tween<Float>(
    durationMillis = 300,
    easing = EmphasizedDecelerate
)

AnimatedContent(
    targetState = currentTheme,
    transitionSpec = {
        fadeIn(themeTransitionSpec) togetherWith 
        fadeOut(themeTransitionSpec)
    }
) { theme ->
    when (theme) {
        MATERIAL -> PokemonTheme { AppContent() }
        UNSTYLED -> UnstyledPokemonTheme { AppContent() }
    }
}
```

### FAB/Button Interaction
```kotlin
// Material 3 Expressive: Ripple effect with scale
FloatingActionButton(
    onClick = { viewModel.toggleTheme() },
    modifier = Modifier.scale(animateFloatAsState(if (pressed) 0.95f else 1f).value)
)

// Compose Unstyled: Simple opacity change
Button(
    onClick = { viewModel.toggleTheme() },
    modifier = Modifier.alpha(animateFloatAsState(if (pressed) 0.7f else 1f).value)
)
```

---

## Related Documentation

- [DUAL_UI_IMPLEMENTATION_PLAN.md](DUAL_UI_IMPLEMENTATION_PLAN.md) — Full 7-phase plan
- [prd.md](prd.md) — Product requirements for Pokédex core features
- [ui_ux.md](ui_ux.md) — Design guidelines for Material 3 Expressive theme
- [conventions.md](../tech/conventions.md) — Architecture and module patterns
- [navigation.md](../tech/navigation.md) — Navigation 3 implementation guide
- [Material 3 Adaptive Docs](https://developer.android.com/jetpack/androidx/releases/compose-material3-adaptive)
- [Compose Unstyled Docs](https://github.com/compose-unstyled/unstyled)

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | Dec 20, 2025 | Initial design document with 6 options evaluated, Option A recommended |

