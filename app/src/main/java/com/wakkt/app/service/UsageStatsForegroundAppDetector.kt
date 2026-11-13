package com.wakkt.app.service

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/** Polls [UsageStatsManager] events to find the foreground app. Requires Usage access. */
@Singleton
class UsageStatsForegroundAppDetector @Inject constructor(
    @ApplicationContext private val context: Context,
) : ForegroundAppDetector {

    override fun foregroundApp(): Flow<String> = flow {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val event = UsageEvents.Event()
        var lastQuery = System.currentTimeMillis() - INITIAL_WINDOW_MS
        while (true) {
            if (!pm.isInteractive) {
                delay(SCREEN_OFF_POLL_MS)
                continue
            }
            val now = System.currentTimeMillis()
            var latest: String? = null
            try {
                val events = usm.queryEvents(lastQuery, now)
                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    // MOVE_TO_FOREGROUND == ACTIVITY_RESUMED (both 1).
                    @Suppress("DEPRECATION")
                    if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                        val pkg = event.packageName
                        // wakkt itself is emitted too, so opening wakkt hides any note.
                        if (pkg != null) latest = pkg
                    }
                }
                // Overlap slightly so events at the boundary are not missed.
                lastQuery = now - OVERLAP_MS
            } catch (_: SecurityException) {
                // Usage access revoked; keep polling until restored.
            }
            latest?.let { emit(it) }
            delay(POLL_MS)
        }
    }.distinctUntilChanged().flowOn(Dispatchers.Default)

    private companion object {
        const val POLL_MS = 500L
        const val SCREEN_OFF_POLL_MS = 2_000L
        const val INITIAL_WINDOW_MS = 60_000L
        const val OVERLAP_MS = 1_000L
    }
}
