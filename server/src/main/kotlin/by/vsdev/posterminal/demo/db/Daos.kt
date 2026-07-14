package by.vsdev.posterminal.demo.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface DeviceDao {
    @Upsert
    suspend fun upsert(device: DeviceEntity)

    @Query("SELECT * FROM devices ORDER BY name")
    suspend fun getAll(): List<DeviceEntity>

    @Query("SELECT * FROM devices WHERE id = :id")
    suspend fun getById(id: String): DeviceEntity?

    @Query("DELETE FROM devices WHERE id = :id")
    suspend fun delete(id: String): Int
}

@Dao
interface CommandDao {
    @Insert
    suspend fun insert(command: CommandEntity)

    /** All unfinished commands for the device (PENDING + DELIVERED), in enqueue order. */
    @Query("SELECT * FROM commands WHERE deviceId = :deviceId AND status != 'DONE' ORDER BY createdAt")
    suspend fun getPending(deviceId: String): List<CommandEntity>

    @Query("SELECT * FROM commands WHERE id = :id")
    suspend fun getById(id: String): CommandEntity?

    @Query("UPDATE commands SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("SELECT * FROM commands WHERE deviceId = :deviceId ORDER BY createdAt DESC")
    suspend fun getAllForDevice(deviceId: String): List<CommandEntity>

    @Query("DELETE FROM commands WHERE deviceId = :deviceId")
    suspend fun deleteForDevice(deviceId: String)
}
