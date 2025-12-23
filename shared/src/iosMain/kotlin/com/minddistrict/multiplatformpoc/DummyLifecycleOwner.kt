package com.minddistrict.multiplatformpoc

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * Minimal LifecycleOwner implementation for iOS.
 * 
 * Used to satisfy DefaultLifecycleObserver's LifecycleOwner parameter requirement
 * when calling onStart()/onStop() from SwiftUI views.
 * 
 * Note: This is a simple stub. The actual lifecycle is managed by SwiftUI's
 * @StateObject and the ViewModel's onCleared() is called by ViewModelStore.clear()
 * in IosViewModelStoreOwner's deinit.
 */
class DummyLifecycleOwner : LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
}
