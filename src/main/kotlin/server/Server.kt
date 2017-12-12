package server
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
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

private var moshi = Moshi.Builder().build()
private var settingsAdapter: JsonAdapter<SolveSettings> = moshi.adapter(SolveSettings::class.java)

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
            cookie<Session>("SESSION")
        }
        intercept(ApplicationCallPipeline.Infrastructure) {
            if (call.sessions.get<Session>() == null) {
                call.sessions.set(nextNonce())
            }
        }
        get("/") {
            call.respondText("Hello!", ContentType.Text.Html)
        }
        webSocket("/ws") {
            val session = call.sessions.get<Session>()
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

data class Session(val id: String)

private suspend fun receivedMessage(id: String, command: String) {
    when {
        command.startsWith("settings") -> {
            println(command)
            val settings = settingsAdapter.fromJson(command.removePrefix("settings").trim()) ?: SolveSettings()
            server[id] = server[id].copy(settings = settings)
        }
        command.startsWith("solve") -> {
            server[id].startSolve()
        }
        else -> server[id].send("Unknown command!")
    }
}
