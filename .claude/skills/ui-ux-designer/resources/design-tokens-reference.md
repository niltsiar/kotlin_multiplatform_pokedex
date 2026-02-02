# Design Tokens Reference

This document provides a quick reference for design tokens used across the Pokédex app.

## Colors

### Material 3 Color Roles (Light Mode)

| Token | Hex Value | Usage |
|-------|-----------|-------|
| Primary | `#FF5E57` | Primary buttons, active states, navigation items |
| On Primary | `#FFFFFF` | Text and icons on primary color |
| Secondary | `#FFCA3A` | Secondary actions, badges, success states |
| On Secondary | `#1A1A1A` | Text and icons on secondary color |
| Tertiary | `#78C850` | Tertiary actions, accents (grass green) |
| On Tertiary | `#FFFFFF` | Text and icons on tertiary color |
| Surface | `#FFFBF0` | Cards, dialogs, sheets (warm white) |
| On Surface | `#1A1A1A` | Text and icons on surface color |
| Surface Variant | `#F5F5DC` | Alternate surface (beige) |
| On Surface Variant | `#2C3E50` | Text on surface variant |
| Error | `#B00020` | Error states, destructive actions |
| On Error | `#FFFFFF` | Text and icons on error color |
| Outline | `#79747E` | Borders, dividers |

### Material 3 Color Roles (Dark Mode)

| Token | Hex Value | Usage |
|-------|-----------|-------|
| Primary | `#FF8A80` | Primary buttons, active states (adjusted for contrast) |
| On Primary | `#1A1A1A` | Text and icons on primary color |
| Secondary | `#FFD54F` | Secondary actions, badges (adjusted for contrast) |
| On Secondary | `#1A1A1A` | Text and icons on secondary color |
| Tertiary | `#A5D6A7` | Tertiary actions, accents (adjusted for contrast) |
| On Tertiary | `#1A1A1A` | Text and icons on tertiary color |
| Surface | `#1A1A1A` | Cards, dialogs, sheets (near-black) |
| On Surface | `#F5F5F5` | Text and icons on surface color |
| Surface Variant | `#2C2C2C` | Alternate surface |
| On Surface Variant | `#E0E0E0` | Text on surface variant |
| Error | `#CF6679` | Error states (light red for dark mode) |
| On Error | `#1A1A1A` | Text and icons on error color |
| Outline | `#938F99` | Borders, dividers |

### Pokémon Type Colors

| Type | Hex Value | Usage |
|------|-----------|-------|
| Normal | `#A8A878` | Normal type badges, accents |
| Fire | `#F08030` | Fire type badges, accents |
| Water | `#6890F0` | Water type badges, accents |
| Electric | `#F8D030` | Electric type badges, accents |
| Grass | `#78C850` | Grass type badges, accents |
| Ice | `#98D8D8` | Ice type badges, accents |
| Fighting | `#C03028` | Fighting type badges, accents |
| Poison | `#A040A0` | Poison type badges, accents |
| Ground | `#E0C068` | Ground type badges, accents |
| Flying | `#A890F0` | Flying type badges, accents |
| Psychic | `#F85888` | Psychic type badges, accents |
| Bug | `#A8B820` | Bug type badges, accents |
| Rock | `#B8A038` | Rock type badges, accents |
| Ghost | `#705898` | Ghost type badges, accents |
| Dragon | `#7038F8` | Dragon type badges, accents |
| Dark | `#705848` | Dark type badges, accents |
| Steel | `#B8B8D0` | Steel type badges, accents |
| Fairy | `#EE99AC` | Fairy type badges, accents |

**Note:** Type colors are adjusted for WCAG AA compliance in dark mode by lightening shades.

## Typography

### Material 3 Typography Scale

