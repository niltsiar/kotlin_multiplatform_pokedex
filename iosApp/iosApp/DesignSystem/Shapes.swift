import SwiftUI

/**
 * Shape tokens for rounded corners.
 * Matches MaterialTokens.shapes from Compose Multiplatform.
 */
extension PokemonTheme {
    struct Shapes {
        let xs: CGFloat = 8     // MaterialTokens.shapes.shapeXs
        let sm: CGFloat = 12    // MaterialTokens.shapes.shapeSm
        let md: CGFloat = 16    // MaterialTokens.shapes.shapeMd
        let lg: CGFloat = 24    // MaterialTokens.shapes.shapeLg
        let xl: CGFloat = 28    // MaterialTokens.shapes.shapeXl (Material 3 Expressive)
        
        static let rounded = Shapes()
    }
}
