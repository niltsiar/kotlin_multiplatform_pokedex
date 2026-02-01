---
name: user-flows
description: This skill should be used when mapping user journeys, defining navigation contracts, and planning UX flows. Use for end-to-end journeys and navigation design.
---

## When to Use

Use this skill when:

**Trigger Scenarios:**
- Mapping end-to-end user journeys across multiple screens
- Defining navigation contracts (routes, parameters, transitions)
- Planning screen-to-screen flows and decision points
- Documenting happy paths, error paths, and edge cases
- Creating wireframes or flow diagrams
- Specifying navigation animations and transitions
- Planning onboarding or first-run experiences
- Designing deep linking and external navigation

**Exclusion Scenarios (DO NOT use):**
- Implementing actual Compose UI code → use Compose Screen Implementation Mode
- Implementing SwiftUI UI code → use SwiftUI Screen Implementation Mode
- Product requirements/PRD creation → use Product Design Mode
- Visual design specifications → use UI/UX Design Mode
- Test planning → use Testing Strategy Mode
- Navigation 3 implementation details → use technical documentation

## Essential Workflows

### Workflow 1: Map End-to-End User Journey

To document a complete user journey:

1. **Define User Persona and Goal**
   - Identify who is performing the action (persona name, role, context)
   - State their primary goal (what they want to achieve)
   - Note where/when they use the app (context: mobile, desktop, offline)

2. **Identify Entry and Exit Points**
   - Entry: Where does the journey start? (app launch, deep link, push notification)
   - Exit: Where does it end? (goal completed, abandoned, error)
   - Document any alternative entry points

3. **Map Screen-by-Screen Flow**
   - List each screen in order of traversal
   - For each screen: user action → system response → emotion
   - Identify decision points (conditional branching)
   - Note error states and recovery paths

4. **Document Navigation Contracts**
   - Define route objects (e.g., `PokemonDetail(id: Int)`)
   - Specify required parameters and types
   - Document return values (if navigation returns results)
   - Specify animations and transitions

5. **Handle Edge Cases**
   - Network errors and retry logic
   - Empty states and no data scenarios
   - Invalid input or missing parameters
   - Rapid navigation (debouncing, state preservation)

6. **Create Flow Diagram (ASCII or description)**
   - Visual representation of the journey
   - Show decision points with branching
   - Indicate happy path vs alternative paths

### Workflow 2: Define Navigation Contracts

To specify clear, type-safe navigation contracts:

1. **Design Route Objects**
   - Use simple Kotlin objects (no `@Serializable` needed for Navigation 3)
   - `object` for screens without parameters (e.g., `object PokemonList`)
   - `data class` for screens with parameters (e.g., `data class PokemonDetail(val id: Int)`)
   - Keep routes in `:api` module for iOS export

2. **Specify Parameter Contracts**
   - Document required vs optional parameters
   - Define parameter types (primitive, enum, custom types)
   - Specify validation rules (e.g., "id must be positive integer")
   - Document parameter encoding if using URL-based navigation

3. **Define Navigation Actions**
   - Forward navigation: `navigator.goTo(Route(param))`
   - Back navigation: `navigator.goBack()`
   - Conditional navigation: `if (success) navigator.goBack()`
   - Result passing: use result store for pop-with-result

4. **Specify Navigation State**
   - Is scroll position preserved on back navigation?
   - What state is shared across screens?
   - Does navigation reset or maintain ViewModels?
   - Are there any deep link handling requirements?

5. **Document Transitions and Animations**
   - Specify animation type (slide, fade, scale, shared element)
   - Define duration (standard: 300ms, fast: 150ms, slow: 500ms)
   - Choose easing curve (EmphasizedDecelerate, EmphasizedAccelerate, Linear)
   - Document any platform-specific behavior (iOS vs Android)

### Workflow 3: Document Decision Points and Branching

To create clear, testable flow logic:

1. **Identify All Decision Points**
   - User actions that trigger branching (tap, scroll, swipe)
   - System conditions (network state, data availability, permissions)
   - Error states (timeout, 404, 500, parsing error)
   - A/B testing or feature flags

2. **Define Branching Logic**
   - Use conditional format: "If [condition] → Go to [screen], Else → Go to [screen]"
   - Document all branches explicitly
   - Handle default/fallback paths
   - Specify any retry or recovery mechanisms

3. **Create State Transition Table**
   - Current state → Event → Next state
   - Document all possible state transitions
   - Identify unreachable or invalid states
   - Specify entry and exit actions for each state

4. **Document Error Recovery Paths**
   - How does the user recover from errors? (retry button, automatic retry, manual intervention)
   - What state is preserved during error?
   - Can the user continue or must they restart?
   - Is offline mode supported?

