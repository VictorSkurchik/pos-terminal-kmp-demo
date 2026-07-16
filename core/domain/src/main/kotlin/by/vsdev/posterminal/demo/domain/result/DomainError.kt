package by.vsdev.posterminal.demo.domain.result

/**
 * A typed, exhaustive set of failure modes the app cares about. Repositories translate raw
 * exceptions (Ktor, IO, serialization) into one of these so the presentation layer can react
 * meaningfully instead of inspecting stack traces.
 */
sealed interface DomainError {
    /** No connectivity / host unreachable / socket failure. */
    data object Network : DomainError

    /** The request outlived its timeout. */
    data object Timeout : DomainError

    /** 401/403 — missing or rejected credentials. */
    data object Unauthorized : DomainError

    /** 404 — the resource (usually this device) is unknown to the backend. */
    data object NotFound : DomainError

    /** Any 5xx from the backend. */
    data class Server(val code: Int) : DomainError

    /** The response body could not be parsed into the expected shape. */
    data object Serialization : DomainError

    /** Anything not otherwise classified; keeps the original cause for logging. */
    data class Unknown(val cause: Throwable? = null) : DomainError
}
