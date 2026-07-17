package by.vsdev.posterminal.demo.feature.mdm.data

import by.vsdev.posterminal.demo.dto.parseEnrollmentToken
import by.vsdev.posterminal.demo.feature.mdm.domain.model.EnrollmentToken
import by.vsdev.posterminal.demo.feature.mdm.domain.service.EnrollmentTokenParser

/** Parses a scanned QR payload using the shared `:core` codec, mapping to the domain token. */
class EnrollmentTokenParserImpl : EnrollmentTokenParser {
    override fun parse(payload: String): EnrollmentToken? =
        parseEnrollmentToken(payload)?.let { EnrollmentToken(token = it.token, serverUrl = it.serverUrl) }
}
