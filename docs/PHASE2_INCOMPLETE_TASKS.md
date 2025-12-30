# Phase 2 Incomplete Tasks (From Material UI Professional Redesign)

**Extracted:** December 30, 2025  
**Source:** MATERIAL_UI_PROFESSIONAL_REDESIGN.md  
**Status:** Ready for implementation

## 🔄 Tasks Marked as NEEDS IMPLEMENTATION/REVIEW

### Task 1: Typography Refinements (45 min)
**Status:** 🔄 NEEDS IMPLEMENTATION  
**Files:**
- `features/pokemonlist/ui-material/.../PokemonListCard.kt`
- `features/pokemondetail/ui-material/.../HeroSection.kt`
- `features/pokemondetail/ui-material/.../PhysicalAttributesCard.kt`

**Changes:**
1. Pokemon list cards: Add `fontWeight = FontWeight.SemiBold` to names
2. Hero section: 
   - Upgrade name to `displayLarge` (from `displayMedium`)
   - ID to `headlineSmall` (from `titleLarge`)
   - Add `fontWeight = FontWeight.Bold` to name
3. Physical attributes:
   - Emoji to `headlineMedium` (from `displaySmall`)
   - Label to `labelLarge` (from `labelMedium`)
   - Value: Add `fontWeight = FontWeight.SemiBold`

---

### Task 2: Surface Elevation Updates (30 min)
**Status:** 🔄 NEEDS IMPLEMENTATION  
**Files:**
- `features/pokemondetail/ui-material/.../PhysicalAttributesCard.kt`
- `features/pokemondetail/ui-material/.../AbilitiesSection.kt`

**Changes:**
1. PhysicalAttributesCard:
   ```kotlin
   colors = CardDefaults.cardColors(
       containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
   )
   elevation = CardDefaults.cardElevation(
       defaultElevation = 2.dp  // ADD
   )
   ```

2. AbilitiesSection chips:
   ```kotlin
   // Non-hidden abilities
   colors = FilterChipDefaults.filterChipColors(
       containerColor = MaterialTheme.colorScheme.surfaceContainer
   )
   
   // Hidden abilities
   colors = FilterChipDefaults.filterChipColors(
       containerColor = MaterialTheme.colorScheme.primaryContainer,
       labelColor = MaterialTheme.colorScheme.onPrimaryContainer
   )
   ```

---

### Task 3: Spacing Token Review (15 min)
**Status:** 🔄 NEEDS REVIEW  
**Action:** Verify all hardcoded spacing values use tokens

**Check for:**
- Hardcoded `.dp` values that should be tokens
- Spacer heights using magic numbers
- Padding values not from token system

**Should use:**
```kotlin
MaterialTheme.tokens.spacing.small    // 8.dp
MaterialTheme.tokens.spacing.medium   // 16.dp
MaterialTheme.tokens.spacing.large    // 24.dp
MaterialTheme.tokens.spacing.xl       // 32.dp
```

---

### Task 4: Enhanced Loading States (45 min)
**Status:** 🔄 NEEDS IMPLEMENTATION (OPTIONAL)  
**Files:**
- `features/pokemonlist/ui-material/.../LoadingState.kt` (new component)
- `features/pokemonlist/ui-material/.../PokemonListMaterialScreen.kt`

**Changes:**
- Create skeleton loading with shimmer effect (see detailed implementation in MATERIAL_UI_PROFESSIONAL_REDESIGN.md)
- OR enhance simple loading state with text label

**Note:** Skeleton loading is polish, not critical. Simple enhancement is fine.

---

### Task 5: Enhanced Error States (45 min)
**Status:** 🔄 NEEDS IMPLEMENTATION  
**Files:**
- `features/pokemonlist/ui-material/.../ErrorState.kt`
- `features/pokemondetail/ui-material/.../PokemonDetailMaterialScreen.kt`

**Changes:**
1. Add error icon (using Material Symbols we already have)
2. Add error title
3. Use `FilledTonalButton` for retry
4. Proper spacing with tokens

**Current implementation needs:**
- Icon from our Material Symbols (ic_error_outline)
- Better visual hierarchy (icon → title → message → button)
- Token-based spacing

---

### Task 6: Dark Mode Validation (15 min)
**Status:** 🔄 NEEDS TESTING  
**Action:** Manual testing checklist

**Test:**
- [ ] Cards have visible elevation in dark mode
- [ ] Text is readable (not pure white on pure black)
- [ ] Hover states visible
- [ ] Animations smooth
- [ ] Typography doesn't disappear

**Potential fixes:**
- Increase hover elevation from 6dp → 8dp for dark mode visibility
- Verify using proper surface tokens (not hardcoded colors)

---

### Task 7: Color Contrast Audit (30 min)
**Status:** 🔄 NEEDS VALIDATION  
**Action:** WCAG AA compliance check

**Requirements:**
- Normal text: 4.5:1 minimum
- Large text: 3:1 minimum  
- Interactive elements: 3:1 minimum

**Check locations:**
- Pokemon name on card vs background
- Pokemon ID (#001) vs background
- Type badge text vs type color
- Stat labels vs surface
- Error text vs background

**Tool:** https://webaim.org/resources/contrastchecker/

---

## 🎯 Critical vs Optional Tasks

### CRITICAL (Must do before Step 6 complete):
1. ✅ Typography refinements (fontWeight, proper scales)
2. ✅ Surface elevation (cards, chips)
3. ✅ Spacing token review (remove hardcoded values)
4. ✅ Enhanced error states (better hierarchy)
5. ✅ Dark mode validation (visual test)
6. ✅ Color contrast audit (WCAG AA)

### OPTIONAL (Polish for later):
7. ⏳ Skeleton loading with shimmer (nice to have)
8. ⏳ Additional icons from Material Symbols (as needed)

---

## 📊 Estimated Time

**Critical tasks:** ~3 hours  
**Optional tasks:** ~1 hour

**Total:** 3-4 hours to complete Phase 2

---

## Next Action

Start with **Task 1: Typography Refinements** - most visible improvement with least risk.
