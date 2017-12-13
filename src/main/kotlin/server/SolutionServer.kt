package server

import Solver
import Tile
import board
import com.squareup.moshi.Types
import io.ktor.util.ByteBufferBuilder
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.io.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

private val pairType = Types.newParameterizedType(Pair::class.java, Integer::class.java, Integer::class.java)
private val mapType = Types.newParameterizedType(Map::class.java, pairType, Tile::class.java)
private val solutionAdapter = moshi.adapter<Map<Pair<Int, Int>, Tile>>(mapType)
private val tileAdapter = moshi.adapter(Tile::class.java)

class SolutionServer {
    private val listeners = ConcurrentHashMap<String, SolveSession>()

    operator fun get(id: String): SolveSession = listeners[id] ?: throw NoSuchElementException()
    operator fun set(id: String, session: SolveSession) {
        listeners[id] = session
        async {
            session.sendBoard()
        }
    }

    suspend fun createOrRecoverSession(sessionId: String, socket: WebSocketSession) {
        val listener = listeners.getOrPut(sessionId) { SolveSession(id = sessionId, socket = socket) }
        listener.sendBoard()
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
    suspend fun sendBoard() {
        if (settings.tiles.isNotEmpty()) {
            val board = settings.tiles.board()
            message("board", tileAdapter.toJson(board))
        } else {
            send("board")
        }
    }

    suspend fun startSolve() {
        send("solving")
        val solver = Solver(settings.tiles)
        solver.onSolution.subscribe { solution ->
            async {
                message("solution", solutionAdapter.toJson(solution))
            }
        }
        async {
            solver.solve()
            message("solved", solver.solutions.size.toString())
        }
    }

    suspend fun message(command: String, payload: String) {
        send("$command $payload")
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
