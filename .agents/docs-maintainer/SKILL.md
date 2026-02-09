---
name: docs-maintainer
description: "Maintain documentation, validate links, consolidate duplicates, and audit skill quality. Use when: (1) Updating docs or skills, (2) Checking/fixing broken links, (3) Consolidating duplicate content, (4) Auditing skill quality against standards, (5) Synchronizing multi-entrypoint docs (AGENTS.md, llms.txt). Keywords: documentation, links, duplicate content, skill quality, skill audit"
---

## When to Use

**Triggers:**
- "update documentation", "update docs", "update guide"
- "check links", "verify links", "validate documentation"
- "consolidate documentation", "merge docs", "consolidate info"
- "documentation consistency", "doc sync", "keep docs aligned"
- "Last Updated date", "update doc date", "refresh docs"
- "orphaned docs", "dead links", "broken references"
- "cross-reference maintenance", "link verification", "doc cleanup"

**Exclusions:**
- New feature docs from scratch (use feature-specific skills)
- PRD/product/UI/UX docs (use @product-designer or @ui-ux-designer)
- Architecture decisions (use @kmp-architecture skill)

## Essential Workflows

### Workflow 1: Update Documentation with Link-First Strategy

To maintain documentation without duplicating content:

1. **Identify canonical source**: Find the original location (e.g., skill SKILL.md for patterns)
2. **Link, don't copy**: Reference canonical sources instead of duplicating content
   - ✅ `See [@kmp-critical-patterns](../kmp-critical-patterns/SKILL.md#viewmodel-pattern)`
   - ❌ Copying pattern details into multiple docs
3. **Update Last Updated**: Change date header when content changes
4. **Sync entrypoints**: Update `AGENTS.md` + `llms.txt` together when structure changes
5. **Validate links**: Run `./.agents/docs-maintainer/scripts/validate-links.sh`
6. **Commit**: Use conventional format: `docs(scope): description`

### Workflow 2: Validate and Fix Broken Links

1. **Run validation**: `./.agents/docs-maintainer/scripts/validate-links.sh`
2. **Analyze failures**: File moved → update link, typo → fix, external → verify
3. **Fix systematically**: Update broken paths, use @skill-name syntax for skills
4. **Find orphans**: `rg "removed-file.md" docs/ AGENTS.md` to find all references
5. **Re-validate**: Run script again, update cross-reference tables if needed

### Workflow 3: Consolidate Duplicate Content

1. **Find duplicates**: `rg "pattern text" docs/ .agents/ --type md`
2. **Choose canonical**: Skills for patterns, `docs/project/` for artifacts
3. **Replace with links**: Convert duplicate content to links to canonical source
4. **Validate**: Check no content lost, run link validation, update `AGENTS.md` if major change

### Workflow 4: Skill Maintenance Guide

Maintaining the 26 skills in `.agents/`:

1. **Structure**: All skills follow pattern: frontmatter + When to Use + Workflows + Guardrails + Quick Reference + Cross-References
2. **Cross-references**: Use `../skill-name/SKILL.md` or `@skill-name` syntax, preserve existing links
3. **Sync with docs**: When docs move, update skill cross-refs and run link validation
4. **Inventory**: Ensure AGENTS.md lists all skills: `find .agents -name "SKILL.md" -type f | wc -l`
5. **Quality**: Target <500 lines for SKILL.md (Grade A standard), use references/ for deep content

**Commands:**
- List skills: `find .agents -name "SKILL.md" -type f`
- Find cross-refs: `rg '@[a-z-]+' .agents/ --type md`
- Validate links: `./.agents/docs-maintainer/scripts/validate-links.sh`
- Check sizes: `wc -l .agents/*/SKILL.md`

## Documentation Architecture

**Documentation Architecture:**
- **Skills (`.agents/`)**: 26 agent-executable pattern libraries with SKILL.md + references/
- **Docs (`docs/`)**: 5 files total:
  - `docs/project/prd.md` - Product requirements (canonical)
  - `docs/project/user_flow.md` - User journeys
  - `docs/project/ui_ux.md` - UI/UX guidelines  
  - `docs/project/onboarding.md` - Onboarding flows
  - `docs/pokeapi-openapi.yml` - API specification
- **Entry Points**: `AGENTS.md` (primary), `llms.txt` (AI discovery), `README.md` (human entry)

**Linking Rules:**
- **Docs → Skills**: Encouraged. Project artifacts reference skills for implementation patterns.
- **Skills → Docs**: Minimal. Skills reference `docs/project/` only when context-specific (e.g., PRD, user flows).
- **Skills ↔ Skills**: Always use relative paths: `../skill-name/SKILL.md` or `@skill-name` syntax.

## Drift Prevention

To prevent documentation from diverging or duplicating:

1. **Canonical Markers**: Project artifacts in `docs/project/` include YAML frontmatter (`Canonical: true`, `Type: Artifact`) to signal they are the primary source.
2. **Skill Self-Containment**: Each skill in `.agents/` is self-contained for its domain. Technical patterns live in skills, not docs.
3. **Entry Point Consistency**: `AGENTS.md`, `llms.txt`, and `README.md` must stay synchronized when skills or documentation structure changes.

