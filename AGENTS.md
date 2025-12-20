# AI Agent Instructions for Kotlin Multiplatform Pokedex

**Last Updated:** December 20, 2025

> 🔗 **Base Prompt**: All agents share [`base_agent_prompt.md`](docs/agent-prompts/base_agent_prompt.md) — patterns,
> conventions, rules
>
> 📋 **Agent Index**: [`docs/agent-prompts/README.md`](docs/agent-prompts/README.md) — Full specialized agent catalog

## 🎯 Specialized Agent Routing

| Task Type | Agent Mode | Prompt | When to Use |
| --- | --- | --- | --- |
| 🧩 Product | Product Design | [`product_designer_agent_system_prompt_DELTA.md`](docs/agent-prompts/product_designer_agent_system_prompt_DELTA.md) | PRD/acceptance criteria, scope decisions |
| 🎨 Visual Design | UI/UX Design | [`uiux_agent_system_prompt_DELTA.md`](docs/agent-prompts/uiux_agent_system_prompt_DELTA.md) | Screen layouts, motion, interaction design |
| 📱 Compose UI | Screen (Compose) | [`ui_ux_system_agent_for_generic_screen_DELTA.md`](docs/agent-prompts/ui_ux_system_agent_for_generic_screen_DELTA.md) | Implement Android/Desktop Compose screens |
|  SwiftUI | Screen (SwiftUI) | [`ui_ux_system_agent_for_swiftui_screen_DELTA.md`](docs/agent-prompts/ui_ux_system_agent_for_swiftui_screen_DELTA.md) | Implement native iOS screens |
| 🔧 KMP Logic | KMP Mobile Expert | [`kmp_mobile_expert_agent_system_prompt_DELTA.md`](docs/agent-prompts/kmp_mobile_expert_agent_system_prompt_DELTA.md) | Shared ViewModels, repositories, iOS bridging |
| 🚪 Onboarding | Onboarding Design | [`onboarding_agent_system_prompt_DELTA.md`](docs/agent-prompts/onboarding_agent_system_prompt_DELTA.md) | Onboarding flows and copy |
| 🗺️ Flows | User Flow Planning | [`user_flow_agent_system_prompt_DELTA.md`](docs/agent-prompts/user_flow_agent_system_prompt_DELTA.md) | End-to-end journeys, navigation contracts |
| 🧪 Test Planning | Testing Strategy | [`testing_agent_system_prompt_DELTA.md`](docs/agent-prompts/testing_agent_system_prompt_DELTA.md) | Coverage analysis, test design |
| 🧰 Backend | Backend Development | [`backend_agent_system_prompt_DELTA.md`](docs/agent-prompts/backend_agent_system_prompt_DELTA.md) | Ktor server endpoints and contracts |
| 📝 Docs | Documentation | [`documentation_agent_system_prompt_DELTA.md`](docs/agent-prompts/documentation_agent_system_prompt_DELTA.md) | Keep docs consistent + link-first |
| ⚙️ Standard | Development | *(this file)* | General implementation tasks |

## 🧠 Context Packing (LLM Efficiency)

Prefer **links over pasted prose**.

Low-token pack:

- [`testing_quick_ref.md`](docs/tech/testing_quick_ref.md)
- [`critical_patterns_compact.md`](docs/tech/critical_patterns_compact.md)

If more context is needed, add **one link at a time** (specific file paths, diffs, or one canonical guide).

## 🏗️ Canonical Documentation

`docs/**` is the single source of truth for architecture, patterns, prompts, and product requirements.

Start here:

- Architecture + conventions: [`docs/tech/conventions.md`](docs/tech/conventions.md)
- Critical patterns: [`docs/tech/critical_patterns_quick_ref.md`](docs/tech/critical_patterns_quick_ref.md)
- Testing strategy: [`docs/tech/testing_strategy.md`](docs/tech/testing_strategy.md)
- Product canon: [`docs/project/prd.md`](docs/project/prd.md)

Legacy note:

- ✅ The legacy Junie guides folder has been removed (cleanup complete). Canonicals live in `docs/**`.

## 🔄 Multi-Entrypoint Synchronization (Guardrails)

These entrypoints MUST remain aligned (routing table + top-level guardrails only; no duplicated canonicals):

- `AGENTS.md` (this file)
- `.github/copilot-instructions.md`
- `.junie/guidelines.md`
- `docs/agent-prompts/base_agent_prompt.md`
- `docs/agent-prompts/README.md`

Canonicals they must link to (single source of truth):

- `docs/tech/conventions.md`
- `docs/tech/critical_patterns_quick_ref.md`

## ✅ When Agentic Docs Change (Checklist)

If you change anything in the agentic system (routing, prompts, canonicals), update **all relevant entrypoints** so they
stay link-first and consistent.

Required alignment set:

