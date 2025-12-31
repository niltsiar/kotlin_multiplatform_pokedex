import SwiftUI

/**
 * Spacing tokens following 8pt grid system.
 * Matches BaseTokens.spacing from Compose Multiplatform.
 */
extension PokemonTheme {
    struct Spacing {
        let xxxs: CGFloat = 2   // BaseTokens.spacing.spacingXxxs
        let xxs: CGFloat = 4    // BaseTokens.spacing.spacingXxs
        let xs: CGFloat = 8     // BaseTokens.spacing.spacingXs
        let sm: CGFloat = 12    // BaseTokens.spacing.spacingSm
        let md: CGFloat = 16    // BaseTokens.spacing.spacingMd
        let lg: CGFloat = 20    // BaseTokens.spacing.spacingLg
        let xl: CGFloat = 24    // BaseTokens.spacing.spacingXl
        let xxl: CGFloat = 32   // BaseTokens.spacing.spacingXxl
        let xxxl: CGFloat = 64  // BaseTokens.spacing.spacingXxxl
        
        static let standard = Spacing()
    }
}
