package com.wakkt.app.ui.applist

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class InstalledApp(
    val packageName: String,
    val name: String,
    val icon: ImageBitmap?,
)

/** Loads launchable apps (excluding ourselves). Icons are cached by package. */
@Singleton
class InstalledAppsSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val iconCache = ConcurrentHashMap<String, ImageBitmap>()

    suspend fun load(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        pm.queryIntentActivities(intent, 0)
            .asSequence()
            .map { it.activityInfo.packageName to it }
            .filter { (pkg, _) -> pkg != context.packageName }
            .distinctBy { it.first }
            .map { (pkg, info) ->
                InstalledApp(pkg, info.loadLabel(pm).toString(), icon(pm, pkg) { info.loadIcon(pm) })
            }
            .toList()
    }

    /** A single app by package (works for non-launchable too); null if not installed. */
    suspend fun get(packageName: String): InstalledApp? = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        try {
            val info = pm.getApplicationInfo(packageName, 0)
            InstalledApp(
                packageName,
                info.loadLabel(pm).toString(),
                icon(pm, packageName) { info.loadIcon(pm) },
            )
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }

    private inline fun icon(
        pm: PackageManager,
        pkg: String,
        load: () -> android.graphics.drawable.Drawable,
    ): ImageBitmap? = iconCache[pkg] ?: try {
        load().toBitmap(ICON_PX, ICON_PX).asImageBitmap().also { iconCache[pkg] = it }
    } catch (_: Exception) {
        null
    }

    private companion object {
        const val ICON_PX = 96
    }
}