- `AGENTS.md`
- `.github/copilot-instructions.md`
- `.junie/guidelines.md`
- `docs/agent-prompts/base_agent_prompt.md`
- `docs/agent-prompts/README.md`

Then run the lightweight link checks:

- Sanity scan (no legacy paths should remain):
  ```bash
  rg "\\.junie/guides" -n
  ```

  Expected: no matches (the legacy Junie guides folder has been removed).

## 🚀 Essential Workflow

Primary validation (run first, always):

```bash
./gradlew :composeApp:assembleDebug test --continue
```

All commands: [`docs/QUICK_REFERENCE.md`](docs/QUICK_REFERENCE.md)

## 🚀 Quick Start Workflow

### 1. Identify Task Type → Choose Agent

Is this about product requirements/PRD/acceptance criteria?
    → YES: SWITCH_TO: Product Design Mode

Is this about visual design/animations/flows?
    → YES: SWITCH_TO: UI/UX Design Mode

Is this about implementing Compose UI from specs?
    → YES: SWITCH_TO: Compose Screen Implementation Mode

Is this about implementing SwiftUI UI from specs?
    → YES: SWITCH_TO: SwiftUI Screen Implementation Mode

Is this about KMP ViewModels/repositories/shared logic?
    → YES: SWITCH_TO: KMP Mobile Expert Mode

Is this about onboarding flows/first-run experience?
    → YES: SWITCH_TO: Onboarding Design Mode

Is this about user journeys/navigation contracts?
    → YES: SWITCH_TO: User Flow Planning Mode

Is this about test strategy/coverage/property tests?
    → YES: SWITCH_TO: Testing Strategy Mode

Is this about Ktor server endpoints/backend?
    → YES: SWITCH_TO: Backend Development Mode

Is this about doc sync/consistency/updates?
    → YES: SWITCH_TO: Documentation Mode

Otherwise:
    → Use Standard Development Mode (this document)

### 2. Validate Before Starting

```bash
# ALWAYS run Android build + ALL tests (fastest feedback):
./gradlew :composeApp:assembleDebug test --continue

# Check dependency updates periodically:
./gradlew dependencyUpdates

# iOS builds (5–10 min) — run ONLY when working on iOS features:
open iosApp/iosApp.xcodeproj
```

### 3. Implementation Checklist

