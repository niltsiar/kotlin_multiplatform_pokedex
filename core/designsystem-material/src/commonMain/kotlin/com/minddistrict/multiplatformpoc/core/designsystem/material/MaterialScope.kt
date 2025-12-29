package com.minddistrict.multiplatformpoc.core.designsystem.material

/**
 * Marker class for Material Design system scope.
 * 
 * Used to scope Koin navigation entries to Material theme implementation.
 * All Material UI navigation entries should be registered in a scope<MaterialScope> block.
 * 
 * Example:
 * ```
 * val myNavigationModule = module {
 *     scope<MaterialScope> {
 *         navigation<MyRoute> { route ->
 *             MyMaterialScreen(...)
 *         }
 *     }
 * }
 * ```
 */
class MaterialScope
