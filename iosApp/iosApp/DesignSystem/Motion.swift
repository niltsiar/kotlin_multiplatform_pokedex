import SwiftUI

/**
 * Motion tokens for animation timing.
 * Uses iOS native spring animations with Material 3 Expressive timing.
 */
extension PokemonTheme {
    struct Motion {
        // Spring animations (iOS native)
        let spring: Animation = .spring(response: 0.3, dampingFraction: 0.7)
        let springBouncy: Animation = .spring(response: 0.4, dampingFraction: 0.6)
        
        // Durations matching BaseTokens.motion
        let durationShort: Double = 0.2     // 200ms
        let durationMedium: Double = 0.3    // 300ms
        let durationLong: Double = 0.4      // 400ms
        
        static let emphasized = Motion()
    }
}
