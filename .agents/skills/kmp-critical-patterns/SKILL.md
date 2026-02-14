---
name: kmp-critical-patterns
description: Quick reference for 6 core KMP patterns - Impl+Factory, Either Boundary, ViewModel, Navigation 3, Testing, Convention Plugins. Use when the agent needs a fast pattern reminder or initial project setup guidance without full skill context. Triggers - 'show me the patterns', 'quick reference', 'pattern overview', before implementing new features, when switching skills, token-constrained scenarios.
---

# KMP Critical Patterns

6 essential patterns for this Kotlin Multiplatform codebase. Quick reference format - follow links for detailed implementation.

## When to Use

**Use when:** Quick pattern reminders, token-constrained scenarios, pattern overview requests, switching between skills.

**Do NOT use when:** Need full implementation guidance (use @kmp-developer or layer-specific skills), writing tests (@kmp-testing-strategy), building UI (@compose-screen, @swiftui-screen).

## Loading Triggers

**MANDATORY - READ ENTIRE FILE**: Before implementing any of the 6 patterns for the first time, you MUST read [pattern-examples.md](references/pattern-examples.md) (~300 lines) for complete code examples with detailed syntax.

**Do NOT load** `pattern-examples.md` if you only need quick pattern reminders or decision matrices.

## Pattern Overview

| # | Pattern | One-Line Rule |
|---|---------|---------------|
| 1 | **Impl+Factory** | Internal `Impl` class + public factory function |
| 2 | **Either Boundary** | Repos return `Either<RepoError, T>`, never throw |
| 3 | **ViewModel** | Pass scope to constructor, NO work in init |
| 4 | **Navigation 3** | Routes in `:api`, providers in wiring |
| 5 | **Testing** | NO CODE WITHOUT TESTS, property + Turbine |
| 6 | **Convention Plugins** | Use feature.* convention plugins for modules |

---

## Pattern 1: Impl + Factory (Koin)

**Key Rule:** Internal `Impl` class + public factory function. Production classes stay DI-agnostic.

**NEVER:**
- Make `Impl` classes public
- Use `@Inject constructor` in production code
- Create interfaces with single `Impl` differently

**For detailed code examples:** See [pattern-examples.md](references/pattern-examples.md)

---

## Pattern 2: Either Boundary

**Key Rule:** Repositories return `Either<RepoError, T>`. Map errors, never throw.

**Error Types:**
- `RepoError.Network` - IO exceptions
- `RepoError.Http` - HTTP error codes
- `RepoError.Unknown` - Everything else

**NEVER:**
- Return nullable types
- Return `Result<T>`
- Throw exceptions from repositories

**For detailed code examples:** See [pattern-examples.md](references/pattern-examples.md)

---

## Pattern 3: ViewModel Pattern

**Key Rule:** Pass scope to constructor, NO work in init, use `onStart()`.

**NEVER:**
- Store `CoroutineScope` as field
- Do work in `init` block
- Use default ViewModel() constructor without scope

**For detailed code examples:** See [pattern-examples.md](references/pattern-examples.md)

---

## Pattern 4: Navigation 3 Pattern

**Key Rule:** Routes in `:api`, navigation providers in wiring modules.

**Module Structure:**
- `:api` - Route objects and navigation entry points
- `:wiring-ui-material` - Material Design navigation providers
- `:wiring-ui-unstyled` - Compose Unstyled navigation providers

**For detailed code examples:** See [pattern-examples.md](references/pattern-examples.md)

---

## Pattern 5: Testing Pattern

**Key Rule:** NO CODE WITHOUT TESTS. Property tests for mappers, Turbine for flows.

**Test Distribution:**
- 40% property-based tests (mappers, invariant properties)
- 60% concrete tests (ViewModels, repositories)

**NEVER:**
- Skip tests for production code
- Forget Turbine for StateFlow testing
- Skip error path testing

**For detailed code examples:** See [pattern-examples.md](references/pattern-examples.md)

---

## Pattern 6: Convention Plugins

**Key Rule:** Use `convention.feature.*` plugins for feature modules.

**Plugin Matrix:**

| Plugin | Use For |
|--------|---------|
| `convention.feature.api` | `:api` modules (contracts, models) |
| `convention.feature.data` | `:data` modules (repositories, mappers) |
| `convention.feature.presentation` | `:presentation` modules (ViewModels) |
| `convention.feature.ui` | `:ui-*` modules (Compose screens) |
| `convention.feature.wiring` | `:wiring*` modules (Koin modules) |
| `convention.feature.base` | Feature foundation dependencies |

**For detailed code examples:** See [pattern-examples.md](references/pattern-examples.md)

---

## Usage Workflows

**Starting a New Feature:**
1. Check Pattern Overview table → identify applicable patterns
2. Review Key Rules and NEVER lists
3. Load [pattern-examples.md](references/pattern-examples.md) for detailed syntax
4. Use Pattern Enforcement Checklist before committing

**Pattern Validation:**
1. Verify no NEVER rules violated
2. Check Pattern Enforcement Checklist
3. Cross-reference to full skills for deep context

