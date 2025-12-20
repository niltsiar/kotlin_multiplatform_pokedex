# Agentic System Upgrade Plan (Mandolin-Cocotte Alignment)

**Status**: Planning only (no changes executed)

**Created**: 2025-12-20

## Context

This repository currently keeps most agentic guidance under `.junie/guides/**` (prompts, tech guides, patterns, product docs) and uses a large, monolithic `AGENTS.md` plus mirrored content in `.github/copilot-instructions.md` and `.junie/guidelines.md`.

Inside this repo there is a reference project at `mandolin-cocotte/mobile/` that evolved the agentic system to be more **agent-agnostic**, **token-efficient**, and **docs-first** by moving canonicals to `docs/**` and using a **base prompt + delta prompts** model.

This plan describes all tasks needed to upgrade *this* repo’s agentic system to reflect those best practices.

## Goals (target outcomes)

1. **Agent-agnostic doc layout**: most agent-facing documentation lives under `docs/` (not `.junie/`).
2. **Prompt architecture**: adopt **`base_agent_prompt` + role “DELTA” prompts**.
3. **Clickability**: all references in prompts and agent docs are **clickable markdown links**, not backticked paths.
4. **Single source of truth**: reduce duplication across `AGENTS.md`, `.github/copilot-instructions.md`, and `.junie/guidelines.md` by linking to canonicals.
5. **Operational guardrails**: add explicit sync rules, freshness timestamps, and a lightweight validation workflow.
6. **Backwards compatibility**: keep `.junie/` as a thin compatibility layer (pointers) during migration.
7. **Platform agnostic (tooling)**: the upgraded agentic system must work with any agent/tooling that can read `docs/**`.
8. **Prominent support for Junie + Copilot**: keep first-class entrypoints for JetBrains Junie (`.junie/**`) and GitHub Copilot (`.github/copilot-instructions.md`), while keeping the canonicals agent-agnostic.

## Execution Principles (expert prompt + context engineering)

This plan is intended to be executed by an expert prompt/context engineer. The goal is not just “move files”, but to
produce an agentic system that is:

- **Robust** (clear canonicals, low drift)
- **Token-efficient** (link-first, compact refs)
- **Agent/tool agnostic** (docs-first; integrations are thin)
- **Operationally safe** (frequent commits, reversible phases)

### Decision-making framework (use throughout)

- **Prefer “reference, don’t duplicate”**: if a rule already exists in a canonical doc, link to it and delete the copy.
- **Minimize “blast radius”**:
  - Keep changes additive until a phase explicitly flips references.
  - Avoid breaking links; deprecate with pointers first.
- **Make tradeoffs explicit**:
  - For any non-trivial choice (structure, deprecation window, tool checks), write down: options, pros/cons, decision,
    and rollback path.

### Context packing (token budget discipline)

When creating/rewriting prompts and entrypoints:

- Prefer **clickable links** to canonicals over copying prose.
- Provide **low-token packs** for common work:
  - Base prompt + one DELTA prompt
  - `docs/tech/testing_quick_ref.md` + `docs/tech/critical_patterns_compact.md` (once they exist)
- Add context *incrementally*: if more is needed, link one more file at a time.

### Consultation protocol (ask the user when uncertain)

During implementation, stop and ask you (the maintainer) for confirmation whenever a decision could materially change the
repo’s future maintenance.

Ask-first triggers (non-exhaustive):

- Removing or permanently deprecating `.junie/guides/**` (vs pointer-only indefinitely)
- Adding repo tooling (pre-commit, CI link checkers) that affects contributors
- Splitting docs by domain (mobile vs server) or introducing additional top-level entrypoints
- Renaming prompts/modes (changes mental model + downstream references)

Preferred question format:

- Offer **2–4 options** with clear consequences.
- Provide a recommended default, plus the rollback path.

### Implementation evidence & traceability

To keep the process auditable and “future-agent friendly”:

- Maintain a small **Decision Log** during implementation (either appended to this plan under a dated “Audit/Decisions”
  section, or as a dedicated doc added in Phase 0).
