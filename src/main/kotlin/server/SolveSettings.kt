package server
import Tile

data class SolveSettings(
    val reflections: Boolean = false,
    val rotations: Boolean = true,
    val serial: Boolean = false,
    val puzzle: String = "",
    @Transient
    val tiles: List<Tile>? = null
)
