# Phase 2 Validation Checklist

**Status**: Ready for Manual Testing  
**Date**: December 30, 2025

## ✅ Completed Improvements

1. **Typography Enhancements** ✅
   - Pokemon names: `fontWeight.SemiBold` 
   - Hero section: `displayLarge` typography
   - Physical attributes: `headlineMedium` for emojis, proper weights for labels/values
   - Abilities: `fontWeight.Medium` for ability names, `fontWeight.SemiBold` for labels

2. **Surface Elevation** ✅
   - PhysicalAttributesCard: `surfaceContainerLow` + 1dp elevation
   - Hidden ability chips: `surfaceContainerHigh` 
   - Consistent Material 3 surface tokens throughout

3. **Enhanced Error States** ✅
   - Visual hierarchy: ⚠️ emoji → title → message → 🔄 retry button
   - Token-based spacing and proper color usage

4. **Enhanced Loading States** ✅
   - Skeleton screens with shimmer animation (1000ms linear)
   - 12 skeleton cards matching real layout
   - Professional perceived performance

5. **Animation Optimization** ✅
   - TypeBadgeRow: 25ms stagger (50% faster)
   - EaseOutCubic easing for smooth motion
   - Viewport-capped list animations (8 items max)

## 🔄 Manual Validation Required

### Dark Mode Testing (15 minutes)

**Test Steps**:
1. Switch device/desktop to dark mode
2. Launch app: `./gradlew :composeApp:run`
3. Navigate: List → Detail → Back → Error state

**Checklist**:
- [ ] Cards have visible elevation (not blending into background)
- [ ] Text is readable (not pure white #FFFFFF on pure black #000000)
- [ ] Colored surfaces maintain hierarchy (primary, error containers distinct)
- [ ] Pokemon sprites have sufficient contrast
- [ ] Hover states visible on cards (desktop)
- [ ] Animations smooth (no jarring color transitions)
- [ ] Typography doesn't disappear (SemiBold weight visible on dark)

**Common Issues to Fix**:
- If cards blend: Verify `surfaceContainerHigh` vs `surface` usage
- If hover invisible: Increase elevation delta (6dp → 8dp)
- If text too bright: Verify using `onSurface` token (not hardcoded white)

### WCAG AA Contrast Audit (30 minutes)

**Requirements**:
- Normal text (< 18px): **4.5:1** minimum
- Large text (≥ 18px or ≥ 14px bold): **3:1** minimum
- Interactive elements: **3:1** minimum

**Test Locations**:

**Pokemon List**:
- [ ] Pokemon name (titleLarge, SemiBold) on `surfaceContainerHigh` card
- [ ] Pokemon ID (#001) on `surfaceContainerHigh` card
- [ ] Error message text on error state background
- [ ] "Retry" button text contrast

**Pokemon Detail**:
- [ ] Hero name (displayLarge, SemiBold) on gradient background
- [ ] ID number on gradient background
- [ ] Type badge text on type color backgrounds (Fire=red, Water=blue, etc.)
- [ ] Stat labels (labelLarge) on surface
- [ ] Stat values (titleMedium, SemiBold) on surface
- [ ] Ability text on card background
- [ ] "Hidden" chip text on `surfaceContainerHigh`

**Tools**:
1. **WebAIM Contrast Checker**: https://webaim.org/resources/contrastchecker/
   - Copy background color hex from Android Studio Layout Inspector
   - Copy foreground text color hex
   - Paste into checker, verify ratio

2. **Android Studio**:
   - Tools → Layout Inspector → Select text element
   - View "Color" property for text
   - View parent "containerColor" for background
   - Calculate contrast ratio

3. **Android Accessibility Scanner** (on device):
   - Install from Play Store
   - Run scan on Pokemon List screen
   - Run scan on Pokemon Detail screen
   - Fix any flagged contrast issues

**Expected Results**:
- ✅ All normal text: ≥ 4.5:1
- ✅ All large text: ≥ 3:1
- ✅ Type badges: May need adjustment for yellow/light blue types on light backgrounds

### Performance Validation (15 minutes)

**Test on Low-End Device** (or Desktop with CPU throttling):

1. **List Scroll Performance**:
   - [ ] Maintain 60fps during scroll
   - [ ] Animations complete within 500ms
   - [ ] No dropped frames visible
   - [ ] Infinite scroll loads smoothly

2. **Detail Screen Load**:
   - [ ] Type badges animate smoothly (< 400ms total)
   - [ ] Stats bars animate without jank
   - [ ] Back navigation smooth

3. **Reduced Motion Preference**:
   - [ ] Enable "Remove animations" in Android accessibility
   - [ ] Verify instant appearances (no animation delays)
   - [ ] App still functional without animations

## 📊 Acceptance Criteria

**Pass Conditions**:
- ✅ Build successful: `./gradlew :composeApp:assembleDebug test --continue`
- ✅ All 84 tests passing
- ✅ Dark mode looks professional (no pure black/white)
- ✅ WCAG AA contrast ratios met for all text
- ✅ 60fps scrolling on target devices
- ✅ No accessibility warnings

**Fail Conditions**:
- ❌ Cards invisible in dark mode
- ❌ Text contrast < 4.5:1 for normal text
- ❌ Animations drop frames
- ❌ Accessibility Scanner flags errors

## 🔧 Quick Fixes Reference

### If Cards Blend in Dark Mode
```kotlin
// Verify elevation token usage
colors = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh  // NOT surface
)
elevation = CardDefaults.cardElevation(
    defaultElevation = 2.dp  // Minimum for visibility
)
```

### If Text Contrast Too Low
```kotlin
// Increase alpha for better contrast
color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
    alpha = 0.85f  // Increase from 0.7f
)
```

### If Hover States Invisible (Dark Mode)
```kotlin
val elevation by animateDpAsState(
    targetValue = when {
        isPressed -> 1.dp
        isHovered -> 8.dp  // Increase from 6dp for dark mode
        else -> 2.dp
    }
)
```

## 📝 Testing Notes

**Build Command** (concise output):
```bash
./gradlew :composeApp:assembleDebug --quiet && echo "✅ Build success" || echo "❌ Build failed"
```

**Run Desktop App**:
```bash
./gradlew :composeApp:run > /dev/null 2>&1 &
echo "Desktop app started in background"
```

**Test Status Check**:
```bash
./gradlew test --quiet && echo "✅ All tests passing" || echo "❌ Tests failed"
```

## 🎯 Next Steps

After manual validation completes:

1. **If all tests pass**: Mark Phase 2 as ✅ COMPLETE
2. **If issues found**: Document specific contrast ratios or visual issues
3. **Optional**: Migrate from emoji icons to Material Symbols from composables.com/icons

## 📚 Reference

- Current work documented: `docs/MATERIAL_UI_PROFESSIONAL_REDESIGN.md`
- Build status: ✅ PASSING (as of last commit)
- Tests: 84/84 passing
- Commit: "feat(ui): Phase 2 Material UI enhancements with emoji icon workaround"
