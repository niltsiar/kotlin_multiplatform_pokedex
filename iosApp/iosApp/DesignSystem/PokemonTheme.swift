import SwiftUI

/**
 * PokemonTheme provides centralized design tokens for consistent styling across the iOS app.
 * 
 * Usage:
 * ```swift
 * @Environment(\.pokemonTheme) var theme
 * 
 * Text("Hello")
 *     .font(theme.typography.title)
 *     .padding(theme.spacing.medium)
 * ```
 */
struct PokemonTheme {
    var spacing: PokemonTheme.Spacing
    var shapes: PokemonTheme.Shapes
    var typography: PokemonTheme.Typography
    var colors: PokemonTheme.Colors
    var motion: PokemonTheme.Motion
    
    static let `default` = PokemonTheme(
        spacing: PokemonTheme.Spacing.standard,
        shapes: PokemonTheme.Shapes.rounded,
        typography: PokemonTheme.Typography.system,
        colors: PokemonTheme.Colors.light,
        motion: PokemonTheme.Motion.emphasized
    )
}

// MARK: - Modern Environment Value (iOS 17+)

extension EnvironmentValues {
    @Entry var pokemonTheme: PokemonTheme = PokemonTheme.default
}

// MARK: - View Extension

extension View {
    func pokemonTheme(_ theme: PokemonTheme) -> some View {
        environment(\.pokemonTheme, theme)
    }
}
