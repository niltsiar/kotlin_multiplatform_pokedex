import SwiftUI
import Shared

/**
 * Pokemon detail screen displaying comprehensive information.
 * 
 * Professional redesign matching Compose Unstyled quality:
 * - Hero section with 256pt image
 * - Type badges using TypeBadgeView
 * - Physical info cards (height, weight, base XP)
 * - Abilities with "Hidden" indicators
 * - Base stats with animated progress bars using StatBarView
 * - Theme spacing tokens throughout (20dp/16dp/12dp)
 * - Three UI states: Loading, Content, Error
 * - AsyncStream lifecycle management via .task
 * 
 * CONSISTENT SPACING:
 * - Section spacing: 24pt
 * - Component spacing: 12pt
 * - Content padding: 20pt (horizontal)
 */
struct PokemonDetailView: View {
    let pokemonId: Int
    
    @Environment(\.pokemonTheme) var theme
    @StateObject private var owner = IosViewModelStoreOwner()
    
    // Computed property delegates to generic viewModel(intParam:)
    private var viewModel: PokemonDetailViewModel {
        owner.viewModel(intParam: pokemonId)
    }
    
    @State private var uiState: PokemonDetailUiState = PokemonDetailUiStateLoading()
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        content
            .navigationBarTitleDisplayMode(.inline)
            .onAppear {
                viewModel.onStart(owner: Shared.DummyLifecycleOwner())
            }
            .task {
                // Observe StateFlow - automatically cancels when view disappears
                for await state in viewModel.uiState {
                    uiState = state
                }
            }
            .onDisappear {
                viewModel.onStop(owner: Shared.DummyLifecycleOwner())
            }
    }
    
    @ViewBuilder
    private var content: some View {
        switch uiState {
        case is PokemonDetailUiStateLoading:
            LoadingStateView(message: "Loading Pokémon details...")
            
        case let content as PokemonDetailUiStateContent:
            detailContent(pokemon: content.pokemon)
            
        case let error as PokemonDetailUiStateError:
            ErrorStateView(message: error.message) {
                viewModel.retry()
            }
            
        default:
            EmptyView()
        }
    }
    
    // MARK: - Detail Content
    
    private func detailContent(pokemon: PokemonDetail) -> some View {
        ScrollView {
            VStack(spacing: theme.spacing.xl) {  // Section spacing using theme
                // Hero section with sprite (256pt image)
                heroSection(pokemon: pokemon)
                
                // Type badges
                typeBadgesSection(types: pokemon.types)
                
                // Physical info cards
                physicalInfoSection(pokemon: pokemon)
                
                // Abilities
                abilitiesSection(abilities: pokemon.abilities)
                
                // Base stats
                baseStatsSection(stats: pokemon.stats)
            }
            .padding(.bottom, 32)
        }
        .navigationTitle(pokemon.name.capitalized)
    }
    
    // MARK: - Hero Section
    
    private func heroSection(pokemon: PokemonDetail) -> some View {
        VStack(spacing: theme.spacing.sm) {
            AsyncImage(url: URL(string: pokemon.imageUrl)) { phase in
                switch phase {
                case .success(let image):
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .frame(width: 256, height: 256)  // 256pt matching Compose
                case .failure:
                    Image(systemName: "photo.fill")
                        .font(.system(size: 80))
                        .foregroundColor(.secondary)
                        .frame(width: 256, height: 256)
                case .empty:
                    ProgressView()
                        .frame(width: 256, height: 256)
                @unknown default:
                    EmptyView()
                }
            }
            
            Text("#\(String(format: "%03d", pokemon.id))")
                .font(theme.typography.title)
                .foregroundColor(theme.colors.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, theme.spacing.xl)
        .background(theme.colors.surface)
    }
    
    // MARK: - Type Badges
    
    private func typeBadgesSection(types: [TypeOfPokemon]) -> some View {
        HStack(spacing: theme.spacing.sm) {
            ForEach(types, id: \.name) { type in
                TypeBadgeView(typeName: type.name)
            }
        }
        .padding(.horizontal, theme.spacing.lg)
    }
    
    // MARK: - Physical Info
    
    private func physicalInfoSection(pokemon: PokemonDetail) -> some View {
        HStack(spacing: theme.spacing.sm) {
            PhysicalInfoCardView(
                icon: "figure.walk",
                label: "Height",
                value: String(format: "%.1f m", Double(pokemon.height) / 10)
            )
            
            PhysicalInfoCardView(
                icon: "scalemass",
                label: "Weight",
                value: String(format: "%.1f kg", Double(pokemon.weight) / 10)
            )
            
            PhysicalInfoCardView(
                icon: "star.fill",
                label: "Base XP",
                value: "\(pokemon.baseExperience)"
            )
        }
        .padding(.horizontal, 20)
    }
    
    // MARK: - Abilities
    
    private func abilitiesSection(abilities: [Ability]) -> some View {
        VStack(alignment: .leading, spacing: theme.spacing.sm) {
            Text("Abilities")
                .font(theme.typography.title)
                .padding(.horizontal, theme.spacing.lg)
            
            VStack(spacing: theme.spacing.xs) {
                ForEach(abilities, id: \.name) { ability in
                    HStack {
                        Text(ability.name.replacingOccurrences(of: "-", with: " ").capitalized)
                            .font(theme.typography.body)
                        
                        Spacer()
                        
                        if ability.isHidden {
                            Text("Hidden")
                                .font(.system(size: 12, weight: .medium))
                                .foregroundColor(.white)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(Color.purple)
                                .clipShape(RoundedRectangle(cornerRadius: 8))
                        }
                    }
                    .padding(.vertical, theme.spacing.xs)
                    .padding(.horizontal, theme.spacing.md)
                }
            }
            .background(theme.colors.surface)
            .clipShape(RoundedRectangle(cornerRadius: theme.shapes.md))
            .padding(.horizontal, theme.spacing.lg)
        }
    }
    
    // MARK: - Base Stats
    
    private func baseStatsSection(stats: [Stat]) -> some View {
        VStack(alignment: .leading, spacing: theme.spacing.md) {
            Text("Base Stats")
                .font(theme.typography.title)
                .padding(.horizontal, theme.spacing.lg)
            
            VStack(spacing: theme.spacing.sm) {
                ForEach(stats, id: \.name) { stat in
                    StatBarView(name: stat.name, value: stat.baseStat)
                }
            }
            .padding(theme.spacing.md)
            .background(theme.colors.surface)
            .clipShape(RoundedRectangle(cornerRadius: theme.shapes.md))
            .padding(.horizontal, theme.spacing.lg)
        }
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