---

## Quick Reference

### Common Violations & Fixes

| Violation | Correct Pattern |
|-----------|----------------|
| `class XImpl : X` (public) | `internal class XImpl : X` + factory |
| `suspend fun get(): T?` | `suspend fun get(): Either<RepoError, T>` |
| `private val scope = ...` | `viewModelScope: CoroutineScope` param |
| `init { loadData() }` | `override fun onStart(owner: LifecycleOwner)` |
| FQN in code | Import first, then use short name |
| Empty use case | Call repository directly from ViewModel |
| @Composable without @Preview | Add `@Preview` with realistic data |

### Critical DON'Ts (Top 10)

1. ❌ **NEVER run iOS builds** unless explicitly required (5-10min builds)
2. ❌ **NEVER store `CoroutineScope` as field** in ViewModels (pass to constructor)
3. ❌ **NEVER perform work in `init`** blocks in ViewModels (use lifecycle callbacks)
4. ❌ **NEVER return `Result` or nullable** from repositories (use `Either<RepoError, T>`)
5. ❌ **NEVER swallow `CancellationException`** (use `Either.catch` which handles it)
6. ❌ **NEVER create empty pass-through** use cases (call repos directly)
7. ❌ **NEVER export `:data`, `:ui`, or `:wiring`** to iOS (only `:api`, `:presentation`, `:core:*`)
8. ❌ **NEVER put business logic in `:shared`** itself (it's an umbrella; logic goes in feature/core modules)
9. ❌ **NEVER add DI annotations** to production classes (wire in wiring modules)
10. ❌ **NEVER use star imports or FQN** — Import explicitly, use short names
11. ❌ **NEVER omit @Preview** for @Composable functions (MANDATORY)

### Decision Matrices

#### When to Create a New Module?
```
IF defining cross-feature contracts → :features:<name>:api (export to iOS)
IF implementing data layer         → :features:<name>:data (do NOT export)
IF implementing ViewModels         → :features:<name>:presentation (export to iOS)
IF implementing Compose UI         → :features:<name>:ui (do NOT export)
IF wiring dependencies             → :features:<name>:wiring (do NOT export)
IF shared utilities (3+ features)  → :core:util (export to iOS)
IF common domain models            → :core:domain (export to iOS)
ELSE modify existing modules
```

#### When to Create a Use Case?
```
IF orchestrating 2+ repositories   → Create use case
IF applying business rules         → Create use case
IF single repository call only     → Call directly from ViewModel
```

#### When to Use expect/actual?
```
IF platform-specific API access    → Use expect/actual in feature/core modules
IF platform-specific UI:
  - Android/Desktop               → Use Compose source sets (androidMain, jvmMain)
  - iOS Production                → Use SwiftUI in :iosApp (separate from Compose)
  - iOS Experimental              → Use Compose in :iosAppCompose (shares UI)
IF shared business logic           → Use commonMain in feature/core modules
IF simple constants                → Use commonMain in appropriate module
```

#### When to Remove Redundant Tests?
```
1. Does a property test cover this scenario?        → Remove concrete test
2. Is this an edge case not covered by properties?  → Keep concrete test
3. Does this test document important behavior?      → Keep but add comment
4. Is this test redundant with another test?        → Merge or remove
```

### Pattern Enforcement Checklist

Before implementing any feature:

- [ ] Repository uses `Either<RepoError, T>`
- [ ] ViewModel passes scope to constructor
- [ ] ViewModel uses `onStart()` not `init`
- [ ] Tests exist for all production code
- [ ] Impl class is `internal`, factory is `public`
- [ ] Feature module uses correct convention plugin
- [ ] Navigation routes in `:api`, providers in wiring

### Pattern-to-Skill Mapping

| Pattern | Primary Skill | Secondary Skills |
|---------|--------------|------------------|
| Impl+Factory | @kmp-di | @kmp-data-layer, @kmp-presentation |
| Either Boundary | @kmp-data-layer | @kmp-domain |
| ViewModel | @kmp-presentation | @kmp-mobile-expert |
| Navigation 3 | @kmp-navigation | @compose-screen, @swiftui-screen |
| Testing | @kmp-testing-patterns | @kmp-testing-strategy |
| Convention Plugins | @kmp-gradle | @kmp-architecture |

## Critical Guardrails

1. NEVER use this as the only reference → cross-reference to full skills for complete context
2. NEVER skip Pattern Enforcement Checklist before committing
3. NEVER treat patterns as suggestions → they are mandatory project conventions
4. NEVER use for initial learning → this is a refresher, not a tutorial

---

## Cross-References

**For detailed implementation:** See Pattern-to-Skill Mapping table above.

**Key Skills:**
- @kmp-architecture - Module structure, vertical slicing
- @kmp-developer - Full implementation patterns
- @kmp-mobile-expert - ViewModel + repository patterns

**Reference Implementation:** `features/pokemonlist/` demonstrates all 6 patterns end-to-end.
