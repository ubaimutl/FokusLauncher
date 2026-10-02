package com.lu4p.fokuslauncher.data.model

/**
 * Visual style and glow, always resolved from the same preference snapshot.
 *
 * [usesPhotoWallpaper] is true when the user keeps or sets an image wallpaper (vs solid black from
 * this app). Styles and glow stay available on image wallpapers; outlined text keeps them readable.
 *
 * [homeCustomAccentArgb] / [drawerCustomAccentArgb] are the user-defined accents for
 * [LauncherVisualStyle.CUSTOM] on each surface (0 = unset). Settings screens always use the
 * classic theme so dark customs stay readable there.
 */
data class LauncherAppearance(
        val visualStyle: LauncherVisualStyle,
        val glowEnabled: Boolean,
        val usesPhotoWallpaper: Boolean = false,
        val homeCustomAccentArgb: Int = 0,
        val drawerCustomAccentArgb: Int = 0,
)
