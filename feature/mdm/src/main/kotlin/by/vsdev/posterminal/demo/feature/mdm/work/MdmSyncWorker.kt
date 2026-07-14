package by.vsdev.posterminal.demo.feature.mdm.work

import android.content.Context
import android.os.BatteryManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import by.vsdev.posterminal.demo.core.data.prefs.SettingsRepository
import by.vsdev.posterminal.demo.core.data.repo.RemoteRepository
import by.vsdev.posterminal.demo.feature.mdm.CommandExecutor
import kotlinx.coroutines.flow.first

/**
 * Periodic agent: heartbeat → pull commands → execute → ack.
 * Registration is done manually/via QR (see EnrollmentViewModel); if not enrolled — no-op.
 */
class MdmSyncWorker(
    appContext: Context,
    params: WorkerParameters,
    private val remote: RemoteRepository,
    private val executor: CommandExecutor,
    private val settings: SettingsRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            if (!settings.enrolled.first()) return Result.success()

            remote.heartbeat(batteryLevel())
            remote.fetchCommands().forEach { command ->
                if (executor.execute(command)) {
                    remote.ack(command.id)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun batteryLevel(): Int? {
        val bm = applicationContext.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.takeIf { it in 0..100 }
    }
}
