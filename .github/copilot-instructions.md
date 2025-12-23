# Copilot Instructions for Kotlin Multiplatform Pokedex

**Last Updated:** December 20, 2025

> 🔗 **Related**: See also [`.junie/guidelines.md`](../.junie/guidelines.md) (Junie) and [`AGENTS.md`](../AGENTS.md)
> (agent routing).
>
> ✅ **Canonical Source of Truth**: `docs/**` (architecture, patterns, prompts, product).
> Do not duplicate canonicals in this file—link to them.

## Base Prompt + Specialized Agents

All modes should include the shared base prompt:

- [`docs/agent-prompts/base_agent_prompt.md`](../docs/agent-prompts/base_agent_prompt.md)

**Canonical agent routing table:** See [`docs/agent-prompts/README.md`](../docs/agent-prompts/README.md#-agent-routing-table-canonical) for complete routing table.

## Context Packs (LLM Efficiency)

| Scenario              | Include (minimum)                                 | Optional Adds                                                                             | Target Tokens |
| --------------------- | ------------------------------------------------- | ----------------------------------------------------------------------------------------- | ------------- |
| Standard dev/bugfix   | `base_agent_prompt` + 1 relevant delta prompt     | Link to exact file/diff; use testing_quick_ref + critical_patterns_compact when tight     | ≤ 1.6k        |
| Documentation edits   | Documentation delta + `AGENTS.md` (routing table) | Canonical links (`conventions`, `critical_patterns`); swap to compact/quick refs if tight | ≤ 1.2k        |
| Architecture/Q&A only | `AGENTS.md` + links to canonicals                 | Add one feature example link; compact/quick refs for patterns/tests                       | ≤ 0.9k        |

- Prefer links over pasted excerpts; add one link at a time to stay within budget.
- When more context is needed, **switch modes** rather than stacking deltas.

Low-token pack:
- [`docs/tech/testing_quick_ref.md`](../docs/tech/testing_quick_ref.md)
- [`docs/tech/critical_patterns_compact.md`](../docs/tech/critical_patterns_compact.md)

## Essential Rules (Copilot)

### 1) Follow canonicals (link-first)

- Architecture master: [`docs/tech/conventions.md`](../docs/tech/conventions.md)
- Critical patterns: [`docs/tech/critical_patterns_quick_ref.md`](../docs/tech/critical_patterns_quick_ref.md)
- Testing strategy: [`docs/tech/testing_strategy.md`](../docs/tech/testing_strategy.md)
- Product canon: [`docs/project/prd.md`](../docs/project/prd.md)

### 2) Primary validation command (run first)

```bash
./gradlew :composeApp:assembleDebug test --continue
```

All commands: [`docs/QUICK_REFERENCE.md`](../docs/QUICK_REFERENCE.md)

### 3) iOS policy

- Do not run iOS builds unless explicitly required (slow).
- Prefer Android/Desktop validation.

## Sync Guardrail

When updating agent routing or top-level guardrails, keep these aligned:

- [`AGENTS.md`](../AGENTS.md)
- [`.junie/guidelines.md`](../.junie/guidelines.md)
- this file
- [`docs/agent-prompts/base_agent_prompt.md`](../docs/agent-prompts/base_agent_prompt.md)
- [`docs/agent-prompts/README.md`](../docs/agent-prompts/README.md)
