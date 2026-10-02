package com.lu4p.fokuslauncher.ui.theme

import androidx.compose.ui.graphics.Color
import com.lu4p.fokuslauncher.data.model.LauncherVisualStyle

/** Bright primary and muted secondary for neon presets; null for [LauncherVisualStyle.CLASSIC]. */
data class NeonPalette(val primary: Color, val muted: Color)

/**
 * Parses a user-entered hex color (`RRGGBB` or `AARRGGBB`, with or without `#`).
 * Returns the ARGB int, or null when invalid.
 */
fun parseCustomHexColor(input: String): Int? {
    val hex = input.trim().removePrefix("#")
    if (hex.length != 6 && hex.length != 8) return null
    if (!hex.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) return null
    val full = if (hex.length == 6) "FF$hex" else hex
    return try {
        full.toLong(16).toInt()
    } catch (_: NumberFormatException) {
        null
    }
}

/** Formats an ARGB int as `#RRGGBB` (or `#AARRGGBB` when not opaque) for the hex field. */
fun formatCustomHexColor(argb: Int): String =
        if ((argb ushr 24) == 0xFF) "#%06X".format(argb and 0xFFFFFF)
        else "#%08X".format(argb)

/**
 * Palette for [LauncherVisualStyle.CUSTOM]; null when no custom color is stored yet
 * (callers fall back to classic white). Muted is a dimmed primary for secondary text.
 */
fun customNeonPalette(argb: Int): NeonPalette? {
    if (argb == 0) return null
    val primary = Color(argb)
    return NeonPalette(primary = primary, muted = primary.copy(alpha = 0.65f))
}

fun LauncherVisualStyle.neonPalette(customAccentArgb: Int = 0): NeonPalette? =
        when (this) {
            LauncherVisualStyle.CUSTOM -> customNeonPalette(customAccentArgb)
            LauncherVisualStyle.CLASSIC -> null
            LauncherVisualStyle.NEON_MAGENTA ->
                    NeonPalette(primary = Color(0xFFE070FF), muted = Color(0xFFB092D0))
            LauncherVisualStyle.NEON_LIME ->
                    NeonPalette(primary = Color(0xFF58FF7A), muted = Color(0xFF78C892))
            LauncherVisualStyle.GOLD ->
                    NeonPalette(primary = Color(0xFFFFD700), muted = Color(0xFFFFC125))
            LauncherVisualStyle.NEON_AMBER ->
                    NeonPalette(primary = Color(0xFFFF8F70), muted = Color(0xFFD9A894))
            LauncherVisualStyle.NEON_PINK ->
                    NeonPalette(primary = Color(0xFFD45080), muted = Color(0xFFA03860))
            LauncherVisualStyle.LAVENDER ->
                    NeonPalette(primary = Color(0xFFC49EE8), muted = Color(0xFF9870C0))
            LauncherVisualStyle.SKY ->
                    NeonPalette(primary = Color(0xFF87CEEB), muted = Color(0xFF6098B8))
            LauncherVisualStyle.SAGE ->
                    NeonPalette(primary = Color(0xFF8FBC8F), muted = Color(0xFF6A9070))
            LauncherVisualStyle.ROSE ->
                    NeonPalette(primary = Color(0xFFFB7185), muted = Color(0xFFD04060))
            LauncherVisualStyle.EMERALD ->
                    NeonPalette(primary = Color(0xFF10B981), muted = Color(0xFF0A8060))
        }

/** Primary accent as shown in settings (Classic = launcher white). */
fun LauncherVisualStyle.settingsPreviewColor(customAccentArgb: Int = 0): Color =
        neonPalette(customAccentArgb)?.primary ?: White
