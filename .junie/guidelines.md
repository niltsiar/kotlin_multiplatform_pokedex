# Junie Guidelines (Entrypoint)

**Last Updated:** December 20, 2025

> 🔗 **Related**: [`.github/copilot-instructions.md`](../.github/copilot-instructions.md) (Copilot),
> [`AGENTS.md`](../AGENTS.md) (Agents) — kept in sync
>
> 🔄 **Sync Guardrail**: These three files + `conventions.md` + `critical_patterns_quick_ref.md` MUST stay synchronized.
> See [`AGENTS.md`](../AGENTS.md) → "Multi-Entrypoint Synchronization" for validation workflow.

`docs/**` is the single source of truth for architecture, patterns, prompts, and product requirements.
This file is intentionally link-first and minimal.

## Documentation Principle: Reference, don't duplicate

Rather than embedding verbose examples in documentation files, **provide clickable links** to canonical source files.
This ensures docs stay in sync automatically and keeps instruction files concise.

**When adding examples:**

1. Find or create a canonical implementation in the codebase
2. Link to it with a relative path:
   `[PokemonListRepository.kt](../features/pokemonlist/api/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/PokemonListRepository.kt)`
3. Add only minimal context—let the source file speak for itself

## Code Style

All code must comply with `.editorconfig` settings. Key rules:

- **Max line length**: 120 characters
- **Indent size**: 4 spaces (Kotlin), 2 spaces (Swift/YAML/JSON)
- **Trailing commas**: Required in multiline constructs
- **No star imports**: Explicit imports only (`import x.y.ClassName`, NOT `import x.y.*`)
- **Trailing whitespace**: Not allowed
- **Final newline**: Required

## Start Here

- Base prompt (shared rules): [`docs/agent-prompts/base_agent_prompt.md`](../docs/agent-prompts/base_agent_prompt.md)
- Agent catalog (pick a mode): [`docs/agent-prompts/README.md`](../docs/agent-prompts/README.md)
- Routing overview: [`AGENTS.md`](../AGENTS.md)

## Canonical Docs

- Architecture + conventions: [`docs/tech/conventions.md`](../docs/tech/conventions.md)
- Critical patterns: [`docs/tech/critical_patterns_quick_ref.md`](../docs/tech/critical_patterns_quick_ref.md)
- Testing strategy: [`docs/tech/testing_strategy.md`](../docs/tech/testing_strategy.md)
- Product requirements (PRD): [`docs/project/prd.md`](../docs/project/prd.md)

Low-token packs:

- [`docs/tech/testing_quick_ref.md`](../docs/tech/testing_quick_ref.md)
- [`docs/tech/critical_patterns_compact.md`](../docs/tech/critical_patterns_compact.md)

## Context Packs (LLM Efficiency)

| Scenario              | Include (minimum)                                 | Optional Adds                                                                             | Target Tokens |
| --------------------- | ------------------------------------------------- | ----------------------------------------------------------------------------------------- | ------------- |
| Standard dev/bugfix   | `base_agent_prompt` + 1 relevant delta prompt     | Link to exact file/diff; use testing_quick_ref + critical_patterns_compact when tight     | ≤ 1.6k        |
| Documentation edits   | Documentation delta + `AGENTS.md` (routing table) | Canonical links (`conventions`, `critical_patterns`); swap to compact/quick refs if tight | ≤ 1.2k        |
| Architecture/Q&A only | `AGENTS.md` + links to canonicals                 | Add one feature example link; compact/quick refs for patterns/tests                       | ≤ 0.9k        |

- Prefer links over pasted excerpts; add one link at a time to stay within budget.
- When more context is needed, **switch modes** rather than stacking deltas.

## Tech Stack

- **Kotlin Multiplatform** — Shared business logic across platforms
- **Compose Multiplatform** — UI for Android + Desktop + iOS Compose
- **SwiftUI** — Native iOS UI (production app)
- **Koin DI** — Dependency injection
- **Ktor** — HTTP client (PokéAPI) + Backend server
- **Arrow Either** — Functional error handling
- **Navigation 3** — Type-safe modular navigation
- **Kotest + MockK** — Testing framework with property-based tests
- **Roborazzi** — Screenshot testing

## Essential Commands

```bash
# PRIMARY validation (run first, always):
./gradlew :composeApp:assembleDebug test --continue

# Check dependency updates:
./gradlew dependencyUpdates

# Desktop app:
./gradlew :composeApp:run

# Ktor server (port 8080):
./gradlew :server:run

# iOS builds (5-10min - only when needed):
open iosApp/iosApp.xcodeproj

# Screenshot tests:
./gradlew recordRoborazziDebug   # Record baselines
./gradlew verifyRoborazziDebug   # Verify against baselines
```

All commands: [`docs/QUICK_REFERENCE.md`](../docs/QUICK_REFERENCE.md)

## Sync Guardrail

When updating agent routing or entrypoint guardrails, keep these aligned:

- [`AGENTS.md`](../AGENTS.md)
- [`.github/copilot-instructions.md`](../.github/copilot-instructions.md)
- this file
- [`docs/agent-prompts/base_agent_prompt.md`](../docs/agent-prompts/base_agent_prompt.md)
- [`docs/agent-prompts/README.md`](../docs/agent-prompts/README.md)

## Commits & Changelog

### Conventional Commits (Required)

All commits must follow Conventional Commits format for automatic changelog generation:

```bash
git commit -m "type(scope): description"
```

**Commit Types:**

- `feat` → New feature (✨ Features in changelog)
- `fix` → Bug fix (🐛 Bug Fixes)
- `docs` → Documentation (📝 Documentation)
- `test` → Tests (✅ Tests)
- `build` → Build system (🔧 Build System)
- `refactor` → Code refactoring (♻️ Refactoring)
- `chore` → Maintenance (🧹 Chores)

**Examples:**

```bash
git commit -m "feat(pokemonlist): add search functionality"
git commit -m "refactor(navigation): align package with folder structure"
git commit -m "docs(conventions): update testing requirements"
```

### CHANGELOG Policy

- ❌ **DO NOT manually edit CHANGELOG.md** — it's auto-generated by git-cliff
- ✅ Use proper Conventional Commits format — git-cliff parses these automatically
- ✅ Regenerate changelog before releases: `git cliff -o CHANGELOG.md`
- ✅ Preview without writing: `git cliff --dry-run`
