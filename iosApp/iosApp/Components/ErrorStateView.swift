import SwiftUI

/**
 * Reusable error state component.
 * 
 * Displays error icon, message, and retry button.
 * Uses theme tokens for consistent spacing, colors, and shapes.
 */
struct ErrorStateView: View {
    let message: String
    let onRetry: () -> Void
    
    @Environment(\.pokemonTheme) var theme
    
    var body: some View {
        VStack(spacing: theme.spacing.md) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 50))
                .foregroundColor(.orange)
            
            Text("Oops!")
                .font(theme.typography.title)
            
            Text(message)
                .font(theme.typography.body)
                .foregroundColor(theme.colors.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, theme.spacing.md)
            
            Button(action: onRetry) {
                Text("Retry")
                    .font(theme.typography.button)
                    .foregroundColor(.white)
                    .padding(.horizontal, theme.spacing.xl)
                    .padding(.vertical, theme.spacing.sm)
                    .background(Color.blue)
                    .clipShape(RoundedRectangle(cornerRadius: theme.shapes.md))
            }
            .padding(.top, theme.spacing.xs)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("Error: \(message). Retry button available.")
    }
}

#Preview {
    ErrorStateView(message: "Failed to load Pokémon data") {
        print("Retry tapped")
    }
}
