# Commit Message Convention

This project follows [Conventional Commits](https://www.conventionalcommits.org/) specification for commit messages.

## Format

```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

## Types

| Type | Description | Example |
|------|-------------|---------|
| `feat` | New feature | `feat(pokemonlist): add infinite scroll pagination` |
| `fix` | Bug fix | `fix(pokemondetail): resolve crash on missing sprite data` |
| `docs` | Documentation changes | `docs(conventions): add commit message guidelines` |
| `test` | Adding or updating tests | `test(pokemonlist): add property-based tests for repository` |
| `build` | Build system or dependencies | `build(gradle): update Kotlin to 2.1.0` |
| `refactor` | Code refactoring | `refactor(designsystem): extract theme tokens to constants` |
| `chore` | Maintenance tasks | `chore: update .gitignore for build artifacts` |

## Scopes

Use general feature or area names as scopes. Keep them simple and broad:

**Feature scopes:**
- `pokemonlist` - Pokemon list feature
- `pokemondetail` - Pokemon detail feature

**Area scopes:**
- `designsystem` - Design system components and theming
- `navigation` - Navigation architecture
- `testing` - Test infrastructure and patterns
- `ios` - iOS-specific implementations
- `di` - Dependency injection setup
- `conventions` - Project conventions and documentation

**Avoid overly specific scopes** like `feat(di,navigation)` or `feat(koin-module-wiring)`. Keep scopes focused on the user-visible feature or architectural area.

## Examples

### Before/After from Actual History

**Poor:**
```
Update tests
```

**Good:**
```
test(pokemonlist): add property-based tests for pagination

- Add Arb generators for PokemonDto and API responses
- Test HTTP error code ranges with checkAll
- Verify ID preservation in mapper transformations
- Achieve 40% property test coverage
```

---

**Poor:**
```
Modify guidelines
```

**Good:**
```
docs(conventions): consolidate DI patterns into critical_patterns_quick_ref.md

- Extract Impl+Factory pattern to canonical reference
- Add anchored links from guidelines.md
- Remove duplicate explanations from AGENTS.md
- Update 6 core pattern definitions for consistency
```

---

**Poor:**
```
Update convention plugins
```

**Good:**
```
build(gradle): refactor convention plugins to use shared config utilities

- Extract common configurations into KotlinMultiplatformConfigExtensions
- Reduce duplication across feature convention plugins
- Follow Now in Android base plugin composition pattern
- Achieve 38% code reduction in build-logic
```

## Validation

### Conceptual Approach

To maintain commit message quality, consider implementing validation:

**Regex Pattern Matching:**
- Pattern: `^(feat|fix|docs|test|build|refactor|chore)(\([a-z]+\))?: .+$`
- Validates: type, optional scope, colon separator, description
- Enforces: lowercase types and scopes, proper formatting

**git-cliff Linting:**
- Use `git cliff --context` to validate commit parses correctly
- Catches malformed messages before they're committed
- Ensures CHANGELOG generation works smoothly

**Implementation Options:**
- Pre-commit hook: Validate before commit is created
- CI/CD check: Validate during pull request review
- Manual: Run validation script before pushing

For this POC, validation is manual. Production projects should automate via pre-commit hooks or CI/CD pipelines.

## Guidelines

1. **Use imperative mood** in descriptions: "add feature" not "added feature"
2. **Keep descriptions concise**: 50-72 characters for first line
3. **Capitalize first letter** of description
4. **No period at end** of description
5. **Use body for context**: What changed and why (optional but recommended)
6. **Reference issues** in footer: `Closes #123` or `Fixes #456`

## Why Conventional Commits?

- **Automated CHANGELOG generation** via git-cliff
- **Semantic versioning** automation possibilities
- **Better collaboration** through clear commit history
- **Easier debugging** with searchable, categorized commits
- **Tool integration** with CI/CD pipelines and release management
