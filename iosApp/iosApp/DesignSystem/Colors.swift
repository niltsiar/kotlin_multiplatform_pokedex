import SwiftUI

/**
 * Semantic colors using iOS system colors for automatic dark mode support.
 */
extension PokemonTheme {
    struct Colors {
        // Use iOS semantic colors for automatic dark mode
        let background: Color = Color(.systemBackground)
        let surface: Color = Color(.secondarySystemBackground)
        let primary: Color = Color(.systemBlue)
        let onBackground: Color = Color(.label)
        let onSurface: Color = Color(.label)
        let secondary: Color = Color(.secondaryLabel)
        let tertiary: Color = Color(.tertiaryLabel)
        let border: Color = Color(.separator)
        
        static let light = Colors()
    }
}
