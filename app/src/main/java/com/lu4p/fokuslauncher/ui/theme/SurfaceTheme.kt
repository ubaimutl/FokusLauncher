package com.lu4p.fokuslauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.lu4p.fokuslauncher.data.model.LauncherVisualStyle

/**
 * Classic settings-look wrapper for UI that opens from a custom-colored surface (e.g. the
 * drawer's overflow menu): always the readable classic scheme, while keeping the user's font
 * and shapes from the outer theme. Icon glow follows the outer toggle but uses a white halo
 * so dark customs can't smudge light icons.
 */
@Composable
fun ClassicSettingsSurface(content: @Composable () -> Unit) {
    val glowEnabled = LocalLauncherIconGlow.current.enabled
    MaterialTheme(
            colorScheme = fokusColorSchemeFor(LauncherVisualStyle.CLASSIC),
            shapes = MaterialTheme.shapes,
            typography = MaterialTheme.typography,
    ) {
        CompositionLocalProvider(
                LocalLauncherIconGlow provides
                        if (glowEnabled) {
                            LauncherIconGlowSpec(enabled = true, haloColor = White)
                        } else {
                            LauncherIconGlowSpec.None
                        }
        ) {
            content()
        }
    }
}
