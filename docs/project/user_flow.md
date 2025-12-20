# Pokédex App - User Flow Document

Last Updated: November 26, 2025

## Screens

### 1. **Pokémon List Screen**
- **Purpose**: Enable users to browse all available Pokémon with infinite scroll pagination and navigate to individual Pokémon details
- **Key Elements**:
  - Grid layout (2 columns mobile, 3-4 columns tablet/desktop)
  - Pokémon cards with: sprite image, name (capitalized), Pokédex number (#001 format)
  - Infinite scroll with automatic next-page loading
  - Loading state (initial: full-screen spinner, pagination: bottom indicator)
  - Error state with retry button
  - App title/header ("PokéDex" or "Pokémon Explorer")
- **Expected Actions**:
  - Scroll vertically through grid
  - Tap/click any Pokémon card → Navigate to Detail Screen
  - Automatic trigger: Load next page when near bottom (4 cards from end)
  - On error: Tap retry button → Reload data
  - Pull-to-refresh (optional future feature)

### 2. **Pokémon Detail Screen**
- **Purpose**: Display comprehensive information about a selected Pokémon including stats, types, abilities, and physical attributes
- **Key Elements**:
  - Back button/navigation (top-left corner)
  - Hero section: Large Pokémon sprite, name (title case), Pokédex number, type-colored background gradient
  - Type badges: Pill-shaped with official type colors and names
  - Physical info cards: Height (meters), Weight (kilograms), Base Experience
  - Abilities section: List of abilities with hidden ability indicators
  - Base stats section: Six horizontal progress bars (HP, Attack, Defense, Sp. Atk, Sp. Def, Speed) with values and color coding
- **Expected Actions**:
  - Scroll vertically through all sections
  - Tap back button → Navigate back to List Screen (scroll position preserved)
  - System back gesture/button → Navigate back to List Screen
  - Long-press sprite → Easter egg animation (360° spin)
  - Tap stat bar 5× rapidly → Easter egg confetti burst

### 3. **Loading State (Initial)**
- **Purpose**: Provide visual feedback while initial data loads
- **Key Elements**:
  - Full-screen centered circular progress indicator
  - Optional: App logo or Pokéball icon above spinner
- **Expected Actions**:
  - Automatic: Transition to List Screen once data loads
  - On error: Transition to Error State

### 4. **Error State (List Screen)**
- **Purpose**: Inform user of network/API failure and provide recovery action
- **Key Elements**:
  - Error icon (connection symbol or sad Pokéball)
  - Error message: "Unable to load Pokémon. Please check your connection."
  - Retry button (prominent, colorful)
- **Expected Actions**:
  - Tap retry button → Attempt to reload data
  - If successful → Transition to List Screen
  - If failed again → Remain in Error State (retry available)

### 5. **Pagination Loading State**
- **Purpose**: Indicate additional Pokémon are being loaded during infinite scroll
- **Key Elements**:
  - Small circular progress indicator at bottom of list
  - Text: "Loading more Pokémon..."
- **Expected Actions**:
  - Automatic: Appears when user scrolls near bottom
  - Automatic: Disappears when next page loads
  - Continue scrolling while loading

---

## User Flow

### Primary Flow: Browse & Discover Pokémon

**Happy Path (First-Time User)**:
1. **App Launch** → Instant load to Loading State (Initial)
2. **Loading State (Initial)** → API fetches first 20 Pokémon (offset=0, limit=20)
3. **List Screen** → Grid fades in with staggered entrance animation
4. **User scrolls down** → Views Pokémon cards (#001 Bulbasaur, #002 Ivysaur, etc.)
5. **User scrolls near bottom (16th card)** → Automatic trigger: Load next page
6. **Pagination Loading State** → "Loading more..." indicator appears at bottom
7. **Next 20 Pokémon load** → Cards append to grid seamlessly
8. **User taps Pokémon card** (e.g., #025 Pikachu) → Circular reveal transition begins
9. **Detail Screen** → Hero image scales up, stats animate in sequentially
10. **User scrolls down** → Views types, abilities, stats with progress bars
11. **User taps back button** → Smooth transition back to List Screen
12. **List Screen** → Scroll position preserved (user returns to #025 Pikachu position)
13. **User continues browsing** → Repeat steps 4-12 for other Pokémon

**Returning User Flow**:
1. **App Launch** → Instant load to Loading State (Initial)
2. **Loading State (Initial)** → API fetches first 20 Pokémon
3. **List Screen** → User immediately at familiar position
4. **Continue browsing** → Same as steps 4-13 above

### Alternative Flow: Network Error on Launch

1. **App Launch** → Loading State (Initial)
2. **API request fails** (no internet, timeout, server error)
3. **Error State (List Screen)** → Error message with retry button
4. **User taps retry button** → Attempt to reload
5. **If successful** → List Screen with data
6. **If failed again** → Error State persists, retry available

### Alternative Flow: Network Error During Pagination

1. **User scrolling List Screen** → Near bottom, triggers next page load
2. **Pagination Loading State** → "Loading more..." appears
3. **API request fails** → Loading indicator disappears
4. **Error indicator appears at bottom** → "Failed to load more. Tap to retry."
5. **User taps retry** → Attempt to load next page again
6. **If successful** → New cards append
7. **If failed** → Error indicator remains

### Alternative Flow: Image Load Failure

1. **List Screen or Detail Screen** → Pokémon image fails to load
2. **Placeholder remains visible** → Light gray with Pokéball icon
3. **User taps card/image** → Attempt to retry image load
4. **If successful** → Image fades in
5. **If failed** → Placeholder persists (app remains functional)

### Alternative Flow: Detail Screen Direct Navigation (Deep Link)

1. **User receives deep link** (e.g., `pokedex://pokemon/25`)
2. **App Launch** → Loading State (Initial, brief)
3. **Detail Screen for Pokémon #25** → Loads directly without List Screen
4. **User taps back button** → Navigate to List Screen (starts at top)

### Edge Case: No Internet Connection

1. **App Launch** → Loading State (Initial)
2. **No network detected** → Error State immediately
3. **Error message**: "No internet connection. Please connect and try again."
4. **User connects to network** → Tap retry button
5. **List Screen** → Data loads successfully

### Edge Case: Rapid Card Tapping

1. **List Screen** → User rapidly taps multiple cards
2. **First tap registers** → Navigation to Detail Screen begins
3. **Subsequent taps ignored** → Prevents navigation stack issues
4. **Detail Screen loads** → Single Pokémon displayed

---

## Navigation Structure

### Primary Navigation
- **Type**: Single-level navigation (List ↔ Detail)
- **Pattern**: Stack navigation
  - List Screen is root
  - Detail Screen pushes onto stack
  - Back navigation pops Detail Screen, returns to List
- **No tabs, no side menu** (simple two-screen flow)

### Secondary Navigation
- **Back Button** (Detail Screen → List Screen):
  - Visual back button in top-left corner (icon: `<` or back arrow)
  - System back gesture (swipe from left edge on mobile)
  - System back button (Android hardware/software back button)
- **Deep Links** (Future enhancement):
  - `pokedex://pokemon/{id}` → Direct navigation to Detail Screen
  - `pokedex://list` → Opens List Screen
- **Scroll Position Memory**:
  - When navigating back from Detail → List, scroll position is preserved
  - User returns to exact position where they tapped card

### Navigation Transitions
- **List → Detail**: 
  - Circular reveal animation expanding from tapped card
  - Hero image scales from card size to detail size
  - Duration: ~300ms with spring easing
- **Detail → List**:
  - Reverse circular reveal (collapse back to card)
  - Smooth fade-out of detail content
  - Duration: ~250ms with fast-out-slow-in easing
- **Error → List (after retry)**:
  - Simple crossfade transition
  - Duration: ~200ms

---

## State Management Summary

### List Screen States
1. **Loading (Initial)**: Full-screen spinner, no content
2. **Content (Success)**: Grid of Pokémon cards, scrollable
3. **Loading More (Pagination)**: Content visible + bottom loading indicator
4. **Error**: Error message + retry button, no content

### Detail Screen States
1. **Loading (Initial)**: Brief spinner or skeleton screen
2. **Content (Success)**: Full Pokémon details visible, scrollable
3. **Error**: Error message + retry button (rare, as ID is guaranteed valid)

### Data Flow
- **API calls**: 
  - List: `GET /api/v2/pokemon?limit=20&offset={offset}`
  - Detail: `GET /api/v2/pokemon/{id}/`
- **Caching**: Images cached locally (Coil3 handles automatically)
- **Persistence**: None (app does not store Pokémon data locally, always fresh from API)

---

## User Goals & Success Metrics

### Primary Goal
**Users can effortlessly browse and learn about Pokémon**

**Success Indicators**:
- User can find any Pokémon within seconds of searching/scrolling
- Detail screen loads in <1 second
- Zero navigation confusion (back button always works as expected)
- Smooth 60fps scrolling on list grid

### Secondary Goals
- **Discovery**: Users explore Pokémon they didn't know existed
- **Quick Reference**: Users quickly check stats/types mid-gameplay
- **Collection Tracking**: Users feel progress as they browse through generations

---

## Developer Implementation Notes

### Navigation Parameters
- **List → Detail**: Pass `pokemonId: Int` as navigation argument
- **Detail Screen**: Receives `pokemonId`, fetches data via `GET /api/v2/pokemon/{id}/`

### Scroll Position Preservation
- Use `rememberLazyGridState()` (Compose) or equivalent state restoration
- Navigation library should preserve state on back navigation

### Error Handling
- Distinguish between:
  - Network errors (timeout, no connection)
  - HTTP errors (404, 500)
  - Parsing errors (malformed JSON)
- All mapped to user-friendly messages

### Performance Considerations
- **Pagination**: Load 20 items at a time (balance between UX and API efficiency)
- **Prefetching**: Trigger next page load 4 items before end (seamless experience)
- **Image Optimization**: Sprites are small (~96x96px), Coil3 handles caching
- **Animation Performance**: Use hardware-accelerated animations, avoid jank

---

## Future Enhancements (Post-MVP)

### Search & Filter
- Add search bar to List Screen
- Filter by type, generation, name
- Flow: List Screen (with search bar) → Filtered results → Detail Screen

### Favorites
- Add favorite button to Detail Screen
- Favorites collection screen accessible via bottom navigation
- Flow: Detail Screen → Tap favorite → List Screen (favorited) → Favorites Screen

### Offline Mode
- Cache viewed Pokémon locally
- Error state shows cached data with "Offline Mode" indicator

### Team Builder
- Select up to 6 Pokémon for a team
- New screen: Team Screen (accessible via tab navigation)
- Flow: List Screen → Detail Screen → Add to Team → Team Screen

**Current Scope**: MVP focuses on List ↔ Detail flow only. Enhancements deferred to post-MVP.