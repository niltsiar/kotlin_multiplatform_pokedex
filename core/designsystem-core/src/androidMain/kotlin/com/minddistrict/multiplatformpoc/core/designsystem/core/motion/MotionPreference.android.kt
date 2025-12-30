
package com.minddistrict.multiplatformpoc.core.designsystem.core.motion

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Android implementation of motion preference detection.
 * 
 * Checks Settings.Global.TRANSITION_ANIMATION_SCALE to determine if animations are disabled.
 * This respects the "Remove animations" accessibility setting and the "Animator duration scale"
 * developer option.
 * 
 * @return true if transition animation scale is 0 (animations disabled), false otherwise
 */
@Composable
actual fun isReduceMotionEnabled(): Boolean {
    val context = LocalContext.current
    return try {
        val animationScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            1f
        )
        animationScale == 0f
    } catch (e: Settings.SettingNotFoundException) {
        false
    }
}