| Scale | Size | Line Height | Weight | Usage |
|-------|------|-------------|--------|-------|
| Display Large | 57sp | 64sp | 400 (Regular) | Hero headings, splash screens |
| Display Medium | 45sp | 52sp | 400 (Regular) | Large hero text |
| Display Small | 36sp | 44sp | 400 (Regular) | Prominent display text |
| Headline Large | 32sp | 40sp | 500 (Medium) | Page titles |
| Headline Medium | 28sp | 36sp | 500 (Medium) | Section headings |
| Headline Small | 24sp | 32sp | 500 (Medium) | Card titles |
| Title Large | 22sp | 28sp | 700 (Bold) | List item titles |
| Title Medium | 16sp | 24sp | 700 (Bold) | Button text |
| Title Small | 14sp | 20sp | 700 (Bold) | Small headings |
| Body Large | 16sp | 24sp | 400 (Regular) | Primary body text |
| Body Medium | 14sp | 20sp | 400 (Regular) | Secondary body text |
| Body Small | 12sp | 16sp | 400 (Regular) | Captions, labels |
| Label Large | 14sp | 20sp | 700 (Bold) | Button labels |
| Label Medium | 12sp | 16sp | 700 (Bold) | Badge text |
| Label Small | 11sp | 16sp | 700 (Bold) | Tiny labels |

### Dynamic Typography (Material 3 Expressive)

- **Button press animation:** Font weight animates from 400 to 700
- **Card hover animation:** Font weight animates from 400 to 500
- **Font family:** Google Sans Flex (variable font) on Android/Desktop, San Francisco on iOS

## Spacing

### 8dp Grid System

| Token | Value | Usage |
|-------|-------|-------|
| xxs | 2dp | Ultra-tight spacing (rare) |
| xs | 4dp | Tight spacing between related elements |
| sm | 8dp | Default padding for compact layouts |
| md | 16dp | Standard padding and margins (most common) |
| lg | 24dp | Section spacing, large gaps |
| xl | 32dp | Major section breaks |
| xxl | 48dp | Screen-level padding (edges) |
| xxxl | 64dp | Hero sections, full-screen padding |

### Usage Examples

```kotlin
// Card padding
modifier.padding(MaterialTheme.tokens.spacing.md)

// Section gap
Spacer(modifier = Modifier.height(MaterialTheme.tokens.spacing.lg))

// Screen edges
modifier.padding(horizontal = MaterialTheme.tokens.spacing.xxl)
```

## Shapes

### Corner Radius Scale

| Token | Value | Usage |
|-------|-------|-------|
| small | 8dp | Buttons, chips, small cards |
| medium | 16dp | Standard cards, dialogs |
| large | 24dp | Bottom sheets, navigation drawers |
| extraLarge | 28dp | Hero cards (Material 3 expressive) |

### Material 3 Expressive vs Compose Unstyled

| Theme | Card Radius | Badge Radius | Button Radius |
|-------|-------------|--------------|---------------|
| Material 3 Expressive | 28dp (extraLarge) | 16dp (medium) | 8dp (small) |
| Compose Unstyled | 12dp (custom) | 12dp (custom) | 8dp (small) |

## Elevation

### Shadow Depth Scale

| Token | Value | Usage |
|-------|-------|-------|
| level0 | 0dp | Background elements, no shadow |
| level1 | 1dp | Subtle cards, list items |
| level2 | 3dp | Standard cards (most common) |
| level3 | 6dp | Elevated cards, dropdown menus |
| level4 | 8dp | Dialogs, bottom sheets |
| level5 | 12dp | FABs, navigation drawers |

### Elevation Change on Interaction

- **Hover (Desktop):** Elevation increases by 1 level
- **Pressed (Tap):** Elevation decreases by 1 level
- **Focus (Keyboard):** Elevation increases by 1 level

## Animation

### Motion Durations

| Token | Value | Usage |
|-------|-------|-------|
| durationShort | 200ms | Micro-interactions (tap feedback, hover) |
| durationMedium | 300ms | Standard transitions, loading states |
| durationLong | 400ms | Major transitions, hero animations, screen enter |

### Material 3 Expressive Easing Curves

| Curve | Control Points | Use Case |
|-------|----------------|----------|
| EmphasizedDecelerate | (0.05, 0.7, 0.1, 1.0) | Enter animations, scale up, fade in |
| EmphasizedAccelerate | (0.3, 0.0, 0.8, 0.15) | Exit animations, scale down, fade out |
| StandardDecelerate | (0.0, 0.0, 0.0, 1.0) | Fading in, sliding in |
| Linear | (0.0, 0.0, 1.0, 1.0) | Functional animations, reduced motion mode |

### Spring Physics

