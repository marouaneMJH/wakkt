package com.wakkt.app.ui

/**
 * Starts/stops the monitoring service. The UI only depends on this interface; the integrator
 * binds it to service.MonitorStarter.
 */
interface MonitorControl {
    fun start()
    fun stop()
}
