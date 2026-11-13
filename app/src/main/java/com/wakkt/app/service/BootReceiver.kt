package com.wakkt.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wakkt.app.settings.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Restarts the monitor after boot or app update when permissions and the global switch allow it. */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var settings: SettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return
        val app = context.applicationContext
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                if (MonitorStarter.canRun(app) && settings.labelsEnabled.first()) {
                    MonitorStarter.start(app)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
