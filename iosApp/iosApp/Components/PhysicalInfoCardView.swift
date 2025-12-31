import SwiftUI

/**
 * Physical info card component.
 * 
 * Displays a single physical attribute (height, weight, base XP) with icon and value.
 * Uses consistent sizing and spacing for professional appearance.
 */
struct PhysicalInfoCardView: View {
    let icon: String
    let label: String
    let value: String
    
    @Environment(\.pokemonTheme) var theme
    
    var body: some View {
        VStack(spacing: theme.spacing.xs) {
            Image(systemName: icon)
                .font(.system(size: 28))
                .foregroundColor(.blue)
            
            Text(label)
                .font(theme.typography.caption)
                .foregroundColor(theme.colors.secondary)
            
            Text(value)
                .font(theme.typography.body)
                .foregroundColor(theme.colors.onSurface)
        }
        .frame(maxWidth: .infinity)
        .padding(theme.spacing.md)
        .background(theme.colors.surface)
        .clipShape(RoundedRectangle(cornerRadius: theme.shapes.lg))
    }
}

#Preview {
    HStack(spacing: 12) {
        PhysicalInfoCardView(icon: "figure.walk", label: "Height", value: "0.7 m")
        PhysicalInfoCardView(icon: "scalemass", label: "Weight", value: "6.9 kg")
        PhysicalInfoCardView(icon: "star.fill", label: "Base XP", value: "64")
    }
    .padding()
}
