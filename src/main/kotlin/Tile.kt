typealias Color = Char

fun <T : Comparable<T>> unsafeMax(list: Iterable<T>): T {
    return list.max() ?: throw IllegalStateException("List must be nonempty!")
}

class Tile(private val squares: Map<Pair<Int, Int>, Color>) {
    val size = squares.size
    val width = unsafeMax(squares.keys.map { it.first }) + 1
    val height = unsafeMax(squares.keys.map { it.second }) + 1
    val dims = width to height

    operator fun get(x: Int, y: Int): Color? {
        return squares[x to y]
    }

    fun fitAt(board: Tile, x: Int, y: Int): Boolean {
        for ((pos, c) in squares) {
            if (c != board[x + pos.first, y + pos.second]) {
                return false
            }
        }
        return true
    }

    val reflectLR: Tile by lazy {
        val newSquares = mutableMapOf<Pair<Int, Int>, Color>()
        for ((pos, c) in squares) {
            newSquares[width - pos.first - 1 to pos.second] = c
        }
        Tile(newSquares)
    }

    override fun toString(): String {
        return (0 until height).joinToString(separator = "\n") {
            c -> (0 until width).map { r -> this[r, c] ?: ' ' } .joinToString(separator = "")
        }
    }
}