5. **Specify Edge Cases**
   - Rapid user input (double-tap, spam clicking)
   - Concurrent actions (user navigates while loading)
   - State conflicts (user edits while syncing)
   - Boundary conditions (first item, last item, empty list)

## Critical Guardrails

| Rule | Description | Rationale |
|------|-------------|-----------|
| **One primary flow per screen** | Each screen should have a single, clear primary action path | Prevents confused users, simplifies implementation |
| **Clear entry and exit points** | Explicitly state where the flow starts and ends | Enables proper navigation stack management |
| **Handle all error paths** | Document error states, retry logic, and recovery paths | Users must never be stuck without an option |
| **Preserve navigation state** | Scroll position, form data, and selections persist on back navigation | Users expect to return to where they left off |
| **Type-safe route contracts** | Use Kotlin objects with typed parameters, not string-based routes | Prevents runtime navigation errors, enables compile-time safety |
| **Document decision points** | All conditional branching must be explicitly documented | Enables testability and predictable behavior |
| **Respect platform patterns** | Follow Android back button, iOS gestures, platform-specific navigation | Feels native to users, reduces learning curve |

## Quick Reference

| Template | When to Use | Format |
|----------|-------------|--------|
| **User Journey Map** | Documenting end-to-end user flow | Persona → Goal → Steps (Screen, Action, Response, Emotion) → Navigation Contracts |
| **Navigation Contract** | Defining type-safe navigation between screens | Route object + Parameters + Return value + Animation |
| **Decision Point** | Documenting conditional branching | If [condition] → Go to [screen], Else → Go to [screen] |
| **Flow Diagram** | Visual representation of user journey | ASCII or description showing screens, decisions, transitions |
| **Wireframe Specification** | Planning screen layout and interactions | Screen name + Key elements + User actions + System responses |

### Navigation Contract Format

**Simple Route (no parameters):**
```kotlin
// Route object
object PokemonList

// Navigation
navigator.goTo(PokemonList)
```

**Parameterized Route:**
```kotlin
// Route object
data class PokemonDetail(val id: Int)

// Navigation
navigator.goTo(PokemonDetail(pokemonId = 25))
```

**With Return Result:**
```kotlin
// In destination screen
resultStore.setResult("updated", resultKey = "profile_action")
navigator.goBack()

// In source screen
val result = resultStore.consumeResult<String>(resultKey = "profile_action")
```

### Flow Diagram Notation

Use ASCII art or structured description:

```
[Launch] → [Loading] → [PokemonList]
                              ↓
                         Tap Pokemon → [Loading] → [PokemonDetail]
                                                      ↓
                                                 Tap Back → [PokemonList] (scroll preserved)
```

**Decision Point:**
```
[Login Screen]
    ↓
    ├─ Valid credentials → [Home Screen]
    │
    └─ Invalid → [Error Message] → [Login Screen]
```

## Cross-References

| Document | Purpose | Location |
|----------|---------|----------|
| `user_flow.md` | Complete example of user flows for this project | `docs/project/user_flow.md` |
| `navigation.md` | Navigation 3 architecture and implementation patterns | `docs/tech/navigation.md` |
| `prd.md` | Product requirements with acceptance criteria | `docs/project/prd.md` |
| `ui_ux.md` | UI/UX guidelines and design system | `docs/project/ui_ux.md` |
| `conventions.md` | Architecture and technical patterns | `docs/tech/conventions.md` |
| `critical_patterns_quick_ref.md` | Core patterns including Navigation 3 | `docs/tech/critical_patterns_quick_ref.md` |
| `journey-mapping-template.md` | Ready-to-use journey mapping template | `.claude/skills/user-flows/resources/journey-mapping-template.md` |

## Pro Tips

1. **Start with Happy Path, Then Add Branches** — Map the ideal user journey first, then layer in error states and edge cases
2. **Use Real User Language** — Write flows from the user's perspective ("Tap the Pokémon card" not "Navigate to Detail route")
3. **Document Emotions** — Note how the user feels at each step (confused, delighted, frustrated) to identify pain points
4. **Think About "What If Scenarios"** — What if network is slow? What if user taps twice? What if image fails to load?
5. **Keep Navigation Contracts Simple** — Avoid complex parameter passing; use shared ViewModels or result stores when needed
6. **Test Flows, Not Just Screens** — Write end-to-end tests that cover complete journeys, not just individual screens
7. **Respect Platform Navigation Patterns** — Android expects back button, iOS expects swipe gestures, don't fight the OS
8. **Preserve User Context** — When going back, users expect to return to exactly where they left off
