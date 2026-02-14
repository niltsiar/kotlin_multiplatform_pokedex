---
name: ui-ux-designer
description: This skill should be used when creating visual designs, animations, and interaction patterns. Use for design systems, Material 3, and accessibility.
---

## When to Use

Use this skill when creating visual designs, animations, and interaction patterns:

- Designing new screens and components from scratch
- Creating animation specifications (timing, easing, transitions)
- Defining design tokens and spacing systems
- Implementing Material 3 expressive patterns
- Ensuring WCAG 2.1 accessibility compliance
- Designing responsive layouts (mobile, tablet, desktop)
- Creating color schemes and typography hierarchies
- Defining micro-interactions and feedback patterns

Do NOT use this skill for:
- Implementing Compose UI code → switch to Compose Screen Implementation Mode
- Implementing SwiftUI UI code → switch to SwiftUI Screen Implementation Mode
- Product/PRD decisions → switch to Product Design Mode
- Shared ViewModel logic → switch to KMP Mobile Expert Mode
- Navigation flow definitions → switch to User Flow Planning Mode

## Essential Workflows

### Workflow 1: Design System Creation

Create consistent design tokens following Material 3 guidelines:

1. Define spacing scale using 8dp grid system (xs: 4dp → xxxl: 64dp)
2. Establish elevation hierarchy (level0: 0dp → level5: 12dp)
3. Create color roles with semantic naming (primary, secondary, tertiary, surface, error)
4. Define shape system with corner radii (small: 8dp → extraLarge: 28dp)
5. Specify motion durations (short: 200ms, medium: 300ms, long: 400ms)
6. Set typography scale with line heights and weights
7. Implement component-specific tokens (card, badge, progress bar)
8. Validate WCAG 2.1 contrast ratios (4.5:1 for normal text, 3:1 for large text)

### Workflow 2: Animation Specification

Define clear animation behaviors using Material 3 motion principles:

1. Choose appropriate duration for interaction type (micro: 150-200ms, standard: 300ms, major: 400-600ms)
2. Select easing curve based on emotional goal (EmphasizedDecelerate for enter, EmphasizedAccelerate for exit, Linear for functional)
3. Specify transition pattern (circular reveal, shared axis, fade through, scale)
4. Define stagger pattern for multiple elements (50-100ms delay per element, cap at 10 items)
5. Document spring physics values (stiffness, damping ratio, for bounce effects)
6. Specify animation triggers (tap, hover, scroll, load)
7. Define accessibility considerations (respect reduced motion preference)
8. Provide code examples for implementation

### Workflow 3: Responsive Layout Design

Create adaptive layouts that work across all screen sizes:

1. Define breakpoints (compact: <600dp, medium: 600-840dp, expanded: >840dp)
2. Specify layout changes per breakpoint (grid columns, navigation style, component spacing)
3. Design component variations per breakpoint (card density, image sizes, typography scale)
4. Define adaptive navigation pattern (bottom bar for compact, rail for medium, drawer for expanded)
5. Specify touch target sizes (minimum 48x48dp for all interactive elements)
6. Design safe area handling for notched devices
7. Consider foldable device layouts if applicable
8. Test across all breakpoints during implementation

## Critical Guardrails

| Rule | Description | Example |
|------|-------------|---------|
| **Always use tokens** | Never hardcode dp values or colors | Use `MaterialTheme.tokens.spacing.medium` not `16.dp` |
| **WCAG 2.1 compliance** | Ensure minimum contrast ratios and touch targets | 4.5:1 contrast, 48x48dp touch targets |
| **Respect reduced motion** | Always honor user's reduced motion preference | Disable spring animations, keep fade transitions |
| **Token-based theming** | Use CompositionLocal for theme-provided values | `MaterialTheme.tokens` for design, `MaterialTheme.componentTokens` for components |
| **Material 3 expressive** | Follow Material 3 guidelines for patterns, colors, typography | Use EmphasizedDecelerate/EmphasizedAccelerate easing |

## Quick Reference

### Design Tokens

| Category | Token | Value | Usage |
|----------|-------|-------|-------|
| **Spacing** | `xs` | 4dp | Tight spacing between related elements |
| | `sm` | 8dp | Default padding for compact layouts |
| | `md` | 16dp | Standard padding and margins |
| | `lg` | 24dp | Section spacing |
| | `xl` | 32dp | Major section breaks |
| **Elevation** | `level0` | 0dp | Background elements |
| | `level1` | 1dp | Subtle cards |
| | `level2` | 3dp | Standard cards |
| | `level3` | 6dp | Elevated cards |
| | `level4` | 8dp | Dialogs, sheets |
| | `level5` | 12dp | FABs, navigation drawers |
| **Shapes** | `small` | 8dp | Buttons, chips |
| | `medium` | 16dp | Cards, dialogs |
| | `large` | 24dp | Bottom sheets |
| | `extraLarge` | 28dp | Hero cards (Material 3 expressive) |
| **Motion** | `durationShort` | 200ms | Micro-interactions (tap feedback) |
| | `durationMedium` | 300ms | Standard transitions |
| | `durationLong` | 400ms | Major transitions, hero animations |
| **Typography** | `displayLarge` | 57sp / Regular | Hero headings |
| | `headlineLarge` | 32sp / Medium | Section titles |
| | `titleLarge` | 22sp / Bold | Card titles |
| | `bodyLarge` | 16sp / Regular | Body text |

