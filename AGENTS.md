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
| SwiftUI | Screen (SwiftUI) | [`ui_ux_system_agent_for_swiftui_screen_DELTA.md`](docs/agent-prompts/ui_ux_system_agent_for_swiftui_screen_DELTA.md) | Implement native iOS screens |
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

- `.junie/guides/**` is now a **pointer layer** during migration and will be cleaned up at the end of the plan.

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

## 🚀 Essential Workflow

Primary validation (run first, always):

```bash
./gradlew :composeApp:assembleDebug test --continue
```

All commands: [`docs/QUICK_REFERENCE.md`](docs/QUICK_REFERENCE.md)

## Reference Implementation (Use as Template)

Use `pokemonlist` as the canonical “how it’s done here” vertical slice example:

| Layer | File | Purpose |
| --- | --- | --- |
| API | [PokemonListRepository.kt](features/pokemonlist/api/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/PokemonListRepository.kt) | Interface + public types |
| Data | [PokemonListRepositoryImpl.kt](features/pokemonlist/data/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/data/PokemonListRepositoryImpl.kt) | Repo impl + error mapping |
| Presentation | [PokemonListViewModel.kt](features/pokemonlist/presentation/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/presentation/PokemonListViewModel.kt) | Shared ViewModel |
| UI | [PokemonListScreen.kt](features/pokemonlist/ui/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/ui/PokemonListScreen.kt) | Compose screen + previews |
| Wiring | [PokemonListModule.kt](features/pokemonlist/wiring/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/wiring/PokemonListModule.kt) | Koin DI |
