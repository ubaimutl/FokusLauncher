package com.lu4p.fokuslauncher.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min

// Note: glyph outlines intentionally stay developer-original (fixed black stroke in
// OutlinedText); only chrome text (chips, sheets, pickers) auto-contrasts.

/**
 * WCAG relative-contrast ratio between two opaque colors (1 = identical, 21 = black vs white).
 * Alpha is ignored; callers pass the composited background (e.g. accent tints over black).
 */
fun contrastRatio(a: Color, b: Color): Float {
    val l1 = a.luminance()
    val l2 = b.luminance()
    return (max(l1, l2) + 0.05f) / (min(l1, l2) + 0.05f)
}

/**
 * Text color for content drawn on [background]: [preferred] (usually the accent) when it reads
 * well, otherwise whichever of black/white reads better. Keeps curated presets untouched (they
 * all clear [minContrast]) while rescuing near-black customs (e.g. black label on a black
 * chip/sheet becomes white).
 */
fun legibleTextOn(
        background: Color,
        preferred: Color,
        minContrast: Float = 2f,
): Color {
    if (contrastRatio(preferred, background) >= minContrast) return preferred
    return if (contrastRatio(Color.White, background) >= contrastRatio(Color.Black, background)) {
        Color.White
    } else {
        Color.Black
    }
}
