package by.vsdev.posterminal.demo.core.data.error

import by.vsdev.posterminal.demo.domain.result.DomainError
import by.vsdev.posterminal.demo.network.DeviceNotFoundException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.serialization.JsonConvertException
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.net.UnknownHostException

/** Translates raw transport exceptions into the app's typed [DomainError] taxonomy. */
fun Throwable.toDomainError(): DomainError = when (this) {
    is DeviceNotFoundException -> DomainError.NotFound
    is HttpRequestTimeoutException,
    is ConnectTimeoutException,
    is SocketTimeoutException,
    -> DomainError.Timeout

    is ClientRequestException -> when (response.status.value) {
        401, 403 -> DomainError.Unauthorized
        404 -> DomainError.NotFound
        else -> DomainError.Server(response.status.value)
    }

    is ServerResponseException -> DomainError.Server(response.status.value)
    is JsonConvertException, is SerializationException -> DomainError.Serialization
    is UnknownHostException, is IOException -> DomainError.Network
    else -> DomainError.Unknown(this)
}
