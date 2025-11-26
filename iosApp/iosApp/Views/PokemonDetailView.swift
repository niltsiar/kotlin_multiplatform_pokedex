import SwiftUI
import Shared

/**
 * Pokemon detail screen displaying comprehensive information.
 * 
 * Features:
 * - Hero section with large sprite and gradient background
 * - Type badges with color-coded backgrounds
 * - Physical info (height, weight, base XP)
 * - Abilities with "Hidden" indicators
 * - Base stats with animated progress bars
 * - Three UI states: Loading, Content, Error
 * - AsyncStream lifecycle management via .task
 * - Back navigation with swipe gesture support
 * 
 * This view integrates with the KMP PokemonDetailViewModel via PokemonDetailViewModelWrapper,
 * observing StateFlow updates and triggering retry on errors.
 */
struct PokemonDetailView: View {
    let pokemonId: Int
    private var wrapper: PokemonDetailViewModel
    @State private var uiState: PokemonDetailUiState = PokemonDetailUiStateLoading()
    @Environment(\.dismiss) private var dismiss
    
    init(pokemonId: Int) {
        self.pokemonId = pokemonId
        wrapper = KoinIosKt.getPokemonDetailViewModel(pokemonId: Int32(pokemonId))
    }
    
    var body: some View {
        content
            .navigationBarTitleDisplayMode(.inline)
            .task {
                // Observe StateFlow - automatically cancels when view disappears
                for await state in wrapper.uiState {
                    uiState = state
                }
            }
    }
    
    @ViewBuilder
    private var content: some View {
        switch uiState {
        case is PokemonDetailUiStateLoading:
            loadingView
            
        case let content as PokemonDetailUiStateContent:
            detailContent(pokemon: content.pokemon)
            
        case let error as PokemonDetailUiStateError:
            errorView(message: error.message)
            
        default:
            EmptyView()
        }
    }
    
    // MARK: - Loading State
    
    private var loadingView: some View {
        VStack(spacing: 16) {
            ProgressView()
                .scaleEffect(1.5)
            Text("Loading Pokémon...")
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("Loading Pokémon details")
    }
    
    // MARK: - Detail Content
    
    private func detailContent(pokemon: PokemonDetail) -> some View {
        ScrollView {
            VStack(spacing: 24) {
                // Hero section with sprite
                heroSection(pokemon: pokemon)
                
                // Type badges
                typeBadgesSection(types: pokemon.types)
                
                // Physical info
                physicalInfoSection(pokemon: pokemon)
                
                // Abilities
                abilitiesSection(abilities: pokemon.abilities)
                
                // Base stats
                baseStatsSection(stats: pokemon.stats)
            }
            .padding(.bottom, 32)
        }
        .navigationTitle(pokemon.name)
    }
    
    // MARK: - Hero Section
    
    private func heroSection(pokemon: PokemonDetail) -> some View {
        VStack(spacing: 12) {
            AsyncImage(url: URL(string: pokemon.imageUrl)) { phase in
                switch phase {
                case .success(let image):
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 200, height: 200)
                case .failure:
                    Image(systemName: "photo.fill")
                        .font(.system(size: 80))
                        .foregroundColor(.secondary)
                        .frame(width: 200, height: 200)
                case .empty:
                    ProgressView()
                        .frame(width: 200, height: 200)
                @unknown default:
                    EmptyView()
                }
            }
            
            Text("#\(String(format: "%03d", pokemon.id))")
                .font(.title2)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 24)
        .background(
            typeGradient(types: pokemon.types)
        )
    }
    
    // MARK: - Type Badges
    
    private func typeBadgesSection(types: [Type_]) -> some View {
        HStack(spacing: 12) {
            ForEach(types, id: \.name) { type in
                Text(type.name.capitalized)
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .foregroundColor(.white)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                    .background(typeColor(for: type.name))
                    .cornerRadius(16)
            }
        }
        .padding(.horizontal)
    }
    
    // MARK: - Physical Info
    
    private func physicalInfoSection(pokemon: PokemonDetail) -> some View {
        HStack(spacing: 16) {
            infoCard(
                icon: "figure.walk",
                label: "Height",
                value: String(format: "%.1f m", Double(pokemon.height) / 10)
            )
            
            infoCard(
                icon: "scalemass",
                label: "Weight",
                value: String(format: "%.1f kg", Double(pokemon.weight) / 10)
            )
            
            infoCard(
                icon: "star.fill",
                label: "Base XP",
                value: "\(pokemon.baseExperience)"
            )
        }
        .padding(.horizontal)
    }
    