**Monitoring Workflow:**
- New technical patterns → Add to relevant skill in `.agents/`, not `docs/`
- Descriptive artifacts containing implementation patterns → Migrate patterns to skills
- Link validation: Run `./.agents/docs-maintainer/scripts/validate-links.sh` before commits

## Skill Quality Auditing

When auditing skills for quality, evaluate against these criteria:

**Key Quality Indicators:**

1. **Description Field (CRITICAL)** - Determines if skill gets used
   - Answers: WHAT does this skill do? WHEN to use it? 
   - Includes: Keywords for LLM discovery
   - Format: `"<What>. Use when: (1) <scenario>, (2) <scenario>. Keywords: <comma-separated>"`

2. **Progressive Disclosure** - Information hierarchy
    - Metadata (frontmatter) → SKILL.md body (<500 lines) → references/ (unlimited)
    - Quick answers in SKILL.md, deep dives in references/
    - Target: <500 lines for SKILL.md (<500 lines target)

3. **Knowledge Delta** - Expert knowledge only
   - What Claude doesn't already know
   - Domain-specific patterns, project conventions, anti-patterns
   - NOT: Generic explanations, language basics, framework fundamentals

4. **Anti-Patterns** - Specific NEVER rules with reasoning
   - Concrete examples of what NOT to do
   - Explanation of consequences
   - Pattern: `NEVER <action> → <consequence>`

**Quick Audit Commands:**
```bash
# Check description format (WHAT/WHEN/KEYWORDS)
rg "^description:" .agents/*/SKILL.md

# Check file sizes (<500 lines target)
wc -l .agents/*/SKILL.md

# Find skills exceeding target
find .agents -name "SKILL.md" -exec wc -l {} + | awk '$1 > 500'

# Validate links in skills
./.agents/docs-maintainer/scripts/validate-links.sh
```

## Critical Guardrails

| Rule | Consequence |
|------|-------------|
| NEVER duplicate content across docs → Use links to canonical sources | Causes drift, maintenance burden |
| ALWAYS update Last Updated when content changes | Dates signal relevance to readers |
| ALWAYS verify links on documentation PRs | Broken links frustrate users |
| NEVER create orphaned documentation → All docs must be referenced | Unreferenced docs become stale |
| ALWAYS sync multi-entrypoint docs together | Inconsistency breaks routing |
| NEVER add content without checking for canonical source first | Duplication creates debt |
| ALWAYS use relative paths for internal links | Absolute paths break on move |
| NEVER remove content without checking references | Causes broken links across docs |
| ALWAYS use markdown hyperlinks `[text](path)` NOT code spans `` `path` `` | Code spans are not clickable or validated |

## Quick Reference

| Command | Purpose |
|---------|---------|
| `./.agents/docs-maintainer/scripts/validate-links.sh` | Validate all markdown links |
| `rg "pattern" docs/ --type md` | Search documentation for content |
| `rg "broken-link.md" docs/ AGENTS.md` | Find all references to a file |
| `bash -n script.sh` | Validate script syntax |
| `wc -l file.md` | Count lines in document |
| `git diff docs/` | See documentation changes |

**Link-First Examples:**
```markdown
<!-- Architecture reference -->
See [@kmp-architecture](../kmp-architecture/SKILL.md) for architecture rules

<!-- Pattern reference -->
Follow the [ViewModel Pattern](See @kmp-critical-patterns skill#viewmodel-pattern)

<!-- Skill reference -->
Switch to [Compose Screen skill](../compose-screen/SKILL.md)

<!-- Implementation reference -->
Reference: [PokemonListViewModel.kt](../../features/pokemonlist/presentation/...PokemonListViewModel.kt)
```

**Multi-Entrypoint Sync Checklist:**
- [ ] `AGENTS.md` updated (primary entrypoint)
- [ ] `llms.txt` updated (AI discovery index)
- [ ] All Last Updated dates synchronized
- [ ] Links validated with `validate-links.sh`
- [ ] Legacy path check: `rg "junie/guides|copilot-instructions|agent-prompts" -n` (should return no matches)

## Cross-References

| Document | Purpose | Link |
|----------|---------|------|
| [@kmp-architecture](../kmp-architecture/SKILL.md) | Master architecture and conventions reference |
| [AGENTS.md](../../AGENTS.md) | Agent routing table (26 skills, decision trees) |
| [llms.txt](../../llms.txt) | AI discovery index |
| [@kmp-critical-patterns](../kmp-critical-patterns/SKILL.md) | 6 core patterns quick reference |
| [@kmp-testing-strategy](../kmp-testing-strategy/SKILL.md) | Testing strategy and coverage guidelines |

**Documentation Hierarchy:**
- **Entry Points**: `AGENTS.md` (primary routing), `llms.txt` (AI discovery), `README.md` (human entry)
- **Skills (26)**: `.agents/*/SKILL.md` + `references/` - Agent-executable patterns
- **Project Artifacts (5)**: `docs/project/*.md` + `docs/pokeapi-openapi.yml` - Canonical product/design docs

**Linking Strategy:** Always link to canonical sources. NEVER duplicate content across boundaries.
