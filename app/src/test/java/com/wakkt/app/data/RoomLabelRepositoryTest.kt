package com.wakkt.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeLabelDao : LabelDao {
    val state = MutableStateFlow<Map<String, LabelEntity>>(emptyMap())
    override fun observeAll(): Flow<List<LabelEntity>> = state.map { it.values.sortedBy { l -> l.packageName } }
    override fun observe(packageName: String): Flow<LabelEntity?> = state.map { it[packageName] }
    override suspend fun get(packageName: String) = state.value[packageName]
    override suspend fun upsert(label: LabelEntity) { state.value = state.value + (label.packageName to label) }
    override suspend fun delete(label: LabelEntity) { state.value = state.value - label.packageName }
    override suspend fun deleteByPackage(packageName: String) { state.value = state.value - packageName }
}

class RoomLabelRepositoryTest {
    private val dao = FakeLabelDao()
    private val repo = RoomLabelRepository(dao)

    @Test fun saveGetObserveDelete() = runTest {
        val l = LabelEntity("a", "note")
        repo.save(l)
        assertEquals(l, repo.get("a"))
        assertEquals(l, repo.observe("a").first())
        assertEquals(listOf(l), repo.observeAll().first())
        repo.delete("a")
        assertNull(repo.get("a"))
        assertNull(repo.observe("a").first())
    }

    @Test fun dismissPeriodSnoozes() = runTest {
        repo.save(LabelEntity("a", "n", LabelBehavior.CLOSABLE_PERIOD, snoozeMinutes = 15))
        repo.onDismissed("a", 1000)
        assertEquals(1000 + 15 * 60_000L, repo.get("a")!!.snoozedUntil)
    }

    @Test fun dismissForeverAndSticky() = runTest {
        repo.save(LabelEntity("f", "n", LabelBehavior.CLOSABLE_FOREVER))
        repo.save(LabelEntity("s", "n", LabelBehavior.STICKY))
        repo.onDismissed("f", 1000)
        repo.onDismissed("s", 1000)
        assertTrue(repo.get("f")!!.dismissedForever)
        assertFalse(repo.get("s")!!.dismissedForever)
        assertEquals(0L, repo.get("s")!!.snoozedUntil)
    }

    @Test fun dismissUnknownIsNoop() = runTest {
        repo.onDismissed("zzz", 1000)
        assertNull(repo.get("zzz"))
    }

    @Test fun reEnableClearsDismissal() = runTest {
        repo.save(
            LabelEntity("a", "n", LabelBehavior.CLOSABLE_FOREVER, dismissedForever = true, snoozedUntil = 9999, enabled = false),
        )
        repo.setEnabled("a", true)
        val l = repo.get("a")!!
        assertTrue(l.enabled)
        assertFalse(l.dismissedForever)
        assertEquals(0L, l.snoozedUntil)
        assertTrue(LabelRules.shouldShow(l, 1))
    }

    @Test fun disableKeepsDismissalState() = runTest {
        repo.save(LabelEntity("a", "n", dismissedForever = true, snoozedUntil = 5))
        repo.setEnabled("a", false)
        val l = repo.get("a")!!
        assertFalse(l.enabled)
        assertTrue(l.dismissedForever)
        assertEquals(5L, l.snoozedUntil)
    }
}
