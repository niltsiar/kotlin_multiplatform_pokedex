import SwiftUI
import Shared

/**
 * Pokemon detail screen placeholder.
 * 
 * This is a temporary placeholder view that displays the Pokemon ID.
 * In the future, this will be replaced with a full detail implementation including:
 * - Hero image (large Pokemon sprite)
 * - Name and Pokédex number
 * - Type badges with official colors
 * - Physical attributes (height, weight, base experience)
 * - Abilities list
 * - Base stats with progress bars
 * 
 * For now, it serves as a navigation target to test the list → detail flow
 * and scroll position preservation.
 */
struct PokemonDetailView: View {
    let pokemonId: Int
    
    var body: some View {
        VStack(spacing: 24) {
            // Placeholder icon
            Image(systemName: "questionmark.circle.fill")
                .font(.system(size: 80))
                .foregroundColor(.blue)
            
            // Pokemon ID
            Text("Pokémon #\(String(format: "%03d", pokemonId))")
                .font(.largeTitle)
                .fontWeight(.bold)
            
            // Coming soon message
            VStack(spacing: 8) {
                Text("Detail view coming soon!")
                    .font(.title3)
                    .foregroundColor(.primary)
                
                Text("This will include stats, abilities, and more")
                    .font(.body)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
            }
            .padding(.horizontal)
            
            Spacer()
        }
        .padding(.top, 40)
        .navigationTitle("Detail")
        .navigationBarTitleDisplayMode(.inline)
    }
}

// MARK: - Previews

#Preview("Pikachu") {
    NavigationStack {
        PokemonDetailView(pokemonId: 25)
    }
}

#Preview("Bulbasaur") {
    NavigationStack {
        PokemonDetailView(pokemonId: 1)
    }
}

#Preview("Dark Mode") {
    NavigationStack {
        PokemonDetailView(pokemonId: 25)
    }
    .preferredColorScheme(.dark)
}