- Each phase should have:
  - a **definition of done**
  - a **verification checklist**
  - a **rollback checkpoint** (commit/tag)

## Version Control Strategy (branch + frequent commits)

**Goal**: preserve a granular history so we can bisect changes, rollback safely, and avoid losing work.

### Branching

- Do the implementation on a dedicated branch (recommended):
  - `chore/agentic-system-upgrade` (or `chore/docs-agentic-system-upgrade`)
- Keep `main` clean; use PRs for review and for an audit trail.
- Optional safety net: use a Git worktree if you want to keep multiple implementation phases isolated.

### Commit discipline

- Commit early and often: **one logical change per commit**.
- Prefer **phase/step commits** over “mega commits”.
- Ensure every commit leaves the repo in a sensible state:
  - Docs-only commits are OK even if links are temporarily duplicated, but avoid breaking existing links until a phase
    explicitly flips references.

### Suggested conventional commit messages

Use `docs(...)` for documentation-only changes; use `chore(...)` for tooling and repo-meta.

- `docs(agentic): add docs-first canonical structure (copy only)`
- `docs(agentic): add base agent prompt (docs/agent-prompts)`
- `docs(agentic): add delta prompts for specialized modes`
- `docs(agentic): shrink AGENTS.md and link to canonicals`
- `docs(agentic): update copilot instructions to link to docs canonicals`
- `docs(agentic): trim Junie guidelines and link to docs canonicals`
- `docs(agentic): convert .junie/guides to pointer layer`
- `chore(docs): add doc link-check script`
- `chore(precommit): add markdown/yaml/json lint hooks`

### Release/rollback checkpoints (recommended)

- Tag phase boundaries so they’re easy to revert to:
  - `agentic-plan-start` (if needed)
  - `agentic-phase1-docs-canonicals`
  - `agentic-phase2-prompts-base-delta`
  - `agentic-phase3-entrypoints`
  - `agentic-phase4-deprecate-junie-guides`
  - `agentic-phase5-validation`

This is optional, but it makes rollback and comparison much easier.

## Non-goals

- No changes to Kotlin/KMP architecture, build logic, or feature modules.
- No iOS builds required for this documentation-only upgrade.
- No large content rewrites unless needed to eliminate contradictions/duplication.

## Reference: What Mandolin-Cocotte Mobile Does Differently

Use `mandolin-cocotte/mobile/` as the reference implementation:

- Canonical docs in `mandolin-cocotte/mobile/docs/tech/**` and `docs/patterns/**`
- Prompt catalog in `mandolin-cocotte/mobile/docs/agent-prompts/**`
- `docs/agent-prompts/base_agent_prompt.md`: shared rules, canonical link table, guardrails, essential commands
- `docs/agent-prompts/*_DELTA.md`: role-specific additions only
- Strong “reference, don’t duplicate” and “clickable links only” enforcement
- Explicit **context packing** / token budget guidance
- Explicit **multi-file synchronization** contract (what must stay in sync)

## Target-State Directory Layout (proposed)

Create a docs-first layout similar to mandolin-cocotte/mobile:

```
docs/
  AGENTIC_SYSTEM_UPGRADE_PLAN.md   (this file)
  QUICK_REFERENCE.md              (commands quick ref; currently lives in .junie/guides)
  agent-prompts/
    base_agent_prompt.md
    README.md
    uiux_agent_system_prompt_DELTA.md
    ui_ux_system_agent_for_generic_screen_DELTA.md
    ui_ux_system_agent_for_swiftui_screen_DELTA.md
    product_designer_agent_system_prompt_DELTA.md
    user_flow_agent_system_prompt_DELTA.md
    testing_agent_system_prompt_DELTA.md
    backend_agent_system_prompt_DELTA.md
    documentation_agent_system_prompt_DELTA.md
    (supporting: animation_example_guides*.md, easter_eggs_and_mini_games_guide.md, etc.)
  tech/
    conventions.md
    critical_patterns_quick_ref.md
    critical_patterns_compact.md
    testing_strategy.md
    testing_quick_ref.md
    ... (existing tech guides)
  patterns/
    ... (existing extended patterns)
  project/
    prd.md
    user_flow.md
    ui_ux.md
    onboarding.md

.junie/
  guidelines.md                    (kept, but trimmed; points to docs canonicals)
  guides/                          (deprecated; becomes pointer-only or removed after migration)
```

