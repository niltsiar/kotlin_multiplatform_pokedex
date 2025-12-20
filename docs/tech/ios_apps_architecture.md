# iOS Apps Architecture

This repo ships **two iOS apps** that consume Kotlin Multiplatform code with **different boundaries**.

## Two iOS apps, two frameworks

### `iosApp` (production, SwiftUI)

- UI: SwiftUI
- Kotlin framework: `Shared.framework`
- Rule: **MUST remain Compose-free** (no Compose UI and no Compose-only navigation symbols)

Framework definition:
- [`shared/build.gradle.kts`](../../shared/build.gradle.kts)

App Xcode project:
- [`iosApp/iosApp.xcodeproj`](../../iosApp/iosApp.xcodeproj)

### `iosAppCompose` (experimental, SwiftUI wrapper around Compose)

- UI: Compose Multiplatform (hosted in SwiftUI)
- Kotlin framework: `ComposeApp.framework`
- Rule: **is allowed to include Compose UI + Compose navigation**

Framework definition:
- [`composeApp/build.gradle.kts`](../../composeApp/build.gradle.kts)

App Xcode project:
- [`iosAppCompose/iosAppCompose.xcodeproj`](../../iosAppCompose/iosAppCompose.xcodeproj)

## Boundary rules (critical)

The core rule for the dual-iOS-app setup:

- `Shared.framework` must export only business logic that the SwiftUI app can consume.
- Compose UI code lives behind `ComposeApp.framework` only.

Related conventions and module guidelines:

- Architecture and module conventions: [`docs/tech/conventions.md`](./conventions.md)
- iOS integration overview: [`docs/tech/ios_integration.md`](./ios_integration.md)
- Navigation conventions: [`docs/tech/navigation.md`](./navigation.md)

## Validation commands

### Always run (primary)

```bash
./gradlew :composeApp:assembleDebug test --continue
```

### iOS frameworks (CLI-friendly)

The `embedAndSignAppleFrameworkForXcode` tasks require Xcode-provided env vars. From CLI, prefer framework link tasks:

```bash
# iosApp (Shared.framework)
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 :shared:linkDebugFrameworkIosArm64

# iosAppCompose (ComposeApp.framework)
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64 :composeApp:linkDebugFrameworkIosArm64
```

### Boundary checks (symbols)

```bash
echo "Checking iosApp framework (must be Compose-free):"
nm -g shared/build/bin/iosArm64/debugFramework/Shared.framework/Shared \
  | grep -i "compose\|navigation" && echo "❌ VIOLATION: Compose/Navigation leaked to iosApp" || echo "✅ iosApp boundary clean"

echo "Checking iosAppCompose framework (must have Compose):"
nm -g composeApp/build/bin/iosArm64/debugFramework/ComposeApp.framework/ComposeApp \
  | grep -i "compose" && echo "✅ iosAppCompose has Compose" || echo "❌ ERROR: Missing Compose in iosAppCompose"
```

### Xcode builds (milestones)

```bash
cd iosApp && xcodebuild -scheme iosApp -sdk iphonesimulator build CODE_SIGN_IDENTITY="" CODE_SIGNING_REQUIRED=NO
cd ../iosAppCompose && xcodebuild -scheme iosAppCompose -sdk iphonesimulator build CODE_SIGN_IDENTITY="" CODE_SIGNING_REQUIRED=NO
```
