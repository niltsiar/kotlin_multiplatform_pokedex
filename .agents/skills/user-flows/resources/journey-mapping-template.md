# Journey Mapping Template

Use this template to document end-to-end user journeys across multiple screens.

## User Persona

**Name:** [Persona name, e.g., "Pokémon Trainer"]

**Goal:** [What they want to achieve, e.g., "Find information about Pikachu"]

**Context:** [Where/when they use the app, e.g., "On mobile device while playing Pokémon Go"]

## Journey Steps

### Step 1: [Action Name, e.g., "Launch App"]

- **Screen:** [Screen name, e.g., "Loading State (Initial)"]
- **User Action:** [What they do, e.g., "Taps app icon"]
- **System Response:** [What happens, e.g., "Shows full-screen spinner, fetches first 20 Pokémon"]
- **Emotion:** [How they feel, e.g., "Anticipating, ready to explore"]
- **Pain Point:** [Any friction, e.g., "Spinner takes 1-2 seconds"]

### Step 2: [Action Name, e.g., "Browse Pokémon List"]

- **Screen:** [Screen name, e.g., "Pokémon List Screen"]
- **User Action:** [What they do, e.g., "Scrolls through grid of Pokémon cards"]
- **System Response:** [What happens, e.g., "Infinite scroll loads more cards automatically"]
- **Emotion:** [How they feel, e.g., "Curious, engaged"]
- **Pain Point:** [Any friction, e.g., "Loading spinner appears at bottom"]

### Step 3: [Action Name]

- **Screen:** [Screen name]
- **User Action:** [What they do]
- **System Response:** [What happens]
- **Emotion:** [How they feel]
- **Pain Point:** [Any friction]

*(Add more steps as needed)*

## Navigation Contracts

### Primary Flow Navigation

**Route 1:** [Route name]
- **Route Object:** `object RouteName` or `data class RouteName(val param: Type)`
- **From:** [Source screen]
- **To:** [Destination screen]
- **Parameters:** [List parameters with types and descriptions]
  - `param: Type` - [Description, e.g., "Pokemon ID (1-1025)"]
- **Return:** [None or return type, e.g., "Result<Boolean>" for confirmation dialogs]
- **Animation:** [e.g., "Slide in horizontally (300ms, EmphasizedDecelerate)"]

**Example:**
```
Route: PokemonList → PokemonDetail
Route Object: data class PokemonDetail(val id: Int)
Parameters: id: Int - Pokémon ID (1-1025)
Return: None
Animation: Slide in from right, circular reveal from card (300ms, EmphasizedDecelerate)
```

*(Add more routes as needed)*

## Decision Points

### Decision Point 1: [Decision name, e.g., "Network Error on Launch"]

**Condition:** [What triggers the decision, e.g., "API request fails"]
```
If [network is available AND retry succeeds]
  → Go to [PokemonList Screen]
Else
  → Go to [Error State Screen]
  → User taps retry → Attempt API request again
```

**Error Handling:**
- Error message: [User-facing text, e.g., "Unable to load Pokémon. Please check your connection."]
- Retry action: [What user does, e.g., "Taps 'Retry' button"]
- Recovery path: [How to get back to happy path, e.g., "List loads on successful retry"]

### Decision Point 2: [Decision name]

**Condition:** [What triggers the decision]
```
If [condition]
  → Go to [screen]
Else If [condition]
  → Go to [screen]
Else
  → Go to [default screen]
```

**Error Handling:**
- Error message: [User-facing text]
- Retry action: [What user does]
- Recovery path: [How to get back to happy path]

*(Add more decision points as needed)*

## Flow Diagram

Use ASCII art to visualize the complete journey:

```
[Launch] → [Loading State] → [List Screen]
                                  ↓
                             [Decision: Tap Pokémon]
                                  ↓
                          ┌────────────────┐
                          │                ↓
                      [Network OK?]    [Network Error?]
                          │                ↓
                      Yes                ↓
                          │         [Error State]
                          ↓                │
                   [Detail Screen] ◄──────┘
                          ↓
                   [Scroll View Stats]
                          ↓
                   [Decision: Tap Back]
                          ↓
                   [List Screen] ← (scroll position preserved)
```

**Legend:**
- `[ ]` = Screen or state
- `→` = Navigation (happy path)
- `?` = Decision point
- `◄────` = Recovery path

## Edge Cases and Error States

### Edge Case 1: [Edge case name, e.g., "Rapid Card Tapping"]

**Description:** [What happens, e.g., "User rapidly taps multiple Pokémon cards"]

**Expected Behavior:**
- [First action triggers navigation, subsequent taps ignored]
- [Prevents navigation stack issues]

**User Experience:**
- [User sees detail screen for first tapped Pokémon]
- [No jank or navigation errors]

### Edge Case 2: [Edge case name, e.g., "No Internet Connection"]

**Description:** [What happens, e.g., "User has no network connection"]

**Expected Behavior:**
- [Error screen appears with clear message]
- [Retry button available when connection restored]

**User Experience:**
- [User understands the problem]
- [Clear path to retry]

*(Add more edge cases as needed)*

## Success Metrics

**Primary Goal:** [What defines success for this journey, e.g., "User finds Pokémon information within 10 seconds"]

**Success Indicators:**
- [Metric 1, e.g., "Journey completion rate: >90%"]
- [Metric 2, e.g., "Time to complete journey: <10s average"]
- [Metric 3, e.g., "Navigation error rate: <1%"]

**Measurement Method:**
- [How to measure, e.g., "Analytics tracking for screen transitions and user actions"]

## User Stories

**User Story 1:** [As a [persona], I want [goal], so that [benefit]]
- **Acceptance Criteria:** [Gherkin format: Given/When/Then]
  - Given [context], When [action], Then [expected result]

**User Story 2:** [As a [persona], I want [goal], so that [benefit]]
- **Acceptance Criteria:** [Gherkin format]

*(Add more user stories as needed)*

## Cross-References

| Related Document | Link | Notes |
|------------------|------|-------|
| [Document name] | [Path to document] | [Brief note, e.g., "Contains detailed screen specs"] |
| [user_flow.md] | `docs/project/user_flow.md` | Complete flows for this project |
| [navigation.md] | See @kmp-navigation skill | Navigation contracts implementation |