Notes:

- `docs/` is the canonical location for agent-facing documentation.
- `.junie/` and `.github/` remain for tool-specific integration and “entrypoint hints”, but should no longer be the primary canonical store.

## Platform-Agnostic Strategy (while keeping Junie + Copilot prominent)

**Principle**: Canonicals live in `docs/**` and must not assume any specific agent runtime. Tool-specific entrypoints may
add small, targeted guidance, but must only *reference* canonicals.

### Canonical layer (agent-agnostic)

- `docs/tech/**`, `docs/patterns/**`, `docs/project/**` are the **single source of truth** for rules and patterns.
- `docs/agent-prompts/base_agent_prompt.md` defines shared agent rules and links to canonicals.
- `docs/agent-prompts/*_DELTA.md` files add role-specific deltas only.

### Tool integration layer (agent-prominent)

- **Junie**
  - Keep `.junie/guidelines.md` as Junie’s primary “entrypoint”, but trim it to a short selector + links into
    `docs/**`.
  - Keep `.junie/guides/**` as a compatibility layer during migration (pointer-only), then deprecate.

- **Copilot**
  - Keep `.github/copilot-instructions.md` as Copilot’s primary “entrypoint”, but shrink it to a Copilot-friendly
    subset + links into `docs/**`.

### Optional: other agents/tools (future-proof)

- If later supporting Cursor/Claude/Codeium/Continue/etc., add *entrypoint stubs* (e.g. `docs/AGENTS_QUICKSTART.md` or
  tool-specific config files) that link to `docs/agent-prompts/base_agent_prompt.md`.
- Do **not** fork canonicals per tool; only add integration-specific notes where a tool truly needs it.

## Work Plan (step-by-step)

### Phase 0 — Pre-migration audit (no edits yet)

**Objective**: produce a clear inventory and a safe migration order.

- [x] Inventory current agentic entrypoints:
  - [x] `AGENTS.md`
  - [x] `.github/copilot-instructions.md`
  - [x] `.junie/guidelines.md`
  - [x] `.junie/guides/**`
  - [x] `README.md` references to agent docs
- [x] Identify duplication hotspots (same rules repeated across 2–3 files).
- [x] Identify all references to `.junie/guides/**` that will need updates.
- [x] Decide whether we want `.junie/guides/**` to be:
  - [x] a pointer layer (recommended), or
  - [ ] removed entirely after a deprecation window.
- [x] Establish the “ask-first” decision points for this repo (from the Consultation Protocol above) and confirm
  decisions with the maintainer before proceeding to irreversible steps.

Deliverable:

- A short “Audit/Decisions” section appended to this plan in the implementation session:
  - what duplicates were found
  - what will become canonical
  - any maintainer-approved decisions that affect later phases

Commit checkpoints:

- [x] `docs(agentic): document audit findings and migration order` (commit: `6b91189`)

### Phase 1 — Establish `docs/` canonicals (additive)

**Objective**: create the new canonical doc structure without breaking existing links.

- [x] Create folders: `docs/agent-prompts`, `docs/tech`, `docs/patterns`, `docs/project`.
- [x] Copy (not move yet) content from `.junie/guides/**` into the corresponding `docs/**` folders.
  - [x] Keep paths stable long enough to update links atomically later.
- [x] Add missing mandolin-style docs that this repo currently lacks:
  - [x] `docs/tech/critical_patterns_compact.md`
  - [x] `docs/tech/testing_quick_ref.md`

Acceptance checks:

- [x] `docs/**` is complete enough to serve as canonicals.
- [x] No existing doc path has been removed yet.

Commit checkpoints (recommended granularity):

