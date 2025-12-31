import SwiftUI

/**
 * Animated stat bar component.
 * 
 * Displays a stat name, value, and progress bar with smooth animation.
 * Color coding: Red (<50), Yellow (50-99), Green (100+)
 */
struct StatBarView: View {
    let name: String
    let value: Int32
    let maxValue: Int32 = 255
    
    @Environment(\.pokemonTheme) private var theme
    @State private var animatedProgress: CGFloat = 0
    
    private var progress: CGFloat {
        CGFloat(value) / CGFloat(maxValue)
    }
    
    private var displayName: String {
        name.replacingOccurrences(of: "-", with: " ")
            .split(separator: " ")
            .map { $0.prefix(1).uppercased() + $0.dropFirst() }
            .joined(separator: " ")
    }
    
    private var barColor: Color {
        if value < 50 {
            return Color(red: 0.96, green: 0.26, blue: 0.21) // Red
        } else if value < 100 {
            return Color(red: 1.0, green: 0.92, blue: 0.23)  // Yellow
        } else {
            return Color(red: 0.30, green: 0.69, blue: 0.31) // Green
        }
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: theme.spacing.xxs) {
            HStack {
                Text(displayName)
                    .font(theme.typography.caption)
                    .foregroundColor(theme.colors.secondary)
                    .frame(width: 100, alignment: .leading)
                
                Text("\(value)")
                    .font(theme.typography.body)
                    .foregroundColor(theme.colors.onSurface)
                    .frame(width: 40, alignment: .trailing)
                
                GeometryReader { geometry in
                    ZStack(alignment: .leading) {
                        // Background bar
                        RoundedRectangle(cornerRadius: theme.shapes.xs)
                            .fill(theme.colors.surface)
                            .frame(height: 8)
                        
                        // Progress bar with animation
                        RoundedRectangle(cornerRadius: theme.shapes.xs)
                            .fill(barColor)
                            .frame(width: geometry.size.width * animatedProgress, height: 8)
                    }
                }
                .frame(height: 8)
            }
        }
        .onAppear {
            withAnimation(.easeOut(duration: theme.motion.durationMedium)) {
                animatedProgress = progress
            }
        }
    }
}

#Preview("Low Stat") {
    StatBarView(name: "speed", value: 35)
        .padding()
}

#Preview("Medium Stat") {
    StatBarView(name: "attack", value: 82)
        .padding()
}

#Preview("High Stat") {
    StatBarView(name: "defense", value: 120)
        .padding()
}

#Preview("All Stats") {
    VStack(spacing: 12) {
        StatBarView(name: "hp", value: 45)
        StatBarView(name: "attack", value: 49)
        StatBarView(name: "defense", value: 49)
        StatBarView(name: "special-attack", value: 65)
        StatBarView(name: "special-defense", value: 65)
        StatBarView(name: "speed", value: 45)
    }
    .padding()
}
