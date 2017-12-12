package server

import Solver
import io.ktor.util.ByteBufferBuilder
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import kotlinx.coroutines.experimental.io.ByteBuffer
import textInput
import java.util.concurrent.ConcurrentHashMap

class SolutionServer {
    private val listeners = ConcurrentHashMap<String, SolutionListener>()

    suspend fun createOrRecoverSession(sessionId: String, socket: WebSocketSession) {
        listeners.putIfAbsent(sessionId, SolutionListener(0.0, socket))
    }

    suspend fun sessionClosed(sessionId: String, socket: WebSocketSession) {
        listeners[sessionId]
        listeners.remove(sessionId)
    }

    suspend fun startSolve(sessionId: String, puzzle: String) {
        val tiles = textInput(puzzle)
        Solver(tiles).solve()
    }

    suspend fun broadcast(message: String) {
        broadcast(ByteBufferBuilder.build {
            putString(message, Charsets.UTF_8)
        })
    }

    suspend fun broadcast(serialized: ByteBuffer) {
        listeners.values.forEach { listener ->
            listener.socket.send(Frame.Text(true, serialized.duplicate()))
        }
    }
}
