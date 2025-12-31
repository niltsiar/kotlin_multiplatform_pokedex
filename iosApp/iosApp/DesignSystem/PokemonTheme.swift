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
    var spacing: Spacing
    var shapes: Shapes
    var typography: Typography
    var colors: Colors
    var motion: Motion
    
    static let `default` = PokemonTheme(
        spacing: .standard,
        shapes: .rounded,
        typography: .system,
        colors: .light,
        motion: .emphasized
    )
}

// MARK: - Environment Key

private struct PokemonThemeKey: EnvironmentKey {
    static let defaultValue = PokemonTheme.default
}

extension EnvironmentValues {
    var pokemonTheme: PokemonTheme {
        get { self[PokemonThemeKey.self] }
        set { self[PokemonThemeKey.self] = newValue }
    }
}

// MARK: - View Extension

extension View {
    func pokemonTheme(_ theme: PokemonTheme) -> some View {
        environment(\.pokemonTheme, theme)
    }
}
