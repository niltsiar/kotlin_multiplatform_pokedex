package com.minddistrict.multiplatformpoc

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import kotlin.reflect.KClass
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.getOriginalKotlinClass
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform

/**
 * Generic ViewModel helpers using ObjCClass for type passing from Swift.
 * 
 * Aligned with official Android KMP ViewModel guide:
 * https://developer.android.com/kotlin/multiplatform/viewmodel#connect-viewmodel
 * 
 * Uses ObjCClass to preserve type information across Kotlin/Swift boundary.
 */

/**
 * Get any non-parametric ViewModel.
 * 
 * @param viewModelStore The ViewModelStore to cache ViewModels
 * @param viewModelClass The ObjCClass representing the ViewModel type from Swift
 * @return The cached or newly created ViewModel
 */
fun getViewModel(
    viewModelStore: ViewModelStore,
    viewModelClass: ObjCClass
): ViewModel {
    val kClass = getOriginalKotlinClass(viewModelClass)
        ?: error("Cannot get Kotlin class for ${viewModelClass}")
    
    val key = kClass.simpleName ?: error("Cannot get class name")
    
    return viewModelStore.get(key) ?: run {
        val vm = createViewModel(kClass)
        viewModelStore.put(key, vm)
        vm
    }
}

/**
 * Get any parametric ViewModel with Int parameter.
 * 
 * @param viewModelStore The ViewModelStore to cache ViewModels
 * @param viewModelClass The ObjCClass representing the ViewModel type from Swift
 * @param intParam The integer parameter (e.g., pokemonId)
 * @return The cached or newly created ViewModel
 */
fun getViewModelWithInt(
    viewModelStore: ViewModelStore,
    viewModelClass: ObjCClass,
    intParam: Int
): ViewModel {
    val kClass = getOriginalKotlinClass(viewModelClass)
        ?: error("Cannot get Kotlin class for ${viewModelClass}")
    
    val key = "${kClass.simpleName}:$intParam"
    
    return viewModelStore.get(key) ?: run {
        val vm = createViewModelWithInt(kClass, intParam)
        viewModelStore.put(key, vm)
        vm
    }
}

/**
 * Create a non-parametric ViewModel via Koin.
 */
private fun createViewModel(kClass: KClass<*>): ViewModel {
    return KoinPlatform.getKoin().get(kClass) {
        parametersOf(SavedStateHandle())
    } as ViewModel
}

/**
 * Create a parametric ViewModel with Int parameter via Koin.
 */
private fun createViewModelWithInt(kClass: KClass<*>, intParam: Int): ViewModel {
    return KoinPlatform.getKoin().get(kClass) {
        parametersOf(intParam, SavedStateHandle())
    } as ViewModel
}

