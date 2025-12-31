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
    
    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: icon)
                .font(.system(size: 28))
                .foregroundColor(.blue)
            
            Text(label)
                .font(.system(size: 12))
                .foregroundColor(.secondary)
            
            Text(value)
                .font(.system(size: 16, weight: .semibold))
                .foregroundColor(.primary)
        }
        .frame(maxWidth: .infinity)
        .padding(16)
        .background(Color(red: 0.95, green: 0.95, blue: 0.95))
        .clipShape(RoundedRectangle(cornerRadius: 12))
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
