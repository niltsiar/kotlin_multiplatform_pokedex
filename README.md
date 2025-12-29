# Pokédex - Kotlin Multiplatform Showcase

A modern, cross-platform Pokédex application demonstrating **dual-UI design systems** (Material Design 3 Expressive + Compose Unstyled) with shared business logic across Android, iOS, Desktop, and Server.

## ✨ Key Features

- **Dual-UI Design Systems**: Runtime theme switching between Material Design 3 and Compose Unstyled
- **Adaptive Layouts**: Responsive grid columns (2/3/4) and navigation (bottom bar → rail → drawer) based on window size
- **Cross-Platform**: Single Kotlin codebase for Android, iOS, Desktop (JVM), and Server
- **Shared Business Logic**: ViewModels, repositories, and domain models 100% shared across platforms
- **Material 3 Adaptive**: Uses official Material3 Adaptive library for responsive layouts
- **Type-Safe Navigation**: Navigation Compose 3 with Koin DI integration
- **PokéAPI Integration**: Real-time data from official PokéAPI v2

## 🏗️ Architecture

This project demonstrates production-ready Kotlin Multiplatform architecture with:

* **[/composeApp](./composeApp/src)** - Compose Multiplatform UI (Android, Desktop, iOS Compose)
  - [commonMain](./composeApp/src/commonMain/kotlin) - Shared UI code and app entry point
  - Platform-specific folders for Android/iOS/Desktop customizations
  - Dual-theme support with runtime switching

* **[/features](./features/)** - Vertical slice feature modules
  - `:features:<feature>:api` - Public contracts (interfaces, domain models, navigation)
  - `:features:<feature>:data` - Repositories, DTOs, API services
  - `:features:<feature>:presentation` - ViewModels and UI state (shared with iOS)
  - `:features:<feature>:ui-material` - Material Design 3 UI implementation
  - `:features:<feature>:ui-unstyled` - Compose Unstyled UI implementation
  - `:features:<feature>:wiring` - Business logic DI (Koin modules)
  - `:features:<feature>:wiring-ui-material` - Material navigation registration
  - `:features:<feature>:wiring-ui-unstyled` - Unstyled navigation registration

* **[/core](./core/)** - Shared infrastructure modules
  - `:core:designsystem-core` - Shared theme utilities
  - `:core:designsystem-material` - Material 3 Expressive theme
  - `:core:designsystem-unstyled` - Compose Unstyled theme
  - `:core:navigation` - Navigation 3 modular architecture
  - `:core:di` - Koin DI configuration
  - `:core:httpclient` - Ktor HTTP client setup

* **[/iosApp](./iosApp/iosApp)** - **Production iOS app** using native SwiftUI
  - Consumes KMP ViewModels via `:shared` framework
  - Native iOS UI with SwiftUI
  - Direct Integration pattern for ViewModel usage

* **[/iosAppCompose](./iosAppCompose/iosAppCompose)** - **Experimental iOS app** using Compose Multiplatform
  - Shares same Compose UI code with Android/Desktop
  - See the [README](./iosAppCompose/README.md) for details

* **[/server](./server/src/main/kotlin)** - Ktor Backend-for-Frontend (BFF)

* **[/shared](./shared/src)** - iOS umbrella framework
  - Exports feature APIs and ViewModels to iOS
  - [commonMain](./shared/src/commonMain/kotlin) - Shared Kotlin code

## 🚀 Quick Start

### Primary Validation (Always Run First)

**Before making changes, always run:**
```shell
./gradlew :composeApp:assembleDebug test --continue
```
This builds the Android app and runs all 84 tests across all modules (~45 seconds on modern hardware).

### Build and Run Android Application

To run the development version of the Android app:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug test --continue
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug test --continue
  ```

**Features in Android App:**
- Dual-theme switcher (FAB button in bottom-right)
- Adaptive grid layout (2/3/4 columns based on window size)
- Adaptive navigation (bottom bar → rail → drawer)
- Pokemon list with infinite scroll
- Pokemon detail with stats, types, abilities

### Build and Run Desktop (JVM) Application

To run the development version of the desktop app:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:run
  ```

**Features in Desktop App:**
- Same dual-theme switcher as Android
- Window resizing triggers adaptive layouts (grid columns + navigation style)
- Full keyboard and mouse support
- Native window decorations

### Build and Run Server

