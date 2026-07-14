package by.vsdev.posterminal.demo.feature.mdm.enrollment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.vsdev.posterminal.demo.core.data.prefs.SettingsRepository
import by.vsdev.posterminal.demo.core.data.repo.RemoteRepository
import by.vsdev.posterminal.demo.dto.parseEnrollmentToken
import by.vsdev.posterminal.demo.feature.mdm.MdmController
import by.vsdev.posterminal.demo.feature.mdm.work.MdmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EnrollmentUiState(
    val deviceId: String = "…",
    val name: String = "POS Terminal",
    val enrolled: Boolean = false,
    val busy: Boolean = false,
    val status: String? = null,
)

class EnrollmentViewModel(
    private val remote: RemoteRepository,
    private val settings: SettingsRepository,
    private val scheduler: MdmScheduler,
    controller: MdmController,
) : ViewModel() {

    private val deviceId = MutableStateFlow("…")
    private val name = MutableStateFlow("POS Terminal")
    private val busy = MutableStateFlow(false)
    private val status = MutableStateFlow<String?>(null)

    val kioskActive: StateFlow<Boolean> = controller.kioskActive

    val uiState: StateFlow<EnrollmentUiState> =
        combine(deviceId, name, settings.enrolled, busy, status) { id, nm, enrolled, isBusy, st ->
            EnrollmentUiState(id, nm, enrolled, isBusy, st)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EnrollmentUiState())

    init {
        viewModelScope.launch {
            deviceId.value = settings.getOrCreateDeviceId()
            name.value = settings.deviceName.first()
        }
    }

    fun onNameChange(value: String) { name.value = value }

    fun enroll() {
        if (busy.value) return
        viewModelScope.launch {
            busy.value = true
            status.value = null
            try {
                settings.setDeviceName(name.value)
                val device = remote.registerThisDevice()
                settings.setEnrolled(true, name = name.value, serverUrl = SettingsRepository.DEFAULT_SERVER_URL)
                scheduler.schedulePeriodic()
                status.value = "Enrolled as ${device.id} ✓"
            } catch (e: Exception) {
                status.value = "Enrollment failed: ${e.message}"
            } finally {
                busy.value = false
            }
        }
    }

    /** QR enrollment: parse the token, register with it, and store the serverUrl from the QR. */
    fun enrollWithToken(payload: String) {
        if (busy.value) return
        val parsed = parseEnrollmentToken(payload)
        if (parsed == null) {
            status.value = "Invalid QR payload"
            return
        }
        viewModelScope.launch {
            busy.value = true
            status.value = null
            try {
                settings.setDeviceName(name.value)
                val device = remote.registerThisDevice(enrollmentToken = parsed.token)
                settings.setEnrolled(true, name = name.value, serverUrl = parsed.serverUrl)
                scheduler.schedulePeriodic()
                status.value = "Enrolled via QR as ${device.id} (token ${parsed.token}) ✓"
            } catch (e: Exception) {
                status.value = "QR enrollment failed: ${e.message}"
            } finally {
                busy.value = false
            }
        }
    }

    fun syncNow() {
        scheduler.triggerOnce()
        status.value = "Sync requested"
    }

    /** Logout: cancel the agent, delete this device from the backend, clear local enrollment. */
    fun logout() {
        if (busy.value) return
        viewModelScope.launch {
            busy.value = true
            status.value = null
            try {
                scheduler.cancel()
                remote.logout()
            } catch (e: Exception) {
                status.value = "Logout error: ${e.message}"
            } finally {
                busy.value = false
            }
        }
    }
}
