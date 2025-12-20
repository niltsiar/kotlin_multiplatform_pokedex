# Base Agent Prompt (Shared Core)

Last Updated: December 20, 2025

Purpose: Single source of truth for shared rules and workflows across all agent modes (AGENTS.md, Copilot, Junie, and
role prompts). Role prompts MUST reference this file and only add role-specific deltas.

## How to Use

- All role prompts begin with: “Include Base Agent Prompt + Canonical Links”.
- Do not duplicate sections from this base; link to the canonical sources below when detail is needed.
- When token budget is tight, include the compact quick refs (`critical_patterns_compact`, `testing_quick_ref`) with this
  base instead of full guides.

## 🔗 Canonical Sources (Click to Navigate)

| Category | Document | Purpose |
| --- | --- | --- |
| 🎯 Start Here | [conventions.md](../tech/conventions.md) | Architecture, modules, DI, patterns |
| 📋 Patterns | [critical_patterns_quick_ref.md](../tech/critical_patterns_quick_ref.md) | Canonical pattern rules (DI, Either, VM, Nav, Tests) |
| 📋 Patterns (compact) | [critical_patterns_compact.md](../tech/critical_patterns_compact.md) | Low-token pattern cards |
| 🧪 Testing | [testing_strategy.md](../tech/testing_strategy.md) | Kotest, MockK, Turbine, property tests |
| 🧪 Testing (quick) | [testing_quick_ref.md](../tech/testing_quick_ref.md) | Token-lean enforcement + locations |
| 🧭 Navigation | [navigation.md](../tech/navigation.md) | Navigation 3, routing |
| 📱 iOS | [ios_integration.md](../tech/ios_integration.md) | SwiftUI + KMP ViewModels |
| ⚡ Commands | [QUICK_REFERENCE.md](../QUICK_REFERENCE.md) | Gradle commands and troubleshooting |
| 📚 Agent Index | [README.md](README.md) | All specialized agents + entry files |
| 🗺️ Agent Routing | [AGENTS.md](../../AGENTS.md) | Routing table, decision tree, sync guardrails |
| 📦 Product Canon | [prd.md](../project/prd.md) | Acceptance criteria and scope |

**Reference Implementation** (pokemonlist feature — use as template):

- [API](../../features/pokemonlist/api/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/PokemonListRepository.kt) • [Data](../../features/pokemonlist/data/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/data/PokemonListRepositoryImpl.kt) • [Presentation](../../features/pokemonlist/presentation/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModel.kt) • [UI](../../features/pokemonlist/ui/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/ui/PokemonListScreen.kt) • [Wiring](../../features/pokemonlist/wiring/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/wiring/PokemonListModule.kt)

## Mode Selection (Quick Decision)

| Input Type | Mode |
| --- | --- |
| Greeting, question, explanation | Chat / Advanced Chat |
| Run/test commands (no edits) | Run & Verify |
| 1–3 step edit, single file | Fast Code |
| Everything else | Code ✅ default |

## Architecture Quick Reference

**Tech Stack**: Kotlin Multiplatform • Compose (Android/Desktop) • SwiftUI (iOS) • Koin DI • Ktor • Arrow Either •
Navigation 3

**Vertical slice feature pattern (REQUIRED):**

- `:features:<feature>:api` → Contracts (exported to iOS)
- `:features:<feature>:data` → Network + data layer (NOT exported)
- `:features:<feature>:presentation` → ViewModels (exported to iOS)
- `:features:<feature>:ui` → Compose UI (NOT exported)
- `:features:<feature>:wiring` → DI assembly (NOT exported)

📖 **Details**: [conventions.md](../tech/conventions.md) • [ios_integration.md](../tech/ios_integration.md)

## Critical Guardrails (Top 10)

1. Do not run iOS builds for routine validation; prefer Android
2. Never store `CoroutineScope` as a field in ViewModels
3. Never do work in ViewModel `init`; use lifecycle callbacks
4. Repositories return `Either<RepoError, T>` (no `Result`, no nullable)
5. Never swallow `CancellationException` (use `Either.catch` / `arrow.core.raise.catch`)
6. No empty pass-through use cases (call repos directly when no orchestration is needed)
7. Don’t export `:data`, `:ui`, `:wiring` to iOS (only `api` + `presentation`)
8. No business logic in `:shared` (it is an umbrella/export module)
9. No DI annotations in production classes (wire in wiring modules)
10. Every `@Composable` needs a realistic `@Preview`

## Essential Workflows

**Primary validation** (run first, always):

```bash
./gradlew :composeApp:assembleDebug test --continue
```

📖 **All commands**: [QUICK_REFERENCE.md](../QUICK_REFERENCE.md)
