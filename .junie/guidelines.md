# Junie Guidelines (Entrypoint)

**Last Updated:** December 20, 2025

`docs/**` is the single source of truth for architecture, patterns, prompts, and product requirements.
This file is intentionally link-first and minimal.

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

## Essential Command

```bash
./gradlew :composeApp:assembleDebug test --continue
```

## Sync Guardrail

When updating agent routing or entrypoint guardrails, keep these aligned:

- [`AGENTS.md`](../AGENTS.md)
- [`.github/copilot-instructions.md`](../.github/copilot-instructions.md)
- this file
- [`docs/agent-prompts/base_agent_prompt.md`](../docs/agent-prompts/base_agent_prompt.md)
- [`docs/agent-prompts/README.md`](../docs/agent-prompts/README.md)
