# UI Redesign Implementation Plan

**Created:** December 30, 2025  
**Status:** Not Started  
**Target:** Material 3 Expressive + Clean Unstyled UI with SwiftUI parity

## Overview

Transform the Pokédex app UI with Material 3 Expressive personality and minimalist elegance, using:
- Unified theme token systems with delegation
- Shared component abstractions (no duplication)
- Comprehensive unit tests (no screenshots for now)
- Complex predictive back gesture animations
- SwiftUI-native theming system
- Google Sans Flex typography for Compose
- Hand-written component documentation

## Agent Routing

**IMPORTANT**: Steps 6 and 7 (UI redesign) should be implemented by a specialized UI/UX designer.

**When to switch agents:**
- **Steps 1-5**: Standard Development Mode (this agent)
- **Step 6 & 7**: Switch to **UI/UX Design Mode** for screen implementation
  - Command: `SWITCH_TO: UI/UX Design Mode`
  - See: [docs/agent-prompts/uiux_agent_system_prompt_DELTA.md](agent-prompts/uiux_agent_system_prompt_DELTA.md)
  - Alternative: Use **Screen (Compose)** mode for implementation
  - See: [docs/agent-prompts/ui_ux_system_agent_for_generic_screen_DELTA.md](agent-prompts/ui_ux_system_agent_for_generic_screen_DELTA.md)
- **Steps 8-10**: Standard Development Mode

**Why**: UI redesign requires experienced, imaginative design expertise with deep understanding of Material 3 Expressive principles and minimalist aesthetics. The UI/UX Design agent specializes in:
- Screen layouts and visual hierarchy
- Motion design and animations
- Interaction patterns and micro-interactions
- Material Design 3 Expressive implementation
- Cross-theme consistency

## Progress Tracker

- [ ] **Step 1:** Fix NavigationProvider naming + Create core token foundation
- [ ] **Step 2:** Implement theme token systems with delegation
- [ ] **Step 3:** Configure Google Sans Flex typography
- [ ] **Step 4:** Create shared component abstraction layer
- [ ] **Step 5:** Implement motion preference + predictive back
- [ ] **Step 6:** Redesign Material screens
- [ ] **Step 7:** Redesign Unstyled screens
- [ ] **Step 8:** Create SwiftUI design system
- [ ] **Step 9:** Add comprehensive unit tests
- [ ] **Step 10:** Write component guides and documentation

---

## Step 1: Fix NavigationProvider Naming + Core Token Foundation

### Objective
Fix file naming collisions and create the foundation for unified token system.

### Tasks

#### 1.1 Rename NavigationProvider Files
- [ ] Rename `features/pokemonlist/wiring-ui-material/.../PokemonListNavigationProviders.kt` → `PokemonListMaterialNavigationProviders.kt`
- [ ] Rename `features/pokemonlist/wiring-ui-unstyled/.../PokemonListNavigationProviders.kt` → `PokemonListUnstyledNavigationProviders.kt`
- [ ] Rename `features/pokemondetail/wiring-ui-material/.../PokemonDetailNavigationProviders.kt` → `PokemonDetailMaterialNavigationProviders.kt`
- [ ] Rename `features/pokemondetail/wiring-ui-unstyled/.../PokemonDetailNavigationProviders.kt` → `PokemonDetailUnstyledNavigationProviders.kt`

#### 1.2 Update Imports
- [ ] Update `composeApp/src/commonMain/kotlin/.../App.kt` to import renamed files (module variable names stay unchanged)

#### 1.3 Create Core Token Interfaces
- [ ] Create `core/designsystem-core/src/commonMain/kotlin/.../tokens/ThemeTokens.kt`
  ```kotlin
  package com.minddistrict.multiplatformpoc.core.designsystem.core.tokens
  
  import androidx.compose.ui.unit.Dp
  import androidx.compose.ui.graphics.Shape
  import androidx.compose.animation.core.AnimationSpec
  
  interface SpacingTokens {
      val xxxs: Dp
      val xxs: Dp
      val xs: Dp
      val small: Dp
      val medium: Dp
      val large: Dp
      val xl: Dp
      val xxl: Dp
      val xxxl: Dp
  }
  
  interface ShapeTokens {
      val extraSmall: Shape
      val small: Shape
      val medium: Shape
      val large: Shape
      val extraLarge: Shape
  }
  
  interface ElevationTokens {
      val level0: Dp
      val level1: Dp
      val level2: Dp
      val level3: Dp
      val level4: Dp
      val level5: Dp
  }
  
  interface MotionTokens {
      val durationShort: Int // milliseconds
      val durationMedium: Int
      val durationLong: Int
      val easingStandard: androidx.compose.animation.core.Easing
      val easingEmphasizedDecelerate: androidx.compose.animation.core.Easing
      val easingEmphasizedAccelerate: androidx.compose.animation.core.Easing
  }
  ```

- [ ] Create `core/designsystem-core/src/commonMain/kotlin/.../tokens/BaseTokens.kt`
  ```kotlin
  package com.minddistrict.multiplatformpoc.core.designsystem.core.tokens
  
  import androidx.compose.foundation.shape.RoundedCornerShape
  import androidx.compose.ui.unit.dp
  import androidx.compose.animation.core.CubicBezierEasing
  
  object BaseTokens {
      object spacing : SpacingTokens {
          override val xxxs = 2.dp
          override val xxs = 4.dp
          override val xs = 8.dp
          override val small = 12.dp
          override val medium = 16.dp
          override val large = 20.dp
          override val xl = 24.dp
          override val xxl = 32.dp
          override val xxxl = 64.dp
      }
      
      object shapes : ShapeTokens {
          override val extraSmall = RoundedCornerShape(4.dp)
          override val small = RoundedCornerShape(8.dp)
          override val medium = RoundedCornerShape(12.dp)
          override val large = RoundedCornerShape(16.dp)
          override val extraLarge = RoundedCornerShape(20.dp)
      }
      
      object elevation : ElevationTokens {
          override val level0 = 0.dp
          override val level1 = 1.dp
          override val level2 = 3.dp
          override val level3 = 6.dp
          override val level4 = 8.dp
          override val level5 = 12.dp
      }
      
      object motion : MotionTokens {
          override val durationShort = 200
          override val durationMedium = 300
          override val durationLong = 400
          override val easingStandard = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
          override val easingEmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
          override val easingEmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
      }
  }
  ```

#### 1.4 Update Conventions Documentation
- [ ] Update `docs/tech/conventions.md` with NavigationProvider naming pattern
- [ ] Add token system architecture section to conventions

### Validation
```bash
# Verify builds successfully
./gradlew :composeApp:assembleDebug --continue

# Verify no compilation errors
./gradlew :core:designsystem-core:build
```

