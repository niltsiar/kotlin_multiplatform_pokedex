# Skill Quick Reference Card

Last Updated: February 7, 2026

## 🚀 One-Page Cheat Sheet for KMP Skills

### When to Use Which Skill

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

### Skill Categories

**Core Architecture (5 skills):**
- @kmp-critical-patterns → Quick reference for 6 core patterns
- @kmp-architecture → Module structure, vertical slicing
- @kmp-domain → Domain models, use cases
- @kmp-di → Dependency injection with Koin
- @kmp-gradle → Build configuration, convention plugins

**Layer Implementation (4 skills):**
- @kmp-presentation → ViewModels, lifecycle, SavedStateHandle
- @kmp-data-layer → Repositories, Either<RepoError,T>
- @kmp-api-services → Ktor, API services, DTOs
- @kmp-testing-patterns → Kotest, MockK, property testing

**Platform & Design (5 skills):**
- @kmp-ios → SwiftUI + KMP integration
- @kmp-navigation → Navigation 3, scoped routes
- @kmp-design-systems → Design tokens, components
- @kmp-compose-unstyled → Headless components
- @kmp-desktop → Desktop-specific patterns

**Development & QA (5 skills):**
- @kmp-developer → General development and features
- @kmp-mobile-expert → ViewModels, repositories, iOS
- @compose-screen → Compose UI screens (Material + Unstyled)
- @swiftui-screen → SwiftUI iOS screens
- @ktor-backend → Ktor server endpoints

**Build & Testing (2 skills):**
- @kmp-commands → Build, test, validation commands
- @kmp-testing-strategy → Testing philosophy, coverage

**Design & Planning (4 skills):**
- @product-designer → PRD, acceptance criteria
- @ui-ux-designer → Visual design, animations
- @onboarding → Onboarding flows
- @user-flows → Journey mapping

**Quality (1 skill):**
- @docs-maintainer → Documentation maintenance

### Quick Validation

```bash
# Always run before finishing
./gradlew :composeApp:assembleDebug test --continue
```

### Critical Patterns (Never Forget)

1. **ViewModel**: Pass scope, NO work in init
2. **Repository**: Return `Either<RepoError, T>`
3. **Compose**: Always add @Preview
4. **iOS**: Only export `:api` and `:presentation`

### File Locations

- **Skills**: `.claude/skills/<skill-name>/SKILL.md`
- **Validation**: `./.claude/skills/docs-maintainer/scripts/validate-links.sh`
- **Token Check**: `python3 scripts/check-tokens.py`
- **Full Guide**: `docs/SKILL_USAGE.md`
- **Decision Trees**: `AGENTS.md`

### Getting Help

1. **Quick help**: Ask the skill directly
2. **Decision trees**: Check `AGENTS.md` for routing
3. **Full guide**: Read `docs/SKILL_USAGE.md`
4. **Patterns**: Load `@kmp-critical-patterns`
5. **Architecture**: Load `@kmp-architecture`

---

**Print this page and keep it handy!** 📋
