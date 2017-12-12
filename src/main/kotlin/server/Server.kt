package server
import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.sessions.*
import io.ktor.util.nextNonce
import io.ktor.websocket.*
import kotlinx.coroutines.experimental.channels.consumeEach
import java.time.Duration

private val server = SolutionServer()

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    install(WebSockets) {
        pingPeriod = Duration.ofMinutes(1)
    }
    install(CORS) {
        host("localhost:3000")
    }
    install(Routing) {
        install(Sessions) {
            cookie<SolveSession>("SESSION")
        }
        intercept(ApplicationCallPipeline.Infrastructure) {
            if (call.sessions.get<SolveSession>() == null) {
                call.sessions.set(SolveSession(nextNonce()))
            }
        }
        get("/") {
            call.respondText("Hello!", ContentType.Text.Html)
        }
        webSocket("/ws") {
            val session = call.sessions.get<SolveSession>()
            if (session == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
                return@webSocket
            }

            server.createOrRecoverSession(session.id, this)

            try {
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        receivedMessage(session.id, frame.readText())
                    }
                }
            } finally {
                server.sessionClosed(session.id, this)
            }
        }
    }
}

data class SolveSession(val id: String)

private suspend fun receivedMessage(id: String, command: String) {
    when {
        command.startsWith("solve ") -> {
            val puzzle = command.removePrefix("solve ")
            server.startSolve(id, puzzle)
        }
    }
}
