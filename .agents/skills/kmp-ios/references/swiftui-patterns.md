# SwiftUI Patterns

Complete guide for SwiftUI-specific patterns when consuming KMP ViewModels.

## Overview

This document covers SwiftUI-specific patterns for consuming Kotlin Multiplatform ViewModels, including state handling, type conversions, and common UI patterns.

## StateFlow Observation

### SKIE Automatic Bridging

SKIE automatically converts Kotlin `StateFlow` to Swift `AsyncSequence`, enabling native `for await ... in` syntax:

```swift
// SKIE makes this possible:
for await state in viewModel.uiState {
    // Receive StateFlow emissions
}
```

**Key Points**:
- ✅ SKIE makes `StateFlow` iterable with `for await ... in`
- ✅ Use `.task` modifier for automatic cancellation
- ✅ Properly cancels when view disappears (no memory leaks)

### Observation Pattern: Direct Integration

```swift
import SwiftUI
import Shared

struct PokemonListView: View {
    @StateObject private var owner = IosViewModelStoreOwner()

    private var viewModel: PokemonListViewModel {
        owner.viewModel()
    }

    @State private var uiState: PokemonListUiState = PokemonListUiStateLoading()

    var body: some View {
        content
            .onAppear {
                if case is PokemonListUiStateLoading = uiState {
                    viewModel.loadInitialPage()
                }
            }
            .task {
                // Observe StateFlow via SKIE AsyncSequence
                // Auto-cancels when view disappears
                for await state in viewModel.uiState {
                    self.uiState = state
                }
            }
    }

    @ViewBuilder
    private var content: some View {
        switch uiState {
        case is PokemonListUiStateLoading:
            ProgressView("Loading...")

        case let error as PokemonListUiStateError:
            ErrorView(message: error.message)

        case let content as PokemonListUiStateContent:
            PokemonListContent(pokemons: content.pokemons)

        default:
            EmptyView()
        }
    }
}
```

**Key Points**:
- ✅ Direct ViewModel access (no wrapper)
- ✅ `@State` holds current UI state for SwiftUI reactivity
- ✅ `.task` lifecycle automatically cancels on view disappear
- ✅ Simple, minimal boilerplate

## Sealed Class Pattern Matching

SwiftUI pattern matches Kotlin sealed classes using `is` and `as` keywords:

```swift
switch uiState {
case is PokemonListUiStateLoading:
    ProgressView("Loading...")

case let error as PokemonListUiStateError:
    VStack {
        Text("Error: \(error.message)")
        Button("Retry") { viewModel.loadInitialPage() }
    }

case let content as PokemonListUiStateContent:
    List(content.pokemons, id: \.id) { pokemon in
        PokemonRow(pokemon: pokemon)
    }

default:
    EmptyView()
}
```

**Key Points**:
- Use `is` for type checking without binding
- Use `let ... as` for type checking with binding
- Always include `default` case (Swift requirement for sealed classes)

## Type Conversions

### Kotlin Int → Swift Int32

Kotlin's `Int` maps to Swift's `Int32`, not `Int`. **Explicit casting required**.

**Problem**:
```swift
// ❌ Compile error: Cannot assign Int32 to Int
scrollPosition = pokemon.id  // pokemon.id is Int32
navigationPath.append(pokemon.id)
```

**Solution**:
```swift
// ✅ Explicit cast
scrollPosition = Int(pokemon.id)
navigationPath.append(Int(pokemon.id))
```

**When to Cast**:
- Assigning Kotlin `Int` to Swift `Int` variables
- Passing Kotlin `Int` to Swift APIs expecting `Int`
- Formatting strings: `String(format: "%03d", Int(pokemon.id))`

**Rule**: Always cast Kotlin numeric types when interfacing with Swift stdlib.

### String Formatting

**Problem**: Swift string interpolation doesn't support format specifiers like Kotlin.

```swift
// ❌ Swift compile error
let height = "Height: \(pokemon.height/10.0:.1f) m"
// Error: Cannot use specifier with string interpolation
```

**Solution**: Use `String(format:_:)` for formatted output.

```swift
// ✅ Correct Swift syntax
let height = String(format: "%.1f m", Double(pokemon.height) / 10.0)
let weight = String(format: "%.1f kg", Double(pokemon.weight) / 10.0)
let id = String(format: "#%03d", Int(pokemon.id))
```

**Common Format Specifiers**:
- `%.1f` - Float with 1 decimal place
- `%.2f` - Float with 2 decimal places
- `%03d` - Integer with leading zeros (3 digits)
- `%d` - Integer
- `%@` - String (for Swift objects)

**Type Conversions for Formatting**:
```swift
// Kotlin Int32 → Swift Int
String(format: "#%03d", Int(pokemon.id))

// Kotlin Int32 → Swift Double for division
String(format: "%.1f m", Double(pokemon.height) / 10.0)

// Explicit Double wrapper
let heightInMeters = Double(pokemon.height) / 10.0
let heightString = String(format: "%.1f m", heightInMeters)
```

