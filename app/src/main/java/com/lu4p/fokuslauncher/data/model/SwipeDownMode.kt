package com.lu4p.fokuslauncher.data.model

import androidx.annotation.StringRes
import com.lu4p.fokuslauncher.R

/**
 * What a swipe-down on the home screen expands.
 *
 * Split modes divide the screen in halves by the swipe's horizontal start
 * position: left half vs right half of the display width.
 */
enum class SwipeDownMode(@param:StringRes val labelRes: Int) {
    NOTIFICATIONS_ONLY(R.string.swipe_down_mode_notifications_only),
    QUICK_SETTINGS_ONLY(R.string.swipe_down_mode_quick_settings_only),
    SPLIT_LEFT_NOTIF_RIGHT_SETTINGS(R.string.swipe_down_mode_split_left_notif_right_settings),
    SPLIT_LEFT_SETTINGS_RIGHT_NOTIF(R.string.swipe_down_mode_split_left_settings_right_notif);

    /** True when the swipe-down zone decides between notifications and quick settings. */
    val isSplit: Boolean
        get() = this == SPLIT_LEFT_NOTIF_RIGHT_SETTINGS || this == SPLIT_LEFT_SETTINGS_RIGHT_NOTIF

    /**
     * Resolves which panel to open for a swipe starting at [startX] within a
     * container of [widthPx]. Returns true for quick settings, false for notifications.
     */
    fun opensQuickSettings(startX: Float, widthPx: Float): Boolean =
        when (this) {
            NOTIFICATIONS_ONLY -> false
            QUICK_SETTINGS_ONLY -> true
            SPLIT_LEFT_NOTIF_RIGHT_SETTINGS -> startX >= widthPx / 2f
            SPLIT_LEFT_SETTINGS_RIGHT_NOTIF -> startX < widthPx / 2f
        }

    companion object {
        fun fromString(value: String?): SwipeDownMode {
            if (value.isNullOrBlank()) return NOTIFICATIONS_ONLY
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: NOTIFICATIONS_ONLY
        }
    }
}
