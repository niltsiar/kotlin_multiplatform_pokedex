# AGENTS.md

**Last Updated:** February 7, 2026

> **📌 Important:** Agent Skills in `.agents/` are the **canonical technical reference**. The `docs/` directory contains supplementary guides, project documentation, and quick references. When in doubt, load the skill.

## Quick Start

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
| Validate docs | `.agents/docs-maintainer/scripts/validate-links.sh` |

## Documentation

- Full skill guide: [docs/SKILL_USAGE.md](docs/SKILL_USAGE.md)
- Doc index: [docs/README.md](docs/README.md)
- AI discovery: [llms.txt](llms.txt)
- Architecture: [See @kmp-architecture skill](See @kmp-architecture skill) (or load @kmp-architecture)

## Validation

Before committing, always run:
```bash
./gradlew :composeApp:assembleDebug test --continue
```
