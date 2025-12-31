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
    
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 50))
                .foregroundColor(.orange)
            
            Text("Oops!")
                .font(.system(size: 22, weight: .bold))
            
            Text(message)
                .font(.system(size: 16))
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 16)
            
            Button(action: onRetry) {
                Text("Retry")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(.white)
                    .padding(.horizontal, 32)
                    .padding(.vertical, 12)
                    .background(Color.blue)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
            }
            .padding(.top, 8)
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
