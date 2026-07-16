package by.vsdev.posterminal.demo.dto

import by.vsdev.posterminal.demo.network.posJson
import kotlinx.serialization.encodeToString

/** Encode/decode [EnrollmentToken] for QR — shared by the web generator and the Android scanner. */

fun EnrollmentToken.toQrPayload(): String = posJson.encodeToString(this)

fun parseEnrollmentToken(payload: String): EnrollmentToken? =
    runCatching { posJson.decodeFromString<EnrollmentToken>(payload) }.getOrNull()
