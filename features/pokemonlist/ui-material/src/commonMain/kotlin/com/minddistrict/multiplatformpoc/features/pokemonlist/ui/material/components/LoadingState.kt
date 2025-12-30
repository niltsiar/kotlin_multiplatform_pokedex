package com.minddistrict.multiplatformpoc.features.pokemonlist.ui.material.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.minddistrict.multiplatformpoc.core.designsystem.material.tokens.tokens

/**
 * Material Design 3 loading state for Pokémon list.
 * 
 * Displays a centered circular progress indicator while data is loading.
 * 
 * @param modifier Modifier for the container
 */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}
