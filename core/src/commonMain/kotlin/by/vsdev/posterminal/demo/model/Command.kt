package by.vsdev.posterminal.demo.model

import kotlinx.serialization.Serializable

/** Type of a remote MDM command. See the implementation in feature:mdm CommandExecutor. */
@Serializable
enum class CommandType {
    LOCK,
    KIOSK_ON,
    KIOSK_OFF,
    SHOW_MESSAGE,
    RESTRICT_APP,

    /** Admin-initiated reset: the terminal un-enrolls, deletes itself, returns to Registration. */
    WIPE,
}

@Serializable
enum class CommandStatus { PENDING, DELIVERED, DONE }

/** A command in the device queue. [payload] is e.g. the text for SHOW_MESSAGE. */
@Serializable
data class DeviceCommand(
    val id: String,
    val deviceId: String,
    val type: CommandType,
    val payload: String? = null,
    val status: CommandStatus = CommandStatus.PENDING,
    val createdAt: Long,
)
