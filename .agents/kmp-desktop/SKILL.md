---
name: kmp-desktop
description: "Desktop (JVM) patterns for Kotlin Multiplatform including SavedStateHandle, Koin integration, and platform-specific configuration. Use when: (1) Working on Desktop/JVM ViewModels, (2) Setting up SavedStateHandle on Desktop, (3) Configuring Koin for Desktop, (4) Understanding platform differences from Android"
---

# KMP Desktop (JVM) Skill

Patterns for implementing ViewModels and DI on Desktop/JVM with Kotlin Multiplatform.

## When to Use This Skill

**MANDATORY**: Load this skill when working on:
- Desktop/JVM ViewModel creation and wiring
- SavedStateHandle on Desktop (CreationExtras issue)
- Koin configuration for Desktop
- Platform-specific Desktop setup (ProvideDesktopLifecycle)

**Do NOT use for**: General ViewModel patterns → use @kmp-presentation, General DI → use @kmp-di

## Critical Patterns (Read First)

### Desktop SavedStateHandle Pattern

**Desktop/JVM requires explicit SavedStateHandle creation** when using Koin's `koinViewModel()`:

```kotlin
// Desktop/JVM usage - explicit SavedStateHandle required
val savedStateHandle = SavedStateHandle()
val viewModel: PokemonDetailViewModel = koinViewModel(
    parameters = { parametersOf(route.id, savedStateHandle) }
)
```

**Koin module definition**:
```kotlin
viewModel { (pokemonId: Int, savedStateHandle: SavedStateHandle) ->
    PokemonDetailViewModel(
        repository = get(),
        pokemonId = pokemonId,
        savedStateHandle = savedStateHandle,
    )
}
```

### Why This Happens

- Android's `ComponentActivity` provides proper `CreationExtras` automatically
- Desktop/JVM lacks this automatic population of `CreationExtras`
- Solution: Pass `SavedStateHandle` explicitly via `parametersOf`

## Reference Loading Guide

| Task | Reference | Load When |
|------|-----------|-----------|
| SavedStateHandle patterns | [desktop-savedstate.md](references/desktop-savedstate.md) | Desktop ViewModel wiring |
| JVM-specific patterns | [jvm-patterns.md](references/jvm-patterns.md) | Platform configuration |

## Related Skills

| Skill | Use For |
|-------|---------|
| @kmp-presentation | General ViewModel patterns (lifecycle, UiStateHolder) |
| @kmp-di | Koin configuration and wiring |
| @kmp-architecture | Module structure and vertical slicing |
| @compose-screen | Compose UI implementation |

## Documentation Sources

| Document | Purpose | Tokens |
|----------|---------|--------|
| Desktop SavedStateHandle | Complete Desktop SavedStateHandle guide | This skill |

## Decision Framework

Before implementing Desktop/JVM features, ask yourself:

1. **Does this ViewModel need state persistence?**
   - YES → Create `SavedStateHandle` manually with `SavedStateHandle()`
   - NO → Pass `SavedStateHandle()` anyway for consistency with Android
   - Desktop lacks automatic CreationExtras, must create explicitly

2. **What platform-specific code is needed?**
   - File operations → Use `expect/actual` in `:core` modules
   - Window management → Desktop-specific in `jvmMain` source set
   - Shared business logic → Keep in `commonMain` (ViewModels, repos)

3. **How do I test Desktop-specific code?**
   - Use `jvmTest` source set for Desktop-specific tests
   - Test SavedStateHandle persistence with `by saved` delegate
   - Verify window lifecycle with Desktop test harness

## Essential Workflows

### Workflow 1: Configure SavedStateHandle on Desktop

Desktop/JVM requires explicit `SavedStateHandle` creation because it lacks the automatic `CreationExtras` population found on Android.

1. **Define ViewModel in Koin**: Ensure your Koin module expects a `SavedStateHandle` parameter.
   ```kotlin
   viewModel { (id: Int, savedStateHandle: SavedStateHandle) ->
       MyViewModel(id, get(), savedStateHandle)
   }
   ```

