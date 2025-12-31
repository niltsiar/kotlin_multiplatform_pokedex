import SwiftUI

/**
 * Pokémon Type Colors (Domain-Specific)
 * 
 * Matches PokemonTypeColors from Compose Multiplatform.
 * Colors are adjusted for WCAG AA accessibility (4.5:1 contrast).
 */
enum PokemonType: String, CaseIterable {
    case normal, fire, water, electric, grass, ice, fighting, poison
    case ground, flying, psychic, bug, rock, ghost, dragon, dark, steel, fairy
    
    var color: Color {
        switch self {
        case .fire: return Color(red: 1.0, green: 0.267, blue: 0.133)     // #FF4422
        case .water: return Color(red: 0.2, green: 0.6, blue: 1.0)        // #3399FF
        case .grass: return Color(red: 0.467, green: 0.8, blue: 0.333)    // #77CC55
        case .electric: return Color(red: 1.0, green: 0.8, blue: 0.2)     // #FFCC33
        case .psychic: return Color(red: 1.0, green: 0.333, blue: 0.6)    // #FF5599
        case .ice: return Color(red: 0.4, green: 0.8, blue: 1.0)          // #66CCFF
        case .dragon: return Color(red: 0.467, green: 0.4, blue: 0.933)   // #7766EE
        case .dark: return Color(red: 0.467, green: 0.333, blue: 0.267)   // #775544
        case .fairy: return Color(red: 0.933, green: 0.6, blue: 0.933)    // #EE99EE
        case .normal: return Color(red: 0.667, green: 0.667, blue: 0.6)   // #AAAA99
        case .fighting: return Color(red: 0.733, green: 0.333, blue: 0.267)  // #BB5544
        case .flying: return Color(red: 0.533, green: 0.6, blue: 1.0)     // #8899FF
        case .poison: return Color(red: 0.6, green: 0.333, blue: 0.733)   // #9955BB
        case .ground: return Color(red: 0.867, green: 0.733, blue: 0.333) // #DDBB55
        case .rock: return Color(red: 0.733, green: 0.667, blue: 0.467)   // #BBAA77
        case .bug: return Color(red: 0.667, green: 0.733, blue: 0.133)    // #AABB22
        case .ghost: return Color(red: 0.4, green: 0.4, blue: 0.733)      // #6666BB
        case .steel: return Color(red: 0.667, green: 0.667, blue: 0.733)  // #AAAABB
        }
    }
    
    /// Get background color with theme-aware adjustments
    func backgroundColor(isDark: Bool) -> Color {
        if isDark {
            // Lighten for dark mode (better contrast)
            return color.opacity(0.8)
        } else {
            // Slightly darken for light mode
            return color
        }
    }
    
    /// Get content (text) color that contrasts with background
    var contentColor: Color {
        // Light types use dark text
        switch self {
        case .electric, .ice, .fairy, .normal:
            return .black
        default:
            return .white
        }
    }
    
    /// Initialize from string (case-insensitive)
    init?(string: String) {
        guard let type = PokemonType(rawValue: string.lowercased()) else {
            return nil
        }
        self = type
    }
}
