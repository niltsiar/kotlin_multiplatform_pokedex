# Documentation Index - AI Discovery

**Last Updated:** February 7, 2026

> **Quick Navigation**: This index helps AI agents and developers find the right documentation quickly.

## Agent Skills (26 Total)

**For AI Agents:** Load skills via `@skill-name` syntax for domain-specific guidance.  
**For Humans:** Skills are organized by task domain. See [AGENTS.md](../AGENTS.md) decision trees for when to use each skill.

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
- **@kmp-testing-strategy** - Testing philosophy and coverage guidelines
- **@kmp-commands** - Build, test, validation commands

### Development
- **@kmp-developer** - General development
- **@kmp-mobile-expert** - ViewModels, repositories, iOS
- **@compose-screen** - Compose UI screens (Material + Unstyled)
- **@swiftui-screen** - SwiftUI iOS
- **@ktor-backend** - Ktor server endpoints

### Design & Planning
- **@product-designer** - PRD, acceptance criteria
- **@ui-ux-designer** - Visual design, animations
- **@onboarding** - Onboarding flows
- **@user-flows** - Journey mapping

### Quality
- **@docs-maintainer** - Documentation maintenance

---

## Start Here (< 1K tokens)
- [QUICK_REFERENCE.md](QUICK_REFERENCE.md) - Essential commands and workflows
- [SKILL_QUICK_REFERENCE.md](SKILL_QUICK_REFERENCE.md) - Quick skill reference card
- [tech/critical_patterns_compact.md](tech/critical_patterns_compact.md) - 6 core patterns (see @kmp-critical-patterns)

## Architecture & Patterns (1-3K tokens)
- [tech/conventions.md](tech/conventions.md) - Master architecture reference (see @kmp-architecture)
- [tech/critical_patterns_quick_ref.md](tech/critical_patterns_quick_ref.md) - 6 core patterns (see @kmp-critical-patterns)
- [tech/navigation.md](tech/navigation.md) - Navigation 3 modular architecture (see @kmp-navigation)
- [tech/domain.md](tech/domain.md) - Domain layer guidelines (see @kmp-domain)
- [tech/api_services.md](tech/api_services.md) - API service patterns (see @kmp-api-services)
- [tech/utility_organization.md](tech/utility_organization.md) - Utility module organization
- [tech/predictive_back_notes.md](tech/predictive_back_notes.md) - Predictive back gesture notes

## Platform-Specific Guides
- [tech/ios_integration.md](tech/ios_integration.md) - SwiftUI + KMP ViewModels Direct Integration (see @kmp-ios)
- [tech/ios_official_pattern_guide.md](tech/ios_official_pattern_guide.md) - iOS official pattern quick reference (see @kmp-ios)
- [tech/desktop_viewmodel_savedstate.md](tech/desktop_viewmodel_savedstate.md) - Desktop ViewModel + SavedStateHandle (see @kmp-desktop)
- [tech/testing_strategy.md](tech/testing_strategy.md) - Kotest, MockK, Turbine, property tests (see @kmp-testing-strategy)
- [tech/dependency_injection.md](tech/dependency_injection.md) - Koin patterns and troubleshooting (see @kmp-di)
- [tech/compose_unstyled_reference.md](tech/compose_unstyled_reference.md) - Compose Unstyled reference (see @kmp-compose-unstyled)

## Project Documentation
- [project/prd.md](project/prd.md) - Product requirements and acceptance criteria
- [project/user_flow.md](project/user_flow.md) - User journeys and flows
- [project/ui_ux.md](project/ui_ux.md) - UI/UX guidelines
- [project/onboarding.md](project/onboarding.md) - Onboarding flow documentation

## Complete Document Catalog

### Tech Guides (21 files)
See [tech/](tech/) directory for:
- Architecture patterns
- Testing strategies
- iOS integration
- Navigation
- Design systems
- API services
- And more...

### Patterns (5 files)
See [patterns/](patterns/) directory for:
- Architecture patterns
- ViewModel patterns
- DI patterns
- Error handling
- Testing patterns

---

## Token Budget Guide

When working with AI agents, manage context efficiently:

| Task Complexity | Recommended Docs | Token Range |
|----------------|------------------|-------------|
| Quick lookup | QUICK_REFERENCE + critical_patterns_compact | ~800-1200 |
| Feature work | Add conventions.md | ~2000-3000 |
| iOS work | Add ios_integration.md | ~3000-4000 |
| Full context | Load all relevant guides | ~5000+ |

**Rule**: Prefer links over pasted prose. Load incrementally.
