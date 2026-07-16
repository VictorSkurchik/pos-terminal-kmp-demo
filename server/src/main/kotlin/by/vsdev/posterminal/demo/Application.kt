package by.vsdev.posterminal.demo

import by.vsdev.posterminal.demo.data.DeviceRepository
import by.vsdev.posterminal.demo.db.createDatabase
import by.vsdev.posterminal.demo.dto.AckRequest
import by.vsdev.posterminal.demo.dto.HeartbeatRequest
import by.vsdev.posterminal.demo.dto.NewCommandRequest
import by.vsdev.posterminal.demo.dto.RegisterRequest
import by.vsdev.posterminal.demo.network.posJson
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun main() {
    // Render (and most PaaS) inject the port to bind via the PORT env var.
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val repository = DeviceRepository(createDatabase())

    install(ContentNegotiation) { json(posJson) }
    install(CallLogging)
    // CORS for the web admin (Compose/JS); anyHost for the demo. TODO prod: restrict origin.
    install(CORS) {
        anyHost()
        allowHeaders { true }
        allowNonSimpleContentTypes = true
        listOf(
            io.ktor.http.HttpMethod.Get,
            io.ktor.http.HttpMethod.Post,
        ).forEach { allowMethod(it) }
    }
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (cause.message ?: "unknown")))
        }
    }

    routing {
        // Device registration (including via a QR-enrollment token).
        post("/devices/register") {
            val request = call.receive<RegisterRequest>()
            val device = repository.register(request, now())
            call.respond(HttpStatusCode.Created, device)
        }

        // Heartbeat: update lastSeenAt/status/battery.
        post("/devices/{id}/heartbeat") {
            val id = call.deviceId() ?: return@post
            val request = call.receive<HeartbeatRequest>()
            val device = repository.heartbeat(id, request)
            if (device == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "device not found"))
            } else {
                call.respond(device)
            }
        }

        // Device pulls pending commands (polling).
        get("/devices/{id}/commands") {
            val id = call.deviceId() ?: return@get
            if (!repository.exists(id)) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "device not found"))
                return@get
            }
            call.respond(repository.pendingCommands(id))
        }

        // Acknowledge command execution.
        post("/devices/{id}/commands/{cmdId}/ack") {
            val cmdId = call.parameters["cmdId"]
            if (cmdId.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "missing cmdId"))
                return@post
            }
            val request = call.receive<AckRequest>()
            if (repository.ack(cmdId, request)) {
                call.respond(HttpStatusCode.OK, mapOf("ok" to true))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "command not found"))
            }
        }

        // Admin enqueues a new command.
        post("/devices/{id}/commands") {
            val id = call.deviceId() ?: return@post
            if (!repository.exists(id)) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "device not found"))
                return@post
            }
            val request = call.receive<NewCommandRequest>()
            call.respond(HttpStatusCode.Created, repository.enqueue(id, request, now()))
        }

        // List of all devices for the admin.
        get("/devices") {
            call.respond(repository.listDevices())
        }

        // Admin removes a device (Wipe / device Logout). Its commands are deleted too.
        delete("/devices/{id}") {
            val id = call.deviceId() ?: return@delete
            if (repository.deleteDevice(id)) {
                call.respond(HttpStatusCode.OK, mapOf("ok" to true))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "device not found"))
            }
        }
    }
}

private fun now(): Long = System.currentTimeMillis()

private suspend fun io.ktor.server.application.ApplicationCall.deviceId(): String? {
    val id = parameters["id"]
    if (id.isNullOrBlank()) {
        respond(HttpStatusCode.BadRequest, mapOf("error" to "missing device id"))
        return null
    }
    return id
}
