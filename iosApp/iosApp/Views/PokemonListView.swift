import SwiftUI
import Shared

/**
 * Main Pokemon list screen displaying an adaptive grid of Pokemon cards.
 * 
 * Features:
 * - ADAPTIVE GRID: 2/3/4 columns based on window width
 * - CONSISTENT CARD SIZING: All cards exactly same size using aspectRatio
 * - Infinite scroll (loads more at 4-item threshold)
 * - Three UI states: Loading, Content, Error
 * - Scroll position preservation on navigation return
 * - Navigation to detail screen
 * - AsyncStream lifecycle management via .task
 * 
 * Professional redesign matching Compose Unstyled quality.
 */

struct PokemonListView: View {
    @StateObject private var owner = IosViewModelStoreOwner()
    
    // Computed property delegates to generic viewModel() (ViewModelStore cached)
    private var viewModel: PokemonListViewModel {
        owner.viewModel()
    }
    
    @State private var navigationPath: [Int] = []
    @State private var scrollPosition: Int?
    @State private var uiState: PokemonListUiState = PokemonListUiStateLoading()
    
    var body: some View {
        NavigationStack(path: $navigationPath) {
            content
                .navigationTitle("Pokémon")
                .navigationDestination(for: Int.self) { pokemonId in
                    PokemonDetailView(pokemonId: pokemonId)
                }
        }
        .onAppear {
            // Restore UX state from ViewModel (survives SwiftUI view recreation).
            if scrollPosition == nil {
                scrollPosition = viewModel.restoredScrollAnchorPokemonId?.intValue
                    ?? viewModel.restoredLastSelectedPokemonId?.intValue
            }

            // Directly call ViewModel lifecycle method
            viewModel.onStart(owner: Shared.DummyLifecycleOwner())
        }
        .onDisappear {
            // Call lifecycle stop method
            viewModel.onStop(owner: Shared.DummyLifecycleOwner())
        }
        .task {
            // Observe StateFlow - automatically cancels when view disappears
            for await state in viewModel.uiState {
                self.uiState = state
            }
        }
    }
    
    @ViewBuilder
    private var content: some View {
        switch uiState {
        case is PokemonListUiStateLoading:
            LoadingStateView()
            
        case let content as PokemonListUiStateContent:
            gridView(content: content)
            
        case let error as PokemonListUiStateError:
            ErrorStateView(message: error.message) {
                viewModel.loadInitialPage()
            }
            
        default:
            EmptyView()
        }
    }
    
    // MARK: - Adaptive Grid Content
    
    private func gridView(content: PokemonListUiStateContent) -> some View {
        GeometryReader { geometry in
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVGrid(
                        columns: adaptiveColumns(for: geometry.size.width),
                        spacing: 20  // Consistent spacing matching Compose Unstyled
                    ) {
                        // Pokemon cards
                        ForEach(Array(content.pokemons.enumerated()), id: \.element.id) { index, pokemon in
                            PokemonCard(pokemon: pokemon) {
                                // Save scroll position before navigating
                                let id = Int(pokemon.id)
                                scrollPosition = id

                                // Persist selection + anchor for full state restoration.
                                viewModel.onPokemonSelected(pokemonId: Int32(id))
                                viewModel.onScrollAnchorPokemonIdChanged(pokemonId: Int32(id))

                                // Navigate to detail
                                navigationPath.append(id)
                            }
                            .id(Int(pokemon.id))
                            .onAppear {
                                // Trigger infinite scroll when approaching end (4 items threshold)
                                let pokemonCount = Int(content.pokemons.count)
                                if index >= pokemonCount - 4 &&
                                   !content.isLoadingMore &&
                                   content.hasMore {
                                    viewModel.loadNextPage()
                                }
                            }
                        }
                        
                        // Bottom loading indicator during pagination
                        if content.isLoadingMore {
                            HStack {
                                Spacer()
                                ProgressView()
                                    .padding()
                                Spacer()
                            }
                            .gridCellColumns(adaptiveColumnCount(for: geometry.size.width))
                        }
                    }
                    .padding(20)  // Consistent padding matching Compose Unstyled
                }
                .onAppear {
                    // Restore scroll position when returning from detail
                    if let position = scrollPosition {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                            withAnimation {
                                proxy.scrollTo(position, anchor: .center)
                            }
                        }
                    }
                }
            }
        }
    }
    
    // MARK: - Adaptive Grid Logic (2/3/4 columns based on width)
    
    /// Returns adaptive grid columns based on window width
    /// - Compact (< 600pt): 2 columns
    /// - Medium (600-900pt): 3 columns
    /// - Expanded (> 900pt): 4 columns
    private func adaptiveColumns(for width: CGFloat) -> [GridItem] {
        let count = adaptiveColumnCount(for: width)
        return Array(repeating: GridItem(.flexible(), spacing: 20), count: count)
    }
    
    private func adaptiveColumnCount(for width: CGFloat) -> Int {
        if width < 600 {
            return 2  // Compact - phones in portrait
        } else if width < 900 {
            return 3  // Medium - phones in landscape, small tablets
        } else {
            return 4  // Expanded - large tablets, desktop
        }
    }
}


// MARK: - Previews

#Preview("Loading") {
    PokemonListView()
}

#Preview("Dark Mode") {
    PokemonListView()
        .preferredColorScheme(.dark)
}
