package com.minddistrict.multiplatformpoc.core.designsystem.navigation

/**
 * Navigation destination contract for Material 3 Adaptive navigation.
 * 
 * Used with Metro DI multibinding (@IntoSet) to collect all app destinations
 * for NavigationSuiteScaffold configuration.
 */
interface AppDestination {
    /**
     * Type-safe navigation route (e.g., "pokemonList", "pokemonDetail/{id}")
     */
    val route: String
    
    /**
     * Display label for navigation item
     */
    val label: String
    
    /**
     * Whether this destination should appear in the navigation suite
     * (NavigationBar/Rail/Drawer)
     */
    val showInNavigation: Boolean
        get() = true
}
