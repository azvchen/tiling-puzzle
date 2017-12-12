package server

import io.ktor.websocket.WebSocketSession

data class SolutionListener(
    val progress: Double,
    val socket: WebSocketSession
)
