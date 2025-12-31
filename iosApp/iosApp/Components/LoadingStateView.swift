import SwiftUI

/**
 * Reusable loading state component.
 * 
 * Displays a centered loading spinner with message.
 * Uses theme tokens for consistent spacing and typography.
 */
struct LoadingStateView: View {
    var message: String = "Loading Pokémon..."
    
    @Environment(\.pokemonTheme) var theme
    
    var body: some View {
        VStack(spacing: theme.spacing.md) {
            ProgressView()
                .scaleEffect(1.5)
            Text(message)
                .font(theme.typography.body)
                .foregroundColor(theme.colors.secondary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .accessibilityElement(children: .combine)
        .accessibilityLabel(message)
    }
}

#Preview {
    LoadingStateView()
}

#Preview("Custom Message") {
    LoadingStateView(message: "Loading details...")
}