### Animation Easing Curves

| Curve | Control Points | Use Case |
|-------|----------------|----------|
| `EmphasizedDecelerate` | (0.05, 0.7, 0.1, 1.0) | Enter animations, scale up |
| `EmphasizedAccelerate` | (0.3, 0.0, 0.8, 0.15) | Exit animations, scale down |
| `StandardDecelerate` | (0.0, 0.0, 0.0, 1.0) | Fading in, sliding in |
| `Linear` | (0.0, 0.0, 1.0, 1.0) | Functional animations, reduced motion |
| `Spring` | stiffness=200, dampingRatio=0.7 | Bouncy interactions, overshoot |

### WCAG 2.1 Accessibility

| Requirement | Minimum Standard | Example |
|-------------|------------------|---------|
| **Contrast ratio (normal text)** | 4.5:1 | Body text on background |
| **Contrast ratio (large text)** | 3:1 | Headings, buttons |
| **Touch targets** | 48x48dp minimum | All interactive elements |
| **Touch target spacing** | 8dp between targets | Prevent accidental taps |
| **Text scaling** | Support up to 200% | System font size settings |
| **Color alone** | Never use color alone to convey info | Add icons, patterns, or text labels |

### Material 3 Color Roles

| Role | Light Mode | Dark Mode | Purpose |
|------|-----------|-----------|---------|
| `primary` | Coral (#FF5E57) | Adjusted coral | Primary buttons, active states |
| `onPrimary` | White (#FFFFFF) | Adjusted white | Text/icon on primary |
| `secondary` | Yellow (#FFCA3A) | Adjusted yellow | Secondary actions, badges |
| `surface` | Warm white (#FFFBF0) | Near-black (#1A1A1A) | Cards, dialogs |
| `onSurface` | Near-black (#1A1A1A) | Off-white (#F5F5F5) | Text/icon on surface |
| `error` | Red (#B00020) | Light red (#CF6679) | Error states, destructive actions |

## Related Skills

- **@kmp-design-systems** - Design tokens and component token system patterns
- **@kmp-compose-unstyled** - Platform-native theming patterns for Compose Unstyled

## Cross-References

| Document | Purpose | Link |
|----------|---------|------|
| Design token architecture | CompositionLocal pattern for design + component tokens | [@kmp-design-systems](../kmp-design-systems/SKILL.md) |
| UI/UX guidelines | Brand essence, dual design system, animation styles | [ui_ux.md](../../docs/project/ui_ux.md) |
| Component library | Component implementations with token abstraction | [@kmp-design-systems](../kmp-design-systems/SKILL.md) |
| Material 3 guidelines | Official Material Design 3 documentation | [material.io](https://m3.material.io/) |
| Critical patterns | ViewModel, Either, Impl+Factory, Navigation 3, Testing | [@kmp-critical-patterns](../kmp-critical-patterns/SKILL.md) |
| Accessibility | WCAG 2.1 guidelines and implementation patterns | [Material 3 accessibility](https://m3.material.io/foundations/accessible-design/overview) |
| Product requirements | Feature acceptance criteria and user flows | [prd.md](../../docs/project/prd.md) |

### Reference Implementations

**Material 3 Design System:**
- [MaterialTokens.kt](../../core/designsystem-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/core/designsystem/material/tokens/MaterialTokens.kt)
- [MaterialThemeTokens.kt](../../core/designsystem-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/core/designsystem/material/tokens/MaterialThemeTokens.kt)
- [Theme.kt](../../core/designsystem-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/core/designsystem/material/theme/Theme.kt)

**Animated Components:**
- [PokemonListGrid.kt](../../features/pokemonlist/ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemonlist/ui/material/components/PokemonListGrid.kt) - Staggered entrance animation
- [BaseStatsSection.kt](../../features/pokemondetail/ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/ui/material/components/BaseStatsSection.kt) - Sequential stat bar animation
- [TypeBadgeRow.kt](../../features/pokemondetail/ui-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/features/pokemondetail/ui/material/components/TypeBadgeRow.kt) - Badge bounce animation

**Design Token Customization:**
- [MaterialComponentTokens.kt](../../core/designsystem-material/src/commonMain/kotlin/com/minddistrict/multiplatformpoc/core/designsystem/material/tokens/MaterialComponentTokens.kt)
- See @kmp-compose-unstyled skill
