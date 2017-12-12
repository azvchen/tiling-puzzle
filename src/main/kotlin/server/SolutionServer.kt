package server

import Solver
import io.ktor.util.ByteBufferBuilder
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.io.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

class SolutionServer {
    private val listeners = ConcurrentHashMap<String, SolveSession>()

    operator fun get(id: String): SolveSession = listeners[id] ?: throw NoSuchElementException()
    operator fun set(id: String, session: SolveSession) {
        listeners[id] = session
        async {
            session.send("success")
        }
    }

    suspend fun createOrRecoverSession(sessionId: String, socket: WebSocketSession) {
        listeners.putIfAbsent(sessionId, SolveSession(id = sessionId, socket = socket))
    }

    suspend fun sessionClosed(sessionId: String, socket: WebSocketSession) {
        listeners.remove(sessionId)
    }

    suspend fun broadcast(message: String) {
        listeners.values.forEach { listener ->
            listener.send(message)
        }
    }
}

data class SolveSession(
    val id: String,
    val settings: SolveSettings = SolveSettings(),
    val socket: WebSocketSession
) {
    suspend fun startSolve() {
        send("solving")
        Solver(settings.tiles).solve()
    }
    suspend fun send(message: String) {
        send(ByteBufferBuilder.build {
            putString(message, Charsets.UTF_8)
        })
    }
    private suspend fun send(serialized: ByteBuffer) {
        socket.send(Frame.Text(true, serialized.duplicate()))
    }
}
