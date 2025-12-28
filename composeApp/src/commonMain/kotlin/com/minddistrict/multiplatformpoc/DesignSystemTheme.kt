package com.minddistrict.multiplatformpoc

import kotlinx.serialization.Serializable

/**
 * Enum representing the available design systems in the app.
 *
 * Used for theme switching between Material Design 3 and Compose Unstyled implementations.
 * Persisted via SavedStateHandle to survive configuration changes and process death.
 */
@Serializable
enum class DesignSystemTheme {
    /**
     * Material Design 3 Expressive theme with official Material components.
     */
    MATERIAL,
    
    /**
     * Compose Unstyled theme with custom-styled components.
     */
    UNSTYLED
}
