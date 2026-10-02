package com.lu4p.fokuslauncher.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import org.junit.Assert.assertEquals
import org.junit.Test

class LegibleColorsTest {

    @Test
    fun `contrastRatio of black on white is 21`() {
        val ratio = contrastRatio(Color.Black, Color.White)
        assertEquals(21f, ratio, 0.01f)
    }

    @Test
    fun `legibleTextOn keeps readable presets untouched`() {
        // Selected chips use the full accent as background: black label stays on pink/gold.
        assertEquals(
            Color.Black,
            legibleTextOn(Color(0xFFD45080.toInt()), Color.Black),
        )
        assertEquals(Color.Black, legibleTextOn(Color(0xFFFFD700.toInt()), Color.Black))
        // Pink accent on its own dark sheet tint (composited like the real scheme) is kept.
        val pinkSheetBg =
                Color(0xFFD45080.toInt()).copy(alpha = 0.09f).compositeOver(Color.Black)
        assertEquals(
            Color(0xFFD45080.toInt()),
            legibleTextOn(pinkSheetBg, Color(0xFFD45080.toInt())),
        )
    }

    @Test
    fun `legibleTextOn flips near-black customs to white`() {
        assertEquals(Color.White, legibleTextOn(Color.Black, Color.Black))
        assertEquals(
            Color.White,
            legibleTextOn(Color(0xFF141414.toInt()), Color(0xFF000000.toInt())),
        )
        // Classic white on black stays white.
        assertEquals(Color.White, legibleTextOn(Color.Black, Color.White))
    }
}
