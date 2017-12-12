package server

import Tile
import textInput

data class SolveSettings(
    val reflections: Boolean = true,
    val rotations: Boolean = true,
    val puzzle: String = ""
) {
    val tiles: List<Tile>
        get() = textInput(puzzle)
}
