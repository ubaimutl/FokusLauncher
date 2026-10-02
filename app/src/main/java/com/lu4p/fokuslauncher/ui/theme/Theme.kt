package com.lu4p.fokuslauncher.ui.theme

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontFamily
import com.lu4p.fokuslauncher.data.model.LauncherFontScale
import com.lu4p.fokuslauncher.data.model.LauncherVisualStyle
import com.lu4p.fokuslauncher.ui.util.ProvideSystemClickSound

private val FokusColorSchemeClassic = darkColorScheme(
        primary = White,
        onPrimary = Black,
        secondary = LightGray,
        onSecondary = Black,
        secondaryContainer = ChipBackground,
        onSecondaryContainer = White,
        background = Transparent,
        onBackground = White,
        surface = Transparent,
        onSurface = White,
        surfaceVariant = DarkGray,
        onSurfaceVariant = LightGray,
        surfaceContainerLowest = Transparent,
        surfaceContainerLow = Transparent,
        surfaceContainer = Transparent,
        surfaceContainerHigh = Transparent,
        surfaceContainerHighest = Transparent,
        surfaceBright = Transparent,
        surfaceDim = Transparent,
        inverseSurface = White,
        inverseOnSurface = Black,
        error = DestructiveRed,
        onError = Black,
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD4),
)

fun fokusColorSchemeFor(style: LauncherVisualStyle, customAccentArgb: Int = 0): ColorScheme {
    val palette = style.neonPalette(customAccentArgb) ?: return FokusColorSchemeClassic
    val sheetSurface = palette.primary.copy(alpha = 0.09f).compositeOver(Black)
    val segmentSelectedSurface =
            palette.primary.copy(alpha = 0.22f).compositeOver(Black)
    // Text drawn ON colored fills must contrast with the fill: curated presets keep their
    // black-on-color look, while near-black customs flip to white instead of vanishing.
    return FokusColorSchemeClassic.copy(
            primary = palette.primary,
            secondary = palette.muted,
            onPrimary = legibleTextOn(palette.primary, Black),
            onSecondary = legibleTextOn(palette.muted, Black),
            onBackground = palette.primary,
            onSurface = palette.primary,
            secondaryContainer = segmentSelectedSurface,
            onSecondaryContainer = legibleTextOn(segmentSelectedSurface, palette.primary),
            surfaceVariant = sheetSurface,
            onSurfaceVariant = palette.muted,
            error = NeonDestructiveRed,
            onError = Black,
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD4),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FokusLauncherTheme(
        fontFamily: FontFamily = FontFamily.Default,
        fontScale: Float = 1f,
        visualStyle: LauncherVisualStyle = LauncherVisualStyle.CLASSIC,
        glowEnabled: Boolean = false,
        customAccentArgb: Int = 0,
        content: @Composable () -> Unit
) {
    val colorScheme =
            remember(visualStyle, customAccentArgb) {
                fokusColorSchemeFor(visualStyle, customAccentArgb)
            }
    val typography =
            remember(fontFamily, fontScale, visualStyle, glowEnabled) {
                fokusTypographyForLauncher(
                        fontFamily,
                        fontScale,
                        visualStyle,
                        glowEnabled,
                )
            }
    val resolvedIconScale =
            remember(fontScale) {
                fontScale.coerceIn(LauncherFontScale.MIN, LauncherFontScale.MAX)
            }
    val iconGlow =
            remember(visualStyle, glowEnabled, customAccentArgb) {
                if (!glowEnabled) {
                    LauncherIconGlowSpec.None
                } else {
                    val palette = visualStyle.neonPalette(customAccentArgb)
                    val halo = palette?.primary ?: White
                    LauncherIconGlowSpec(enabled = true, haloColor = halo)
                }
            }
    CompositionLocalProvider(
            LocalOverscrollFactory provides null,
            LocalLauncherFontScale provides resolvedIconScale,
            LocalLauncherIconGlow provides iconGlow,
    ) {
        ProvideSystemClickSound {
            MaterialTheme(colorScheme = colorScheme, typography = typography, content = content)
        }
    }
}
