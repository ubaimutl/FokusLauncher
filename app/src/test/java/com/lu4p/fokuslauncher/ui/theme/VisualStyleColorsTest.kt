package com.lu4p.fokuslauncher.ui.theme

import androidx.compose.ui.graphics.Color
import com.lu4p.fokuslauncher.data.model.LauncherVisualStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class VisualStyleColorsTest {

    @Test
    fun `parseCustomHexColor accepts 6 and 8 digit hex`() {
        assertEquals(0xFF58FF7A.toInt(), parseCustomHexColor("#58FF7A"))
        assertEquals(0xFF58FF7A.toInt(), parseCustomHexColor("58ff7a"))
        assertEquals(0xFF000000.toInt(), parseCustomHexColor("#000000"))
        assertEquals(0x802196F3.toInt(), parseCustomHexColor("#802196F3"))
    }

    @Test
    fun `parseCustomHexColor rejects invalid input`() {
        assertNull(parseCustomHexColor(""))
        assertNull(parseCustomHexColor("#FFF"))
        assertNull(parseCustomHexColor("#GGGGGG"))
        assertNull(parseCustomHexColor("58FF7")) 
        assertNull(parseCustomHexColor("#58FF7A00FF"))
    }

    @Test
    fun `formatCustomHexColor round-trips opaque colors`() {
        assertEquals("#58FF7A", formatCustomHexColor(0xFF58FF7A.toInt()))
        assertEquals("#000000", formatCustomHexColor(0xFF000000.toInt()))
        assertEquals("#802196F3", formatCustomHexColor(0x802196F3.toInt()))
    }

    @Test
    fun `customNeonPalette needs a stored color`() {
        assertNull(customNeonPalette(0))
        val palette = customNeonPalette(0xFF000000.toInt())
        assertNotNull(palette)
        assertEquals(Color(0xFF000000.toInt()), palette!!.primary)
    }

    @Test
    fun `CUSTOM style falls back to white without stored color`() {
        assertEquals(White, LauncherVisualStyle.CUSTOM.settingsPreviewColor(0))
        assertEquals(
            Color(0xFF58FF7A.toInt()),
            LauncherVisualStyle.CUSTOM.settingsPreviewColor(0xFF58FF7A.toInt()),
        )
    }
}
