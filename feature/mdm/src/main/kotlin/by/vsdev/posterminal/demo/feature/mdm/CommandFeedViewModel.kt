package by.vsdev.posterminal.demo.feature.mdm

import androidx.lifecycle.ViewModel
import by.vsdev.posterminal.demo.core.data.prefs.SettingsRepository
import by.vsdev.posterminal.demo.core.data.repo.RemoteRepository
import by.vsdev.posterminal.demo.model.DeviceCommand
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first

/**
 * Foreground command feed: while the app is visible, poll the backend for pending commands,
 * surface each as a snackbar and acknowledge it. Complements the background [work.MdmSyncWorker]
 * (which is throttled to ~15 min by WorkManager) with near-real-time delivery for the demo.
 */
class CommandFeedViewModel(
    private val settings: SettingsRepository,
    private val remote: RemoteRepository,
) : ViewModel() {

    private val _snackbar = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val snackbar: SharedFlow<String> = _snackbar

    /** Infinite polling loop; launched from the UI so it lives only while the app is on screen. */
    suspend fun poll() {
        while (true) {
            runCatching {
                if (settings.enrolled.first()) {
                    remote.fetchCommands().forEach { command ->
                        _snackbar.emit(describe(command))
                        remote.ack(command.id)
                    }
                }
            }
            delay(POLL_INTERVAL_MS)
        }
    }

    private fun describe(command: DeviceCommand): String = buildString {
        append("Command: ")
        append(command.type.name)
        command.payload?.let {
            append(" — ")
            append(it)
        }
    }

    companion object {
        private const val POLL_INTERVAL_MS = 4000L
    }
}
