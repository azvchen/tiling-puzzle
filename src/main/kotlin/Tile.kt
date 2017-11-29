typealias Color = Char

fun <T : Comparable<T>> Iterable<T>.unsafeMax() = this.max() ?: throw IllegalStateException("List must be nonempty!")

class Tile(private val squares: Map<Pair<Int, Int>, Color>) {
    val size = squares.size
    val width = squares.keys.map { it.first }.unsafeMax() + 1
    val height = squares.keys.map { it.second }.unsafeMax() + 1
    val dims = width to height

    operator fun get(x: Int, y: Int): Color? = squares[x to y]

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
