import SwiftUI
import Shared

/**
 * Type badge displaying a Pokémon type with colored background.
 * Matches TypeBadge from Compose Multiplatform.
 * 
 * Usage:
 * ```swift
 * TypeBadgeView(type: "fire")
 * ```
 */
struct TypeBadgeView: View {
    @Environment(\.pokemonTheme) var theme
    @Environment(\.colorScheme) var colorScheme
    
    let type: String
    
    var pokemonType: PokemonType {
        PokemonType(string: type) ?? .normal
    }
    
    var body: some View {
        Text(type.capitalized)
            .font(theme.typography.label)
            .foregroundColor(pokemonType.contentColor)
            .padding(.horizontal, theme.spacing.md)
            .padding(.vertical, theme.spacing.xs)
            .background(pokemonType.backgroundColor(isDark: colorScheme == .dark))
            .cornerRadius(theme.shapes.lg)
    }
}

// MARK: - Preview

#Preview("Fire") {
    TypeBadgeView(type: "fire")
        .padding()
}

#Preview("Water") {
    TypeBadgeView(type: "water")
        .padding()
}

#Preview("Grass") {
    TypeBadgeView(type: "grass")
        .padding()
}

#Preview("Multiple Types") {
    HStack(spacing: 8) {
        TypeBadgeView(type: "fire")
        TypeBadgeView(type: "flying")
    }
    .padding()
}

#Preview("Dark Mode") {
    VStack(spacing: 12) {
        TypeBadgeView(type: "fire")
        TypeBadgeView(type: "water")
        TypeBadgeView(type: "psychic")
    }
    .padding()
    .preferredColorScheme(.dark)
}
