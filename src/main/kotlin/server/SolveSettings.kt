package server

import textInput

data class SolveSettings(
    val reflections: Boolean = true,
    val rotations: Boolean = true,
    val puzzle: String = ""
) {
    @Transient
    val tiles = textInput(puzzle)
}
