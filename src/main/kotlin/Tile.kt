typealias Color = Char

class Tile(private val squares: Map<Pair<Int, Int>, Color> = mapOf()) {
    val size = squares.size
    val width = squares.keys.map { it.first } .max() ?.plus(1) ?: 0
    val height = squares.keys.map { it.second } .max() ?.plus(1) ?: 0
    val dims = width to height

    operator fun get(x: Int, y: Int): Color? = squares[x to y]

    fun without(tile: Tile, x: Int, y: Int): Tile = Tile(squares.filter { (pos, _) ->
        (pos.first - x to pos.second - y) !in tile.squares
    })

    fun fitAt(board: Tile, x: Int, y: Int): Boolean = squares.all { (pos, color) ->
        color == board[x + pos.first, y + pos.second]
    }

    val reflectLR: Tile by lazy {
        val newSquares = squares.map { (pos, color) -> width - pos.first - 1 to pos.second to color }.toMap()
        Tile(newSquares)
    }

    override fun toString(): String = (0 until height).joinToString(separator = "\n") { c ->
        (0 until width).map { r -> this[r, c] ?: ' ' }.joinToString(separator = "")
    }
}
