package by.vsdev.posterminal.demo.feature.mdm.work

import android.content.Context
import android.os.BatteryManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import by.vsdev.posterminal.demo.domain.repository.SettingsRepository
import by.vsdev.posterminal.demo.domain.result.AppResult
import by.vsdev.posterminal.demo.domain.usecase.mdm.SyncDeviceUseCase
import kotlinx.coroutines.flow.first

/**
 * Periodic fallback agent: delegates one sync cycle to [SyncDeviceUseCase]. If not enrolled it's a
 * no-op. A failed cycle is logged and retried (WorkManager backoff), never swallowed.
 */
class MdmSyncWorker(
    appContext: Context,
    params: WorkerParameters,
    private val sync: SyncDeviceUseCase,
    private val settings: SettingsRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (!settings.enrolled.first()) return Result.success()

        return when (val result = sync(batteryLevel())) {
            is AppResult.Success -> Result.success()
            is AppResult.Failure -> {
                Log.w(TAG, "MDM sync failed: ${result.error}")
                Result.retry()
            }
        }
    }

    private fun batteryLevel(): Int? {
        val bm = applicationContext.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        return bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.takeIf { it in 0..100 }
    }

    private companion object {
        const val TAG = "MdmSyncWorker"
    }
}
