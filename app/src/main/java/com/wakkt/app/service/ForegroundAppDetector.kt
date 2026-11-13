package com.wakkt.app.service

import kotlinx.coroutines.flow.Flow

/** Emits the package name of the app currently in the foreground (distinct changes only). CONTRACT. */
interface ForegroundAppDetector {
    fun foregroundApp(): Flow<String>
}