### Commit
```bash
git add .
git commit -m "refactor(navigation): fix NavigationProvider naming collisions

- Rename all NavigationProvider files with variant suffix
- Update imports in App.kt (module variables unchanged)
- Create core token interfaces (SpacingTokens, ShapeTokens, ElevationTokens, MotionTokens)
- Implement BaseTokens with 8dp grid and standard values
- Update conventions.md with naming pattern

Fixes file naming collisions for better IDE navigation
Establishes foundation for unified token system"
```

---

## Step 2: Implement Theme Token Systems with Delegation

### Objective
Create Material and Unstyled token systems that delegate to base tokens and replace hardcoded values.

### Tasks

#### 2.1 Create Material Tokens
- [ ] Create `core/designsystem-material/src/commonMain/kotlin/.../tokens/MaterialTokens.kt`
  ```kotlin
  package com.minddistrict.multiplatformpoc.core.designsystem.material.tokens
  
  import androidx.compose.foundation.shape.RoundedCornerShape
  import androidx.compose.ui.unit.dp
  import com.minddistrict.multiplatformpoc.core.designsystem.core.tokens.*
  
  object MaterialTokens {
      // Delegate spacing to base (8dp grid)
      val spacing: SpacingTokens = BaseTokens.spacing
      
      // Override shapes with Material 3 Expressive values
      object shapes : ShapeTokens {
          override val extraSmall = RoundedCornerShape(8.dp)
          override val small = RoundedCornerShape(12.dp)
          override val medium = RoundedCornerShape(16.dp)
          override val large = RoundedCornerShape(24.dp)
          override val extraLarge = RoundedCornerShape(28.dp)  // Expressive!
      }
      
      // Material 3 tonal elevation
      object elevation : ElevationTokens {
          override val level0 = 0.dp
          override val level1 = 1.dp
          override val level2 = 3.dp
          override val level3 = 6.dp
          override val level4 = 8.dp
          override val level5 = 12.dp
      }
      
      // Material 3 Expressive motion
      object motion : MotionTokens {
          override val durationShort = 200
          override val durationMedium = 300
          override val durationLong = 400
          override val easingStandard = BaseTokens.motion.easingStandard
          override val easingEmphasizedDecelerate = BaseTokens.motion.easingEmphasizedDecelerate
          override val easingEmphasizedAccelerate = BaseTokens.motion.easingEmphasizedAccelerate
      }
  }
  ```

#### 2.2 Create Unstyled Tokens
- [ ] Create `core/designsystem-unstyled/src/commonMain/kotlin/.../tokens/UnstyledTokens.kt`
  ```kotlin
  package com.minddistrict.multiplatformpoc.core.designsystem.unstyled.tokens
  
  import androidx.compose.foundation.shape.RoundedCornerShape
  import androidx.compose.ui.unit.dp
  import com.minddistrict.multiplatformpoc.core.designsystem.core.tokens.*
  
  object UnstyledTokens {
      // Delegate spacing to base (8dp grid)
      val spacing: SpacingTokens = BaseTokens.spacing
      
      // Minimal shapes (max 12dp)
      object shapes : ShapeTokens {
          override val extraSmall = RoundedCornerShape(4.dp)
          override val small = RoundedCornerShape(6.dp)
          override val medium = RoundedCornerShape(8.dp)
          override val large = RoundedCornerShape(10.dp)
          override val extraLarge = RoundedCornerShape(12.dp)  // Minimal!
      }
      
      // Flat elevations (max 4dp)
      object elevation : ElevationTokens {
          override val level0 = 0.dp
          override val level1 = 1.dp
          override val level2 = 2.dp
          override val level3 = 3.dp
          override val level4 = 4.dp
          override val level5 = 4.dp  // Cap at 4dp
      }
      
      // Linear/standard motion only
      object motion : MotionTokens {
          override val durationShort = 200
          override val durationMedium = 300
          override val durationLong = 300  // Same as medium (minimal)
          override val easingStandard = BaseTokens.motion.easingStandard
          override val easingEmphasizedDecelerate = BaseTokens.motion.easingStandard  // Use standard
          override val easingEmphasizedAccelerate = BaseTokens.motion.easingStandard  // Use standard
      }
  }
  ```

#### 2.3 Replace Hardcoded Values in Material Screens
- [ ] Refactor `features/pokemonlist/ui-material/.../PokemonListScreenMaterial.kt`
  - Replace `16.dp` → `MaterialTokens.spacing.medium`
  - Replace `8.dp` → `MaterialTokens.spacing.small`
  - Replace `20.dp` → `MaterialTokens.spacing.large`

- [ ] Refactor `features/pokemondetail/ui-material/.../PokemonDetailScreenMaterial.kt`
  - Apply token-based spacing throughout
  - Replace hardcoded corner radii with `MaterialTokens.shapes.*`

### Validation
```bash
# Verify Material tokens build
./gradlew :core:designsystem-material:build

# Verify Unstyled tokens build
./gradlew :core:designsystem-unstyled:build

# Verify app builds with token usage
./gradlew :composeApp:assembleDebug
```

### Commit
```bash
git add .
git commit -m "feat(tokens): implement Material and Unstyled token systems

- Create MaterialTokens with expressive shapes (28dp) and emphasized motion
- Create UnstyledTokens with minimal shapes (12dp max) and linear motion
- Both delegate spacing to BaseTokens (8dp grid)
- Replace hardcoded spacing in Material screens with token references
- Material uses tonal elevation, Unstyled uses flat shadows

Token delegation eliminates duplication while allowing customization"
```

---

## Step 3: Configure Google Sans Flex Typography

### Objective
Set up Google Sans Flex variable font for Android/Desktop and San Francisco for iOS.

### Tasks

#### 3.1 Add Font Resources
- [ ] Download Google Sans Flex variable font from Google Fonts
- [ ] Add `google_sans_flex_variable.ttf` to `core/designsystem-material/src/commonMain/composeResources/font/`

#### 3.2 Create Platform Font Abstraction
- [ ] Create `core/designsystem-material/src/commonMain/kotlin/.../typography/PlatformFontFamily.kt`
  ```kotlin
  package com.minddistrict.multiplatformpoc.core.designsystem.material.typography
  
  import androidx.compose.ui.text.font.FontFamily
  
  expect fun googleSansFlex(): FontFamily
  ```

- [ ] Create `core/designsystem-material/src/androidMain/kotlin/.../typography/PlatformFontFamily.android.kt`
  ```kotlin
  package com.minddistrict.multiplatformpoc.core.designsystem.material.typography
  
  import androidx.compose.ui.text.font.Font
  import androidx.compose.ui.text.font.FontFamily
  import com.minddistrict.multiplatformpoc.core.designsystem.material.Res
  
  actual fun googleSansFlex(): FontFamily = FontFamily(
      Font(Res.font.google_sans_flex_variable)
  )
  ```

- [ ] Create `core/designsystem-material/src/jvmMain/kotlin/.../typography/PlatformFontFamily.jvm.kt` (same as Android)

