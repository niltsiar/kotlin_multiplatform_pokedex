package com.minddistrict.multiplatformpoc.core.designsystem.unstyled

/**
 * Marker class for Unstyled design system scope.
 * 
 * Used to scope Koin navigation entries to Unstyled theme implementation.
 * All Unstyled UI navigation entries should be registered in a scope<UnstyledScope> block.
 * 
 * Example:
 * ```
 * val myNavigationModule = module {
 *     scope<UnstyledScope> {
 *         navigation<MyRoute> { route ->
 *             UnstyledTheme {
 *                 MyUnstyledScreen(...)
 *             }
 *         }
 *     }
 * }
 * ```
 */
class UnstyledScope
