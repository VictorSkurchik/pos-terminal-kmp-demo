package by.vsdev.posterminal.demo.domain.usecase.mdm

import by.vsdev.posterminal.demo.domain.repository.DeviceRepository
import by.vsdev.posterminal.demo.domain.result.AppResult
import by.vsdev.posterminal.demo.domain.service.MdmCommandExecutor

/**
 * One MDM sync cycle: send a heartbeat (best-effort), then pull, execute and acknowledge each
 * pending command. Used by both the foreground service and the WorkManager fallback.
 */
class SyncDeviceUseCase(
    private val device: DeviceRepository,
    private val executor: MdmCommandExecutor,
) {
    suspend operator fun invoke(batteryLevel: Int?): AppResult<Unit> {
        // Heartbeat failures are non-fatal — a transient network blip shouldn't stop command polling.
        device.heartbeat(batteryLevel)

        return when (val commands = device.fetchCommands()) {
            is AppResult.Success -> {
                commands.data.forEach { command ->
                    executor.execute(command)
                    device.ack(command.id)
                }
                AppResult.Success(Unit)
            }

            is AppResult.Failure -> commands
        }
    }
}