    private func infoCard(icon: String, label: String, value: String) -> some View {
        VStack(spacing: 8) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(.blue)
            
            Text(label)
                .font(.caption)
                .foregroundColor(.secondary)
            
            Text(value)
                .font(.headline)
                .foregroundColor(.primary)
        }
        .frame(maxWidth: .infinity)
        .padding(16)
        .background(Color(UIColor.secondarySystemBackground))
        .cornerRadius(12)
    }
    
    // MARK: - Abilities
    
    private func abilitiesSection(abilities: [Ability]) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Abilities")
                .font(.title2)
                .fontWeight(.bold)
                .padding(.horizontal)
            
            VStack(spacing: 8) {
                ForEach(abilities, id: \.name) { ability in
                    HStack {
                        Text(ability.name.replacingOccurrences(of: "-", with: " ").capitalized)
                            .font(.body)
                        
                        Spacer()
                        
                        if ability.isHidden {
                            Text("Hidden")
                                .font(.caption)
                                .fontWeight(.medium)
                                .foregroundColor(.white)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(Color.purple)
                                .cornerRadius(8)
                        }
                    }
                    .padding(.vertical, 8)
                    .padding(.horizontal, 16)
                }
            }
            .background(Color(UIColor.secondarySystemBackground))
            .cornerRadius(12)
            .padding(.horizontal)
        }
    }
    
    // MARK: - Base Stats
    
    private func baseStatsSection(stats: [Stat]) -> some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Base Stats")
                .font(.title2)
                .fontWeight(.bold)
                .padding(.horizontal)
            
            VStack(spacing: 12) {
                ForEach(Array(stats.enumerated()), id: \.element.name) { index, stat in
                    statRow(stat: stat, index: index)
                }
            }
            .padding(16)
            .background(Color(UIColor.secondarySystemBackground))
            .cornerRadius(12)
            .padding(.horizontal)
        }
    }
    
    @State private var animatedStats: [String: Double] = [:]
    
    private func statRow(stat: Stat, index: Int) -> some View {
        VStack(spacing: 4) {
            HStack {
                Text(formatStatName(stat.name))
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .frame(width: 100, alignment: .leading)
                
                Text("\(stat.baseStat)")
                    .font(.headline)
                    .foregroundColor(.primary)
                    .frame(width: 40, alignment: .trailing)
                
                GeometryReader { geometry in
                    ZStack(alignment: .leading) {
                        // Background bar
                        Rectangle()
                            .fill(Color(UIColor.systemGray5))
                            .frame(height: 8)
                            .cornerRadius(4)
                        
                        // Progress bar with animation
                        Rectangle()
                            .fill(statColor(value: stat.baseStat))
                            .frame(width: geometry.size.width * CGFloat(animatedStats[stat.name] ?? 0) / 255, height: 8)
                            .cornerRadius(4)
                    }
                }
                .frame(height: 8)
            }
        }
        .onAppear {
            // Animate stat bar with delay based on index
            withAnimation(.easeOut(duration: 0.6).delay(Double(index) * 0.1)) {
                animatedStats[stat.name] = Double(stat.baseStat)
            }
        }
    }
    
    // MARK: - Error State
    
    private func errorView(message: String) -> some View {
        VStack(spacing: 16) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 50))
                .foregroundColor(.orange)
            
            Text("Oops!")
                .font(.title2)
                .fontWeight(.bold)
            
            Text(message)
                .font(.body)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal)
            
            Button(action: {
                wrapper.retry()
            }) {
                Text("Retry")
                    .font(.headline)
                    .foregroundColor(.white)
                    .padding(.horizontal, 32)
                    .padding(.vertical, 12)
                    .background(Color.blue)
                    .cornerRadius(8)
            }
            .padding(.top, 8)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("Error: \(message). Retry button available.")
    }
    
    // MARK: - Helpers
    
    private func formatStatName(_ name: String) -> String {
        name.replacingOccurrences(of: "-", with: " ")
            .split(separator: " ")
            .map { $0.prefix(1).uppercased() + $0.dropFirst() }
            .joined(separator: " ")
    }
    
    private func statColor(value: Int32) -> Color {
        if value < 50 {
            return Color(red: 0.96, green: 0.26, blue: 0.21) // #F44336 (red)
        } else if value < 100 {
            return Color(red: 1.0, green: 0.92, blue: 0.23) // #FFEB3B (yellow)
        } else {
            return Color(red: 0.30, green: 0.69, blue: 0.31) // #4CAF50 (green)
        }
    }
    
    private func typeColor(for typeName: String) -> Color {
        // Pokemon type colors matching the design system
        switch typeName.lowercased() {
        case "normal": return Color(red: 0.66, green: 0.66, blue: 0.47)
        case "fire": return Color(red: 0.93, green: 0.51, blue: 0.19)
        case "water": return Color(red: 0.40, green: 0.56, blue: 0.95)
        case "electric": return Color(red: 0.98, green: 0.82, blue: 0.18)
        case "grass": return Color(red: 0.47, green: 0.78, blue: 0.30)
        case "ice": return Color(red: 0.60, green: 0.85, blue: 0.85)
        case "fighting": return Color(red: 0.75, green: 0.19, blue: 0.15)
        case "poison": return Color(red: 0.64, green: 0.25, blue: 0.64)
        case "ground": return Color(red: 0.89, green: 0.75, blue: 0.42)
        case "flying": return Color(red: 0.66, green: 0.56, blue: 0.95)
        case "psychic": return Color(red: 0.98, green: 0.33, blue: 0.45)
        case "bug": return Color(red: 0.66, green: 0.71, blue: 0.13)
        case "rock": return Color(red: 0.71, green: 0.63, blue: 0.42)
        case "ghost": return Color(red: 0.44, green: 0.35, blue: 0.60)
        case "dragon": return Color(red: 0.44, green: 0.21, blue: 0.99)
        case "dark": return Color(red: 0.44, green: 0.35, blue: 0.30)
        case "steel": return Color(red: 0.71, green: 0.71, blue: 0.82)
        case "fairy": return Color(red: 0.85, green: 0.51, blue: 0.68)
        default: return Color.gray
        }
    }
    
    private func typeGradient(types: [Type_]) -> LinearGradient {
        let colors = types.map { typeColor(for: $0.name).opacity(0.3) }
        return LinearGradient(
            gradient: Gradient(colors: colors),
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
    }
}

// MARK: - Previews

#Preview("Loading") {
    NavigationStack {
        PokemonDetailView(pokemonId: 25)
    }
}

#Preview("Dark Mode") {
    NavigationStack {
        PokemonDetailView(pokemonId: 25)
    }
    .preferredColorScheme(.dark)
}
