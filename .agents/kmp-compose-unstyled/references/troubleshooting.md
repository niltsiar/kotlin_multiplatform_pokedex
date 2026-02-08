# Troubleshooting: Compose Unstyled UI Issues

Common problems with unstyled component implementation.

---

## Clickable Component Not Responding

**Symptom:** Card hover/press states work, but clicking does nothing.

**Cause:** Missing `.clickable()` modifier despite having `MutableInteractionSource`.

**Solution:**
```kotlin
Column(
    modifier = modifier
        .clip(shape)
        .border(...)
        .clickable(  // ← REQUIRED for actual clicks
            interactionSource = interactionSource,
            indication = null,  // Or ripple effect
            onClick = onClick
        )
        .hoverable(interactionSource = interactionSource)  // Only tracks hover
        .padding(...)
)
```

**Why:** `hoverable()` only tracks hover state, doesn't make component clickable. Must add `.clickable()` separately.

**Order matters:**
1. `.clip()` - Define shape first
2. `.border()` - Visual border
3. `.clickable()` - Make clickable
4. `.hoverable()` - Track hover state
5. `.padding()` - Internal padding

---

## Hover Effects Too Subtle

**Symptom:** Hover state implemented but barely visible.

**Cause:** Minimal effect values (brightness 1.1, border alpha 0.2).

**Solution for Unstyled theme:**
```kotlin
val brightness by animateFloatAsState(
    targetValue = when {
        isPressed -> 0.95f
        isHovered -> 1.15f  // More noticeable (was 1.1)
        else -> 1f
    }
)

val borderAlpha by animateFloatAsState(
    targetValue = when {
        isPressed -> 0.3f
        isHovered -> 0.5f   // More prominent (was 0.2)
        else -> 0.2f
    }
)

val scale by animateFloatAsState(
    targetValue = when {
        isPressed -> 0.98f
        isHovered -> 1.02f  // Slight grow (was 1.0)
        else -> 1f
    }
)
```

**Why:** Minimal effects match "unstyled" aesthetic but need sufficient visibility for usability.

---

## Before Implementing Unstyled Components

1. **Verify compilation early:** Build immediately after scaffolding
2. **Check domain classes:** Verify constructor parameters first
3. **Copy working patterns:** Use Material components as reference
4. **Test incrementally:** Build after each component