- [ ] Code follows [conventions.md](docs/tech/conventions.md)
- [ ] Tests written (Kotest in `androidUnitTest/`)
- [ ] Android build + ALL tests pass
- [ ] If working on iOS: iOS app built in Xcode succeeds
- [ ] Dependencies added to `gradle/libs.versions.toml`
- [ ] Commit uses [Conventional Commits](https://www.conventionalcommits.org/) format

### 4. Commits & Changelog

```bash
# Format: type(scope): description
git commit -m "feat(pokemonlist): add search functionality"
git commit -m "fix(navigation): correct back stack handling"

# Types: feat, fix, docs, test, build, refactor, chore
```

**❌ NEVER manually edit CHANGELOG.md** — it's auto-generated by git-cliff

---

## 📋 Critical Patterns (Quick Reference)

> 🔗 **Canonical Source**: [`docs/tech/critical_patterns_quick_ref.md`](docs/tech/critical_patterns_quick_ref.md)

| Pattern             | Rule                                                                     | Link                                                                          |
| ------------------- | ------------------------------------------------------------------------ | ----------------------------------------------------------------------------- |
| **ViewModel**       | Pass `viewModelScope` to constructor, NO work in `init`, lifecycle-aware | [→ Pattern](docs/tech/critical_patterns_quick_ref.md#viewmodel-pattern)       |
| **Either Boundary** | Return `Either<RepoError, T>`, use `Either.catch { }.mapLeft { }`        | [→ Pattern](docs/tech/critical_patterns_quick_ref.md#either-boundary-pattern) |
| **Impl+Factory**    | `internal class XImpl`, `fun X(...): X = XImpl(...)`                     | [→ Pattern](docs/tech/critical_patterns_quick_ref.md#implfactory-pattern)     |
| **Navigation 3**    | Route objects in `:api`, EntryProviderInstaller in wiring                | [→ Pattern](docs/tech/critical_patterns_quick_ref.md#navigation-3-pattern)    |
| **Testing**         | androidUnitTest/ for business logic, 30-40% property tests               | [→ Pattern](docs/tech/critical_patterns_quick_ref.md#testing-pattern)         |

---

## 🚨 Critical Don'ts (Top 10)

| ❌ Never                              | ✅ Instead                          | Reason                          |
| ------------------------------------- | ----------------------------------- | ------------------------------- |
| Run iOS builds for routine validation | Use Android build                   | iOS builds 5-10min slower       |
| Store `CoroutineScope` as field       | Pass to constructor with default    | Violates ViewModel pattern      |
| Work in `init` block                  | Use lifecycle callbacks (`start()`) | Not lifecycle-aware             |
| Return `Result` or nullable           | Return `Either<RepoError, T>`       | Type-safe error handling        |
| Swallow `CancellationException`       | Use `Either.catch`                  | Breaks coroutine cancellation   |
| Create empty use cases                | Call repos directly                 | Reduces unnecessary abstraction |
| Export `:data`/`:ui`/`:wiring` to iOS | Only export `:api`/`:presentation`  | iOS boundary violation          |
| Use star imports                      | Use explicit imports                | .editorconfig rule              |
| Add Compose to iOS modules            | Use convention plugins correctly    | Compose leak prevention         |
| Skip tests                            | Write tests in `androidUnitTest/`   | Test enforcement policy         |

---

## 📚 Essential Documentation

| Guide                                                                        | Purpose                                                   | When to Read            |
| ---------------------------------------------------------------------------- | --------------------------------------------------------- | ----------------------- |
| [`conventions.md`](docs/tech/conventions.md)                                 | **Master reference** — Architecture, modules, DI, testing | START HERE              |
| [`critical_patterns_quick_ref.md`](docs/tech/critical_patterns_quick_ref.md) | 6 core patterns (ViewModel, Either, Impl+Factory, etc.)   | Implementing features   |
| [`ios_integration.md`](docs/tech/ios_integration.md)                         | SwiftUI + KMP ViewModels Direct Integration               | Working on iOS          |
| [`testing_strategy.md`](docs/tech/testing_strategy.md)                       | Kotest, MockK, Turbine, property tests                    | Writing tests           |
| [`dependency_injection.md`](docs/tech/dependency_injection.md)               | Koin patterns, troubleshooting                            | DI issues               |
| [`navigation.md`](docs/tech/navigation.md)                                   | Navigation 3 modular architecture                         | Implementing navigation |
| [`prd.md`](docs/project/prd.md)                                              | Product requirements, acceptance criteria                 | Understanding features  |
| [`user_flow.md`](docs/project/user_flow.md)                                  | User journeys and flows                                   | Planning UX             |

**Build Configuration**: [`gradle/libs.versions.toml`](gradle/libs.versions.toml) | [`settings.gradle.kts`](settings.gradle.kts)

---

## 🎭 Agent Mode Switching

**Command Format**:

```
SWITCH_TO: [Mode Name]
```

**Available Modes**:

- `Product Design Mode` — PRD, acceptance criteria, scope
- `UI/UX Design Mode` — Visual design, animations, user flows
- `Compose Screen Implementation Mode` — Android/Desktop Compose UI
- `SwiftUI Screen Implementation Mode` — iOS native UI
- `KMP Mobile Expert Mode` — Shared ViewModels, repositories
- `Onboarding Design Mode` — First-run experience, flows
- `User Flow Planning Mode` — Journey mapping, navigation
- `Testing Strategy Mode` — Test planning, coverage analysis
- `Backend Development Mode` — Ktor server endpoints
- `Documentation Mode` — Doc sync, consistency audits
- `Standard Development Mode` — General implementation (default)

**Response Format** (when in specialized mode):

```
CURRENT_MODE: [Mode Name]
```

---

## ✅ Success Criteria

You're effective when you can:

- [ ] Implement repositories returning `Either<RepoError, T>`
- [ ] Create ViewModels following lifecycle-aware pattern
- [ ] Write Kotest tests with 30-40% property-based coverage
- [ ] Add dependencies via version catalog
- [ ] Validate with Android build + ALL tests
- [ ] Find answers in `docs/` before asking
- [ ] Switch to appropriate agent mode

---

## 💡 Pro Tips

1. 🔍 **Search `docs/` first** before asking questions
1. ⚡ **Android build = 45s feedback** — run often
1. 🐌 **iOS builds = 5-10min** — only when needed
1. 🎯 **Either at boundaries** — always
1. ⏱️ **ViewModels lifecycle-aware** — never init work
1. 🔗 **Reference, don't duplicate** — link to source files
1. 🎲 **Property tests = 1000x coverage** — one test, many scenarios

---

## Reference Implementation (Use as Template)

Use `pokemonlist` as the canonical “how it’s done here” vertical slice example:

| Layer | File | Purpose |
| --- | --- | --- |
| API | [PokemonListRepository.kt](features/pokemonlist/api/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/PokemonListRepository.kt) | Interface + public types |
| Data | [PokemonListRepositoryImpl.kt](features/pokemonlist/data/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/data/PokemonListRepositoryImpl.kt) | Repo impl + error mapping |
| Presentation | [PokemonListViewModel.kt](features/pokemonlist/presentation/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModel.kt) | Shared ViewModel |
| UI | [PokemonListScreen.kt](features/pokemonlist/ui/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/ui/PokemonListScreen.kt) | Compose screen + previews |
| Wiring | [PokemonListModule.kt](features/pokemonlist/wiring/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/wiring/PokemonListModule.kt) | Koin DI |
