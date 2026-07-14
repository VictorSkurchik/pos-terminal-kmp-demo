package by.vsdev.posterminal.demo.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val model: String?,
    val lastSeenAt: Long,
    val status: String,
    val batteryLevel: Int?,
    val enrollmentToken: String? = null,
    val kioskActive: Boolean = false,
    val restrictPayment: Boolean = false,
)

@Entity(
    tableName = "commands",
    indices = [Index("deviceId"), Index("status")],
)
data class CommandEntity(
    @PrimaryKey val id: String,
    val deviceId: String,
    val type: String,
    val payload: String?,
    val status: String,
    val createdAt: Long,
)