| Property | Value | Usage |
|----------|-------|-------|
| Stiffness | 200 | Standard spring tension |
| Damping Ratio | 0.7 | Standard bounciness (slightly underdamped) |
| Stiffness Low | 100 | Softer spring for gentle bounce |
| Damping High | 0.9 | Heavily damped (minimal overshoot) |

### Animation Patterns

| Pattern | Duration | Easing | Usage |
|---------|----------|--------|-------|
| Staggered Entrance | 400ms per item | EmphasizedDecelerate | Grid items cascade in (50ms delay each) |
| Circular Reveal | 300ms | EmphasizedDecelerate | Screen transition from tapped element |
| Scale and Fade | 200ms | EmphasizedDecelerate | Button tap, card press |
| Sequential Reveal | 200ms per item | EmphasizedDecelerate | Stats animate in one-by-one |
| Shimmer Loading | Infinite loop | Linear | Skeleton screens (2000ms cycle) |
| Hover Lift | 200ms | StandardDecelerate | Card hover effect (desktop) |

### Stagger Delays

```kotlin
// Stagger grid items with 50ms delay per item
val items = list.mapIndexed { index, item ->
    LaunchedEffect(Unit) {
        delay(index * 50L)
        // Animate item enter
    }
}
```

### Reduced Motion Support

When user has reduced motion preference enabled:
- Disable spring animations (use linear easing)
- Keep fade transitions (durationShort: 150ms)
- Remove staggered entrances (all items fade in together)
- Disable scale animations

## Accessibility

### WCAG 2.1 Contrast Ratios

| Text Type | Minimum Ratio | Example |
|-----------|---------------|---------|
| Normal text (< 18sp) | 4.5:1 | Body text, labels |
| Large text (≥ 18sp) | 3:1 | Headings, buttons |
| UI components | 3:1 | Icons, borders, focus indicators |

### Touch Target Sizes

| Element | Minimum Size | Recommended Size |
|---------|--------------|------------------|
| Buttons | 48x48dp | 48x48dp minimum |
| Touchable cards | 48x48dp minimum | 48x48dp minimum |
| Icon buttons | 48x48dp | 48x48dp minimum |
| List items | 48dp height | 48dp height minimum |

### Spacing Between Touch Targets

- Minimum: 8dp between adjacent touch targets
- Prevents accidental taps on mobile devices

### Text Scaling

- Support system font size scaling up to 200%
- Use `sp` units for all text sizes
- Design with overflow in mind for large text

## Component Tokens

### Card Tokens

| Property | Material 3 Value | Compose Unstyled Value |
|----------|------------------|------------------------|
| Shape | extraLarge (28dp) | 12dp custom |
| Elevation | level2 (3dp) | level1 (1dp) |
| Pressed Scale | 0.95x | 0.98x |
| Background | Surface color | Surface color |

### Badge Tokens

| Property | Material 3 Value | Compose Unstyled Value |
|----------|------------------|------------------------|
| Shape | medium (16dp) | 12dp custom |
| Border Width | 0dp (filled) | 1dp (outline) |
| Fill Alpha | 1.0 (filled) | 0.0 (transparent) |
| Text Color | On Primary | Type color |

### Progress Bar Tokens

| Property | Material 3 Value | Compose Unstyled Value |
|----------|------------------|------------------------|
| Height | 8dp | 6dp |
| Shape | small (8dp) | 4dp custom |
| Animation Duration | 600ms | 400ms |
| Easing | EmphasizedDecelerate | Linear |

## Color Scheme Generation

### Material 3 Dynamic Color

The app supports dynamic color theming based on user's wallpaper (Android 12+):

```kotlin
// Automatic dynamic color (Android 12+)
val dynamicColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    DynamicColorScheme(LocalContext.current)
} else {
    // Fallback to custom theme
    CustomColorScheme()
}
```

### Light/Dark Mode

Toggle between light and dark modes:

```kotlin
val isDarkMode = isSystemInDarkTheme()
val colors = if (isDarkMode) darkColorScheme() else lightColorScheme()
```

## Reference Links

- [Material 3 Design Tokens](https://m3.material.io/foundations/design-tokens/overview)
- [Material 3 Motion](https://m3.material.io/styles/motion/easing-and-duration)
- [Material 3 Typography](https://m3.material.io/styles/typography/type-scale-tokens)
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [Android Accessibility Guidelines](https://developer.android.com/guide/topics/ui/accessibility)
