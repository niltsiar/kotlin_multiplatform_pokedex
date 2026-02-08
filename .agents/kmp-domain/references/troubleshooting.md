# Troubleshooting: Domain Model Issues

Common domain model constructor and parameter problems.

---

## Constructor Parameter Mismatches

**Symptom:**
```kotlin
Stat(name = "hp", value = 45)
// Error: No value parameter with type Int
```

**Cause:** Domain class has different parameters than expected.

**Solution:** Always verify domain class signatures:

```kotlin
// ✅ CORRECT domain classes (from api module)
data class Stat(
    val name: String,
    val baseStat: Int,  // NOT 'value'
    val effort: Int     // Required, not optional
)

data class TypeOfPokemon(
    val name: String,
    val slot: Int  // Required for ordering
)

data class Ability(
    val name: String,
    val isHidden: Boolean,
    val slot: Int  // Required for positioning
)
```

**Prevention:** Check `features/<feature>/api/src/commonMain/kotlin/.../domain/` for authoritative definitions.

---

## Before Implementing Features

1. **Verify compilation early:** Build immediately after scaffolding
2. **Check domain classes:** Verify constructor parameters first
3. **Copy working patterns:** Use existing implementations as reference
4. **Test incrementally:** Build after each component
