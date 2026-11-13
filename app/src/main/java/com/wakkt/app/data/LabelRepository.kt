package com.wakkt.app.data

import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for labels. CONTRACT: owned by the foundation; implemented in
 * [RoomLabelRepository] (data agent). Other layers depend only on this interface.
 */
interface LabelRepository {
    fun observeAll(): Flow<List<LabelEntity>>
    fun observe(packageName: String): Flow<LabelEntity?>
    suspend fun get(packageName: String): LabelEntity?
    suspend fun save(label: LabelEntity)
    suspend fun delete(packageName: String)
    suspend fun setEnabled(packageName: String, enabled: Boolean)

    /** Called when the user taps the close button on the overlay. Applies the label's behavior. */
    suspend fun onDismissed(packageName: String, now: Long = System.currentTimeMillis())
}
