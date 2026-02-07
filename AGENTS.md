# AGENTS.md

**Last Updated:** February 7, 2026

> **📌 Important:** Agent Skills in `.agents/` are the **canonical technical reference**. The `docs/` directory contains supplementary guides, project documentation, and quick references. When in doubt, load the skill.

## Quick Start

### For OpenCode Users

Skills are located in `.agents/` and auto-load when you use the `@mention` syntax.

```bash
# Use a skill
@kmp-developer help me implement a new feature

# Or load it explicitly
skill("kmp-developer")
```

### Build & Test

```bash
./gradlew :composeApp:assembleDebug test --continue
```

## Decision Tree for Skill Selection

**What are you building?**

### Feature Implementation
```
Implementing a new feature?
├── Architecture guidance needed? → @kmp-architecture
├── Creating ViewModel? → @kmp-presentation
├── Setting up Repository? → @kmp-data-layer
├── API service configuration? → @kmp-api-services
├── Domain model design? → @kmp-domain
├── DI configuration? → @kmp-di
├── Building UI?
│   ├── Compose (Material) → @compose-screen
│   ├── Compose (Unstyled) → @compose-screen + @kmp-compose-unstyled
│   └── SwiftUI → @swiftui-screen
├── Adding navigation? → @kmp-navigation
└── Testing strategy? → @kmp-testing-strategy
```

### Platform-Specific Work
```
Working on platform-specific code?
├── iOS integration (SwiftUI + KMP) → @kmp-ios
├── Desktop-specific (JVM) → @kmp-desktop
├── Navigation implementation → @kmp-navigation
└── Gradle/Build configuration → @kmp-gradle
```

### Testing
```
Writing or fixing tests?
├── Test strategy & philosophy → @kmp-testing-strategy
├── Test implementation patterns → @kmp-testing-patterns
├── Property-based testing (Kotest) → @kmp-testing-patterns
└── Test failures & debugging → @kmp-testing-strategy + docs/TROUBLESHOOTING.md
```

### Design Systems
```
Working on UI/Design?
├── Design tokens & components → @kmp-design-systems
├── Compose Unstyled patterns → @kmp-compose-unstyled
├── Visual design & animations → @ui-ux-designer
├── Icon strategy → @kmp-design-systems
└── Critical patterns quick ref → @kmp-critical-patterns
```

### Product & Planning
```
Product planning?
├── Writing PRD → @product-designer
├── User flows & journeys → @user-flows
├── Onboarding flows → @onboarding
└── UI/UX guidelines → @ui-ux-designer
```

### Backend
```
Backend development?
├── Ktor API endpoints → @ktor-backend
├── API service patterns → @kmp-api-services
└── Repository patterns → @kmp-data-layer
```

### Build & Workflow
```
Build issues or workflow questions?
├── Build commands & validation → @kmp-commands
├── Gradle configuration → @kmp-gradle
├── Test execution → @kmp-commands
└── Pre-commit checks → @kmp-commands
```

## Skills (27 Total)

Located in `.agents/`

### Core Architecture
- **@kmp-critical-patterns** - 6 core patterns (quick reference)
- **@kmp-architecture** - Module structure, vertical slicing
- **@kmp-domain** - Domain models, use cases
- **@kmp-di** - Koin patterns, DI wiring
- **@kmp-gradle** - Convention plugins, build configuration

### Layer Implementation
- **@kmp-presentation** - ViewModels, lifecycle, SavedStateHandle
- **@kmp-data-layer** - Repository patterns, Either<RepoError,T>
- **@kmp-api-services** - Ktor, API services, DTOs
- **@kmp-testing-patterns** - Kotest, MockK, property testing

### Platform & Design
- **@kmp-ios** - SwiftUI + KMP integration
- **@kmp-navigation** - Navigation 3, scoped routes
- **@kmp-design-systems** - Design tokens, components, icons

### Specialized
- **@kmp-compose-unstyled** - Headless components
- **@kmp-desktop** - JVM-specific patterns
- **@kmp-testing-strategy** - Testing philosophy (renamed from @testing-strategy)
- **@kmp-commands** - Build, test, validation commands

### Development
- **@kmp-developer** - General development
- **@kmp-mobile-expert** - ViewModels, repositories, iOS
- **@compose-screen** - Compose UI
- **@swiftui-screen** - SwiftUI iOS
- **@ktor-backend** - Ktor server

### Design & Planning
- **@product-designer** - PRD, acceptance criteria
- **@ui-ux-designer** - Visual design
- **@onboarding** - Onboarding flows
- **@user-flows** - Journey mapping

### Quality
- **@docs-maintainer** - Documentation

## Skill Selection Guide (Quick Reference)

| I want to... | Use Skill |
|--------------|-----------|
| Implement a new feature | @kmp-developer |
| Create a ViewModel | @kmp-presentation |
| Build a Compose screen | @compose-screen |
| Build a SwiftUI screen | @swiftui-screen |
| Create API endpoints | @ktor-backend |
| Write product requirements | @product-designer |
| Design UI/animations | @ui-ux-designer |
| Plan tests | @kmp-testing-strategy |
| Update documentation | @docs-maintainer |

## Skill Selection & Usage

### Skill Architecture

Each skill in `.agents/` contains:
- **SKILL.md**: Main instructions with YAML frontmatter
- **When to Use**: Trigger conditions and decision points
- **Workflows**: Step-by-step guidance for specific tasks
- **Anti-Patterns**: What NOT to do and why
- **Cross-References**: Links to canonical project documentation

### Usage Examples

#### Example 1: Implement a ViewModel
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

#### Example 2: Build a Compose Screen
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

## Migration & Troubleshooting

