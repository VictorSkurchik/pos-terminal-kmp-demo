package by.vsdev.posterminal.demo.feature.mdm

import android.app.Activity
import android.os.Handler
import android.os.Looper
import by.vsdev.posterminal.demo.domain.service.KioskController
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import java.lang.ref.WeakReference

/**
 * Bridge between the always-on agent (foreground service / WorkManager) and the current Activity/UI.
 * Commands arrive in the background, but kiosk (screen pinning) and showing messages need the
 * foreground. Admin messages are delivered as one-shot [messages] events (not retained state), so
 * they fire exactly once and survive configuration changes cleanly.
 */
class MdmController : KioskController {

    private var activityRef: WeakReference<Activity>? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val messageChannel = Channel<String>(Channel.BUFFERED)

    /** One-shot admin messages (SHOW_MESSAGE), consumed by the UI's MdmMessageHost. */
    val messages: Flow<String> = messageChannel.receiveAsFlow()

    private val _kioskActive = MutableStateFlow(false)
    override val kioskActive: StateFlow<Boolean> = _kioskActive.asStateFlow()

    fun bind(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    fun unbind(activity: Activity) {
        if (activityRef?.get() === activity) activityRef = null
    }

    fun showMessage(text: String) {
        messageChannel.trySend(text)
    }

    fun startKiosk() = mainHandler.post {
        runCatching { activityRef?.get()?.startLockTask() }
        _kioskActive.value = true
    }

    fun stopKiosk() = mainHandler.post {
        runCatching { activityRef?.get()?.stopLockTask() }
        _kioskActive.value = false
    }
}
