package server

import Solver
import Tile
import com.squareup.moshi.Types
import io.ktor.util.ByteBufferBuilder
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.io.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

private val pair = Types.newParameterizedType(Pair::class.java, Integer::class.java, Integer::class.java)
private val type = Types.newParameterizedType(Map::class.java, pair, Tile::class.java)
private val adapter = moshi.adapter<Map<Pair<Int, Int>, Tile>>(type)

class SolutionServer {
    private val listeners = ConcurrentHashMap<String, SolveSession>()

    operator fun get(id: String): SolveSession = listeners[id] ?: throw NoSuchElementException()
    operator fun set(id: String, session: SolveSession) {
        listeners[id] = session
        async {
            session.send("settings received")
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
        val solver = Solver(settings.tiles)
        solver.observable.subscribe { solution ->
            println(solution)
            var serializedSolution = ""
            try {
                serializedSolution = adapter.toJson(solution)
            } catch (e: Exception) {
                println(e)
            }
            println(serializedSolution)
            async {
                send(serializedSolution)
            }
        }
        async {
            solver.solve()
        }
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
