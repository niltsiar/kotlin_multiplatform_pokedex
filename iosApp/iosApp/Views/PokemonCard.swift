import SwiftUI
import Shared

/**
 * Card component displaying a single Pokemon in the grid.
 * 
 * Features:
 * - AsyncImage for sprite loading with SF Symbol placeholder/error
 * - Formatted Pokédex number (#001, #025, etc.)
 * - Title case Pokemon name
 * - Tap scale animation (1.0 → 0.95 → 1.0)
 * - Haptic feedback on tap
 * - iOS semantic colors for dark mode support
 * - VoiceOver accessibility
 * 
 * Usage:
 * ```swift
 * PokemonCard(pokemon: pokemon) {
 *     // Handle tap - navigate to detail
 * }
 * ```
 */
struct PokemonCard: View {
    let pokemon: Pokemon
    let onTap: () -> Void
    
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
                            .frame(width: 96, height: 96)
                    case .failure:
                        // Show SF Symbol on load failure
                        Image(systemName: "photo.fill")
                            .font(.system(size: 40))
                            .foregroundColor(.secondary)
                            .frame(width: 96, height: 96)
                    case .empty:
                        // Show loading placeholder
                        ProgressView()
                            .frame(width: 96, height: 96)
                    @unknown default:
                        EmptyView()
                    }
                }
                
                // Pokédex number (e.g., #025)
                Text("#\(String(format: "%03d", pokemon.id))")
                    .font(.caption)
                    .foregroundColor(.secondary)
                
                // Pokemon name (title case)
                Text(pokemon.name.capitalized)
                    .font(.headline)
                    .foregroundColor(.primary)
                    .lineLimit(1)
            }
            .padding(12)
            .background(Color(UIColor.systemBackground))
            .cornerRadius(12)
            .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 2)
        }
        .buttonStyle(.plain)
        .scaleEffect(isPressed ? 0.95 : 1.0)
        .animation(.spring(response: 0.3, dampingFraction: 0.6), value: isPressed)
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
            id: 25,
            name: "pikachu",
            imageUrl: "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"
        ),
        onTap: {}
    )
    .frame(width: 160)
    .padding()
}

#Preview("Bulbasaur") {
    PokemonCard(
        pokemon: Pokemon(
            id: 1,
            name: "bulbasaur",
            imageUrl: "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png"
        ),
        onTap: {}
    )
    .frame(width: 160)
    .padding()
}

#Preview("Long Name") {
    PokemonCard(
        pokemon: Pokemon(
            id: 144,
            name: "articuno",
            imageUrl: "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/144.png"
        ),
        onTap: {}
    )
    .frame(width: 160)
    .padding()
}

#Preview("Dark Mode") {
    PokemonCard(
        pokemon: Pokemon(
            id: 25,
            name: "pikachu",
            imageUrl: "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"
        ),
        onTap: {}
    )
    .frame(width: 160)
    .padding()
    .preferredColorScheme(.dark)
}
