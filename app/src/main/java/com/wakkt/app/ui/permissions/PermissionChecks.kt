package com.wakkt.app.ui.permissions

import android.content.Context
import com.wakkt.app.service.MonitorStarter

/** UI-facing permission checks; delegates to [MonitorStarter] so there is one implementation. */
object PermissionChecks {
    fun hasOverlayPermission(context: Context): Boolean = MonitorStarter.hasOverlayPermission(context)
    fun hasUsageAccess(context: Context): Boolean = MonitorStarter.hasUsageAccess(context)
    fun allGranted(context: Context): Boolean = MonitorStarter.canRun(context)
}
