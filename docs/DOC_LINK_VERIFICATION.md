# Doc Link Verification (Lightweight)

Last Updated: December 20, 2025

Purpose: Minimal, tooling-free checks to avoid doc drift and broken links during the agentic-docs migration.

## 1) Sanity check: no accidental `.junie/guides/**` canonicals

`.junie/guides/**` has been removed. Canonical docs live in `docs/**`.

```bash
rg "\.junie/guides" -n --glob '!.junie/guides/**'
```

Expected results:

- Mentions in `docs/AGENTIC_SYSTEM_UPGRADE_PLAN.md` (plan/history text)
- Mentions in `docs/DOC_LINK_VERIFICATION.md` (this document)

If you see `.junie/guides/**` inside canonical docs (e.g. `docs/tech/**`, `docs/patterns/**`, `docs/project/**`), flip it
to the corresponding `docs/**` link.

## 2) Entrypoint spot-check (click-through)

Open and click these links:

- `AGENTS.md`
- `.junie/guidelines.md`
- `.github/copilot-instructions.md`

Confirm they route to:

- `docs/agent-prompts/base_agent_prompt.md`
- `docs/agent-prompts/README.md`
- `docs/tech/conventions.md`
- `docs/tech/critical_patterns_quick_ref.md`
- `docs/tech/testing_strategy.md`

## 3) Prompt catalog sanity check

From `docs/agent-prompts/README.md`:

- Each `*_DELTA.md` prompt begins with “Include Base Agent Prompt + Canonical Links.”
- No prompt duplicates canonicals (should be links).

## 4) Optional: quick broken-link scan (manual)

If a link looks suspicious:

- confirm the target file exists
- confirm relative paths are correct (`../` vs `../../`)

Tip: prefer relative links **within** `docs/**`.
