# Skill Usage Guide

Last Updated: February 7, 2026

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
| **Core Architecture** | — | — |
| **@kmp-critical-patterns** | Quick reference for 6 core patterns (ViewModel, Either, Impl+Factory, Navigation, Testing, Convention Plugins) | `.claude/skills/kmp-critical-patterns/` |
| **@kmp-architecture** | Module structure, vertical slicing, feature organization, iOS export boundaries | `.claude/skills/kmp-architecture/` |
| **@kmp-domain** | Domain models, use cases, repository interfaces, domain exceptions | `.claude/skills/kmp-domain/` |
| **@kmp-di** | Koin DI configuration, dependency wiring, parametric injection | `.claude/skills/kmp-di/` |
| **@kmp-gradle** | Convention plugins, build configuration, Gradle customization | `.claude/skills/kmp-gradle/` |
| **Layer Implementation** | — | — |
| **@kmp-presentation** | ViewModels, lifecycle awareness, SavedStateHandle, UI state management | `.claude/skills/kmp-presentation/` |
| **@kmp-data-layer** | Repository patterns, Either<RepoError,T>, error handling, DTO mapping | `.claude/skills/kmp-data-layer/` |
| **@kmp-api-services** | Ktor API services, HTTP clients, API configuration | `.claude/skills/kmp-api-services/` |
| **@kmp-testing-patterns** | Kotest, MockK, Turbine, property-based testing, test implementation | `.claude/skills/kmp-testing-patterns/` |
| **Platform & Design** | — | — |
| **@kmp-ios** | SwiftUI + KMP ViewModels, lifecycle bridging, framework exports, Direct Integration | `.claude/skills/kmp-ios/` |
| **@kmp-navigation** | Navigation 3 modular architecture, type-safe routes, scoped navigation | `.claude/skills/kmp-navigation/` |
| **@kmp-design-systems** | Design tokens, components, icon strategy, Material 3 and Unstyled theming | `.claude/skills/kmp-design-systems/` |
| **Specialized** | — | — |
| **@kmp-compose-unstyled** | Compose Unstyled reference, headless components, platform-native theming | `.claude/skills/kmp-compose-unstyled/` |
| **@kmp-desktop** | Desktop/JVM ViewModels, SavedStateHandle, Koin integration | `.claude/skills/kmp-desktop/` |
| **@kmp-testing-strategy** | Testing philosophy, coverage analysis, Kotest and MockK patterns | `.claude/skills/kmp-testing-strategy/` |
| **@kmp-commands** | Build commands, test execution, validation, pre-commit checks | `.claude/skills/kmp-commands/` |
| **Development** | — | — |
| **@kmp-developer** | General KMP development, feature implementation, bug fixes, refactoring | `.claude/skills/kmp-developer/` |
| **@kmp-mobile-expert** | ViewModels with lifecycle awareness, repositories, iOS integration | `.claude/skills/kmp-mobile-expert/` |
| **@compose-screen** | Compose UI screens for Android and Desktop, Material + Unstyled dual-theme | `.claude/skills/compose-screen/` |
| **@swiftui-screen** | Native iOS UI with SwiftUI, StateFlow bridging, iOS lifecycle management | `.claude/skills/swiftui-screen/` |
| **@ktor-backend** | Ktor server endpoints, BFF APIs, REST service implementation | `.claude/skills/ktor-backend/` |
| **Design & Planning** | — | — |
| **@product-designer** | Product requirements, acceptance criteria, MVP planning | `.claude/skills/product-designer/` |
| **@ui-ux-designer** | Visual design, animations, interaction patterns, design systems | `.claude/skills/ui-ux-designer/` |
| **@onboarding** | First-run experience, user onboarding flows, progressive disclosure | `.claude/skills/onboarding/` |
| **@user-flows** | User journeys, navigation contracts, UX flow mapping | `.claude/skills/user-flows/` |
| **Quality** | — | — |
| **@docs-maintainer** | Documentation updates, link validation, content consolidation | `.claude/skills/docs-maintainer/` |

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
