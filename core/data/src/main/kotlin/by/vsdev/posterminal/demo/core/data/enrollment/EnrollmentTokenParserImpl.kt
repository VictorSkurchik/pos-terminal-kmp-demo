package by.vsdev.posterminal.demo.core.data.enrollment

import by.vsdev.posterminal.demo.domain.model.EnrollmentToken
import by.vsdev.posterminal.demo.domain.service.EnrollmentTokenParser
import by.vsdev.posterminal.demo.dto.parseEnrollmentToken

/** Parses a scanned QR payload using the shared `:core` codec, mapping to the domain token. */
class EnrollmentTokenParserImpl : EnrollmentTokenParser {
    override fun parse(payload: String): EnrollmentToken? =
        parseEnrollmentToken(payload)?.let { EnrollmentToken(token = it.token, serverUrl = it.serverUrl) }
}
