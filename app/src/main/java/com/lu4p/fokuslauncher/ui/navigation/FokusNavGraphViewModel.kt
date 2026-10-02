package com.lu4p.fokuslauncher.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lu4p.fokuslauncher.data.font.CustomFontStore
import com.lu4p.fokuslauncher.data.local.PreferencesManager
import com.lu4p.fokuslauncher.data.model.LauncherAppearance
import com.lu4p.fokuslauncher.data.model.LauncherFontScale
import com.lu4p.fokuslauncher.data.model.LauncherVisualStyle
import com.lu4p.fokuslauncher.data.model.PhotoWallpaperDrawerOverlayIntensity
import com.lu4p.fokuslauncher.data.model.SwipeDownMode
import com.lu4p.fokuslauncher.ui.util.stateWhileSubscribedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class PhotoWallpaperDrawerOverlayUi(
        val usesPhotoWallpaper: Boolean,
        val intensityMultiplier: Float,
)

@HiltViewModel
class FokusNavGraphViewModel @Inject constructor(
        preferencesManager: PreferencesManager,
        private val customFontStore: CustomFontStore,
) : ViewModel() {

    /** Full appearance snapshot for per-surface (home / drawer) theming. */
    val launcherAppearance: StateFlow<LauncherAppearance> =
            preferencesManager.launcherAppearanceFlow.stateWhileSubscribedIn(
                    viewModelScope,
                    LauncherAppearance(
                            visualStyle = LauncherVisualStyle.CLASSIC,
                            glowEnabled = false,
                    ),
            )

    val launcherFontFamilyName: StateFlow<String> =
            preferencesManager.launcherFontFamilyFlow.stateWhileSubscribedIn(
                    viewModelScope,
                    "",
            )

    val launcherFontScale: StateFlow<Float> =
            preferencesManager.launcherFontScaleFlow.stateWhileSubscribedIn(
                    viewModelScope,
                    LauncherFontScale.DEFAULT,
            )

    fun resolveCustomFontFile(storageName: String) = customFontStore.resolveFile(storageName)

    val hasCompletedOnboarding =
            preferencesManager.hasCompletedOnboardingFlow.stateWhileSubscribedIn(
                    viewModelScope,
                    false,
            )

    val swipeDownMode: StateFlow<SwipeDownMode> =
            preferencesManager.swipeDownModeFlow.stateWhileSubscribedIn(
                    viewModelScope,
                    SwipeDownMode.NOTIFICATIONS_ONLY,
            )

    val photoWallpaperDrawerOverlayUiState: StateFlow<PhotoWallpaperDrawerOverlayUi> =
            combine(
                    preferencesManager.launcherAppearanceFlow.map { it.usesPhotoWallpaper },
                    preferencesManager.photoWallpaperDrawerOverlayIntensityFlow,
            ) { usesPhoto, intensity ->
                PhotoWallpaperDrawerOverlayUi(usesPhoto, intensity)
            }
                    .stateWhileSubscribedIn(
                            viewModelScope,
                            PhotoWallpaperDrawerOverlayUi(
                                    usesPhotoWallpaper = false,
                                    intensityMultiplier =
                                            PhotoWallpaperDrawerOverlayIntensity.DEFAULT,
                            ),
                    )
}
