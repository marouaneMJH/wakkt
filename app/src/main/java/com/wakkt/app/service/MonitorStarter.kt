package com.wakkt.app.service

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.content.ContextCompat

/** Entry points for starting/stopping the monitor service and checking required permissions. */
object MonitorStarter {
    fun hasOverlayPermission(context: Context): Boolean = Settings.canDrawOverlays(context)

    @Suppress("DEPRECATION")
    fun hasUsageAccess(context: Context): Boolean {
        val ops = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun canRun(context: Context): Boolean = hasOverlayPermission(context) && hasUsageAccess(context)

    /** Returns false if the system refused to start the service (e.g. background start limits). */
    fun start(context: Context): Boolean = try {
        ContextCompat.startForegroundService(context, Intent(context, LabelMonitorService::class.java))
        true
    } catch (e: IllegalStateException) {
        // ForegroundServiceStartNotAllowedException (API 31+) extends IllegalStateException.
        false
    } catch (e: SecurityException) {
        false
    }

    fun stop(context: Context) {
        context.stopService(Intent(context, LabelMonitorService::class.java))
    }
}
