# Phase 2 Validation Checklist

**Last Updated:** December 31, 2025

**Purpose:** Manual validation steps for Phase 2 UI redesign (Steps 1-7). Run these tests before marking each step complete.

---

## ✅ Step 1: Core Token Foundation (Complete)

### Build Validation
- [ ] `./gradlew :composeApp:assembleDebug test --continue` passes
- [ ] All 84 tests pass
- [ ] No compilation errors

### Feature Validation
- [ ] BaseTokens.kt defines 8dp spacing grid (xxxs: 2dp → xxxl: 64dp)
- [ ] BaseTokens.kt defines standard shapes (4dp, 8dp, 12dp corner radius)
- [ ] BaseTokens.kt defines elevation levels (0dp → 24dp)
- [ ] BaseTokens.kt defines motion curves (Linear, Standard, Emphasized)
- [ ] Navigation providers follow `{Feature}MaterialNavigationProviders.kt` naming
- [ ] Navigation providers follow `{Feature}UnstyledNavigationProviders.kt` naming

---

## ✅ Step 2: Theme Token Systems (Complete)

### Build Validation
- [ ] `./gradlew :composeApp:assembleDebug test --continue` passes
- [ ] All 84 tests pass

### Feature Validation

**MaterialTokens:**
- [ ] Delegates spacing to BaseTokens.spacing
- [ ] Overrides shapes to expressive (28dp large corner)
- [ ] Overrides motion to emphasized curves
- [ ] Elevation uses Material levels (1dp, 3dp, 6dp, 8dp, 12dp, 16dp)

**UnstyledTokens:**
- [ ] Delegates spacing to BaseTokens.spacing
- [ ] Overrides shapes to minimal (12dp max corner)
- [ ] Overrides motion to linear curves
- [ ] Elevation uses flat levels (1dp only)

**Token Delegation:**
- [ ] MaterialTokens.spacing === BaseTokens.spacing (reference equality)
- [ ] UnstyledTokens.spacing === BaseTokens.spacing (reference equality)
- [ ] No duplication of spacing values

---

## ✅ Step 3: Google Sans Flex Typography (Complete)

### Build Validation
- [ ] `./gradlew :composeApp:assembleDebug test --continue` passes
- [ ] All 84 tests pass

### Feature Validation

**Font Loading:**
- [ ] Android: Google Sans fonts load from composeResources
- [ ] Desktop: Google Sans fonts load from composeResources
- [ ] iOS: Falls back to San Francisco system font

**Typography Scale:**
- [ ] 15 custom typography tokens defined (displayLarge → labelSmall)
- [ ] Tokens use pokemonFontFamily() for platform-specific loading
- [ ] Material and Unstyled themes use same typography scale

**Visual Test:**
- [ ] Run desktop app, check font rendering
- [ ] Text appears crisp and properly weighted
- [ ] Medium (500) and Bold (700) weights visible

---

## ✅ Step 4: Shared Component Abstraction (Complete)

### Build Validation
- [ ] `./gradlew :composeApp:assembleDebug test --continue` passes
- [ ] All 84 tests pass

### Feature Validation

**Component Token Interfaces:**
- [ ] CardTokens interface exists in designsystem-core
- [ ] BadgeTokens interface exists in designsystem-core
- [ ] ProgressBarTokens interface exists in designsystem-core

**Shared Components:**
- [ ] PokemonCard accepts optional CardTokens override
- [ ] TypeBadge accepts optional BadgeTokens override
- [ ] AnimatedStatBar accepts optional ProgressBarTokens override

**Theme-Specific Tokens:**
- [ ] MaterialComponentTokens implements all interfaces (filled badges, elevated cards)
- [ ] UnstyledComponentTokens implements all interfaces (outline badges, flat cards)

**PokemonTypeColors:**
- [ ] getBackground(type: String, isDark: Boolean) works for all 18 types
- [ ] WCAG AA contrast ratios met for light mode
- [ ] Colors adjusted for dark mode

