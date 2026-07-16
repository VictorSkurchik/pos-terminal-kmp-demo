package by.vsdev.posterminal.demo.network

import kotlinx.serialization.json.Json

/** The single JSON configuration shared by the client, the server and the QR codec. */
val posJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
}
