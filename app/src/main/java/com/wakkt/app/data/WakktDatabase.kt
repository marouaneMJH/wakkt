package com.wakkt.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [LabelEntity::class], version = 2, exportSchema = false)
abstract class WakktDatabase : RoomDatabase() {
    abstract fun labelDao(): LabelDao

    companion object {
        /** v2: note appearance (color, style, backdrop) and optional image. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE labels ADD COLUMN color TEXT NOT NULL DEFAULT 'YELLOW'")
                db.execSQL("ALTER TABLE labels ADD COLUMN style TEXT NOT NULL DEFAULT 'CLASSIC'")
                db.execSQL("ALTER TABLE labels ADD COLUMN backdrop TEXT NOT NULL DEFAULT 'NONE'")
                db.execSQL("ALTER TABLE labels ADD COLUMN imagePath TEXT")
            }
        }
    }
}
