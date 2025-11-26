import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        // Initialize Koin DI for iOS
        // This must happen before any views try to inject dependencies
        KoinIosKt.doInitKoin(baseUrl: "https://pokeapi.co/api/v2")
    }
    
    var body: some Scene {
        WindowGroup {
            PokemonListView()
        }
    }
}