- [x] `docs(agentic): add docs/* folders for canonicals` (commit: `ab5a917`)
- [x] `docs(agentic): copy tech/patterns/project docs into docs/* (no link flips)` (commit: `ab5a917`)
- [x] `docs(agentic): add low-token quick refs (critical_patterns_compact, testing_quick_ref)` (commit: `1b721a8`)

### Phase 2 — Introduce “base + delta” prompt model

**Objective**: switch prompts to base+delta and make references clickable.

- [x] Create `docs/agent-prompts/base_agent_prompt.md` modeled after `mandolin-cocotte/mobile/docs/agent-prompts/base_agent_prompt.md`.
  - [x] Include canonical link table pointing to `docs/tech/**` and `AGENTS.md`.
  - [x] Include mode selection decision table and tie-breakers.
  - [x] Include “Critical Guardrails (Top 10)”.
  - [x] Include the primary validation command.
- [x] Convert existing prompts from `.junie/guides/prompts/*.md` into DELTA prompts under `docs/agent-prompts/`.
  - [x] DELTA prompts must only contain role-specific additions.
  - [x] DELTA prompts must link to the base prompt and canonicals.
- [x] Create `docs/agent-prompts/README.md` prompt index (mandolin style).
  - [x] Enforce: “ALL references must be clickable links”.

Acceptance checks:

- [x] Every role prompt begins with: base + canonical links.
- [x] Prompts contain no duplicated canonical sections.
- [x] Prompt references are clickable links.

Commit checkpoints (recommended granularity):

- [x] `docs(agentic): add base_agent_prompt and canonical link table` (commit: `f4eeae2`)
- [x] `docs(agentic): add docs/agent-prompts README + clickable-link rule` (commit: `f4eeae2`)
- [x] `docs(agentic): add delta prompts (one commit or one per prompt)` (commit: `f4eeae2`)

### Phase 3 — Redesign entrypoints (keep them small)

**Objective**: shrink high-churn entrypoint files and make them link-out to canonicals.

- [x] Update `AGENTS.md` to:
  - [x] be significantly smaller (routing + essential workflow + sync contract).
  - [x] link to canonicals in `docs/tech/**` and the prompt index in `docs/agent-prompts/README.md`.
  - [x] include a “Multi-Agent File Synchronization” table like mandolin.
  - [x] include context-packing/token-budget guidance.
- [x] Update `.github/copilot-instructions.md` to:
  - [x] keep a concise summary + must-follow rules.
  - [x] link to canonicals rather than duplicating full sections.
  - [x] keep the agent routing table in sync with `AGENTS.md`.
- [x] Update `.junie/guidelines.md` to:
  - [x] become “Junie integration + short rules + links” rather than long duplicates.
  - [x] use docs links (`docs/tech/**`, `docs/agent-prompts/**`) as canonicals.
- [x] Update root `README.md` “Quick References” to point to `docs/` (not `.junie/guides`).

- [x] Add/adjust entrypoint wording so that:
  - [x] The overall system is explicitly **platform agnostic** (works with any agent/tooling).
  - [x] **Junie** and **Copilot** are still called out as first-class supported agents/tools.

Acceptance checks:

- [x] `AGENTS.md`, `.github/copilot-instructions.md`, and `.junie/guidelines.md` all reference the same canonicals.
- [x] Agent mode names + file paths match across all three.
- [x] “Last Updated” timestamps updated consistently.

Commit checkpoints (recommended granularity):

- [x] `docs(agentic): update AGENTS.md to route + link to docs canonicals` (commit: `ddfed3c`)
- [x] `docs(agentic): update copilot instructions to link to docs canonicals` (commit: `ddfed3c`)
- [x] `docs(agentic): update Junie guidelines to be link-heavy + tool-specific` (commit: `ddfed3c`)
- [x] `docs(agentic): update root README quick references to docs/*` (commit: `ab48a00`)

### Phase 4 — Migration: flip links + deprecate `.junie/guides/**`

**Objective**: make `docs/` canonical and prevent drift.

