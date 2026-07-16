package by.vsdev.posterminal.demo.domain.model

/** Enrollment payload decoded from a QR code: an opaque token plus the backend to talk to. */
data class EnrollmentToken(
    val token: String,
    val serverUrl: String,
)
