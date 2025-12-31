import SwiftUI

/**
 * Typography tokens using iOS system fonts (San Francisco).
 * Sizes roughly match MaterialTokens.typography from Compose.
 */
extension PokemonTheme {
    struct Typography {
        let display: Font = .system(size: 57, weight: .bold)        // displayLarge
        let headline: Font = .system(size: 32, weight: .semibold)   // headlineLarge
        let title: Font = .system(size: 22, weight: .medium)        // titleLarge
        let body: Font = .system(size: 16, weight: .regular)        // bodyLarge
        let label: Font = .system(size: 14, weight: .regular)       // labelLarge
        let caption: Font = .system(size: 12, weight: .regular)     // bodySmall
        
        static let system = Typography()
    }
}