2. **Initialize in jvmMain**: Create the handle manually in your navigation entry or screen.
   ```kotlin
   entry<MyRoute> { route ->
       val savedStateHandle = SavedStateHandle() // Explicit creation
       val viewModel: MyViewModel = koinViewModel(
           parameters = { parametersOf(route.id, savedStateHandle) }
       )
       MyScreen(viewModel)
   }
   ```

### Workflow 2: Set up Koin for Desktop

Desktop applications require manual Koin initialization in the `main` entry point.

1. **Initialize Koin**: Call `startKoin` before or within the `application` block.
   ```kotlin
   fun main() = application {
       startKoin {
           modules(sharedModules + desktopModules)
       }
       Window(onCloseRequest = ::exitApplication) {
           App()
       }
   }
   ```

2. **Provide Lifecycle**: Wrap your root Composable with `ProvideDesktopLifecycle` to enable ViewModel support.
   ```kotlin
   ProvideDesktopLifecycle {
       KoinContext {
           AppContent()
       }
   }
   ```

### Workflow 3: Handle Platform-Specific APIs

Use the `expect`/`actual` pattern for JVM-specific functionality like file system access or system properties.

1. **Define in commonMain**: Create an `expect` declaration.
   ```kotlin
   expect fun getPlatformName(): String
   ```

2. **Implement in jvmMain**: Provide the `actual` implementation using standard Java/Kotlin APIs.
   ```kotlin
   actual fun getPlatformName(): String = "Desktop (JVM) ${System.getProperty("os.name")}"
   ```

## Critical Guardrails

1. NEVER assume Android APIs available → Desktop is pure JVM; avoid `android.*` packages and platform-specific classes like `Context`.

2. NEVER skip SavedStateHandle initialization → Desktop requires explicit setup; failing to pass it via `parametersOf` will cause a `CreationExtras` runtime crash.

3. NEVER use Android-specific Koin modules → Desktop has its own wiring; ensure you only include modules compatible with the JVM target.

4. NEVER forget platform checks → Use `expect/actual` or platform-specific source sets for non-shared logic to prevent compilation errors on other targets.

5. NEVER deploy iOS-only code to Desktop → Verify KMP target configuration to ensure iOS-specific dependencies or code don't leak into the JVM build.

6. NEVER use Robolectric on Desktop → Desktop uses the standard JVM test runner; use JUnit or Kotest for desktop-specific unit tests.

## Quick Reference

| Command | Purpose | When to Run |
| --- | --- | --- |
| `./gradlew :composeApp:run` | Run desktop app | Local development |
| `./gradlew :composeApp:assemble` | Build all platform targets | Verification |
| `./gradlew jvmTest` | Run JVM-specific tests | When testing desktop logic |

## Cross-References

### Skills (by Category)

**Architecture**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-architecture | Module structure, vertical slicing, feature boundaries | [SKILL.md](../kmp-architecture/SKILL.md) |
| @kmp-critical-patterns | 6 core patterns quick reference | [SKILL.md](../kmp-critical-patterns/SKILL.md) |

**Layer Implementation**
| Skill | Purpose | Link |
| --- | --- | --- |
| @kmp-presentation | ViewModels, lifecycle, UI state management | [SKILL.md](../kmp-presentation/SKILL.md) |
| @kmp-di | Koin dependency injection patterns | [SKILL.md](../kmp-di/SKILL.md) |

**Platform**
| Skill | Purpose | Link |
| --- | --- | --- |
| @compose-screen | Compose UI implementation and previews | [SKILL.md](../compose-screen/SKILL.md) |

### Documents

| Document | Purpose | Link |
| --- | --- | --- |
| Architecture + conventions | Master reference for architecture, modules, DI | See @kmp-architecture skill |
| Desktop SavedStateHandle | Complete guide for SavedStateHandle on Desktop | See @kmp-desktop skill |
| Dependency injection | Koin patterns and troubleshooting | See @kmp-di skill |

