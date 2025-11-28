package com.minddistrict.multiplatformpoc.core.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Navigator manages the navigation back stack for the application.
 * Follows the pattern from Android nav3-recipes modular architecture.
 *
 * @param startDestination The initial route to display
 */
class Navigator(startDestination: Any) {
    /**
     * The navigation back stack as a mutable state list.
     * Any changes to this list will automatically trigger recomposition.
     */
    val backStack: SnapshotStateList<Any> = mutableStateListOf(startDestination)

    /**
     * Navigate to a new destination by adding it to the back stack.
     *
     * @param destination The route object to navigate to
     */
    fun goTo(destination: Any) {
        backStack.add(destination)
    }

    /**
     * Navigate back by removing the last item from the back stack.
     * If the back stack only has one item (the start destination), does nothing.
     */
    fun goBack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }
}
