import SwiftUI

/**
 * Reusable loading state component.
 * 
 * Displays a centered loading spinner with message.
 * Uses theme tokens for consistent spacing and typography.
 */
struct LoadingStateView: View {
    var message: String = "Loading Pokémon..."
    
    var body: some View {
        VStack(spacing: 16) {
            ProgressView()
                .scaleEffect(1.5)
            Text(message)
                .font(.system(size: 16))
                .foregroundColor(.secondary)
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
