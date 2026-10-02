package com.lu4p.fokuslauncher

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lu4p.fokuslauncher.data.font.CustomFontStore
import com.lu4p.fokuslauncher.data.local.PreferencesManager
import com.lu4p.fokuslauncher.data.model.LauncherFontScale
import com.lu4p.fokuslauncher.data.model.LauncherAppearance
import com.lu4p.fokuslauncher.data.model.LauncherVisualStyle
import com.lu4p.fokuslauncher.data.repository.AppRepository
import com.lu4p.fokuslauncher.ui.navigation.FokusNavGraph
import com.lu4p.fokuslauncher.ui.navigation.LauncherHomeCoordinatorViewModel
import com.lu4p.fokuslauncher.ui.theme.FokusLauncherTheme
import com.lu4p.fokuslauncher.ui.util.ProvideAppLocale
import com.lu4p.fokuslauncher.ui.theme.composeFontFamilyFromStoredName
import com.lu4p.fokuslauncher.accessibility.LockScreenAccessibilityService
import com.lu4p.fokuslauncher.utils.FrozenRendererRecovery
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var appRepository: AppRepository

    @Inject
    lateinit var customFontStore: CustomFontStore

    private val launcherHomeCoordinator: LauncherHomeCoordinatorViewModel by viewModels()

    private var shouldShowStatusBar: Boolean = false

    private val frozenRendererCheck =
            Runnable { FrozenRendererRecovery.maybeRestartIfFrozen(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applyLauncherScreenOrientation(allowLandscape = false)

        // Preload apps in background to warm up cache
        lifecycleScope.launch(Dispatchers.IO) {
            appRepository.getInstalledAppsOnBackground()
        }

        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        window.decorView.isSoundEffectsEnabled = true
        window.decorView.overScrollMode = View.OVER_SCROLL_NEVER
        applySystemBarsAppearance()
        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.RESUMED) {
                withContext(Dispatchers.IO) {
                    preferencesManager.syncHomeUsesPhotoWallpaperFromSystemWallpaper()
                }
                launch {
                    preferencesManager.showStatusBarFlow.collect { showStatusBar ->
                        shouldShowStatusBar = showStatusBar
                        applySystemBarsAppearance()
                    }
                }
                launch {
                    preferencesManager.allowLandscapeRotationFlow.collect { allowLandscape ->
                        applyLauncherScreenOrientation(allowLandscape)
                    }
                }
            }
        }
        setContent {
            val launcherFontFamilyName by produceState("") {
                preferencesManager.launcherFontFamilyFlow.collect { value = it }
            }
            val launcherFontScale by produceState(LauncherFontScale.DEFAULT) {
                preferencesManager.launcherFontScaleFlow.collect { value = it }
            }
            val launcherAppearance by produceState(
                    LauncherAppearance(
                            visualStyle = LauncherVisualStyle.CLASSIC,
                            glowEnabled = false,
                    )
            ) {
                preferencesManager.launcherAppearanceFlow.collect { value = it }
            }
            val appLocaleTag by produceState("") {
                preferencesManager.appLocaleTagFlow.collect { value = it }
            }
            ProvideAppLocale(localeTag = appLocaleTag) {
                val wallpaperIsPhoto = launcherAppearance.usesPhotoWallpaper
                FokusLauncherTheme(
                        fontFamily =
                                composeFontFamilyFromStoredName(launcherFontFamilyName) {
                                    customFontStore.resolveFile(it)
                                },
                        fontScale = launcherFontScale,
                        visualStyle =
                                if (wallpaperIsPhoto) LauncherVisualStyle.CLASSIC
                                else launcherAppearance.visualStyle,
                        glowEnabled = launcherAppearance.glowEnabled && !wallpaperIsPhoto,
                ) {
                    FokusNavGraph()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) {
            lifecycleScope.launch {
                val hasCompleted = preferencesManager.hasCompletedOnboardingFlow.first()
                if (hasCompleted) {
                    setIntent(intent)
                    launcherHomeCoordinator.requestGoHome()
                } else {
                    setIntent(Intent(this@MainActivity, MainActivity::class.java))
                }
            }
        } else {
            setIntent(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // AOSP can leave ViewRootImpl drawing suppressed after sleep/wake with another app on top.
        window.decorView.removeCallbacks(frozenRendererCheck)
        window.decorView.postDelayed(frozenRendererCheck, FrozenRendererRecovery.CHECK_DELAY_MS)
    }

    override fun onPause() {
        window.decorView.removeCallbacks(frozenRendererCheck)
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applySystemBarsAppearance()
            // Unlock / focus after sleep can land after onResume; re-check then too.
            window.decorView.removeCallbacks(frozenRendererCheck)
            window.decorView.postDelayed(frozenRendererCheck, FrozenRendererRecovery.CHECK_DELAY_MS)
        }
    }

    private fun applySystemBarsAppearance() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            // Fokus always draws light content over a dark or photo background. Do not let the
            // device's light theme turn the system-bar icons black, which makes the status bar
            // look hidden when the launcher uses its black wallpaper.
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
            if (shouldShowStatusBar) {
                show(WindowInsetsCompat.Type.statusBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            } else {
                hide(WindowInsetsCompat.Type.statusBars())
                systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            show(WindowInsetsCompat.Type.navigationBars())
        }
    }

    private fun applyLauncherScreenOrientation(allowLandscape: Boolean) {
        requestedOrientation =
                if (allowLandscape) ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                else ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
    }

    companion object {
        private const val TAG = "FokusStatusBar"

        /**
         * Expands the notification shade via StatusBarManager.
         * Fallback: show status bar so user can swipe from top.
         */
        fun expandStatusBar(context: Context) {
            expandNotificationsPanel(context)
        }

        /** Opens the notification shade (left-half default for split swipe-down). */
        fun expandNotificationsPanel(context: Context) {
            Log.d(TAG, "expandNotificationsPanel requested")
            if (LockScreenAccessibilityService.expandNotifications()) {
                Log.d(TAG, "Opened notifications via accessibility global action")
                return
            }
            if (expandPanel(context, "expandNotificationsPanel")) {
                Log.d(TAG, "Opened notifications via StatusBarManager")
                return
            }
            Log.d(TAG, "Notifications expansion unavailable; revealing transient status bar")
            (context as? Activity)?.let { activity ->
                WindowInsetsControllerCompat(activity.window, activity.window.decorView)
                    .show(WindowInsetsCompat.Type.statusBars())
            }
        }

        /**
         * Opens quick settings (tiles).
         * Tries StatusBarManager reflection first: it reliably opens quick settings even on
         * skins (e.g. RedMagic) where the accessibility quick-settings action misroutes to
         * the notification shade. Falls back to the accessibility action, then notifications.
         */
        fun expandQuickSettings(context: Context) {
            Log.d(TAG, "expandQuickSettings requested")
            if (expandPanel(context, "expandSettingsPanel")) {
                Log.d(TAG, "Opened quick settings via StatusBarManager")
                return
            }
            if (LockScreenAccessibilityService.expandQuickSettings()) {
                Log.d(TAG, "Opened quick settings via accessibility global action")
                return
            }
            Log.d(TAG, "Quick-settings expansion unavailable; falling back to notifications")
            expandNotificationsPanel(context)
        }

        private fun expandPanel(context: Context, methodName: String): Boolean {
            try {
                val statusBarManager = context.getSystemService("statusbar") ?: return false
                val clazz = Class.forName("android.app.StatusBarManager")
                val method = clazz.getMethod(methodName)
                method.invoke(statusBarManager)
                return true
            } catch (e: Exception) {
                Log.d(TAG, "StatusBarManager.$methodName failed: ${e.javaClass.simpleName}")
                return false
            }
        }
    }
}
