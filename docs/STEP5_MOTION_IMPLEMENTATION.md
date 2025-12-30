# Step 5: Motion Preference + Predictive Back Implementation

**Completed:** December 30, 2025  
**Status:** ✅ Complete

## Overview

Implemented accessibility-aware motion preferences and Material 3 Expressive navigation transitions.

## Files Created

### Motion Preference Detection (Expect/Actual Pattern)

1. **Common Interface**
   - `core/designsystem-core/src/commonMain/kotlin/.../motion/MotionPreference.kt`
   - Defines `expect fun isReduceMotionEnabled(): Boolean`
   - Provides `@Composable fun rememberReducedMotion()`

2. **Android Implementation**
   - `core/designsystem-core/src/androidMain/kotlin/.../motion/MotionPreference.android.kt`
   - Uses `Settings.Global.TRANSITION_ANIMATION_SCALE`
   - Returns `true` when animation scale is 0

3. **iOS Implementation**
   - `core/designsystem-core/src/iosMain/kotlin/.../motion/MotionPreference.ios.kt`
   - Uses `UIAccessibility.isReduceMotionEnabled`
   - Native accessibility API integration

4. **JVM Implementation**
   - `core/designsystem-core/src/jvmMain/kotlin/.../motion/MotionPreference.jvm.kt`
   - Returns `false` (no standard desktop API)

### Predictive Back Gesture Infrastructure

5. **Back State Machine**
   - `core/navigation/src/commonMain/kotlin/.../predictiveback/PredictiveBackState.kt`
   - Sealed class: `Idle`, `Dragging(progress)`, `Settling`, `Completed`
   - Tracks gesture progress from 0.0 to 1.0

6. **Back Handler with Transforms**
   - `core/navigation/src/commonMain/kotlin/.../predictiveback/PredictiveBackHandler.kt`
   - Scale transform: `1.0 - progress * 0.1` (90-100% scale)
   - Translation: `progress * 48f` (0-48dp slide)
   - Uses `graphicsLayer` for hardware acceleration

### Navigation Transitions

7. **Shared Element Transition**
   - `core/navigation/src/commonMain/kotlin/.../transitions/SharedElementTransition.kt`
   - Material 3 Expressive motion timing
   - Enter: 400ms with emphasized decelerate (0.05, 0.7, 0.1, 1.0)
   - Exit: 200ms with emphasized accelerate (0.3, 0.0, 0.8, 0.15)
   - Combines slide, fade, and scale animations

## Files Modified

### Navigation Provider Updates

8. **Material Navigation**
   - `features/pokemondetail/wiring-ui-material/.../PokemonDetailMaterialNavigationProviders.kt`
   - Replaced inline animation specs with `sharedElementTransition()`
   - Reduced code from ~15 lines to 1 line

9. **Unstyled Navigation**
   - `features/pokemondetail/wiring-ui-unstyled/.../PokemonDetailUnstyledNavigationProviders.kt`
   - Replaced inline animation specs with `sharedElementTransition()`
   - Same Material 3 motion timing as Material theme

## Technical Details

### Motion Preference API Surface

```kotlin
// Detect reduced motion preference
expect fun isReduceMotionEnabled(): Boolean

// Use in Compose
@Composable
fun MyAnimatedComponent() {
    val reducedMotion = rememberReducedMotion()
    val duration = if (reducedMotion) 0 else 300
    // Use duration in animations
}
```

### Predictive Back Usage

```kotlin
@Composable
fun MyScreen(onBack: () -> Unit) {
    PredictiveBackHandler(onBack = onBack) { modifier ->
        // Content with modifier applied
        // Automatically scales/translates during back gesture
        Column(modifier = modifier) {
            // Screen content
        }
    }
}
```

### Shared Element Transition Usage

```kotlin
// In navigation module
val myNavigationModule = module {
    scope<MaterialScope> {
        navigation<MyRoute>(
            metadata = sharedElementTransition()  // ← One line!
        ) { route ->
            MyScreen()
        }
    }
}
```

## Benefits

### Code Reduction
- **Before**: ~15 lines of animation code per route
- **After**: 1 line `sharedElementTransition()`
- **Savings**: 93% reduction in navigation animation boilerplate

### Accessibility
- Respects system reduced motion preferences
- Platform-specific implementations for accurate detection
- Can be used by all animated components

### Performance
- Hardware-accelerated transforms with `graphicsLayer`
- Optimized timing curves from Material 3 Expressive
- Single source of truth for navigation motion

### Consistency
- All detail screen navigation uses same transitions
- Material and Unstyled themes share motion timing
- Centralized motion specifications

## Platform Support

| Platform | Motion Detection | Predictive Back | Transitions |
|----------|-----------------|-----------------|-------------|
| Android  | ✅ Settings API | ✅ Full support | ✅ |
| iOS      | ✅ UIAccessibility | ⚠️ Compose only | ✅ |
| Desktop  | ❌ No API (returns false) | ⚠️ Compose only | ✅ |

## Next Steps (Steps 6-7)

These motion utilities will be used in screen redesigns:

1. **AnimatedStatBar** will use `rememberReducedMotion()` to skip animations
2. **Pokemon Detail** screens can use `PredictiveBackHandler` for gesture support
3. **Navigation transitions** are already applied to detail routes

## Testing

- ✅ All 84 existing tests pass
- ✅ Modules build successfully
- ✅ No new test files needed (infrastructure only)
- Future: Integration tests for motion preferences in component tests

## Documentation

- ✅ Inline KDoc for all public APIs
- ✅ Usage examples in UI_REDESIGN_PLAN.md
- ✅ This summary document for reference