- [x] Replace `.junie/guides/**` content with pointer files that link to `docs/**` equivalents.
  - [x] Prefer short “This file moved” docs with a single link.
- [x] Update all internal references to `.junie/guides/**` to point to `docs/**`.
  - [x] Especially: prompt indexes, quick reference links, and any “start here” guides.

Acceptance checks:

- [x] No canonical content remains only in `.junie/guides/**`.
- [x] `.junie/guides/**` either:
  - [x] contains only pointers, or
  - [ ] is removed (only after a deliberate deprecation decision).

Commit checkpoints (recommended granularity):

- [x] `docs(agentic): flip internal references from .junie/guides to docs/*` (commit: `ab48a00`)
- [x] `docs(agentic): replace .junie/guides content with pointers to docs/*` (commit: `ebc2d96`)
- [ ] *(optional)* `chore(agentic): remove deprecated .junie/guides after window`

### Phase 5 — Add lightweight validation workflow (docs-only)

**Objective**: reduce “doc drift” and broken links.

Options (pick one):

**A) Minimal (recommended to start)**

- [x] Add a `docs/agent-prompts/documentation_agent_system_prompt_DELTA.md` modeled after mandolin. (commit: `f4eeae2`)
- [x] Add a checklist section in `AGENTS.md` for “When agentic docs change, also update X/Y/Z”. (commit: `ec781ae`)
- [x] Add a small “Link Verification” guide: suggested grep patterns and manual checks. (commit: `ec781ae`)

**B) Add repo tooling (optional)**

- [ ] Add `scripts/check-doc-links.sh` that runs a markdown link checker.
- [ ] Add pre-commit hooks for markdown formatting/linting.
  - [ ] Document how to run locally.
  - [ ] Add CI job to run these checks.

Acceptance checks:

- [ ] There is a documented process to keep the system consistent.
- [ ] Broken links can be detected early.

Commit checkpoints (recommended granularity):

- [x] `docs(agentic): add documentation agent delta prompt (docs/agent-prompts)` (commit: `f4eeae2`)
- [ ] `chore(docs): add doc link verification script`
- [ ] `chore(precommit): add markdown/yaml/json lint hooks (optional)`

## Mapping: Current → Target (initial proposal)

This table guides the migration work; adjust during implementation.

| Current path | Target canonical path | Notes |
|---|---|---|
| `.junie/guides/QUICK_REFERENCE.md` | `docs/QUICK_REFERENCE.md` | Update README + base prompt to reference docs |
| `.junie/guides/tech/*` | `docs/tech/*` | Canonical tech docs move to docs |
| `.junie/guides/patterns/*` | `docs/patterns/*` | Extended patterns move to docs |
| `.junie/guides/project/*` | `docs/project/*` | Product docs move to docs |
| `.junie/guides/prompts/*` | `docs/agent-prompts/*` | Convert to base+delta model; keep support docs |
| `.junie/guidelines.md` | `.junie/guidelines.md` (trim) | Keep file but make it link-heavy |
| `AGENTS.md` | `AGENTS.md` (shrink) | Route + sync + pointers |
| `.github/copilot-instructions.md` | `.github/copilot-instructions.md` (shrink) | Copilot-friendly subset + pointers |

## Implementation Order (recommended)

1. Add `docs/` structure and copy canonicals (Phase 1)
2. Add base prompt and DELTA prompts (Phase 2)
3. Update entrypoints to point at docs (Phase 3)
4. Flip links and convert `.junie/guides` into pointers (Phase 4)
5. Add validation workflow (Phase 5)

Rationale: keep changes additive until entrypoints are updated; only then deprecate `.junie/guides/**`.

## Acceptance Criteria (definition of done)

- [ ] `docs/` contains all canonicals (tech, patterns, project, prompts).
- [ ] `docs/agent-prompts/base_agent_prompt.md` exists and is referenced by every DELTA prompt.
- [ ] Agent routing table matches in:
  - [ ] `AGENTS.md`
  - [ ] `.github/copilot-instructions.md`
  - [ ] `.junie/guidelines.md`
  - [ ] `docs/agent-prompts/README.md`
