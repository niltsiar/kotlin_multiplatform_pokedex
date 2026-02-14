# Troubleshooting: Navigation Issues

Common navigation provider and routing problems.

---

## Navigation Provider "Unresolved reference" Errors

**Symptom:**
```kotlin
PokemonListScreenUnstyled(viewModel = ...)
// Error: Unresolved reference 'PokemonListScreenUnstyled'
```

**Cause:** Screen function naming convention mismatch.

**Correct Pattern:**
- ✅ `{Feature}UnstyledScreen` (e.g., `PokemonListUnstyledScreen`)
- ✅ `{Feature}MaterialScreen` (e.g., `PokemonListMaterialScreen`)
- ❌ `{Feature}ScreenUnstyled` (wrong suffix order)

**Solution:**
```kotlin
// ✅ CORRECT imports and usage
import ...ui.unstyled.PokemonListUnstyledScreen
PokemonListUnstyledScreen(viewModel = ...)

import ...ui.material.PokemonListMaterialScreen  
PokemonListMaterialScreen(viewModel = ...)
```

**Why:** Consistent naming convention: `{Adjective}{Noun}` not `{Noun}{Adjective}`.

---

## Before Navigation Debugging

1. **Verify naming convention:** {Feature}UnstyledScreen format
2. **Check both modules:** Provider imports + screen exports
3. **Test with Material first:** Verify pattern works before copying
