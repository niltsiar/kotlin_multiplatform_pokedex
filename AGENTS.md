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

- **Feature**: Architecture (@kmp-architecture), ViewModel (@kmp-presentation), Repository (@kmp-data-layer), API (@kmp-api-services), Domain (@kmp-domain), DI (@kmp-di), Navigation (@kmp-navigation), Testing (@kmp-testing-strategy)
- **UI**: Compose (@compose-screen), Unstyled (@kmp-compose-unstyled), SwiftUI (@swiftui-screen), Design Tokens (@kmp-design-systems), Icons (@kmp-design-systems)
- **Platforms**: iOS Integration (@kmp-ios), Desktop/JVM (@kmp-desktop), Ktor Backend (@ktor-backend)
- **Quality**: Build/Commands (@kmp-commands), Testing Patterns (@kmp-testing-patterns), Documentation (@docs-maintainer)
- **Planning**: PRD (@product-designer), UI/UX (@ui-ux-designer), Onboarding (@onboarding), User Flows (@user-flows)

## Skills (26 Total)

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
- **@kmp-compose-unstyled** - Headless components
- **@kmp-desktop** - JVM-specific patterns

### Specialized
- **@kmp-testing-strategy** - Testing philosophy & coverage
- **@kmp-commands** - Build, test, validation commands
- **@kmp-developer** - General development & features
- **@kmp-mobile-expert** - ViewModels, repositories, iOS

### Development
- **@compose-screen** - Compose UI screens (Material + Unstyled)
- **@swiftui-screen** - SwiftUI iOS screens
- **@ktor-backend** - Ktor server endpoints

### Design & Planning
- **@product-designer** - PRD, acceptance criteria
- **@ui-ux-designer** - Visual design, animations
- **@onboarding** - Onboarding flows
- **@user-flows** - Journey mapping

### Quality
- **@docs-maintainer** - Documentation maintenance

## Skill Selection Guide (Quick Reference)

| Task | Skill | Command |
|------|-------|---------|
| **Quick pattern reference** | @kmp-critical-patterns | `@kmp-critical-patterns show me the patterns` |
| **Architecture guidance** | @kmp-architecture | `@kmp-architecture module structure for...` |
| **Create ViewModel** | @kmp-presentation | `@kmp-presentation create ViewModel for...` |
| **Build repository** | @kmp-data-layer | `@kmp-data-layer implement repository...` |
| **API service setup** | @kmp-api-services | `@kmp-api-services create API service...` |
| **Domain model design** | @kmp-domain | `@kmp-domain design domain model...` |
| **DI configuration** | @kmp-di | `@kmp-di configure Koin for...` |
| **Gradle build config** | @kmp-gradle | `@kmp-gradle configure convention plugins` |
| **iOS integration** | @kmp-ios | `@kmp-ios integrate with SwiftUI...` |
| **Navigation setup** | @kmp-navigation | `@kmp-navigation add navigation for...` |
| **Design system** | @kmp-design-systems | `@kmp-design-systems create tokens...` |
| **Unstyled components** | @kmp-compose-unstyled | `@kmp-compose-unstyled create component...` |
| **Desktop patterns** | @kmp-desktop | `@kmp-desktop configure SavedStateHandle` |
| **Testing strategy** | @kmp-testing-strategy | `@kmp-testing-strategy plan tests` |
| **Build commands** | @kmp-commands | `@kmp-commands show build commands` |
| **General development** | @kmp-developer | `@kmp-developer help me implement...` |
| **Mobile expert** | @kmp-mobile-expert | `@kmp-mobile-expert implement ViewModel for iOS` |
| **Compose screen** | @compose-screen | `@compose-screen implement Pokemon detail screen` |
| **SwiftUI screen** | @swiftui-screen | `@swiftui-screen implement iOS screen...` |
| **Ktor backend** | @ktor-backend | `@ktor-backend create endpoint...` |
| **Testing patterns** | @kmp-testing-patterns | `@kmp-testing-patterns write Kotest tests` |
| **Product design** | @product-designer | `@product-designer write feature PRD` |
| **UI/UX design** | @ui-ux-designer | `@ui-ux-designer design interaction flow` |
| **Onboarding flows** | @onboarding | `@onboarding design first-run experience` |
| **User flows** | @user-flows | `@user-flows map user journey for...` |
| **Documentation** | @docs-maintainer | `@docs-maintainer update documentation` |

## Critical Patterns

- **ViewModel**: Pass scope to constructor, NO work in init
- **Repository**: Return `Either<RepoError, T>`
- **Compose**: Always add @Preview
- **iOS**: Only export `:api` and `:presentation`

## Essential Commands

See @kmp-commands skill for complete command reference.

| Task | Command |
|------|---------|
| Build | `./gradlew :composeApp:assembleDebug` |
| Test | `./gradlew test --continue` |
| Check deps | `./gradlew dependencyUpdates` |
| Validate docs | `.agents/docs-maintainer/scripts/validate-links.sh` |

## Documentation

> **Note:** Skills are the canonical reference. Use `@skill-name` syntax to load them.

### Start Here
- @kmp-commands - Essential commands and validation
- @kmp-critical-patterns - 6 core patterns quick reference

### Project Documentation
- [project/prd.md](docs/project/prd.md) - Product requirements
- [project/user_flow.md](docs/project/user_flow.md) - User journeys
- [project/ui_ux.md](docs/project/ui_ux.md) - UI/UX guidelines
- [project/onboarding.md](docs/project/onboarding.md) - First-run experience

### Token Budget Guide

| Task Complexity | Recommended Approach | Token Range |
|----------------|----------------------|-------------|
| Quick lookup | Load @kmp-critical-patterns skill | ~800-1500 |
| Feature work | Load relevant skill | ~2000-3000 |
| Full context | Load multiple skills + references | ~5000+ |

### Metadata
- AI discovery: [llms.txt](llms.txt)
- Architecture: [See @kmp-architecture skill](See @kmp-architecture skill)

## Validation

Before committing, always run:
```bash
./gradlew :composeApp:assembleDebug test --continue
```
