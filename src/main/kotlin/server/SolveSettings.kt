package server

data class SolveSettings(
    val reflections: Boolean = false,
    val rotations: Boolean = true,
    val serial: Boolean = false,
    val puzzle: String = ""
)
