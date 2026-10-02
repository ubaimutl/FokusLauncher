package com.lu4p.fokuslauncher.data.iconpack

import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.Process
import com.lu4p.fokuslauncher.data.model.AppInfo
import com.lu4p.fokuslauncher.data.model.appListStableKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads the original full-color app icons (as opposed to the Arcticons line pack).
 *
 * Icons are cached by [appListStableKey] as drawable constant states so scrolling /
 * recomposition reuses results instead of re-hitting LauncherApps / PackageManager.
 * The cache is memory-only; call [clear] when the app set may have changed.
 */
@Singleton
class RealAppIconRepository
@Inject
constructor(@param:ApplicationContext private val context: Context) {

    /** appListStableKey → drawable ConstantState. */
    private val iconConstantStateCache = ConcurrentHashMap<String, Drawable.ConstantState>()
    /** Apps with no resolvable icon (uninstalled stubs, restricted profiles, …). */
    private val iconMissCache = ConcurrentHashMap.newKeySet<String>()

    suspend fun getIcon(app: AppInfo): Drawable? =
            withContext(Dispatchers.IO) {
                val appKey = appListStableKey(app)
                if (appKey in iconMissCache) return@withContext null
                iconConstantStateCache[appKey]?.let { return@withContext it.newDrawable().mutate() }

                val loadedState = loadIcon(app)?.constantState
                if (loadedState != null) {
                    iconConstantStateCache[appKey] = loadedState
                    return@withContext loadedState.newDrawable().mutate()
                }
                iconMissCache.add(appKey)
                null
            }

    fun clear() {
        iconConstantStateCache.clear()
        iconMissCache.clear()
    }

    private fun loadIcon(app: AppInfo): Drawable? {
        // Pinned shortcuts (e.g. PWAs) already carry their icon on the row when available.
        if (app.launcherShortcutId != null) {
            return app.icon
        }
        val user = app.userHandle ?: Process.myUserHandle()
        val density = context.resources.displayMetrics.densityDpi
        try {
            val launcherApps = context.getSystemService(LauncherApps::class.java)
            val activity = launcherApps?.getActivityList(app.packageName, user)?.firstOrNull()
            // Badged icon keeps the work/clone profile badge, matching system launchers.
            activity?.getBadgedIcon(density)?.let { return it }
            activity?.getIcon(density)?.let { return it }
        } catch (_: Exception) {
            // Fall through to PackageManager lookups.
        }
        app.componentName?.let { component ->
            try {
                return context.packageManager.getActivityIcon(component)
            } catch (_: Exception) {
                // Fall through.
            }
        }
        return try {
            context.packageManager.getApplicationIcon(app.packageName)
        } catch (_: Exception) {
            null
        }
    }
}
