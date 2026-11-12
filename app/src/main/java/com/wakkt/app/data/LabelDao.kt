package com.wakkt.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LabelDao {
    @Query("SELECT * FROM labels ORDER BY packageName")
    fun observeAll(): Flow<List<LabelEntity>>

    @Query("SELECT * FROM labels WHERE packageName = :packageName LIMIT 1")
    suspend fun get(packageName: String): LabelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(label: LabelEntity)

    @Delete
    suspend fun delete(label: LabelEntity)
}
