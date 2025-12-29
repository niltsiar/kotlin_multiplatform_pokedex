# Pokédex App - Product Requirements Document

Last Updated: December 29, 2025

## Goal
Provide users with a fast, intuitive mobile-first Pokédex that enables browsing all Pokémon and viewing detailed stats, abilities, and attributes using the official PokéAPI. **Additionally, showcase dual design system implementations** (Material Design 3 Expressive + Compose Unstyled) as an educational comparison.

## Target Audience
- **Pokémon fans** (ages 8-40) seeking quick reference to Pokémon data
- **Game players** needing instant access to stats, types, and abilities during gameplay
- **Collectors** tracking their Pokédex completion across generations
- **Casual users** curious about Pokémon characteristics and trivia
- **Mobile-first users** expecting immediate load times and smooth scrolling
- **Cross-platform users** who switch between Android, iOS, and desktop devices
- **Developers and designers** interested in comparing design system implementations

## Core Features

1. **Pokémon List Screen**
    - **Purpose**: Allow users to browse all available Pokémon with infinite scroll pagination
    - **Key Elements**:
        - **Adaptive grid layout**: 2 columns (compact/mobile), 3 columns (medium/tablet), 4 columns (expanded/desktop)
        - Each card displays: Pokémon image, name (capitalized), and national Pokédex number (#001 format)
        - Infinite scroll with automatic loading of next page when user nears bottom
        - Loading indicators (initial load + "loading more" at bottom)
        - Error state with retry option for network failures
        - 20 Pokémon per page load
    - **Expected User Actions**:
        - Scroll through grid of Pokémon cards
        - Tap/click any card to navigate to detail screen
        - View loading state while data fetches
        - Retry on error if network fails
        - Switch between Material and Unstyled themes via FAB button (educational feature)

2. **Pokémon Detail Screen**
    - **Purpose**: Display comprehensive information about a selected Pokémon
    - **Key Elements**:
        - **Header Section**: Large Pokémon image (front sprite), name (title case), national Pokédex number
        - **Type Badges**: List of type(s) with official Pokémon type colors (e.g., Fire=red, Water=blue, Grass=green)
        - **Physical Attributes**: Height (meters), Weight (kilograms), displayed in labeled cards
        - **Abilities Section**: List of abilities with names, indicator for hidden abilities
        - **Base Stats Section**: Six stats (HP, Attack, Defense, Sp. Atk, Sp. Def, Speed) with:
            - Stat name and numeric value
            - Horizontal progress bar (max 255) with color coding
            - Visual comparison to typical ranges
        - **Experience**: Base experience value displayed
        - Back navigation button/gesture to return to list
    - **Expected User Actions**:
        - View complete Pokémon information in single scrollable column
        - Understand type advantages at a glance via color-coded badges
        - Compare stat values via visual progress bars
        - Navigate back to list screen

3. **Navigation Flow**
    - **Purpose**: Seamless transition between list and detail screens with adaptive navigation
    - **Key Elements**:
        - List → Detail: Pass Pokémon ID as navigation parameter
        - Detail → List: Back button or system back gesture
        - Maintain scroll position in list when returning from detail
        - **Adaptive navigation**: Bottom bar (compact), Navigation rail (medium), Navigation drawer (expanded)
        - Smooth animations: slideInHorizontally + fadeIn for detail screen entry
    - **Expected User Actions**:
        - Tap Pokémon card to open detail
        - Use back navigation to return to list at same scroll position
        - Experience responsive navigation that adapts to window size

4. **Theme Switching (Educational Feature)**
    - **Purpose**: Demonstrate design system comparison between Material 3 and Compose Unstyled
    - **Key Elements**:
        - FAB button in bottom-right corner (theme icon)
        - Entire app switches atomically (scaffold + content)
        - State persisted across sessions (DataStore)
        - First-run modal explains the dual-UI showcase feature
        - Theme preference accessible from any screen
    - **Expected User Actions**:
        - Tap FAB to switch between Material and Unstyled themes
        - Observe visual differences in same screens
        - Learn about design system approaches (educational value)

## Competitors
- **Official Pokémon HOME** — Strengths: Official data, cloud sync, team management / Weaknesses: Requires Nintendo account, limited free tier
- **Pokémon Database (pokemondb.net)** — Strengths: Comprehensive web resource, community-driven / Weaknesses: Web-only, ad-supported, cluttered UI
- **PokéDex iOS/Android apps** — Strengths: Offline mode, favorites, type calculators / Weaknesses: Inconsistent data quality, outdated designs, ads
- **Bulbapedia mobile** — Strengths: Wiki-level detail, community content / Weaknesses: Information overload, slow load times, not optimized for mobile
- **Serebii.net mobile** — Strengths: Up-to-date game info, news / Weaknesses: Dense layout, not app-optimized

## Unique Selling Points
- **Dual-UI design system showcase**: Runtime switching between Material 3 Expressive and Compose Unstyled (educational feature)
- **Kotlin Multiplatform architecture**: Single codebase for Android, iOS, and Desktop with platform-specific UI
- **Material 3 Adaptive**: Responsive layouts and navigation that adapt to screen size (bottom bar → rail → drawer)
- **Clean, modern UI**: Material Design 3 (Android/Desktop/iOS Compose) and native SwiftUI (iOS production app)
- **Official PokéAPI integration**: Always up-to-date, canonical Pokémon data
- **Performance-first**: Efficient pagination, image caching, and smooth 60fps scrolling
- **No account required**: Instant access without registration or authentication
- **Cross-platform**: Seamless experience on phone, tablet, or desktop
- **Type-safe architecture**: Robust error handling with Arrow Either pattern prevents crashes
- **Comprehensive testing**: 84 tests with property-based testing (40% property tests, 34,000+ scenarios per run)

## Technical Notes
- **Platforms**: Android (minSDK 24), iOS (14+), Desktop (JVM)
- **Architecture**: Kotlin Multiplatform with vertical slice modularization (split-by-layer pattern)
- **UI Framework**: 
    - **Dual-UI Implementation**: Material Design 3 Expressive + Compose Unstyled (runtime theme switching)
    - Compose Multiplatform for Android, Desktop, and iOS Compose (shared UI code)
    - Native SwiftUI for iOS production app (consumes KMP business logic via shared.framework)
- **API Integration**: PokéAPI v2 REST API (https://pokeapi.co/api/v2)
    - List endpoint: `GET /api/v2/pokemon?limit=20&offset={offset}` 
    - Detail endpoint: `GET /api/v2/pokemon/{id}/`
- **Networking**: Ktor HTTP client with kotlinx.serialization for JSON
- **Error Handling**: Arrow Either for repository boundaries (Either<RepoError, T>)
- **State Management**: androidx.lifecycle ViewModel with StateFlow and SavedStateHandle persistence
- **Navigation**: Navigation Compose 3 with Koin DI integration (Android/Desktop), native navigation (iOS)
- **Image Loading**: Coil3 for async image loading and caching
- **Dependency Injection**: Koin 4.0 with Impl+Factory pattern (classes DI-agnostic, wiring in separate modules)
- **Testing**: Kotest (assertions, property tests), MockK (mocking), Turbine (flow testing), Roborazzi (screenshot tests)
- **Adaptive Layouts**: Material 3 Adaptive library for responsive grid columns and navigation
- **Module Structure**:
    - `:features:<feature>:api` - Public contracts (exported to iOS)
    - `:features:<feature>:data` - Repositories, DTOs, API services
    - `:features:<feature>:presentation` - ViewModels, UI state (exported to iOS)
    - `:features:<feature>:ui-material` - Material Design 3 UI
    - `:features:<feature>:ui-unstyled` - Compose Unstyled UI
    - `:features:<feature>:wiring` - Business logic DI
    - `:features:<feature>:wiring-ui-material` - Material navigation registration
    - `:features:<feature>:wiring-ui-unstyled` - Unstyled navigation registration
    - `:core:designsystem-material` - Material 3 theme + MaterialScope
    - `:core:designsystem-unstyled` - Compose Unstyled theme + UnstyledScope
    - `:core:navigation` - Navigation 3 modular architecture
- **Performance Targets**:
    - List screen initial load: <2 seconds on 4G
    - Detail screen load: <1 second (data fetch + render)
    - Smooth 60fps scrolling on adaptive grid (2/3/4 columns)
    - Image load with placeholder → fade-in animation

## Design Guidelines
- **Visual Style**: 
    - Material Design 3 for Android/Desktop (dynamic color theming)
    - Native iOS design patterns for SwiftUI implementation
    - Official Pokémon type colors for badges (Fire=#FF4422, Water=#3399FF, etc.)
    - Card-based layout with elevation/shadows for depth
- **Typography**:
    - Clear hierarchy: Large titles for names, medium for labels, small for metadata
    - Readable font sizes (minimum 14sp/pt for body text)
- **Accessibility**:
    - WCAG AA compliance (minimum 4.5:1 contrast ratios)
    - Semantic labels for screen readers
    - Touch targets minimum 48dp/pt
    - Support for system font scaling
- **Animations**:
    - Smooth card tap feedback with ripple effect
    - Fade-in for loaded images
    - Subtle scale animation on card press
    - Shared element transition from list card to detail header (platform-dependent)
- **Color Palette**:
    - Pokémon type colors as accents
    - Neutral backgrounds (light/dark mode support)
    - Consistent color coding for stat bars (green=high, yellow=medium, red=low)
- **Responsive Layout**:
    - Mobile: 2-column grid, full-width detail
    - Tablet: 3-column grid, 2/3 width detail with margins
    - Desktop: 4-column grid, centered detail with max-width
- **Empty/Error States**:
    - Friendly error messages (avoid technical jargon)
    - Retry buttons with clear call-to-action
    - Loading skeletons for better perceived performance
- **Branding**:
    - Pokémon-themed but not official branding (avoid trademark issues)
    - App name: "PokéDex" or "Pokémon Explorer" (check trademark availability)
    - Icon: Pokéball-inspired minimalist design