- [ ] Create `core/designsystem-material/src/iosMain/kotlin/.../typography/PlatformFontFamily.ios.kt`
  ```kotlin
  package com.minddistrict.multiplatformpoc.core.designsystem.material.typography
  
  import androidx.compose.ui.text.font.FontFamily
  
  actual fun googleSansFlex(): FontFamily = FontFamily.Default  // San Francisco
  ```

#### 3.3 Update Material Typography
- [ ] Update `core/designsystem-material/src/commonMain/kotlin/.../MaterialTypography.kt`
  - Import `googleSansFlex()`
  - Apply to all text styles in Material 3 Expressive scale
  - Verify scale: Display (57/45/36sp), Headline (32/28/24sp), Title (22/16/14sp), Body (16/14/12sp), Label (14/12/11sp)

### Validation
```bash
# Build Material design system
./gradlew :core:designsystem-material:build

# Run Android app to verify font rendering
./gradlew :composeApp:assembleDebug

# Run Desktop app to verify font rendering
./gradlew :composeApp:run
```

### Commit
```bash
git add .
git commit -m "feat(typography): configure Google Sans Flex with platform fonts

- Add Google Sans Flex variable font resource for Compose
- Create expect/actual PlatformFontFamily abstraction
- Android/Desktop use Google Sans Flex
- iOS uses San Francisco (native)
- Update MaterialTypography with Material 3 Expressive scale
- Verify typography scale matches spec (Display 57sp → Label 11sp)

Provides consistent Compose typography with native iOS fonts"
```

---

## Step 4: Create Shared Component Abstraction Layer

### Objective
Build theme-agnostic components that accept token interfaces, eliminating duplication between Material and Unstyled.

### Tasks

#### 4.1 Create Component Token Interfaces
- [ ] Create `core/designsystem-core/src/commonMain/kotlin/.../components/ComponentTokens.kt`
  ```kotlin
  package com.minddistrict.multiplatformpoc.core.designsystem.core.components
  
  import androidx.compose.ui.graphics.Color
  import androidx.compose.ui.graphics.Shape
  import androidx.compose.ui.unit.Dp
  import androidx.compose.animation.core.AnimationSpec
  
  interface CardTokens {
      val shape: Shape
      val elevation: Dp
      val backgroundColor: Color
      val contentColor: Color
      val pressedScale: Float
  }
  
  interface BadgeTokens {
      val shape: Shape
      val borderWidth: Dp
      val fillAlpha: Float  // 0f = outline only, 1f = filled
      val textColor: Color
  }
  
  interface ProgressBarTokens {
      val height: Dp
      val shape: Shape
      val backgroundColor: Color
      val foregroundColor: Color
      val animationSpec: AnimationSpec<Float>
  }
  ```

#### 4.2 Create Shared PokemonCard Component
- [ ] Create `core/designsystem-core/src/commonMain/kotlin/.../components/PokemonCard.kt`
  ```kotlin
  package com.minddistrict.multiplatformpoc.core.designsystem.core.components
  
  import androidx.compose.foundation.clickable
  import androidx.compose.foundation.layout.Box
  import androidx.compose.foundation.shape.RoundedCornerShape
  import androidx.compose.runtime.*
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.draw.clip
  import androidx.compose.ui.draw.shadow
  import androidx.compose.ui.graphics.Shape
  import androidx.compose.ui.unit.Dp
  
  @Composable
  fun PokemonCard(
      tokens: CardTokens,
      onClick: () -> Unit,
      modifier: Modifier = Modifier,
      overrideShape: Shape? = null,
      overrideElevation: Dp? = null,
      content: @Composable () -> Unit
  ) {
      val shape = overrideShape ?: tokens.shape
      val elevation = overrideElevation ?: tokens.elevation
      
      Box(
          modifier = modifier
              .shadow(elevation = elevation, shape = shape)
              .clip(shape)
              .background(tokens.backgroundColor)
              .clickable(onClick = onClick)
      ) {
          content()
      }
  }
  ```

#### 4.3 Create Shared TypeBadge Component
- [ ] Create `core/designsystem-core/src/commonMain/kotlin/.../components/TypeBadge.kt`
  ```kotlin
  package com.minddistrict.multiplatformpoc.core.designsystem.core.components
  
  import androidx.compose.foundation.background
  import androidx.compose.foundation.border
  import androidx.compose.foundation.layout.padding
  import androidx.compose.material3.Text
  import androidx.compose.runtime.Composable
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.draw.clip
  import androidx.compose.ui.unit.Dp
  import com.minddistrict.multiplatformpoc.core.designsystem.core.PokemonType
  import com.minddistrict.multiplatformpoc.core.designsystem.core.PokemonTypeColors
  
  @Composable
  fun TypeBadge(
      type: PokemonType,
      tokens: BadgeTokens,
      modifier: Modifier = Modifier,
      overrideBorderWidth: Dp? = null
  ) {
      val typeColor = PokemonTypeColors.getColorForType(type)
      val borderWidth = overrideBorderWidth ?: tokens.borderWidth
      
      Text(
          text = type.name.lowercase().replaceFirstChar { it.uppercase() },
          color = tokens.textColor,
          modifier = modifier
              .clip(tokens.shape)
              .background(typeColor.copy(alpha = tokens.fillAlpha))
              .border(borderWidth, typeColor, tokens.shape)
              .padding(horizontal = 16.dp, vertical = 8.dp)
      )
  }
  ```

#### 4.4 Create Shared AnimatedStatBar Component
- [ ] Create `core/designsystem-core/src/commonMain/kotlin/.../components/AnimatedStatBar.kt`
  ```kotlin
  package com.minddistrict.multiplatformpoc.core.designsystem.core.components
  
  import androidx.compose.animation.core.animateFloatAsState
  import androidx.compose.foundation.background
  import androidx.compose.foundation.layout.*
  import androidx.compose.runtime.*
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.draw.clip
  
  @Composable
  fun AnimatedStatBar(
      value: Int,
      maxValue: Int = 255,
      tokens: ProgressBarTokens,
      reducedMotion: Boolean,
      modifier: Modifier = Modifier
  ) {
      val progress = (value.toFloat() / maxValue).coerceIn(0f, 1f)
      
      val animatedProgress by animateFloatAsState(
          targetValue = if (reducedMotion) progress else progress,
          animationSpec = if (reducedMotion) snap() else tokens.animationSpec
      )
      
      Box(
          modifier = modifier
              .height(tokens.height)
              .fillMaxWidth()
              .clip(tokens.shape)
              .background(tokens.backgroundColor)
      ) {
          Box(
              modifier = Modifier
                  .fillMaxHeight()
                  .fillMaxWidth(animatedProgress)
                  .background(tokens.foregroundColor)
          )
      }
  }
  ```

