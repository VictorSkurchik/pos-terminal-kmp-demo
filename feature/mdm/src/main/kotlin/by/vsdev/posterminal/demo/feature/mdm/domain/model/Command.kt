package by.vsdev.posterminal.demo.feature.mdm.domain.model

/** Type of a remote MDM command. Executed by an MdmCommandExecutor. */
enum class CommandType {
    LOCK,
    KIOSK_ON,
    KIOSK_OFF,
    SHOW_MESSAGE,
    RESTRICT_APP,

    /** Admin-initiated reset: the terminal un-enrolls, deletes itself, returns to Registration. */
    WIPE,
}

enum class CommandStatus { PENDING, DELIVERED, DONE }

/** A command in the device queue. [payload] is e.g. the text for SHOW_MESSAGE. */
data class DeviceCommand(
    val id: String,
    val deviceId: String,
    val type: CommandType,
    val payload: String? = null,
    val status: CommandStatus = CommandStatus.PENDING,
    val createdAt: Long,
)
