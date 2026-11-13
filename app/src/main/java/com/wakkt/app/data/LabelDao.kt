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

    @Query("SELECT * FROM labels WHERE packageName = :packageName LIMIT 1")
    fun observe(packageName: String): Flow<LabelEntity?>

    @Query("DELETE FROM labels WHERE packageName = :packageName")
    suspend fun deleteByPackage(packageName: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(label: LabelEntity)

    @Delete
    suspend fun delete(label: LabelEntity)
}
