package com.wakkt.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "labels")
data class LabelEntity(
    @PrimaryKey val packageName: String,
    val text: String,
    val behavior: LabelBehavior = LabelBehavior.CLOSABLE_PERIOD,
    val snoozeMinutes: Int = 60,
    val snoozedUntil: Long = 0L,
    val dismissedForever: Boolean = false,
    val enabled: Boolean = true,
)
