package com.shure.wireless.channels.core.database.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Upsert
import com.shure.wireless.channels.core.database.entity.StoredDeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoredDeviceDao {
    @Query("SELECT * FROM stored_devices ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<StoredDeviceEntity>>

    @Query("SELECT * FROM stored_devices WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): StoredDeviceEntity?

    @Upsert
    suspend fun upsert(device: StoredDeviceEntity)

    @Upsert
    suspend fun upsertAll(devices: List<StoredDeviceEntity>)

    @Delete
    suspend fun delete(device: StoredDeviceEntity)

    @Query("DELETE FROM stored_devices WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM stored_devices")
    suspend fun deleteAll()
}