**Why Not Interpolation?**: Swift doesn't support inline format specifiers in string interpolation. For simple concatenation without formatting, interpolation works:

```swift
// ✅ Simple interpolation (no formatting)
let name = "Name: \(pokemon.name)"
let count = "Total: \(pokemon.stats.count)"

// ❌ Formatted interpolation (use String(format:) instead)
let height = "Height: \(pokemon.height/10.0:.1f) m"  // Compile error
```

## Common UI Patterns

### Pattern 1: Infinite Scroll

```swift
ScrollView {
    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())]) {
        ForEach(Array(content.pokemons.enumerated()), id: \.element.id) { index, pokemon in
            PokemonCard(pokemon: pokemon)
                .onAppear {
                    // Trigger load when reaching last 4 items
                    if index >= content.pokemons.count - 4 && content.hasMore && !content.isLoadingMore {
                        viewModel.loadNextPage()
                    }
                }
        }
    }
}
```

**Key Points**:
- Use `enumerated()` to get index and element
- Load next page when user scrolls near bottom (last 4 items)
- Check `hasMore` and `!isLoadingMore` to prevent duplicate loads
- Convert `KotlinArray` to Swift array with `Array()`

### Pattern 2: Scroll Position Preservation

```swift
struct PokemonListView: View {
    @State private var scrollPosition: Int?

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView {
                LazyVGrid(...) {
                    ForEach(...) { index, pokemon in
                        PokemonCard(pokemon: pokemon) {
                            // Save position before navigating
                            scrollPosition = Int(pokemon.id)
                            // Navigate...
                        }
                        .id(Int(pokemon.id))  // Scroll anchor
                    }
                }
            }
            .onAppear {
                // Restore scroll position on return
                if let position = scrollPosition {
                    proxy.scrollTo(position, anchor: .top)
                }
            }
        }
    }
}
```

**Key Points**:
- Use `ScrollViewReader` to control scroll position
- Assign `.id()` to each card for scroll anchoring
- Store position before navigation
- Restore position on view appearance

### Pattern 3: Error Handling with Retry

```swift
case let error as PokemonListUiStateError:
    VStack(spacing: 16) {
        Image(systemName: "exclamationmark.triangle")
            .font(.system(size: 48))
            .foregroundColor(.orange)

        Text("Something went wrong")
            .font(.headline)

        Text(error.message)
            .font(.body)
            .foregroundColor(.secondary)
            .multilineTextAlignment(.center)

        Button("Retry") {
            viewModel.loadInitialPage()
        }
        .buttonStyle(.borderedProminent)
    }
    .padding()
```

### Pattern 4: Navigation with ID

```swift
struct PokemonListView: View {
    @State private var navigationPath = [Int]()

    var body: some View {
        NavigationStack(path: $navigationPath) {
            PokemonListViewContent(
                onPokemonTapped: { pokemonId in
                    navigationPath.append(Int(pokemonId))
                }
            )
            .navigationDestination(for: Int.self) { pokemonId in
                PokemonDetailView(pokemonId: pokemonId)
            }
        }
    }
}
```

### Pattern 5: Conditional Loading State

```swift
.onAppear {
    // Only load if in loading state (prevents duplicate loads)
    if case is PokemonListUiStateLoading = uiState {
        viewModel.loadInitialPage()
    }
}
```

## SKIE Automatic Renames for Swift Keywords

**Problem**: Kotlin classes named after Swift keywords get automatically renamed by SKIE.

**Example**: `Type` class (Pokemon type) conflicts with Swift's `Type` protocol.

**SKIE Solution**: Automatically renames to `Type_` (appends underscore).

**Impact in Code**:

```swift
// Kotlin side (unchanged)
data class Type(val name: String, val url: String)

// Swift side (SKIE-renamed)
struct PokemonDetail {
    let types: [Type_]  // Note: Type_ not Type
}

// Using in SwiftUI
ForEach(pokemon.types, id: \.name) { type in
    TypeBadge(type: type)  // type is Type_ instance
}

private func typeGradient(types: [Type_]) -> LinearGradient {
    let type = types.first?.name.lowercased() ?? "normal"
    // Use type.name to access properties
}
```

**Other Common Keyword Collisions**:
- `Type` → `Type_`
- `Error` → `Error_`
- `Result` → `Result_`
- `Self` → `Self_`
- `Protocol` → `Protocol_`

**Debugging Renamed Types**:
```swift
// Check in Swift compiler or Xcode autocomplete
// If a Kotlin class doesn't show up, try appending _
import Shared
let type: Type_ = pokemonDetail.types.first!
```

**Best Practice**:
- ✅ Check SKIE-generated Swift interfaces when Kotlin types don't compile
- ✅ Search for `_` suffix on missing types
- ✅ Use Xcode autocomplete to discover renamed types
- ❌ Don't manually rename Kotlin classes to avoid Swift keywords (SKIE handles it)

## Composable Properties vs Functions

