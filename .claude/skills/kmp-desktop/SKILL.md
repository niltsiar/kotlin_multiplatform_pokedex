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
| [desktop_viewmodel_savedstate.md](../../../docs/tech/desktop_viewmodel_savedstate.md) | Complete Desktop SavedStateHandle guide | ~200 |

## Quick Reference

### Desktop vs Android Pattern

| Aspect | Desktop/JVM | Android |
|--------|-------------|---------|
| SavedStateHandle | Explicit creation | Automatic via CreationExtras |
| Koin parameters | `parametersOf(id, SavedStateHandle())` | `parametersOf(id)` |
| Lifecycle setup | `ProvideDesktopLifecycle` | ComponentActivity |

### Validation Command

```bash
./gradlew :composeApp:run
```
