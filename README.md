This is a Kotlin Multiplatform project targeting Android, iOS, Desktop (JVM), Server.

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that's common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple's CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.
  - **Now includes iOS targets** for Compose Multiplatform iOS (see [iosAppCompose](#build-and-run-ios-compose-application)).

* [/iosApp](./iosApp/iosApp) contains the iOS application using **native SwiftUI**. This is the production iOS app
  that uses SwiftUI for UI and consumes KMP business logic via the shared framework.

* [/iosAppCompose](./iosAppCompose/iosAppCompose) contains the **experimental iOS application using Compose Multiplatform**.
  This app shares the same Compose UI code with Android and Desktop. See the [README](./iosAppCompose/README.md) for details.

* [/server](./server/src/main/kotlin) is for the Ktor server application.

* [/shared](./shared/src) is for the code that will be shared between all targets in the project.
  The most important subfolder is [commonMain](./shared/src/commonMain/kotlin). If preferred, you
  can add code to the platform-specific folders here too.

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE's toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug test --continue
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug test --continue
  ```

This command builds the Android app and runs all tests across all modules to ensure code quality.

### Build and Run Desktop (JVM) Application

To build and run the development version of the desktop app, use the run configuration from the run widget
in your IDE’s toolbar or run it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:run
  ```

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

## Quick References

### Testing
- **[Kotest Smart Casting](.junie/guides/tech/kotest_smart_casting_quick_ref.md)** - Avoid unnecessary manual casts in tests

### Build Logic & Convention Plugins
- For how modules are configured (KMP targets, Android config, Compose, and shared deps), see the consolidated guide:
  - [.junie/guides/tech/convention_plugins_guide.md](.junie/guides/tech/convention_plugins_guide.md)
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
