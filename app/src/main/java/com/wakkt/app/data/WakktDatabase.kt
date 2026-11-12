package com.wakkt.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [LabelEntity::class], version = 1, exportSchema = false)
abstract class WakktDatabase : RoomDatabase() {
    abstract fun labelDao(): LabelDao
}
