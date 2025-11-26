# iOS Compose App (iosAppCompose)

This is an **experimental** iOS app that uses **Compose Multiplatform UI** instead of native SwiftUI.

## Overview

- **Main iOS App** (`iosApp/`): Uses native SwiftUI UI with KMP ViewModels (production approach)
- **Compose iOS App** (`iosAppCompose/`): Uses Compose Multiplatform UI shared with Android/Desktop (experimental)

Both apps share the same business logic (ViewModels, repositories, domain models) from the KMP modules.

## Architecture

```
iosAppCompose (Swift)
  └── ContentView.swift → wraps MainViewController from ComposeApp framework
      └── ComposeApp.framework (from composeApp module)
          └── Compose UI screens (shared with Android/Desktop)
              └── KMP ViewModels, repositories, domain
```

## Building the App

### 1. Build the Framework

From the project root:

```bash
./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
```

Or for a specific simulator:

```bash
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

### 2. Open in Xcode

```bash
open iosAppCompose/iosAppCompose.xcodeproj
```

### 3. Run in Simulator

- Select a simulator (iPhone 16 Pro, iPad Pro, etc.)
- Press **Cmd+R** to build and run

## What's Different from iosApp

| Aspect | iosApp (Native) | iosAppCompose (Compose) |
|--------|----------------|------------------------|
| UI Framework | SwiftUI | Compose Multiplatform |
| UI Code | Swift | Kotlin @Composable |
| Platform Feel | 100% native iOS | Cross-platform |
| UI Sharing | iOS-specific | Shared with Android/Desktop |
| ViewModels | KMP via SKIE | KMP direct access |

## Changes Made

### 1. Added iOS Targets to composeApp
- `iosArm64()` (for real devices)
- `iosSimulatorArm64()` (for M1/M2/M3 simulators)
- `iosX64()` (for Intel simulators)

### 2. Updated UI Convention Plugin
Changed `ConventionFeatureUiPlugin` to include iOS targets, enabling Compose UI modules to compile for iOS.

### 3. Created iosMain Source Set
- `PlatformNavigationModules.ios.kt` — iOS navigation providers
- `MainViewController.kt` — UIViewController wrapper for Compose UI

### 4. Created iOS Navigation Providers
Added `iosMain` source sets in wiring modules:
- `features/pokemonlist/wiring/src/iosMain/`
- `features/pokemondetail/wiring/src/iosMain/`

### 5. Fixed Platform-Specific Code
- Replaced `String.format()` with `padStart()` (platform-agnostic)

## Framework Output

The ComposeApp framework is generated at:

```
composeApp/build/bin/iosSimulatorArm64/debugFramework/ComposeApp.framework
```

## Navigation

Uses **Navigation 3** with:
- Route objects: `PokemonList`, `PokemonDetail(id)`
- Navigator: `Navigator` class for navigation state
- Entry providers: Registered in wiring modules

## Known Limitations

1. **Experimental Feature**: Compose for iOS is still evolving
2. **Build Time**: iOS framework builds take 1-2 minutes
3. **Navigation 3**: May have limited iOS-specific features
4. **Platform APIs**: Some iOS-specific APIs may need expect/actual declarations

## Troubleshooting

### Framework Not Found

If Xcode can't find the framework:

```bash
./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
```

Then clean and rebuild in Xcode (**Cmd+Shift+K**, then **Cmd+R**).

### Compilation Errors

If you see "Unresolved reference" errors:

1. Check that all UI modules support iOS targets (see `ConventionFeatureUiPlugin`)
2. Ensure platform-specific code uses `expect/actual` or platform-agnostic APIs
3. Verify navigation providers exist in `iosMain` source sets

## Comparison: When to Use Which App

### Use iosApp (Native SwiftUI) When:
- ✅ Production app
- ✅ Need 100% native iOS feel
- ✅ Platform-specific features (ARKit, WidgetKit, etc.)
- ✅ iOS-only distribution

### Use iosAppCompose (Compose) When:
- ✅ Experimenting with Compose for iOS
- ✅ Maximum code sharing across platforms
- ✅ Unified UI/UX across Android/iOS/Desktop
- ✅ Rapid prototyping

## Future Improvements

- [ ] Add bundle ID configuration in build.gradle.kts
- [ ] Test on real iOS devices
- [ ] Benchmark performance vs native SwiftUI
- [ ] Explore iOS-specific Compose features
- [ ] Add platform-specific UI adaptations (safe areas, etc.)

## Resources

- [Compose Multiplatform iOS](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Navigation 3 Documentation](https://developer.android.com/guide/navigation/navigation-3)
- [Kotlin Multiplatform Mobile](https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html)