#### 4.5 Implement Material Component Tokens
- [ ] Create `core/designsystem-material/src/commonMain/kotlin/.../tokens/MaterialComponentTokens.kt`
  ```kotlin
  package com.minddistrict.multiplatformpoc.core.designsystem.material.tokens
  
  import androidx.compose.material3.MaterialTheme
  import androidx.compose.runtime.Composable
  import androidx.compose.ui.graphics.Color
  import androidx.compose.ui.unit.dp
  import com.minddistrict.multiplatformpoc.core.designsystem.core.components.*
  
  object MaterialComponentTokens {
      val card: @Composable () -> CardTokens = {
          object : CardTokens {
              override val shape = MaterialTokens.shapes.extraLarge
              override val elevation = MaterialTokens.elevation.level2
              override val backgroundColor = MaterialTheme.colorScheme.surface
              override val contentColor = MaterialTheme.colorScheme.onSurface
              override val pressedScale = 0.97f
          }
      }
      
      val badge: @Composable () -> BadgeTokens = {
          object : BadgeTokens {
              override val shape = MaterialTokens.shapes.large  // Pill shape
              override val borderWidth = 0.dp  // No border (filled)
              override val fillAlpha = 1f  // Fully filled
              override val textColor = Color.White
          }
      }
      
      val progressBar: @Composable () -> ProgressBarTokens = {
          object : ProgressBarTokens {
              override val height = 8.dp
              override val shape = MaterialTokens.shapes.small
              override val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
              override val foregroundColor = MaterialTheme.colorScheme.primary
              override val animationSpec = tween(
                  durationMillis = MaterialTokens.motion.durationLong,
                  easing = MaterialTokens.motion.easingEmphasizedDecelerate
              )
          }
      }
  }
  ```

#### 4.6 Implement Unstyled Component Tokens
- [ ] Create `core/designsystem-unstyled/src/commonMain/kotlin/.../tokens/UnstyledComponentTokens.kt`
  ```kotlin
  package com.minddistrict.multiplatformpoc.core.designsystem.unstyled.tokens
  
  import androidx.compose.runtime.Composable
  import androidx.compose.ui.graphics.Color
  import androidx.compose.ui.unit.dp
  import com.minddistrict.multiplatformpoc.core.designsystem.core.components.*
  
  object UnstyledComponentTokens {
      val card: @Composable () -> CardTokens = {
          object : CardTokens {
              override val shape = UnstyledTokens.shapes.medium
              override val elevation = UnstyledTokens.elevation.level1  // Minimal
              override val backgroundColor = Theme[colors][surfaceContainer]
              override val contentColor = Theme[colors][onSurface]
              override val pressedScale = 0.98f  // Subtle
          }
      }
      
      val badge: @Composable () -> BadgeTokens = {
          object : BadgeTokens {
              override val shape = UnstyledTokens.shapes.large
              override val borderWidth = 2.dp  // Border only
              override val fillAlpha = 0f  // No fill (outline)
              override val textColor = Theme[colors][onSurface]
          }
      }
      
      val progressBar: @Composable () -> ProgressBarTokens = {
          object : ProgressBarTokens {
              override val height = 6.dp  // Thinner
              override val shape = UnstyledTokens.shapes.small
              override val backgroundColor = Theme[colors][outline].copy(alpha = 0.2f)
              override val foregroundColor = Theme[colors][primary]
              override val animationSpec = tween(
                  durationMillis = UnstyledTokens.motion.durationMedium,
                  easing = UnstyledTokens.motion.easingStandard  // Linear
              )
          }
      }
  }
  ```

### Validation
```bash
# Build core components
./gradlew :core:designsystem-core:build

# Build Material tokens
./gradlew :core:designsystem-material:build

# Build Unstyled tokens
./gradlew :core:designsystem-unstyled:build

# Verify no compilation errors
./gradlew :composeApp:assembleDebug
```

### Commit
```bash
git add .
git commit -m "feat(components): create shared component abstraction layer

- Define ComponentTokens interfaces (CardTokens, BadgeTokens, ProgressBarTokens)
- Implement theme-agnostic PokemonCard with optional overrides
- Implement shared TypeBadge using PokemonTypeColors
- Implement AnimatedStatBar with reduced motion support
- Create MaterialComponentTokens (filled cards, badges with elevation)
- Create UnstyledComponentTokens (border-only, flat, minimal)

Eliminates duplication: Material and Unstyled use same components with different tokens"
```

---

## Step 5: Implement Motion Preference + Predictive Back

### Objective
Add system motion preference detection and complex predictive back gesture animations.

### Tasks

#### 5.1 Create Motion Preference Detection
- [ ] Create `core/designsystem-core/src/commonMain/kotlin/.../motion/MotionPreference.kt`
  ```kotlin
  package com.minddistrict.multiplatformpoc.core.designsystem.core.motion
  
  import androidx.compose.runtime.Composable
  import androidx.compose.runtime.remember
  
  expect fun isReduceMotionEnabled(): Boolean
  
  @Composable
  fun rememberReducedMotion(): Boolean = remember { isReduceMotionEnabled() }
  ```

- [ ] Implement Android actual in `androidMain`
  ```kotlin
  // Read Settings.Global.TRANSITION_ANIMATION_SCALE via AccessibilityManager
  ```

- [ ] Implement iOS actual in `iosMain`
  ```kotlin
  // Bridge to UIAccessibility.isReduceMotionEnabled via Objective-C
  ```

- [ ] Implement JVM actual in `jvmMain`
  ```kotlin
  actual fun isReduceMotionEnabled(): Boolean = false  // No standard API
  ```

#### 5.2 Create Predictive Back Handler
- [ ] Create `core/navigation/src/commonMain/kotlin/.../PredictiveBackHandler.kt`
  ```kotlin
  package com.minddistrict.multiplatformpoc.core.navigation
  
  import androidx.activity.compose.BackHandler
  import androidx.compose.runtime.*
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.graphics.graphicsLayer
  import androidx.compose.ui.unit.dp
  
  sealed class PredictiveBackState {
      object Idle : PredictiveBackState()
      data class Dragging(val progress: Float) : PredictiveBackState()
      object Settling : PredictiveBackState()
      object Completed : PredictiveBackState()
  }
  
  @Composable
  fun PredictiveBackHandler(
      onBack: () -> Unit,
      content: @Composable (Modifier) -> Unit
  ) {
      var backState by remember { mutableStateOf<PredictiveBackState>(PredictiveBackState.Idle) }
      
      BackHandler(enabled = true) {
          // Track gesture progress
          // Calculate transform
          onBack()
      }
      
      val transform = when (val state = backState) {
          is PredictiveBackState.Dragging -> {
              val scale = 1.0f - state.progress * 0.1f
              val translationX = state.progress * 48f
              Modifier.graphicsLayer {
                  scaleX = scale
                  scaleY = scale
                  this.translationX = translationX
              }
          }
          else -> Modifier
      }
      
      content(transform)
  }
  ```

