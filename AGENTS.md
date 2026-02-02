# AGENTS.md

**Last Updated:** February 2, 2026

## Quick Start

```bash
./gradlew :composeApp:assembleDebug test --continue
```

## Skills (11 Total)

Located in `.claude/skills/`

### Development
- **kmp-developer** - General development (@kmp-developer)
- **kmp-mobile-expert** - ViewModels, repositories (@kmp-mobile-expert)
- **compose-screen** - Compose UI (@compose-screen)
- **swiftui-screen** - SwiftUI iOS (@swiftui-screen)
- **ktor-backend** - Ktor server (@ktor-backend)

### Design & Planning
- **product-designer** - PRD, acceptance criteria (@product-designer)
- **ui-ux-designer** - Visual design (@ui-ux-designer)
- **onboarding** - Onboarding flows (@onboarding)
- **user-flows** - Journey mapping (@user-flows)

### Quality
- **testing-strategy** - Test planning (@testing-strategy)
- **docs-maintainer** - Documentation (@docs-maintainer)

## Skill Selection Guide

| I want to... | Use Skill |
|--------------|-----------|
| Implement a new feature | @kmp-developer |
| Create a ViewModel | @kmp-mobile-expert |
| Build a Compose screen | @compose-screen |
| Build a SwiftUI screen | @swiftui-screen |
| Create API endpoints | @ktor-backend |
| Write product requirements | @product-designer |
| Design UI/animations | @ui-ux-designer |
| Plan tests | @testing-strategy |
| Update documentation | @docs-maintainer |

## Critical Patterns

- **ViewModel**: Pass scope to constructor, NO work in init
- **Repository**: Return `Either<RepoError, T>`
- **Compose**: Always add @Preview
- **iOS**: Only export `:api` and `:presentation`

## Essential Commands

| Task | Command |
|------|---------|
| Build | `./gradlew :composeApp:assembleDebug` |
| Test | `./gradlew test --continue` |
| Check deps | `./gradlew dependencyUpdates` |
| Validate docs | `./scripts/validate-docs.sh` |

## Documentation

- Full skill guide: [docs/SKILL_USAGE.md](docs/SKILL_USAGE.md)
- Doc index: [docs/README.md](docs/README.md)
- AI discovery: [llms.txt](llms.txt)
- Architecture: [docs/tech/conventions.md](docs/tech/conventions.md)

## Validation

Before committing, always run:
```bash
./gradlew :composeApp:assembleDebug test --continue
```
