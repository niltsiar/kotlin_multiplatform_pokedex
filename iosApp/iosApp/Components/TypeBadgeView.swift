import SwiftUI
import Shared

/**
 * Pokémon type badge component.
 * 
 * Displays a type name with color-coded background matching the type.
 * Uses PokemonType color system for consistent coloring.
 */
struct TypeBadgeView: View {
    let typeName: String
    
    @Environment(\.colorScheme) var colorScheme
    
    private var pokemonType: PokemonType? {
        PokemonType(string: typeName)
    }
    
    var body: some View {
        Text(typeName.capitalized)
            .font(.system(size: 14, weight: .semibold))
            .foregroundColor(pokemonType?.contentColor ?? .white)
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(
                pokemonType?.backgroundColor(isDark: colorScheme == .dark) ?? Color.gray
            )
            .clipShape(Capsule())
    }
}

// MARK: - Preview

#Preview("Fire") {
    TypeBadgeView(typeName: "fire")
        .padding()
}

#Preview("Water") {
    TypeBadgeView(typeName: "water")
        .padding()
}

#Preview("Grass") {
    TypeBadgeView(typeName: "grass")
        .padding()
}

#Preview("Multiple Types") {
    HStack(spacing: 8) {
        TypeBadgeView(typeName: "fire")
        TypeBadgeView(typeName: "flying")
    }
    .padding()
}

#Preview("Dark Mode") {
    VStack(spacing: 12) {
        TypeBadgeView(typeName: "fire")
        TypeBadgeView(typeName: "water")
        TypeBadgeView(typeName: "psychic")
    }
    .padding()
    .preferredColorScheme(.dark)
}