#### 5.3 Create Shared Element Transition Specs
- [ ] Create `core/navigation/src/commonMain/kotlin/.../SharedElementTransition.kt`
  ```kotlin
  package com.minddistrict.multiplatformpoc.core.navigation
  
  import androidx.compose.animation.*
  import androidx.compose.animation.core.tween
  import com.minddistrict.multiplatformpoc.core.designsystem.core.tokens.BaseTokens
  
  object SharedElementTransition {
      val enterTransition: EnterTransition = 
          slideInHorizontally(
              initialOffsetX = { it },
              animationSpec = tween(
                  durationMillis = 400,
                  easing = BaseTokens.motion.easingEmphasizedDecelerate
              )
          ) + fadeIn(animationSpec = tween(400)) + 
          scaleIn(initialScale = 0.9f, animationSpec = tween(400))
      
      val exitTransition: ExitTransition = 
          slideOutHorizontally(
              targetOffsetX = { it },
              animationSpec = tween(
                  durationMillis = 200,
                  easing = BaseTokens.motion.easingEmphasizedAccelerate
              )
          ) + fadeOut(animationSpec = tween(200)) + 
          scaleOut(targetScale = 0.9f, animationSpec = tween(200))
  }
  ```

#### 5.4 Update Detail Navigation Providers
- [ ] Update `PokemonDetailMaterialNavigationProviders.kt`:
  - Wrap screen in `PredictiveBackHandler`
  - Add Navigation 3 metadata with `SharedElementTransition`

- [ ] Update `PokemonDetailUnstyledNavigationProviders.kt`:
  - Apply same predictive back behavior

### Validation
```bash
# Build navigation module
./gradlew :core:navigation:build

# Test on Android device/emulator
./gradlew :composeApp:installDebug
# Verify back gesture shows scale/translate animation

# Test reduced motion
# Enable "Remove animations" in Android accessibility settings
# Verify animations are skipped
```

### Commit
```bash
git add .
git commit -m "feat(navigation): implement predictive back gesture with motion preference

- Create MotionPreference with expect/actual for platform detection
- Implement PredictiveBackHandler tracking swipe progress
- Calculate scale (1.0→0.9) and translation (0→48dp) transforms
- Create SharedElementTransition with emphasized timing
- Integrate with Navigation 3 metadata
- Honor reduced motion system preference
- Apply to Material and Unstyled detail screens

Provides Android 13+ predictive back gesture with accessibility support"
```

---

## Step 6: Redesign Material Screens

### ⚠️ Agent Switch Required
**SWITCH_TO: UI/UX Design Mode** (or Screen Implementation Mode)

This step requires a specialized UI/UX designer with expertise in:
- Material 3 Expressive design language
- Animation choreography and micro-interactions
- Visual hierarchy and composition
- Adaptive layout patterns

