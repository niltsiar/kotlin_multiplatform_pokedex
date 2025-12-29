package com.minddistrict.multiplatformpoc

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.serialization.saved
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.plus

/**
 * Root-level ViewModel for managing app-wide design system theme selection.
 *
 * Persists the user's choice via SavedStateHandle, ensuring theme survives:
 * - Configuration changes (rotation, multi-window resize)
 * - Process death (low memory situations)
 * - App restarts
 *
 * Follows ViewModel pattern: passes viewModelScope to constructor, uses SavedStateHandle delegate.
 */
class RootViewModel(
    private val savedStateHandle: SavedStateHandle,
    viewModelScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + kotlinx.coroutines.Dispatchers.Main.immediate
    )
) : ViewModel(viewModelScope) {
    
    /**
     * Current design system theme, persisted automatically via SavedStateHandle delegate.
     * Defaults to MATERIAL on first launch.
     */
    private var _currentTheme by savedStateHandle.saved { DesignSystemTheme.MATERIAL }
    
    private val _currentThemeFlow = MutableStateFlow(_currentTheme)
    val currentTheme: StateFlow<DesignSystemTheme> = _currentThemeFlow.asStateFlow()
    
    /**
     * First-run flag to show intro dialog once.
     */
    private var _hasSeenIntro by savedStateHandle.saved { false }
    
    val hasSeenIntro: Boolean get() = _hasSeenIntro
    
    /**
     * Toggle between Material and Unstyled themes.
     * Automatically persisted via SavedStateHandle delegate.
     * Uses update {} lambda for thread-safe state updates.
     */
    fun toggleTheme() {
        _currentTheme = when (_currentTheme) {
            DesignSystemTheme.MATERIAL -> DesignSystemTheme.UNSTYLED
            DesignSystemTheme.UNSTYLED -> DesignSystemTheme.MATERIAL
        }
        _currentThemeFlow.update { _currentTheme }
    }
    
    /**
     * Set theme directly (used by intro dialog).
     * Uses update {} lambda for thread-safe state updates.
     */
    fun setTheme(theme: DesignSystemTheme) {
        _currentTheme = theme
        _currentThemeFlow.update { theme }
    }
    
    /**
     * Mark intro dialog as seen (won't show again).
     */
    fun markIntroSeen() {
        _hasSeenIntro = true
    }
}
