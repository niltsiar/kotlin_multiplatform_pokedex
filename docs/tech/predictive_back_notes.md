# Predictive Back Implementation Notes

**Last Updated:** December 30, 2025

## Status: DEFERRED

Predictive back gesture implementation has been deferred in favor of the official AndroidX solution.

## Background

Initial implementation used custom `PredictiveBackHandler` with platform-specific handlers (Android 13+ with `OnBackPressedCallback`, iOS/JVM no-ops) and Animatable-based transforms (10% scale reduction, 48dp translation).

## Deprecation Notice

**Custom PredictiveBackHandler is deprecated** in favor of:
- **NavigationBackHandler** from AndroidX Navigation Event library
- Reference: https://developer.android.com/jetpack/androidx/releases/navigationevent

## Future Implementation Path

When implementing predictive back gestures:

1. **Use AndroidX Navigation Event Library**
   - Dependency: `androidx.navigationevent:navigationevent` (or newer version)
   - Official support for predictive back patterns
   - Integrated with Navigation 3

2. **Benefits of Official Solution**
   - First-party support from Android team
   - Better integration with system gestures
   - Consistent behavior across Android versions
   - Automatic handling of edge cases

3. **Migration Plan** (when ready)
   - Add `androidx.navigationevent:navigationevent` dependency
   - Replace custom platform handlers with `NavigationBackHandler`
   - Update navigation metadata to support predictive animations
   - Test on Android 13+ devices

4. **Documentation Links**
   - [AndroidX Navigation Event Releases](https://developer.android.com/jetpack/androidx/releases/navigationevent)
   - [Predictive Back Design Guidelines](https://developer.android.com/guide/navigation/predictive-back-gesture)

## Current State

- ✅ Navigation transitions with motion tokens implemented
- ✅ Theme-based duration/easing curves working
- ❌ Predictive back gestures deferred until official library stable
- 📝 Custom PredictiveBackHandler files remain in codebase but unused

## Related Files (Not Currently Used)

- `core/navigation/src/commonMain/.../predictiveback/PredictiveBackHandler.kt`
- `core/navigation/src/androidMain/.../PlatformPredictiveBackHandler.android.kt`
- `core/navigation/src/iosMain/.../PlatformPredictiveBackHandler.ios.kt`
- `core/navigation/src/jvmMain/.../PlatformPredictiveBackHandler.jvm.kt`

These files can serve as reference for the transform calculations when migrating to the official solution.