- [ ] No broken internal links in agentic docs.
- [ ] `.junie/guides/**` is no longer canonical (pointer-only or removed).

- [ ] Platform-agnostic requirement is met:
  - [ ] No canonical doc under `docs/**` requires a specific agent runtime.
  - [ ] Tool-specific constraints live only in tool entrypoints (`.junie/**`, `.github/**`) and are phrased as
    additions (not contradictions).
  - [ ] Junie and Copilot remain prominent via their entrypoints pointing to the same canonicals.

## Suggested Commands for the Implementation Session

Documentation-only checks (no builds required):

```bash
# Find remaining references to old locations
rg "\.junie/guides" -n

# Find non-clickable references (backticked paths) inside prompts
rg "`[^`]+/[^`]+`" docs/agent-prompts -n
```

If you adopt tooling (optional):

```bash
# Example placeholder; pick a tool and document it during implementation
./scripts/check-doc-links.sh
```

## Open Questions / Decisions Needed

1. ✅ Decision (2025-12-20): follow Mandolin-Cocotte approach — keep `.junie/guides/**` during migration, convert it to pointers, and remove/cleanup only as the final step once the whole plan is implemented.
2. Do we want to add pre-commit / markdown formatting tools now, or keep it manual?
3. Should we split the repo into separate “mobile” and “server” agentic docs like mandolin-cocotte does, or keep one unified doc set?
4. Do we want an explicit, tool-agnostic “agent quickstart” entrypoint (e.g. `docs/AGENTS_QUICKSTART.md`) in addition
   to `AGENTS.md`, to make adoption easier for tools that don’t read `.junie/**` or `.github/**`?

---

## Audit / Decisions (Implementation Log)

### 2025-12-20 — Phase 0 audit

#### Inventory (current entrypoints)

- `AGENTS.md` (large, comprehensive; includes routing, patterns, testing, and repeated “sync maintenance” guidance)
- `.github/copilot-instructions.md` (Copilot entrypoint; duplicates many architecture and testing rules)
- `.junie/guidelines.md` (Junie entrypoint; duplicates many architecture and testing rules)
- `.junie/guides/**` (current canonicals):
  - `tech/` (conventions, DI, repository, presentation, navigation, testing strategy, iOS integration, etc.)
  - `patterns/` (extended pattern guides)
  - `project/` (PRD, user flows, onboarding, UI/UX)
  - `prompts/` (specialized agent prompts + index)
  - `QUICK_REFERENCE.md`
- `README.md` references quick refs under `.junie/guides/**` (e.g. Kotest smart casting, convention plugins guide)

#### Duplication hotspots (to eliminate via link-first canonicals)

The following topics appear (with overlapping or near-identical prose) across `AGENTS.md`, `.github/copilot-instructions.md`, and `.junie/guidelines.md`:

- Agent selector / specialized modes list
- Tech stack overview
- Feature module structure (split-by-layer api/data/presentation/ui/wiring)
- iOS export rules (`:shared` exports, “don’t export :data/:ui/:wiring”)
- Essential commands (primary Gradle validation, dependency updates, iOS build avoidance)
- Critical patterns (Koin Impl+Factory, Arrow Either, ViewModel rules, Navigation 3 rules, testing rules)
- Testing enforcement (Kotest/MockK/Turbine, androidUnitTest location rules, property tests)

#### Canonical target (decision)

- Canonicals will live in `docs/**` (tech/patterns/project/prompts). Tool entrypoints (`AGENTS.md`, `.github/copilot-instructions.md`, `.junie/guidelines.md`) will be trimmed to routing + must-follow rules + clickable links into `docs/**`.

#### `.junie/guides/**` deprecation strategy (decision)

- Keep `.junie/guides/**` during migration.
- After link flips, replace `.junie/guides/**` with pointer stubs to `docs/**`.
- Remove/cleanup `.junie/guides/**` as the last step once the entire migration plan is complete.