---

## ✅ Step 5: Motion Preference + Predictive Back (Complete)

### Build Validation
- [ ] `./gradlew :composeApp:assembleDebug test --continue` passes
- [ ] All 84 tests pass

### Feature Validation

**Motion Preference Detection:**
- [ ] Android: Uses Settings.Global.TRANSITION_ANIMATION_SCALE
- [ ] iOS: Uses UIAccessibility.isReduceMotionEnabled
- [ ] Desktop: Returns false (no standard API)
- [ ] expect/actual pattern works across platforms

**PredictiveBackHandler:**
- [ ] Scale transform applied during drag (0.9x min)
- [ ] Translation transform applied during drag
- [ ] State: Idle → Dragging → Settling → Completed
- [ ] Works with Material 3 back gesture

**SharedElementTransition:**
- [ ] Enter transition: 400ms with emphasized decelerate
- [ ] Exit transition: 200ms with emphasized accelerate
- [ ] Used in Pokemon Detail navigation

**Visual Test:**
- [ ] Run app, enable reduce motion in system settings
- [ ] Verify animations disabled/simplified
- [ ] Swipe back from detail screen, see predictive back

---

## ✅ Step 6: Material Screens Redesign (Complete)

### Build Validation
- [ ] `./gradlew :composeApp:assembleDebug test --continue` passes
- [ ] All 84 tests pass
- [ ] Screenshot tests pass (if implemented)

### Feature Validation

**Pokemon List Material:**
- [ ] PokemonListMaterialScreen.kt exists in ui-material module
- [ ] PokemonListCard with elevation states (level1 pressed, level3 hover, level2 default)
- [ ] PokemonListGrid with adaptive columns (2/3/4 based on window size)
- [ ] LoadingState with shimmer skeleton
- [ ] ErrorState with Material Symbols icons (ic_error_outline, ic_refresh)
- [ ] Staggered entrance animations work

**Pokemon Detail Material:**
- [ ] PokemonDetailMaterialScreen.kt exists in ui-material module
- [ ] HeroSection with 256dp image and gradient background
- [ ] TypeBadgeRow with 25ms stagger animations
- [ ] PhysicalAttributesCard with Material Symbols (ic_height, ic_weight)
- [ ] AbilitiesSection with "Hidden" chip badges
- [ ] BaseStatsSection with AnimatedStatBar and 50ms stagger
- [ ] Multiple preview states (loading, content, error)

**Navigation:**
- [ ] PokemonListMaterialNavigationProviders.kt in wiring-ui-material
- [ ] PokemonDetailMaterialNavigationProviders.kt in wiring-ui-material
- [ ] Both use scope<MaterialScope> { }
- [ ] Navigation works from list to detail

**Visual Test:**
- [ ] Run desktop app, select Material theme
- [ ] Cards have visible elevation shadows
- [ ] Hover states work (elevation increases)
- [ ] Click card, navigates to detail
- [ ] Detail screen shows all sections with animations

---

## ✅ Step 7: Unstyled Screens Redesign (Complete)

### Build Validation
- [ ] `./gradlew :composeApp:assembleDebug test --continue` passes
- [ ] All 84 tests pass
- [ ] No compilation errors in Unstyled modules

### Feature Validation

**Pokemon List Unstyled:**
- [ ] PokemonListUnstyledScreen.kt exists in ui-unstyled module
- [ ] PokemonListCardUnstyled with border-only styling
- [ ] Cards have .clickable() modifier (navigation works)
- [ ] Enhanced hover effects:
  - Brightness: 1.15 (15% increase)
  - Border opacity: 0.5 (from 0.2)
  - Scale: 1.02 (2% grow)
- [ ] PokemonListGridUnstyled uses gridColumns(windowInfo) function
- [ ] LoadingStateUnstyled with linear progress
- [ ] ErrorStateUnstyled with minimal retry button