To build and run the development version of the server, use the run configuration from the run widget
in your IDE’s toolbar or run it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :server:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :server:run
  ```

### Build and Run iOS Application

**Native SwiftUI App (Production):**

To build and run the development version of the iOS app with native SwiftUI, use the run configuration from the run widget
in your IDE's toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

**Compose Multiplatform iOS App (Experimental):**

To build and run the experimental iOS app using Compose Multiplatform:

1. Build the framework:
   ```shell
   ./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
   ```

2. Open in Xcode:
   ```shell
   open iosAppCompose/iosAppCompose.xcodeproj
   ```

3. Run in simulator (Cmd+R)

See [iosAppCompose/README.md](./iosAppCompose/README.md) for detailed information about the Compose iOS implementation.

---

## 📚 Documentation

### Essential Guides
- **[conventions.md](docs/tech/conventions.md)** - Architecture master reference (START HERE)
- **[critical_patterns_quick_ref.md](docs/tech/critical_patterns_quick_ref.md)** - 6 core patterns (ViewModel, Either, Impl+Factory, Navigation, Testing, Convention Plugins)
- **[testing_strategy.md](docs/tech/testing_strategy.md)** - Kotest, MockK, Turbine, property tests (84 tests passing)
- **[QUICK_REFERENCE.md](docs/QUICK_REFERENCE.md)** - Commands, patterns, decision matrices

### Architecture & Patterns
- **[ios_integration.md](docs/tech/ios_integration.md)** - SwiftUI + KMP ViewModels Direct Integration
- **[navigation.md](docs/tech/navigation.md)** - Navigation 3 modular architecture
- **[dependency_injection.md](docs/tech/dependency_injection.md)** - Koin patterns and troubleshooting

### Project Documentation
- **[prd.md](docs/project/prd.md)** - Product requirements (CANONICAL)
- **[user_flow.md](docs/project/user_flow.md)** - User journeys and flows
- **[ui_ux.md](docs/project/ui_ux.md)** - UI/UX guidelines

### Build System
- **[convention_plugins_guide.md](docs/tech/convention_plugins_guide.md)** - Gradle convention plugins reference

---

## 🧪 Testing

**Test Suite:** 84 tests passing (androidUnitTest + commonTest)

**Coverage:**
- Repository tests: Success + all error paths (Network, Http, Unknown)
- ViewModel tests: State transitions with Turbine + TestScope
- Mapper tests: Property-based tests with Kotest
- UI tests: @Preview for all @Composable functions

**Property-Based Testing:**
- 34 property tests (~34,000 scenarios per run)
- 40% property tests, 60% concrete tests
- Mappers: 100% property test coverage

**Quick Start Testing:**
```shell
# Run all tests
./gradlew test --continue

# Run specific module tests
./gradlew :features:pokemonlist:presentation:testDebugUnitTest
```

See [testing_strategy.md](docs/tech/testing_strategy.md) for comprehensive guide.

---

## 🎨 Design Systems

### Material Design 3 Expressive
- Custom Pokémon color scheme (coral primary, yellow secondary, grass green tertiary)
- Google Sans Flex variable font (Android/Desktop), SF Pro (iOS)
- Emphasized easing curves for motion
- 18 Pokémon type colors (WCAG AA compliant)

### Compose Unstyled
- Platform-native theming with automatic light/dark support
- Headless components with full styling control
- Same feature set as Material, different visual approach
- Educational showcase for design system comparison

**Theme Switching:**
- FAB button in bottom-right corner
- Entire app switches atomically (scaffold + content)
- State persisted across sessions
- First-run modal explains the feature

---

## Quick References

### Testing
- **[Kotest Smart Casting](docs/tech/kotest_smart_casting_quick_ref.md)** - Avoid unnecessary manual casts in tests

### Build Logic & Convention Plugins
- For how modules are configured (KMP targets, Android config, Compose, and shared deps), see the consolidated guide:
  - [docs/tech/convention_plugins_guide.md](docs/tech/convention_plugins_guide.md)
  - Plugins you will see across the repo:
    - `convention.kmp.library`, `convention.core.library`
    - `convention.feature.base`, `convention.feature.api`, `convention.feature.data`, `convention.feature.presentation`, `convention.feature.ui`, `convention.feature.wiring`
    - `convention.compose.multiplatform`, `convention.kmp.android.app`

---

## Development Workflow

### Commit Message Convention

This project uses [Conventional Commits](https://www.conventionalcommits.org/) format. See [`.github/COMMIT_CONVENTION.md`](.github/COMMIT_CONVENTION.md) for detailed guidelines.

**Format:**
```
<type>(<scope>): <description>
```

**Quick Reference:**

| Type | Use Case | Example |
|------|----------|---------|
| `feat` | New features | `feat(pokemonlist): add infinite scroll pagination` |
| `fix` | Bug fixes | `fix(pokemondetail): resolve crash on missing sprite data` |
| `docs` | Documentation | `docs(conventions): add commit message guidelines` |
| `test` | Tests | `test(pokemonlist): add property-based tests for repository` |
| `build` | Build/dependencies | `build(gradle): update Kotlin to 2.1.0` |
| `refactor` | Code refactoring | `refactor(designsystem): extract theme tokens to constants` |
| `chore` | Maintenance | `chore: update .gitignore for build artifacts` |

**Scopes:** Use general feature or area names: `pokemonlist`, `pokemondetail`, `designsystem`, `navigation`, `testing`, `ios`, `di`, `conventions`

### CHANGELOG Generation

This project uses [git-cliff](https://git-cliff.org/) to generate changelogs from conventional commits.

**Installation (macOS):**
```shell
brew install git-cliff
```

**Generate/Update CHANGELOG:**
```shell
git cliff -o CHANGELOG.md
```

**For Releases:**
```shell
git cliff -o CHANGELOG.md --tag v1.0.0
```

**Configuration:** See [`cliff.toml`](cliff.toml) for git-cliff configuration including commit grouping, scope-based sub-grouping, and GitHub link templates.

### Commit Validation (Optional)

For production projects, consider automating validation via pre-commit hooks or CI/CD. See `.github/COMMIT_CONVENTION.md` for validation strategies.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