### Migration from Agent Prompts
- **Old Way**: Load agent prompt from `docs/agent-prompts/`
- **New Way**: Use skill from `.agents/`
- **Benefits**: Skills are more focused, include mode detection, and link directly to reference implementations.

### Troubleshooting
- **Skill not loading?** Check skill is in `.agents/<name>/SKILL.md` and verify YAML frontmatter.
- **Token budget exceeded?** Use `@kmp-critical-patterns` for quick reference; load docs incrementally.
- **Need a new skill?** Copy structure from existing skill, define trigger conditions, and include anti-patterns.

## Critical Patterns

- **ViewModel**: Pass scope to constructor, NO work in init
- **Repository**: Return `Either<RepoError, T>`
- **Compose**: Always add @Preview
- **iOS**: Only export `:api` and `:presentation`

## Essential Commands

See [docs/QUICK_REFERENCE.md](docs/QUICK_REFERENCE.md) for complete command reference or load **@kmp-commands** skill.

| Task | Command |
|------|---------|
| Build | `./gradlew :composeApp:assembleDebug` |
| Test | `./gradlew test --continue` |
| Check deps | `./gradlew dependencyUpdates` |
| Check tokens | `./scripts/check-tokens` |
| Validate docs | `.agents/docs-maintainer/scripts/validate-links.sh` |

## Documentation

> **Note:** Most technical documentation has been consolidated into Agent Skills (`.agents/`). Skills are the canonical reference. Use `@skill-name` syntax to load them.

### Start Here (< 1K tokens)
- [QUICK_REFERENCE.md](docs/QUICK_REFERENCE.md) - Essential commands and workflows
- [SKILL_QUICK_REFERENCE.md](docs/SKILL_QUICK_REFERENCE.md) - Quick skill reference card

### Remaining Technical Guides

#### Quick References
- [tech/koin_di_quick_ref.md](docs/tech/koin_di_quick_ref.md) - Koin DI quick reference (see @kmp-di for full guide)
- [tech/kotest_smart_casting_quick_ref.md](docs/tech/kotest_smart_casting_quick_ref.md) - Kotest smart casting patterns
- [tech/testing_quick_ref.md](docs/tech/testing_quick_ref.md) - Testing quick reference (see @kmp-testing-strategy for full guide)

#### Architecture & Platform-Specific
- [tech/ios_apps_architecture.md](docs/tech/ios_apps_architecture.md) - iOS app comparison (native vs Compose)
- [tech/utility_organization.md](docs/tech/utility_organization.md) - Utility module organization
- [tech/predictive_back_notes.md](docs/tech/predictive_back_notes.md) - Predictive back gesture notes
- [tech/coroutines.md](docs/tech/coroutines.md) - Coroutines patterns

#### Pattern References
- [patterns/error_handling_patterns.md](docs/patterns/error_handling_patterns.md) - Error handling patterns (see @kmp-data-layer for full guide)
- [patterns/navigation_patterns.md](docs/patterns/navigation_patterns.md) - Navigation patterns (see @kmp-navigation for full guide)

### Project Documentation
- [project/prd.md](docs/project/prd.md) - Product requirements and acceptance criteria
- [project/user_flow.md](docs/project/user_flow.md) - User journeys and flows
- [project/ui_ux.md](docs/project/ui_ux.md) - UI/UX guidelines
- [project/onboarding.md](docs/project/onboarding.md) - Onboarding flow documentation

### Skill-to-Documentation Mapping

For comprehensive technical guidance, load Agent Skills:

| Topic | Agent Skill | Command |
|-------|-------------|---------|
| Architecture & Modules | @kmp-architecture | Load for module structure, vertical slicing |
| Critical Patterns (6 core) | @kmp-critical-patterns | Load for quick pattern reference |
| Testing Strategy | @kmp-testing-strategy | Load for testing philosophy |
| Testing Patterns | @kmp-testing-patterns | Load for Kotest, MockK, Turbine |
| Dependency Injection | @kmp-di | Load for Koin patterns |
| Navigation | @kmp-navigation | Load for Navigation 3 patterns |
| iOS Integration | @kmp-ios | Load for SwiftUI + KMP |
| Desktop Patterns | @kmp-desktop | Load for JVM-specific patterns |
| ViewModels | @kmp-presentation | Load for ViewModel patterns |
| Repositories | @kmp-data-layer | Load for repository patterns |
| API Services | @kmp-api-services | Load for Ktor API patterns |
| Domain Layer | @kmp-domain | Load for domain models |
| Design Systems | @kmp-design-systems | Load for design tokens, icons |
| Compose Unstyled | @kmp-compose-unstyled | Load for headless components |
| Gradle/Build | @kmp-gradle | Load for convention plugins |

### Token Budget Guide

When working with AI agents, manage context efficiently:

| Task Complexity | Recommended Approach | Token Range |
|----------------|----------------------|-------------|
| Quick lookup | Load @kmp-critical-patterns skill | ~800-1500 |
| Feature work | Load relevant skill (@kmp-architecture, @kmp-developer) | ~2000-3000 |
| iOS work | Load @kmp-ios skill | ~2500-3500 |
| Full context | Load multiple skills + quick references | ~5000+ |

**Rule**: Agent Skills are canonical. Load skills first, docs for supplementary context.

### Metadata
- Full skill guide: [docs/SKILL_USAGE.md.migrated](docs/SKILL_USAGE.md.migrated)
- Doc index: [docs/README.md.migrated](docs/README.md.migrated)
- AI discovery: [llms.txt](llms.txt)
- Architecture: [See @kmp-architecture skill](See @kmp-architecture skill) (or load @kmp-architecture)

## Validation

Before committing, always run:
```bash
./gradlew :composeApp:assembleDebug test --continue
```
