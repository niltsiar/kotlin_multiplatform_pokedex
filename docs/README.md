# Documentation Index - AI Discovery

**Last Updated:** February 7, 2026

> **Quick Navigation**: This index helps AI agents and developers find the right documentation quickly.

## Start Here (< 1K tokens)
- [QUICK_REFERENCE.md](QUICK_REFERENCE.md) - Essential commands and workflows
- [SKILL_QUICK_REFERENCE.md](SKILL_QUICK_REFERENCE.md) - Quick skill reference card
- [tech/critical_patterns_compact.md](tech/critical_patterns_compact.md) - 6 core patterns (quick reference)

## Architecture & Patterns (1-3K tokens)
- [tech/conventions.md](tech/conventions.md) - Master architecture reference (START HERE for new developers)
- [tech/critical_patterns_quick_ref.md](tech/critical_patterns_quick_ref.md) - 6 core patterns (full guide)
- [tech/navigation.md](tech/navigation.md) - Navigation 3 modular architecture
- [tech/domain.md](tech/domain.md) - Domain layer guidelines
- [tech/api_services.md](tech/api_services.md) - API service patterns
- [tech/utility_organization.md](tech/utility_organization.md) - Utility module organization
- [tech/predictive_back_notes.md](tech/predictive_back_notes.md) - Predictive back gesture notes

## Platform-Specific Guides
- [tech/ios_integration.md](tech/ios_integration.md) - SwiftUI + KMP ViewModels Direct Integration
- [tech/ios_official_pattern_guide.md](tech/ios_official_pattern_guide.md) - iOS official pattern quick reference
- [tech/desktop_viewmodel_savedstate.md](tech/desktop_viewmodel_savedstate.md) - Desktop ViewModel + SavedStateHandle
- [tech/testing_strategy.md](tech/testing_strategy.md) - Kotest, MockK, Turbine, property tests
- [tech/dependency_injection.md](tech/dependency_injection.md) - Koin patterns and troubleshooting
- [tech/compose_unstyled_reference.md](tech/compose_unstyled_reference.md) - Compose Unstyled reference

## Skills & Agent Routing
- [AGENTS.md](../AGENTS.md) - Agent routing table and mode selection
- [.claude/skills/](../.claude/skills/) - 27 professional skills for development

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

### Skills (27 professional skills)
See [.claude/skills/](../.claude/skills/) directory for:

**Architecture & Core (2 skills):**
- @kmp-architecture - Module structure, vertical slice organization
- @kmp-critical-patterns - Quick reference for 6 core patterns

**Layer Implementation (5 skills):**
- @kmp-presentation - ViewModels, UI state management
- @kmp-data-layer - Repository patterns with Either<RepoError,T>
- @kmp-domain - Domain models, immutable data classes
- @kmp-api-services - Ktor Client patterns, DTOs
- @kmp-di - Koin dependency injection

**Platform & UI (5 skills):**
- @kmp-ios - SwiftUI + KMP ViewModels integration
- @swiftui-screen - Native iOS UI with SwiftUI
- @compose-screen - Compose UI screens (Material + Unstyled)
- @kmp-navigation - Navigation 3 modular architecture
- @kmp-desktop - Desktop (JVM) patterns

**Design & Testing (5 skills):**
- @kmp-design-systems - Design tokens, Material 3
- @kmp-compose-unstyled - Headless component patterns
- @ui-ux-designer - Visual design, animations
- @kmp-testing-strategy - Testing philosophy, coverage
- @kmp-testing-patterns - Kotest, MockK, Turbine

**Build & Commands (2 skills):**
- @kmp-gradle - Gradle convention plugins
- @kmp-commands - CLI reference card

**Development & Quality (7 skills):**
- @kmp-developer - General KMP development
- @kmp-mobile-expert - Shared business logic
- @ktor-backend - Ktor server endpoints
- @product-designer - PRD creation
- @user-flows - User journey mapping
- @onboarding - First-run experience
- @docs-maintainer - Documentation maintenance

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
