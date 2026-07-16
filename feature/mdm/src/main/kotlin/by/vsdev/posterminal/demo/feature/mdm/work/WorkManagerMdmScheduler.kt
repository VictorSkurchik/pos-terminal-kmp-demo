package by.vsdev.posterminal.demo.feature.mdm.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import by.vsdev.posterminal.demo.domain.service.MdmScheduler
import java.util.concurrent.TimeUnit

/** WorkManager-backed [MdmScheduler] (no FCM). */
class WorkManagerMdmScheduler(private val context: Context) : MdmScheduler {

    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /** Periodic polling. WorkManager minimum is 15 minutes. */
    override fun schedulePeriodic() {
        val request = PeriodicWorkRequestBuilder<MdmSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(UNIQUE_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    /** Immediate run (the "Sync now" button in the demo). */
    override fun triggerOnce() {
        val request = OneTimeWorkRequestBuilder<MdmSyncWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }

    override fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_NAME)
    }

    private companion object {
        const val UNIQUE_NAME = "mdm-sync"
    }
}
