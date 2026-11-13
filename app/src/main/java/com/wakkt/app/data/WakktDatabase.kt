package com.wakkt.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [LabelEntity::class], version = 5, exportSchema = false)
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

        /** v3: attention (sound, voice, vibration, repeat) and visual effects. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                listOf(
                    "sound TEXT NOT NULL DEFAULT 'NONE'",
                    "voice INTEGER NOT NULL DEFAULT 0",
                    "vibration TEXT NOT NULL DEFAULT 'NONE'",
                    "repeatSeconds INTEGER NOT NULL DEFAULT 0",
                    "motion TEXT NOT NULL DEFAULT 'NONE'",
                    "pattern TEXT NOT NULL DEFAULT 'NONE'",
                    "grain INTEGER NOT NULL DEFAULT 0",
                    "glitch INTEGER NOT NULL DEFAULT 0",
                ).forEach { db.execSQL("ALTER TABLE labels ADD COLUMN $it") }
            }
        }

        /** v4: loud mode (alarm volume). */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE labels ADD COLUMN loud INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** v5: "Continue to app" option for the backdrop. */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE labels ADD COLUMN allowContinue INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
