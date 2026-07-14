package by.vsdev.posterminal.demo.dto

import by.vsdev.posterminal.demo.network.PosApiClient
import kotlinx.serialization.encodeToString

/** Encode/decode [EnrollmentToken] for QR — shared by the web generator and the Android scanner. */

fun EnrollmentToken.toQrPayload(): String = PosApiClient.json.encodeToString(this)

fun parseEnrollmentToken(payload: String): EnrollmentToken? =
    runCatching { PosApiClient.json.decodeFromString<EnrollmentToken>(payload) }.getOrNull()
