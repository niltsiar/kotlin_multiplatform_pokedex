# Icon Implementation - Knowledge Base Index

**Purpose:** Quick reference index for all icon-related documentation and learnings.

## 📚 Complete Documentation (Read These First)

1. **[material_icons_strategy.md](tech/material_icons_strategy.md)** - ⭐ PRIMARY REFERENCE
   - Material 3 v1.4.0 deprecation explanation
   - Rounded Filled style rationale
   - Icon inventory (implemented + deferred)
   - **Library resource configuration (CRITICAL)**
   - Vector Drawable XML structure
   - Implementation patterns
   - **Troubleshooting guide**
   - Testing checklist

2. **[QUICK_REFERENCE.md](QUICK_REFERENCE.md#library-resources-quick-reference)** - 🔍 QUICK LOOKUP
   - 3-step library resource setup
   - Package naming transformation (dots → underscores)
   - Usage example
   - Jump-to-section link

3. **[ICON_MIGRATION_COMPLETE.md](ICON_MIGRATION_COMPLETE.md)** - 📋 PROJECT HISTORY
   - What was done (5 commits)
   - Files changed
   - Build verification
   - Key learnings summary

## 🚀 Quick Start Guides

- **[DOWNLOAD_ICONS.md](../DOWNLOAD_ICONS.md)** - Simple step-by-step download guide
- Links to Google Fonts Material Symbols
- Rounded Filled style selection
- File naming conventions

## 🔧 Implementation Patterns (Copy-Paste Ready)

### Adding New Icons (3 Steps)

**1. Download icon:**
```bash
# Go to: https://fonts.google.com/icons
# Select: Rounded style
# Download: Android XML
# Rename: ic_[name].xml
# Save to: core/designsystem-core/src/commonMain/composeResources/drawable/
```

**2. Use in code:**
```kotlin
import multiplatformpoc.core.designsystem_core.generated.resources.Res
import multiplatformpoc.core.designsystem_core.generated.resources.ic_icon_name
import org.jetbrains.compose.resources.painterResource

Icon(
    painter = painterResource(Res.drawable.ic_icon_name),
    contentDescription = "Description",
    tint = MaterialTheme.colorScheme.onSurface
)
```

**3. Build to generate resources:**
```bash
./gradlew :core:designsystem-core:build
```

### Library Resource Setup (One-Time)

Already configured in `core:designsystem-core`, but for new library modules:

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.components.resources)  // 1. Add dependency
        }
    }
}

compose.resources {
    publicResClass = true  // 2. Enable public Res class
}

android {
    namespace = "com.minddistrict.multiplatformpoc.your.module"  // 3. Set namespace
}
```

## 🐛 Troubleshooting (Common Issues)

### Build Error: "Unresolved reference 'generated'"

**Check:**
1. ✅ `compose.components.resources` dependency added?
2. ✅ `publicResClass = true` set?
3. ✅ Using correct package name (underscores not dots)?
4. ✅ Clean build tried? (`./gradlew clean :core:designsystem-core:build`)

**See:** [material_icons_strategy.md - Troubleshooting](tech/material_icons_strategy.md#troubleshooting)

### Icons Not Rendering

**Check:**
1. ✅ Icon file in `composeResources/drawable/`?
2. ✅ File name matches import?
3. ✅ `android:fillColor` set (not transparent)?
4. ✅ `tint` parameter applied?

## 📊 Current Icon Inventory

### ✅ Implemented (Step 5)
- `ic_arrow_back.xml` - Back navigation (RTL support)
- `ic_error_outline.xml` - Error states (64dp)
- `ic_refresh.xml` - Retry buttons
- `ic_settings.xml` - Material theme nav
- `ic_info.xml` - Unstyled theme nav

### ⏳ Deferred (Step 6 - UI Redesign)
- `ic_straighten.xml` or `ic_height.xml` - Height (📏 emoji currently)
- `ic_balance.xml` - Weight (⚖️ emoji currently)
- `ic_star.xml` - Base XP (⭐ emoji currently)

## 🎯 Key Learnings (Remember These!)

1. **Library resources need explicit configuration** - 3 steps required
2. **Public Res class is NOT default** - Must set `publicResClass = true`
3. **Package naming uses underscores** - `com.a.b.c` → `a.b_c.generated.resources`
4. **Transitive dependencies work** - Feature modules get resources via design system
5. **Clean build fixes most issues** - When in doubt, clean and rebuild

## 🔗 External References

- [Compose Multiplatform Resources Docs](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html)
- [Material 3 v1.4.0 Release Notes](https://developer.android.com/jetpack/androidx/releases/compose-material3#1.4.0)
- [Google Fonts Material Symbols](https://fonts.google.com/icons)

## 🗂️ Related Documentation

- `tech/conventions.md` - Architecture patterns
- `tech/critical_patterns_quick_ref.md` - Core implementation patterns
- `UI_REDESIGN_PLAN.md` - Step 6 deferred icons TODO

---

**Last Updated:** December 30, 2025  
**Status:** Documentation complete and battle-tested ✅
