# Skill Usage Guide

Last Updated: February 2, 2026

## Quick Start

### For OpenCode Users

Skills are located in `.claude/skills/` and auto-load when you use the `@mention` syntax.

```bash
# Use a skill
@kmp-developer help me implement a new feature

# Or load it explicitly
skill("kmp-developer")
```

### Available Skills

| Skill | Use When | Location |
|-------|----------|----------|
| **kmp-developer** | General development, features, bugs | `.claude/skills/kmp-developer/` |
| **kmp-mobile-expert** | ViewModels, repositories, iOS | `.claude/skills/kmp-mobile-expert/` |
| **compose-screen** | Compose UI screens | `.claude/skills/compose-screen/` |

## Skill Structure

Each skill contains:
- **SKILL.md** - Main instructions with YAML frontmatter
- **When to Use** - Trigger conditions
- **Workflows** - Step-by-step guidance
- **Anti-Patterns** - What NOT to do
- **Cross-References** - Links to canonical docs

## Examples

### Example 1: Implement a ViewModel

```
User: "I need to create a ViewModel for Pokemon details"

AI: [Loads kmp-mobile-expert skill]

Skill guides through:
1. Mode detection (VM_MODE)
2. Lifecycle-aware pattern
3. State persistence with SavedStateHandle
4. Koin DI wiring
5. Validation
```

### Example 2: Build a Compose Screen

```
User: "Implement the Pokemon detail screen"

AI: [Loads compose-screen skill]

Skill guides through:
1. Mode detection (FROM_SPEC or DESIGN_FIRST)
2. @Preview requirements
3. Dual-theme support (Material + Unstyled)
4. Token-based styling
5. Validation with ./gradlew :composeApp:assembleDebug
```

## Validation

Always validate your work:

```bash
# Check all documentation
./scripts/validate-docs.sh

# Check token budgets
python3 scripts/check-tokens.py

# Full validation
./gradlew :composeApp:assembleDebug test --continue
```

## Migration from Agent Prompts

**Old way**: Load agent prompt from `docs/agent-prompts/`
**New way**: Use skill from `.claude/skills/`

Skills are more focused, task-specific, and include:
- Mode detection for different scenarios
- Concrete code examples
- Anti-patterns with explanations
- Direct links to reference implementations

## Troubleshooting

### Skill not loading?
- Check skill is in `.claude/skills/<name>/SKILL.md`
- Verify YAML frontmatter has `name` and `description`
- Ensure description is specific (includes trigger keywords)

### Token budget exceeded?
- Use compact guides: `docs/tech/critical_patterns_compact.md`
- Load docs incrementally, not all at once
- Reference by link, don't paste content

### Need a new skill?
Follow the pilot pattern:
1. Copy structure from existing skill
2. Define clear trigger conditions
3. Include mode detection table
4. Add anti-patterns section
5. Test on real tasks
6. Document in this guide

## References

- [AGENTS.md](../AGENTS.md) - Agent routing
- [README.md](README.md) - Documentation index
- `.claude/skills/` - All skills (11 skill directories)
- [llms.txt](../llms.txt) - AI discovery index
