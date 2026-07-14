package by.vsdev.posterminal.demo.feature.mdm

import android.app.Activity
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

/**
 * Bridge between the always-on agent (foreground service / WorkManager) and the current Activity/UI.
 * Commands arrive in the background, but kiosk (screen pinning) and showing messages need the foreground.
 */
class MdmController {

    private var activityRef: WeakReference<Activity>? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _kioskActive = MutableStateFlow(false)
    val kioskActive: StateFlow<Boolean> = _kioskActive.asStateFlow()

    /** One-shot command events surfaced by the agent — collected by the UI to show a snackbar. */
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val events: SharedFlow<String> = _events.asSharedFlow()

    fun emitEvent(text: String) { _events.tryEmit(text) }

    fun bind(activity: Activity) { activityRef = WeakReference(activity) }
    fun unbind(activity: Activity) { if (activityRef?.get() === activity) activityRef = null }

    fun showMessage(text: String) { _message.value = text }
    fun consumeMessage() { _message.value = null }

    fun startKiosk() = mainHandler.post {
        runCatching { activityRef?.get()?.startLockTask() }
        _kioskActive.value = true
    }

    fun stopKiosk() = mainHandler.post {
        runCatching { activityRef?.get()?.stopLockTask() }
        _kioskActive.value = false
    }
}