**Problem**: iOS runtime fails to recognize `internal val` properties that return composable lambdas in interfaces.

**Symptom**: Runtime exception in iOS (not compile-time) when accessing composable properties like `val card: @Composable () -> CardTokens`.

**Root Cause**: Kotlin/Native iOS interop doesn't properly handle `internal val` properties with composable function types in interfaces.

**Solution**: Use `@Composable fun` methods instead of `val` properties with composable lambdas.

### Example

```kotlin
// ❌ BREAKS on iOS - val with composable lambda type
interface MaterialComponentTokens {
    val card: @Composable () -> CardTokens
    val badge: @Composable () -> BadgeTokens
}

internal class DefaultMaterialComponentTokens : MaterialComponentTokens {
    override val card: @Composable () -> CardTokens = {
        object : CardTokens {
            override val shape = MaterialTheme.tokens.shapes.extraLarge
            // ... properties
        }
    }
}
```

```kotlin
// ✅ WORKS on iOS - @Composable fun
interface MaterialComponentTokens {
    @Composable
    fun card(): CardTokens

    @Composable
    fun badge(): BadgeTokens
}

internal class DefaultMaterialComponentTokens : MaterialComponentTokens {
    @Composable
    override fun card(): CardTokens = object : CardTokens {
        override val shape = MaterialTheme.tokens.shapes.extraLarge
        // ... properties
    }
}
```

**Key Differences**:
- Interface declares `@Composable fun` not `val`
- Implementation overrides with `@Composable override fun`
- Return the tokens directly (not wrapped in lambda)
- Call site changes from `tokens.card()` to `tokens.card()` (same syntax!)

**When This Applies**:
- ✅ Any interface with composable properties
- ✅ Token providers, theme interfaces
- ✅ Any code shared with iOS (`commonMain`)
- ❌ Not needed for Android-only code (`androidMain`)

**Impact on Callsites**:
```kotlin
// Before
val cardTokens = MaterialTheme.componentTokens.card()  // Invoke lambda

// After
val cardTokens = MaterialTheme.componentTokens.card()  // Call function (same syntax!)
```

**Why This Matters**:
- Runtime exceptions in iOS are harder to debug than compile errors
- Affects any shared design system or token architecture
- Must be tested on actual iOS builds, not just Android

**Lesson Learned**: When working with composables in shared code (especially in interfaces), prefer `@Composable fun` over `val` properties with composable lambdas for iOS compatibility.

## Best Practices

### ✅ DO

1. **Use `.task` for StateFlow observation** - Auto-cancels on view disappear
2. **Cast Kotlin Int32 to Swift Int** - When interfacing with Swift stdlib
3. **Use `String(format:_:)` for formatting** - Swift doesn't support format specifiers in interpolation
4. **Check for SKIE keyword renames** - Look for `_` suffix on missing types
5. **Use `@Composable fun` instead of `val` in interfaces** - For iOS compatibility
6. **Use `enumerated()` for index tracking** - In lazy collections
7. **Check current state before loading** - Prevent duplicate loads

### ❌ DON'T

1. **Don't use string interpolation for formatting** - Use `String(format:_:)`
2. **Don't forget explicit casts** - Kotlin Int32 → Swift Int
3. **Don't assume types exist** - Check for SKIE renames
4. **Don't use `val` with composable lambdas** - In interfaces shared with iOS
5. **Don't create new ViewModel on every View render** - Use `@StateObject`
6. **Don't use `.onAppear` for StateFlow observation** - Use `.task` instead

## Troubleshooting

### StateFlow Not Updating

**Symptom**: UI not reflecting state changes

**Causes**:
- StateFlow observation in wrong modifier
- Missing `.task` modifier

**Solution**:
```swift
.task {
    for await state in viewModel.uiState {  // ✅ Observe in .task
        self.uiState = state
    }
}
```

### Compile Error: Missing Type

**Symptom**: Kotlin class not found in Swift

**Causes**:
- SKIE renamed type due to Swift keyword conflict

**Solution**:
```swift
// Try appending underscore
let type: Type_ = pokemonDetail.types.first!
```

### Type Mismatch Errors

**Symptom**: Cannot assign value of type 'Int32' to type 'Int'

**Causes**:
- Kotlin Int maps to Swift Int32, not Int

**Solution**:
```swift
let id = Int(pokemon.id)  // ✅ Explicit cast
```

## Validation Commands

```bash
# Build iOS frameworks
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 :shared:linkDebugFrameworkIosArm64

# Build and test
./gradlew :composeApp:assembleDebug test --continue

# Xcode builds
cd iosApp && xcodebuild -scheme iosApp -sdk iphonesimulator build CODE_SIGN_IDENTITY="" CODE_SIGNING_REQUIRED=NO
```

## References

- [Direct Integration Pattern](direct-integration.md) - Complete Direct Integration guide
- [Lifecycle Bridging](lifecycle-bridging.md) - SwiftUI lifecycle management
- [Export Setup](export-setup.md) - Framework export configuration
