package com.lu4p.fokuslauncher.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SwipeDownModeTest {

    @Test
    fun `fromString defaults to notifications only`() {
        assertEquals(SwipeDownMode.NOTIFICATIONS_ONLY, SwipeDownMode.fromString(null))
        assertEquals(SwipeDownMode.NOTIFICATIONS_ONLY, SwipeDownMode.fromString(""))
        assertEquals(SwipeDownMode.NOTIFICATIONS_ONLY, SwipeDownMode.fromString("bogus"))
        assertEquals(
            SwipeDownMode.SPLIT_LEFT_NOTIF_RIGHT_SETTINGS,
            SwipeDownMode.fromString("split_left_notif_right_settings"),
        )
    }

    @Test
    fun `single modes ignore touch position`() {
        assertFalse(SwipeDownMode.NOTIFICATIONS_ONLY.opensQuickSettings(0f, 1000f))
        assertFalse(SwipeDownMode.NOTIFICATIONS_ONLY.opensQuickSettings(999f, 1000f))
        assertTrue(SwipeDownMode.QUICK_SETTINGS_ONLY.opensQuickSettings(0f, 1000f))
        assertTrue(SwipeDownMode.QUICK_SETTINGS_ONLY.opensQuickSettings(999f, 1000f))
    }

    @Test
    fun `split modes divide screen into halves`() {
        assertFalse(SwipeDownMode.SPLIT_LEFT_NOTIF_RIGHT_SETTINGS.opensQuickSettings(100f, 1000f))
        assertTrue(SwipeDownMode.SPLIT_LEFT_NOTIF_RIGHT_SETTINGS.opensQuickSettings(900f, 1000f))
        assertTrue(SwipeDownMode.SPLIT_LEFT_SETTINGS_RIGHT_NOTIF.opensQuickSettings(100f, 1000f))
        assertFalse(SwipeDownMode.SPLIT_LEFT_SETTINGS_RIGHT_NOTIF.opensQuickSettings(900f, 1000f))
    }
}
