---
name: docs-maintainer
description: "Maintain and update documentation with link-first strategy, validate links, and consolidate duplicate content. Use when: (1) Updating documentation or guides, (2) Checking or fixing broken links, (3) Consolidating duplicate documentation, (4) Updating Last Updated dates, (5) Synchronizing multi-entrypoint docs (AGENTS.md, llms.txt, docs/README.md)"
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
- Do NOT use for writing new feature documentation from scratch (use agent-specific modes)
- Do NOT use for PRD/product documentation (use Product Design Mode)
- Do NOT use for UI/UX design documentation (use UI/UX Design Mode)
- Do NOT use for architecture decisions (use conventions.md as canonical)

## Essential Workflows

### Workflow 1: Update Documentation with Link-First Strategy

To maintain documentation without duplicating content:

1. **Identify the canonical source**: Find the original location of the information (e.g., `conventions.md` for architecture rules)

2. **Use links instead of copying**: When updating documentation, reference the canonical source:
   ```markdown
   <!-- ❌ WRONG: Duplicate content -->
   The ViewModel pattern requires passing viewModelScope to the constructor...

   <!-- ✅ CORRECT: Link to canonical -->
   ViewModels must follow the [ViewModel Pattern](../../../docs/tech/critical_patterns_quick_ref.md#viewmodel-pattern):
   - Pass `viewModelScope` to constructor
   - Use `onStart()` for initialization (not `init`)
   ```

3. **Update Last Updated headers**: Change the date in the header when content changes:
   ```markdown
   **Last Updated:** December 22, 2025
   ```

4. **Synchronize multi-entrypoint documents**: When updating agentic system docs, update ALL entrypoints:
    - `AGENTS.md` (primary entrypoint)
    - `llms.txt` (AI discovery index)
    - `docs/README.md` (documentation index)

5. **Validate links after changes**: Run link validation:
   ```bash
   ./.claude/skills/docs-maintainer/scripts/validate-links.sh
   ```

6. **Commit with conventional commit format**:
   ```bash
   git commit -m "docs(conventions): add ViewModel pattern guidance"
   ```

### Workflow 2: Validate and Fix Broken Links

To ensure all documentation links are functional:

1. **Run link validation script**:
   ```bash
   ./.claude/skills/docs-maintainer/scripts/validate-links.sh
   ```

2. **Analyze failures**: Check each broken link:
   - File moved/deleted → Update or remove the link
   - Typo in path → Fix the link
   - External link → Verify manually or use archive links

3. **Fix broken links systematically**:
   ```markdown
   <!-- Before: Broken link -->
   See [old-pattern.md](old-pattern.md) for details

    <!-- After: Fixed link -->
    See [critical_patterns_quick_ref.md](../../../docs/tech/critical_patterns_quick_ref.md) for details
    ```

4. **Search for orphaned references**: Use grep to find files that reference removed content:
   ```bash
   rg "old-pattern.md" docs/ AGENTS.md
   ```

5. **Run validation again**: Ensure all fixes pass before committing

6. **Update cross-reference tables** in affected documents to maintain consistency

### Workflow 3: Consolidate Duplicate Content

To eliminate documentation duplication:

1. **Identify duplicate content**: Search for repeated patterns:
   ```bash
   rg "ViewModel pattern" docs/ --type md
   ```

2. **Choose canonical source**: Decide which document is the primary source (based on conventions.md hierarchy)

3. **Replace duplicates with links**:
   ```markdown
   <!-- Original in conventions.md -->
   ## ViewModel Pattern
   Pass viewModelScope to constructor, use onStart() for initialization...

   <!-- Duplicated file - replace with link -->
    ## ViewModel Pattern
    See [conventions.md](../../../docs/tech/conventions.md#presentation-layer) for complete ViewModel pattern guidance.
   ```

4. **Update cross-references**: Ensure all related documents link to the canonical source

5. **Validate no content lost**: Check that all essential information is preserved in the canonical source

6. **Run link validation** to ensure all new references work

7. **Document consolidation**: Add note to `docs/README.md` if major consolidation occurred

### Workflow 4: Skill Maintenance Guide

To maintain the 27 skills in `.claude/skills/` directory:

1. **Validate skill cross-references**: Ensure all skill links are functional
   ```bash
   # Check for broken skill links in all skills
   rg '@[a-z-]+' .claude/skills/ --type md | grep -v "SKILL.md" | while read line; do
     skill=$(echo $line | grep -oP '@[a-z-]+')
     if [ ! -f ".claude/skills/${skill#@}/SKILL.md" ]; then
       echo "Broken link: $line"
     fi
   done
   ```