See [agent routing](#agent-routing) section above.

### Objective
Break Material screens into small focused components using shared abstractions and tokens.

### Tasks

#### 6.1 Create Pokemon List Material Components
- [ ] Create `features/pokemonlist/ui-material/src/.../components/PokemonListCard.kt`
  - Uses shared `PokemonCard` with `MaterialComponentTokens.card`
  - Adds staggered entrance animation (fadeIn + slideInVertically)
  - Delay: `(index * 50).milliseconds`

- [ ] Create `features/pokemonlist/ui-material/src/.../components/PokemonListGrid.kt`
  - `LazyVerticalGrid` with adaptive columns (2/3/4)
  - Spacing: `MaterialTokens.spacing.medium`

- [ ] Create `features/pokemonlist/ui-material/src/.../components/LoadingState.kt`
  - `CircularProgressIndicator` with Material colors

- [ ] Create `features/pokemonlist/ui-material/src/.../components/ErrorState.kt`
  - Error message + Material `Button` for retry

- [ ] Rename `PokemonListScreenMaterial.kt` → `PokemonListMaterialScreen.kt`
  - Compose above components
  - Replace all hardcoded spacing with tokens

- [ ] Create `PokemonListMaterialScreenPreviews.kt`
  - `@Preview` for loading state
  - `@Preview` for content (8 Pokémon)
  - `@Preview` for error state

#### 6.2 Create Pokemon Detail Material Components
- [ ] Create `features/pokemondetail/ui-material/src/.../components/HeroSection.kt`
  - 256dp `AsyncImage` with parallax
  - Scale calculation: `1.0f + (scrollOffset / 1000f).coerceIn(-0.1f, 0.1f)`
  - Name/ID overlay with scrim gradient

- [ ] Create `features/pokemondetail/ui-material/src/.../components/TypeBadgeRow.kt`
  - Uses shared `TypeBadge` with `MaterialComponentTokens.badge`
  - Animated entrance: `slideInHorizontally + fadeIn`
  - Delay: `(index * 100).milliseconds`

- [ ] Create `features/pokemondetail/ui-material/src/.../components/PhysicalAttributesCard.kt`
  - Uses shared `PokemonCard`
  - Height/Weight/Base XP with icons

- [ ] Create `features/pokemondetail/ui-material/src/.../components/AbilitiesSection.kt`
  - List with "Hidden" Material chip

- [ ] Create `features/pokemondetail/ui-material/src/.../components/BaseStatsSection.kt`
  - Uses shared `AnimatedStatBar` with `MaterialComponentTokens.progressBar`
  - 50ms stagger per stat

- [ ] Rename `PokemonDetailScreenMaterial.kt` → `PokemonDetailMaterialScreen.kt`
  - Compose components in `LazyColumn`
  - Apply token-based spacing

- [ ] Create `PokemonDetailMaterialScreenPreviews.kt`
  - `@Preview` for loading state
  - `@Preview` for content (Bulbasaur)
  - `@Preview` for error state

### Validation
```bash
# Build Material UI modules
./gradlew :features:pokemonlist:ui-material:build
./gradlew :features:pokemondetail:ui-material:build

# Run app and verify Material theme
./gradlew :composeApp:run
# Switch to Material theme, navigate list→detail
# Verify animations, spacing, typography

# Check previews work
# Open *Previews.kt files in Android Studio
# Verify preview rendering
```

### Commit
```bash
git add .
git commit -m "feat(ui): redesign Material screens with component library

Pokemon List Material:
- Extract PokemonListCard with shared PokemonCard + staggered animation
- Extract PokemonListGrid with adaptive columns
- Extract LoadingState and ErrorState components
- Rename to PokemonListMaterialScreen.kt
- Add comprehensive @Preview functions

Pokemon Detail Material:
- Extract HeroSection with parallax effect
- Extract TypeBadgeRow with animated entrance
- Extract PhysicalAttributesCard, AbilitiesSection, BaseStatsSection
- Use shared AnimatedStatBar with 50ms stagger
- Rename to PokemonDetailMaterialScreen.kt
- Add loading/content/error @Preview functions

All components use MaterialTokens and shared abstractions"
```

---

## Step 7: Redesign Unstyled Screens

### ⚠️ Agent Switch Required
**SWITCH_TO: UI/UX Design Mode** (or Screen Implementation Mode)

This step requires a specialized UI/UX designer with expertise in:
- Minimalist design principles
- Subtle interaction patterns
- Clean visual hierarchy
- Border-only aesthetics

See [agent routing](#agent-routing) section above.

### Objective
Break Unstyled screens into small focused components using shared abstractions and minimalist tokens.

### Tasks

#### 7.1 Create Pokemon List Unstyled Components
- [ ] Create `features/pokemonlist/ui-unstyled/src/.../components/PokemonListCardUnstyled.kt`
  - Uses shared `PokemonCard` with `UnstyledComponentTokens.card`
  - Hover state: brightness 1.1f

- [ ] Create `features/pokemonlist/ui-unstyled/src/.../components/PokemonListGridUnstyled.kt`
  - Adaptive grid with `UnstyledTokens.spacing.large` (20dp)

- [ ] Create `features/pokemonlist/ui-unstyled/src/.../components/LoadingStateUnstyled.kt`
  - Minimal with Compose Unstyled `ProgressIndicator`

- [ ] Create `features/pokemonlist/ui-unstyled/src/.../components/ErrorStateUnstyled.kt`
  - Clean with Compose Unstyled `Button`

- [ ] Rename `PokemonListScreenUnstyled.kt` → `PokemonListUnstyledScreen.kt`
  - Compose components

- [ ] Create `PokemonListUnstyledScreenPreviews.kt`
  - `@Preview` for loading/content/error

#### 7.2 Create Pokemon Detail Unstyled Components
- [ ] Create `features/pokemondetail/ui-unstyled/src/.../components/HeroSectionUnstyled.kt`
  - 256dp image, no parallax
  - `UnstyledTokens.spacing.large` (24dp) between elements

- [ ] Create `features/pokemondetail/ui-unstyled/src/.../components/TypeBadgeRowUnstyled.kt`
  - Border-only badges via `UnstyledComponentTokens.badge`

- [ ] Create `features/pokemondetail/ui-unstyled/src/.../components/PhysicalAttributesCardUnstyled.kt`
  - Flat cards using `UnstyledComponentTokens.card`

- [ ] Create `features/pokemondetail/ui-unstyled/src/.../components/AbilitiesSectionUnstyled.kt`
  - Simple list, minimal styling

- [ ] Create `features/pokemondetail/ui-unstyled/src/.../components/BaseStatsSectionUnstyled.kt`
  - Monochrome stat bars via `UnstyledComponentTokens.progressBar`

- [ ] Rename `PokemonDetailScreenUnstyled.kt` → `PokemonDetailUnstyledScreen.kt`

- [ ] Create `PokemonDetailUnstyledScreenPreviews.kt`
  - `@Preview` for loading/content/error

### Validation
```bash
# Build Unstyled UI modules
./gradlew :features:pokemonlist:ui-unstyled:build
./gradlew :features:pokemondetail:ui-unstyled:build

# Run app and verify Unstyled theme
./gradlew :composeApp:run
# Switch to Unstyled theme, navigate list→detail
# Verify border-only cards, flat styling, minimal motion

# Check previews
# Verify @Preview rendering in IDE
```

### Commit
```bash
git add .
git commit -m "feat(ui): redesign Unstyled screens with component library

Pokemon List Unstyled:
- Extract PokemonListCardUnstyled with hover state
- Extract PokemonListGridUnstyled with generous 20dp spacing
- Extract LoadingStateUnstyled and ErrorStateUnstyled
- Rename to PokemonListUnstyledScreen.kt
- Add @Preview functions

Pokemon Detail Unstyled:
- Extract HeroSectionUnstyled without parallax
- Extract TypeBadgeRowUnstyled with border-only badges
- Extract PhysicalAttributesCardUnstyled, AbilitiesSectionUnstyled
- Extract BaseStatsSectionUnstyled with monochrome bars
- Rename to PokemonDetailUnstyledScreen.kt
- Add loading/content/error @Preview functions

All components use UnstyledTokens and shared abstractions
Achieves minimalist aesthetic with flat cards and subtle interactions"
```

---

## Step 8: Create SwiftUI Design System

### Objective
Build SwiftUI-native theming system with shared design tokens for easy future customization.

### Tasks

#### 8.1 Create SwiftUI Theme Structure
- [ ] Create `iosApp/iosApp/DesignSystem/PokemonTheme.swift`
  ```swift
  import SwiftUI
  
  struct PokemonTheme {
      var spacing: Spacing
      var shapes: Shapes
      var typography: Typography
      var colors: Colors
      
      static let `default` = PokemonTheme(
          spacing: .standard,
          shapes: .rounded,
          typography: .system,
          colors: .light
      )
  }
  
  // Environment key
  private struct PokemonThemeKey: EnvironmentKey {
      static let defaultValue = PokemonTheme.default
  }
  
  extension EnvironmentValues {
      var pokemonTheme: PokemonTheme {
          get { self[PokemonThemeKey.self] }
          set { self[PokemonThemeKey.self] = newValue }
      }
  }
  ```

#### 8.2 Port Shared Design Tokens
- [ ] Create `iosApp/iosApp/DesignSystem/PokemonTypeColors.swift`
  ```swift
  import SwiftUI
  
  enum PokemonType: String, CaseIterable {
      case fire, water, grass, electric, ice, fighting, poison, ground
      case flying, psychic, bug, rock, ghost, dragon, dark, steel, fairy, normal
      
      var color: Color {
          switch self {
          case .fire: return Color(red: 0.937, green: 0.267, blue: 0.133)  // #EF4423
          case .water: return Color(red: 0.200, green: 0.600, blue: 1.0)  // #3399FF
          // ... port all 18 type colors from PokemonTypeColors.kt
          }
      }
  }
  ```

- [ ] Create `iosApp/iosApp/DesignSystem/Spacing.swift`
  ```swift
  import SwiftUI
  
  extension PokemonTheme {
      struct Spacing {
          let xxxs: CGFloat = 2
          let xxs: CGFloat = 4
          let xs: CGFloat = 8
          let small: CGFloat = 12
          let medium: CGFloat = 16
          let large: CGFloat = 20
          let xl: CGFloat = 24
          let xxl: CGFloat = 32
          let xxxl: CGFloat = 64
          
          static let standard = Spacing()
      }
  }
  ```

- [ ] Create `iosApp/iosApp/DesignSystem/Shapes.swift`
  ```swift
  import SwiftUI
  
  extension PokemonTheme {
      struct Shapes {
          let extraSmall: CGFloat = 8
          let small: CGFloat = 12
          let medium: CGFloat = 16
          let large: CGFloat = 24
          let extraLarge: CGFloat = 28
          
          static let rounded = Shapes()
      }
  }
  ```

- [ ] Create `iosApp/iosApp/DesignSystem/Typography.swift`
  ```swift
  import SwiftUI
  
  extension PokemonTheme {
      struct Typography {
          let display: Font = .system(size: 57, weight: .bold)
          let headline: Font = .system(size: 32, weight: .semibold)
          let title: Font = .system(size: 22, weight: .medium)
          let body: Font = .system(size: 16, weight: .regular)
          let label: Font = .system(size: 14, weight: .regular)
          
          static let system = Typography()
      }
  }
  ```

#### 8.3 Create SwiftUI Reusable Components
- [ ] Create `iosApp/iosApp/Components/PokemonCardView.swift`
  ```swift
  import SwiftUI
  
  struct PokemonCardView<Content: View>: View {
      @Environment(\.pokemonTheme) var theme
      let content: Content
      let action: () -> Void
      
      init(action: @escaping () -> Void, @ViewBuilder content: () -> Content) {
          self.action = action
          self.content = content()
      }
      
      var body: some View {
          Button(action: action) {
              content
                  .frame(maxWidth: .infinity)
                  .background(Color(.secondarySystemBackground))
                  .cornerRadius(theme.shapes.extraLarge)
                  .shadow(radius: 2)
          }
          .buttonStyle(.plain)
      }
  }
  ```

- [ ] Create `iosApp/iosApp/Components/TypeBadgeView.swift`
  ```swift
  import SwiftUI
  
  struct TypeBadgeView: View {
      @Environment(\.pokemonTheme) var theme
      let type: PokemonType
      
      var body: some View {
          Text(type.rawValue.capitalized)
              .font(theme.typography.label)
              .foregroundColor(.white)
              .padding(.horizontal, theme.spacing.medium)
              .padding(.vertical, theme.spacing.xs)
              .background(type.color)
              .cornerRadius(theme.shapes.large)
      }
  }
  ```

#### 8.4 Update SwiftUI Views to Use Theme System
- [ ] Update `iosApp/iosApp/Views/PokemonListView.swift`
  - Use `@Environment(\.pokemonTheme)` for tokens
  - Use `PokemonCardView` component
  - Apply `theme.spacing.*` for padding
  - Use `theme.shapes.extraLarge` for corners
  - Spring animation: `.spring(response: 0.3, dampingFraction: 0.7)`

- [ ] Update `iosApp/iosApp/Views/PokemonDetailView.swift`
  - Use theme tokens for spacing/shapes
  - Use `TypeBadgeView` component
  - Apply `matchedGeometryEffect` for card→detail transition
  - Match hero height to Compose (256pt)

- [ ] Update `iosApp/iosApp/ContentView.swift`
  - Inject theme: `.environment(\.pokemonTheme, PokemonTheme.default)`

### Validation
```bash
# Open iOS project
open iosApp/iosApp.xcodeproj

# Build and run on simulator
# Verify:
# - Type badge colors match Compose
# - Spacing matches Compose (8pt grid)
# - Card corners match (28pt)
# - Native animations (spring, matchedGeometry)
# - Theme injection works
```

### Commit
```bash
git add .
git commit -m "feat(ios): create SwiftUI-native design system with shared tokens

- Create PokemonTheme with Environment-based injection
- Port PokemonTypeColors from Kotlin (18 types, WCAG AA)
- Port Spacing tokens (8pt grid: xxxs 2pt → xxxl 64pt)
- Port Shapes tokens (extraLarge 28pt matches Compose)
- Create Typography with system fonts (San Francisco)
- Build PokemonCardView with matchedGeometryEffect support
- Build TypeBadgeView using shared type colors
- Update PokemonListView and PokemonDetailView to use theme
- Inject PokemonTheme.default in app root

Provides SwiftUI-native theming while maintaining design consistency with Compose
Enables easy future theme customization via Environment"
```

---

## Step 9: Add Comprehensive Unit Tests

### Objective
Achieve 30-40% property test coverage for all new components, tokens, and navigation logic.

### Tasks

#### 9.1 Create Component Tests
- [ ] Create `core/designsystem-core/src/androidUnitTest/.../PokemonCardTest.kt`
  - Verify token application (shape, elevation, colors)
  - Test override parameters work
  - Test reduced motion disables animation
  - Property test: `checkAll(Arb.int(1..10000))` for IDs

- [ ] Create `core/designsystem-core/src/androidUnitTest/.../TypeBadgeTest.kt`
  - Verify color mapping from `PokemonTypeColors`
  - Test Material vs Unstyled rendering differs
  - Property test: `checkAll(Arb.pokemonType())` for all types

- [ ] Create `core/designsystem-core/src/androidUnitTest/.../AnimatedStatBarTest.kt`
  - Verify progress clamping (0-255)
  - Test animation spec applied
  - Test reduced motion handling
  - Property test: `checkAll(Arb.int(0..255))` for stat values

#### 9.2 Create Token Tests
- [ ] Create `core/designsystem-material/src/androidUnitTest/.../MaterialTokensTest.kt`
  - Verify spacing matches 8dp grid
  - Verify shapes use expressive radii (extraLarge = 28dp)
  - Verify motion timing matches emphasized curves
  - Test delegation from BaseTokens works

- [ ] Create `core/designsystem-unstyled/src/androidUnitTest/.../UnstyledTokensTest.kt`
  - Verify minimal shapes (max 12dp)
  - Verify flat elevations (max 4dp)
  - Verify linear motion
  - Test delegation intact

#### 9.3 Create Navigation Tests
- [ ] Create `core/navigation/src/androidUnitTest/.../PredictiveBackHandlerTest.kt`
  - Test state machine: Idle → Dragging → Settling → Completed
  - Test transform calculations at progress 0f, 0.5f, 1f
  - Test completion threshold detection
  - Property test: `checkAll(Arb.float(0f..1f))` for progress values

#### 9.4 Create Custom Arb Generators
- [ ] Create `testFixtures/kotlin/.../TestArbitraries.kt`
  ```kotlin
  fun Arb.Companion.pokemonType(): Arb<PokemonType> = 
      Arb.of(*PokemonType.values())
  
  fun Arb.Companion.pokemonId(): Arb<Int> = 
      Arb.int(1..10000)
  
  fun Arb.Companion.statValue(): Arb<Int> = 
      Arb.int(0..255)
  
  fun Arb.Companion.pokemonTypes(): Arb<List<PokemonType>> = 
      Arb.list(Arb.pokemonType(), 1..2)
  ```

### Validation
```bash
# Run all tests
./gradlew test --continue

# Verify target coverage (30-40% property tests)
./gradlew :core:designsystem-core:testDebugUnitTest
./gradlew :core:designsystem-material:testDebugUnitTest
./gradlew :core:designsystem-unstyled:testDebugUnitTest
./gradlew :core:navigation:testDebugUnitTest

# Check test reports
open build/reports/tests/testDebugUnitTest/index.html
```

### Commit
```bash
git add .
git commit -m "test: add comprehensive unit tests with property-based testing

Component Tests:
- PokemonCardTest: token application, overrides, reduced motion
- TypeBadgeTest: color mapping, Material vs Unstyled rendering
- AnimatedStatBarTest: progress clamping, animation spec, accessibility

Token Tests:
- MaterialTokensTest: expressive shapes (28dp), emphasized motion, delegation
- UnstyledTokensTest: minimal shapes (12dp), flat elevation, linear motion

Navigation Tests:
- PredictiveBackHandlerTest: state machine, transform calculations, completion

Property Tests:
- Custom Arb generators for PokemonType, IDs (1-10000), stats (0-255)
- Target 30-40% property test coverage achieved
- 1000+ scenarios per property test

All tests pass, validates component behavior and token consistency"
```

---

## Step 10: Write Component Guides and Documentation

### Objective
Create hand-written documentation for the design token system and component library.

### Tasks

#### 10.1 Create Design Tokens Reference
- [ ] Create `docs/tech/design_tokens.md`
  - Token hierarchy diagram (Base → Material/Unstyled → Components)
  - Complete token reference tables:
    - Spacing: xxxs (2dp) → xxxl (64dp)
    - Shapes: extraSmall (8dp) → extraLarge (28dp Material / 12dp Unstyled)
    - Elevation: level0 (0dp) → level5 (12dp Material / 4dp Unstyled)
    - Motion: durations + easing curves
  - Usage examples in Compose
  - Usage examples in SwiftUI
  - How to add new themes
  - Delegation pattern explanation

#### 10.2 Create Component Library Guide
- [ ] Create `docs/tech/component_library.md`
  - **PokemonCard**:
    - Purpose and usage
    - Token parameters
    - Override parameters
    - Material vs Unstyled comparison
    - Code example
    - Preview reference
  - **TypeBadge**:
    - Purpose and usage
    - Color system integration
    - Material (filled) vs Unstyled (outline) styling
    - Code example
    - Preview reference
  - **AnimatedStatBar**:
    - Purpose and usage
    - Animation behavior
    - Reduced motion support
    - Material vs Unstyled timing
    - Code example
    - Preview reference

#### 10.3 Update Project Documentation
- [ ] Update `docs/project/ui_ux.md`
  - Explain Material Expressive personality:
    - Playful 28dp corner radii
    - Elevated cards with tonal elevation
    - Emphasized motion curves
    - Google Sans Flex typography
    - Filled type badges
  - Explain Unstyled minimalism:
    - Flat 12dp max corner radii
    - Border-only cards with subtle shadows
    - Linear motion
    - System fonts
    - Outline type badges
  - Include visual comparison screenshots
  - Link to component library guide

- [ ] Update `docs/tech/conventions.md`
  - NavigationProvider naming: `<Feature><Variant>NavigationProviders.kt`
  - Token system architecture section
  - Component override guidelines:
    - Theme-global tokens (default)
    - Per-instance overrides (edge cases only)
  - Small file principle (one major component per file)

- [ ] Update `docs/project/prd.md`
  - Mark Material 3 Expressive as fully implemented
  - Mark design system comparison as fully implemented
  - Update screenshots section
  - Note SwiftUI theming system

### Validation
```bash
# Verify all links work
grep -r "](docs/" docs/

# Check for broken links
find docs/ -name "*.md" -exec grep -H "\](.*\.md)" {} \;

# Verify code examples compile
# Extract code blocks and test (manual)

# Review documentation clarity
# Read through guides as if new to project
```

### Commit
```bash
git add .
git commit -m "docs: add comprehensive design token and component guides

Design Tokens Reference:
- Complete token reference tables (spacing/shapes/elevation/motion)
- Token hierarchy diagram (Base → Theme → Component)
- Delegation pattern explanation
- Usage examples for Compose and SwiftUI
- Guide for adding new themes

Component Library Guide:
- PokemonCard: token parameters, overrides, Material vs Unstyled
- TypeBadge: color system integration, filled vs outline
- AnimatedStatBar: animation behavior, accessibility
- All with code examples and preview references

Updated Project Documentation:
- ui_ux.md: Material Expressive vs Unstyled minimalism comparison
- conventions.md: NavigationProvider naming, token architecture, component guidelines
- prd.md: Mark Material 3 Expressive and dual-UI as fully implemented

Hand-written guides provide context and examples beyond auto-generated API docs"
```

---

## Final Validation

### Complete System Test
```bash
# Build all modules
./gradlew build --continue

# Run all tests
./gradlew test --continue

# Test Android app
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:installDebug

# Test Desktop app
./gradlew :composeApp:run

# Test iOS app (if needed)
open iosApp/iosApp.xcodeproj
# Build and run on simulator
```

### Manual Testing Checklist
- [ ] Material theme: Navigate list → detail, verify animations
- [ ] Unstyled theme: Navigate list → detail, verify minimal styling
- [ ] Switch themes: Verify atomic swap, no crashes
- [ ] Predictive back: Swipe back gesture shows scale/translate
- [ ] Reduced motion: Enable accessibility setting, verify animations skipped
- [ ] Typography: Verify Google Sans Flex on Android/Desktop, SF on iOS
- [ ] Spacing consistency: Verify 8dp grid across all screens
- [ ] Type colors: Verify WCAG AA compliance, match across platforms
- [ ] SwiftUI parity: Verify iOS matches Compose layout/spacing/colors

### Success Criteria
- [ ] All 10 steps completed and committed
- [ ] All tests passing (30-40% property test coverage)
- [ ] Android build succeeds
- [ ] Desktop build succeeds
- [ ] iOS build succeeds (if testing)
- [ ] No NavigationProvider naming collisions
- [ ] No hardcoded spacing in UI screens
- [ ] Material uses expressive shapes (28dp)
- [ ] Unstyled uses minimal shapes (12dp)
- [ ] Shared components eliminate duplication
- [ ] Predictive back gesture functional
- [ ] Reduced motion support working
- [ ] Documentation complete and accurate

---

## Notes

### Session Management
- Check off completed tasks as you progress
- Commit after each step (messages provided)
- If interrupting mid-step, note progress in this file
- Run validation commands before committing
- Update "Status" at top when complete

### Troubleshooting
- **Build errors**: Check imports, verify module dependencies in `build.gradle.kts`
- **Token not found**: Verify token delegation, check object vs class
- **Preview not rendering**: Check `@Preview` syntax, verify Compose resource paths
- **iOS bridge errors**: Verify expect/actual signatures match exactly
- **Animation not working**: Check `rememberReducedMotion()` not always returning true

### Future Enhancements (Not in Scope)
- Dynamic color (Material You) - excluded per requirements
- Screenshot tests (Roborazzi) - unit tests only for now
- Additional design system variants (e.g., high contrast)
- Lottie animations for loading states
- Theme customization UI in app
