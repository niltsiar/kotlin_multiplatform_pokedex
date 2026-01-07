import SwiftUI
import Shared

/**
 * Card component displaying a single Pokemon in the grid.
 * 
 * Features:
 * - AsyncImage for sprite loading with SF Symbol placeholder/error
 * - Formatted Pokédex number (#001, #025, etc.)
 * - Title case Pokemon name
 * - CONSISTENT SIZE using aspectRatio (1:1 square)
 * - Tap scale animation with haptic feedback
 * - iOS semantic colors for dark mode support
 * - VoiceOver accessibility
 * 
 * CRITICAL: Uses .aspectRatio(1, contentMode: .fit) to ensure all cards have exact same size
 */
struct PokemonCard: View {
    let pokemon: Pokemon
    let onTap: () -> Void
    
    @Environment(\.pokemonTheme) var theme
    @State private var isPressed = false
    
    var body: some View {
        Button(action: {
            onTap()
        }) {
            VStack(spacing: 8) {
                // Pokemon sprite image
                AsyncImage(url: URL(string: pokemon.imageUrl)) { phase in
                    switch phase {
                    case .success(let image):
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(maxWidth: .infinity, maxHeight: 96)
                    case .failure:
                        // Show SF Symbol on load failure
                        Image(systemName: "photo.fill")
                            .font(.system(size: 40))
                            .foregroundColor(.secondary)
                            .frame(maxWidth: .infinity, maxHeight: 96)
                    case .empty:
                        // Show loading placeholder
                        ProgressView()
                            .frame(maxWidth: .infinity, maxHeight: 96)
                    @unknown default:
                        EmptyView()
                    }
                }
                .frame(height: 96)
                
                // Pokédex number (e.g., #025)
                Text("#\(String(format: "%03d", pokemon.id))")
                    .font(theme.typography.caption)
                    .foregroundColor(theme.colors.secondary)
                
                // Pokemon name (title case)
                Text(pokemon.name.capitalized)
                    .font(theme.typography.headline)
                    .foregroundColor(theme.colors.onSurface)
                    .lineLimit(1)
            }
            .padding(theme.spacing.sm)
            .frame(maxWidth: .infinity)
            .aspectRatio(1, contentMode: .fit)  // CRITICAL: Ensures consistent size
            .background(theme.colors.surface)
            .clipShape(RoundedRectangle(cornerRadius: theme.shapes.xl))
            .shadow(color: Color.black.opacity(0.08), radius: 4, x: 0, y: 2)
        }
        .buttonStyle(.plain)
        .scaleEffect(isPressed ? 0.95 : 1.0)
        .animation(theme.motion.spring, value: isPressed)
        .simultaneousGesture(
            DragGesture(minimumDistance: 0)
                .onChanged { _ in
                    if !isPressed {
                        isPressed = true
                        // Trigger haptic feedback
                        let impact = UIImpactFeedbackGenerator(style: .light)
                        impact.impactOccurred()
                    }
                }
                .onEnded { _ in
                    isPressed = false
                }
        )
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("Pokémon \(pokemon.name.capitalized), number \(pokemon.id)")
        .accessibilityAddTraits(.isButton)
    }
}

// MARK: - Preview

#Preview("Pikachu") {
    PokemonCard(
        pokemon: Pokemon(
            name: "pikachu",
            detailUrl: "https://pokeapi.co/api/v2/pokemon/25/"
        ),
        onTap: {}
    )
    .frame(width: 160)
    .padding()
}

#Preview("Bulbasaur") {
    PokemonCard(
        pokemon: Pokemon(
            name: "bulbasaur",
            detailUrl: "https://pokeapi.co/api/v2/pokemon/1/"
        ),
        onTap: {}
    )
    .frame(width: 160)
    .padding()
}

#Preview("Long Name") {
    PokemonCard(
        pokemon: Pokemon(
            name: "articuno",
            detailUrl: "https://pokeapi.co/api/v2/pokemon/144/"
        ),
        onTap: {}
    )
    .frame(width: 160)
    .padding()
}

#Preview("Dark Mode") {
    PokemonCard(
        pokemon: Pokemon(
            name: "pikachu",
            detailUrl: "https://pokeapi.co/api/v2/pokemon/25/"
        ),
        onTap: {}
    )
    .frame(width: 160)
    .padding()
    .preferredColorScheme(.dark)
}