2. **Check skill structure**: Verify all skills follow convexskills pattern
   - YAML frontmatter with name, description, version (optional), tags (optional)
   - "When to Use" section with triggers and exclusions
   - "Essential Workflows" section with numbered workflows
   - "Critical Guardrails" section with NEVER rules
   - "Quick Reference" section with commands and patterns
   - "Cross-References" section with documentation links

3. **Update skill metadata**: When skills change, update frontmatter description
   ```markdown
   ---
   name: skill-name
   description: This skill should be used when...
   ---
   ```

4. **Synchronize with documentation changes**: When docs move or change
   - Update skill cross-references to point to new locations
   - Update Related Skills sections in affected skills
   - Run link validation: `./.claude/skills/docs-maintainer/scripts/validate-links.sh`

5. **Validate skill inventory**: Ensure AGENTS.md lists all skills
   ```bash
   # List all skills
   find .claude/skills -name "SKILL.md" -type f | wc -l
   
   # Check AGENTS.md mentions all skills
   rg '@[a-z-]+' AGENTS.md | sort -u
   ```

6. **Update cross-references between skills**: When new skills added
   - Use pattern from Wave 7: Add relevant skills to Related Skills sections
   - Add skill links to Cross-References tables
   - Preserve existing cross-references (NEVER remove)
   - Use relative paths: `../skill-name/SKILL.md` format

**Skill Maintenance Commands:**

| Command | Purpose |
|---------|---------|
| `find .claude/skills -name "SKILL.md" -type f` | List all skill files |
| `rg '@[a-z-]+' .claude/skills/ --type md` | Find all skill cross-references |
| `rg 'name:' .claude/skills/*/SKILL.md` | List all skill names from frontmatter |
| `./.claude/skills/docs-maintainer/scripts/validate-links.sh` | Validate links in skills |
| `wc -l .claude/skills/*/SKILL.md` | Check skill file sizes (target: <300 lines) |

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
| `./.claude/skills/docs-maintainer/scripts/validate-links.sh` | Validate all markdown links |
| `rg "pattern" docs/ --type md` | Search documentation for content |
| `rg "broken-link.md" docs/ AGENTS.md` | Find all references to a file |
| `bash -n script.sh` | Validate script syntax |
| `wc -l file.md` | Count lines in document |
| `git diff docs/` | See documentation changes |

**Link-First Examples:**
```markdown
<!-- Architecture reference -->
See [conventions.md](../../../docs/tech/conventions.md) for architecture rules

<!-- Pattern reference -->
Follow the [ViewModel Pattern](../../../docs/tech/critical_patterns_quick_ref.md#viewmodel-pattern)

<!-- Skill reference -->
Switch to [Compose Screen skill](../compose-screen/SKILL.md)

<!-- Implementation reference -->
Reference: [PokemonListViewModel.kt](../../features/pokemonlist/presentation/...PokemonListViewModel.kt)
```

**Multi-Entrypoint Sync Checklist:**
- [ ] `AGENTS.md` updated (primary entrypoint)
- [ ] `llms.txt` updated (AI discovery index)
- [ ] `docs/README.md` updated (documentation index)
- [ ] All Last Updated dates synchronized
- [ ] Links validated with `validate-links.sh`
- [ ] Legacy path check: `rg "junie/guides|copilot-instructions|agent-prompts" -n` (should return no matches)

## Cross-References

| Document | Purpose | Link |
|----------|---------|------|
| [conventions.md](../../../docs/tech/conventions.md) | Master architecture and conventions reference |
| [AGENTS.md](../../../AGENTS.md) | Agent routing table and mode selection |
| [llms.txt](../../../llms.txt) | AI discovery index |
| [docs/README.md](../../../docs/README.md) | Documentation index for AI discovery |
| [QUICK_REFERENCE.md](../../../docs/QUICK_REFERENCE.md) | Essential commands and workflows |
| [critical_patterns_quick_ref.md](../../../docs/tech/critical_patterns_quick_ref.md) | 6 core patterns reference |
| [testing_strategy.md](../../../docs/tech/testing_strategy.md) | Testing strategy and coverage guidelines |
| [skill-judge](../../opencode-skills/skill-judge/SKILL.md) | Evaluate skill quality against specifications |
| [skill-creator](../../opencode-skills/skill-creator/SKILL.md) | Create new skills following best practices |

**Documentation Hierarchy:**
1. **Canonical sources** (`docs/tech/`, `docs/project/`) → Primary content
2. **Entry points** (`AGENTS.md`, `llms.txt`) → Routing and AI discovery
3. **Skills** (`.claude/skills/*/SKILL.md`) → Agent execution patterns

**When working with docs, always link up to the canonical source, never duplicate down.**