**Pokemon Detail Unstyled:**
- [ ] PokemonDetailUnstyledScreen.kt exists in ui-unstyled module
- [ ] HeroSectionUnstyled with 256dp flat image
- [ ] TypeBadgeRowUnstyled with border-only badges
- [ ] PhysicalAttributesCardUnstyled with flat cards
- [ ] AbilitiesSectionUnstyled with simple list
- [ ] BaseStatsSectionUnstyled with monochrome stat bars
- [ ] All components use Theme[property][token] syntax

**Domain Class Usage:**
- [ ] TypeOfPokemon(name, slot) used correctly (10+ instances)
- [ ] Stat(name, baseStat, effort) used correctly (14+ instances)
- [ ] Ability(name, isHidden, slot) used correctly (6+ instances)

**Navigation:**
- [ ] PokemonListUnstyledNavigationProviders.kt in wiring-ui-unstyled
- [ ] PokemonDetailUnstyledNavigationProviders.kt in wiring-ui-unstyled
- [ ] Both use scope<UnstyledScope> { }
- [ ] Both wrap content in UnstyledTheme { }
- [ ] Navigation works from list to detail

**Theme Token Verification:**
- [ ] Theme[spacing][spacingSm] works (12dp)
- [ ] Theme[shapes][shapeLarge] works (rounded corners)
- [ ] Theme[typography][labelMedium] works
- [ ] Theme[colors][primary] works
- [ ] All token types supported

**Visual Test:**
- [ ] Run desktop app, select Unstyled theme
- [ ] Cards have border-only styling (no elevation shadows)
- [ ] Hover states visible (brightness + border + scale changes)
- [ ] Click card, navigates to detail
- [ ] Detail screen shows all sections with flat styling
- [ ] Compare with Material theme, verify visual differences

---

## 🔜 Step 8: SwiftUI Design System (Pending)

### Planned Validation
- [ ] SwiftUI components match Material and Unstyled designs
- [ ] iOS app uses shared ViewModels from KMP
- [ ] Type-safe color system in Swift
- [ ] Typography scale matches Google Sans
- [ ] Motion respects reduce motion setting

---

## 🔜 Step 9: Comprehensive Unit Tests (Pending)

### Planned Validation
- [ ] Component tests for all Material components
- [ ] Component tests for all Unstyled components
- [ ] Screenshot regression tests (Roborazzi)
- [ ] Property-based tests for theme tokens
- [ ] Motion behavior tests

---

## 🔜 Step 10: Component Guides (Pending)

### Planned Validation
- [ ] Usage guide for each component
- [ ] Token customization examples
- [ ] Animation timing documentation
- [ ] Accessibility guidelines
- [ ] Code examples with @Preview

---

## Common Issues Checklist

Before reporting a bug, verify:

- [ ] Clean build attempted: `./gradlew clean :composeApp:assembleDebug test`
- [ ] All tests passing: 84/84 expected
- [ ] Domain class constructors correct (TypeOfPokemon, Stat, Ability)
- [ ] Navigation function names correct ({Feature}UnstyledScreen)
- [ ] .clickable() modifier present on interactive components
- [ ] Theme imports from custom theme, not platform theme

**See [TROUBLESHOOTING.md](TROUBLESHOOTING.md) for detailed solutions**

---

## Testing Commands

```bash
# Primary validation (always run)
./gradlew :composeApp:assembleDebug test --continue

# Desktop app (visual testing)
./gradlew :composeApp:run

# Clean build (cache issues)
./gradlew clean :composeApp:assembleDebug test --continue

# Screenshot tests
./gradlew recordRoborazziDebug
./gradlew verifyRoborazziDebug
```

---

## Related Documentation

- [UI_REDESIGN_PLAN.md](UI_REDESIGN_PLAN.md) - Overall plan and progress
- [CODE_REFERENCES.md](CODE_REFERENCES.md) - Implementation examples
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - Common issues and solutions
- [QUICK_REFERENCE.md](QUICK_REFERENCE.md) - Commands and patterns
