package by.vsdev.posterminal.demo.network

import io.ktor.client.engine.HttpClientEngineFactory

/**
 * Platform-specific Ktor client engine:
 * - android/jvm → OkHttp
 * - js → Js (fetch)
 */
expect val httpClientEngineFactory: HttpClientEngineFactory<*>
