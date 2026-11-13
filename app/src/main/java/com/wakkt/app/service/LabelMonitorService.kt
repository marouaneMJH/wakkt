package com.wakkt.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.wakkt.app.MainActivity
import com.wakkt.app.R
import com.wakkt.app.data.LabelEntity
import com.wakkt.app.data.LabelRepository
import com.wakkt.app.data.LabelRules
import com.wakkt.app.overlay.OverlayController
import com.wakkt.app.settings.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LabelMonitorService : LifecycleService() {

    @Inject lateinit var detector: ForegroundAppDetector
    @Inject lateinit var labels: LabelRepository
    @Inject lateinit var settings: SettingsRepository
    @Inject lateinit var overlay: OverlayController

    override fun onCreate() {
        super.onCreate()
        startAsForeground()
        monitor()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        overlay.hide()
        super.onDestroy()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun monitor() {
        settings.labelsEnabled
            .distinctUntilChanged()
            .flatMapLatest { enabled ->
                if (!enabled) {
                    // Disabled: stop polling and clear any visible note.
                    overlay.hide()
                    emptyFlow()
                } else {
                    watchForeground()
                }
            }
            .onEach { (pkg, label) -> render(pkg, label) }
            .launchIn(lifecycleScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun watchForeground(): Flow<Pair<String, LabelEntity?>> =
        detector.foregroundApp().flatMapLatest { pkg ->
            // Re-evaluates when the label of the current app is edited or dismissed.
            labels.observe(pkg).distinctUntilChanged().map { pkg to it }
        }

    // lifecycleScope runs on Dispatchers.Main.immediate, as OverlayController requires.
    private fun render(pkg: String, label: LabelEntity?) {
        if (label != null && LabelRules.shouldShow(label, System.currentTimeMillis())) {
            overlay.show(label) {
                lifecycleScope.launch(Dispatchers.Main) {
                    labels.onDismissed(pkg)
                    overlay.hide()
                }
            }
        } else {
            overlay.hide()
        }
    }

    private fun startAsForeground() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, getString(R.string.service_channel_name), NotificationManager.IMPORTANCE_LOW),
        )
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(getString(R.string.service_notification_title))
            .setContentText(getString(R.string.service_notification_text))
            .setContentIntent(open)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private companion object {
        const val CHANNEL_ID = "monitor"
        const val NOTIFICATION_ID = 1001
    }
}
