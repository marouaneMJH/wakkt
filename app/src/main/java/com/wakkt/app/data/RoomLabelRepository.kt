package com.wakkt.app.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomLabelRepository @Inject constructor(
    private val dao: LabelDao,
) : LabelRepository {
    override fun observeAll(): Flow<List<LabelEntity>> = dao.observeAll()

    override fun observe(packageName: String): Flow<LabelEntity?> = dao.observe(packageName)

    override suspend fun get(packageName: String): LabelEntity? = dao.get(packageName)

    override suspend fun save(label: LabelEntity) = dao.upsert(label)

    override suspend fun delete(packageName: String) = dao.deleteByPackage(packageName)

    override suspend fun setEnabled(packageName: String, enabled: Boolean) {
        val label = dao.get(packageName) ?: return
        dao.upsert(
            if (enabled) label.copy(enabled = true, dismissedForever = false, snoozedUntil = 0L)
            else label.copy(enabled = false),
        )
    }

    override suspend fun onDismissed(packageName: String, now: Long) {
        val label = dao.get(packageName) ?: return
        dao.upsert(LabelRules.afterDismiss(label, now))
    }
}
