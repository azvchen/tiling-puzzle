import java.util.*
import kotlin.math.*

private val letter = 'X'

fun generatePuzzle(width: Int, height: Int): List<Tile> {
    val grid = Tile((0 until width).map { r -> (0 until height).map { c -> r to c to letter }} .flatten() .toMap())
    return generatePuzzle(grid)
}

fun generatePuzzle(grid: Tile): List<Tile> {
    val tiles = mutableListOf<Tile>()
    val selected = mutableSetOf<Pos>()
    for (r in 0 until grid.width) {
        for (c in 0 until grid.height) {
            if (r to c !in selected && grid[r, c] != null) {
                tiles.add(generateTile(grid, selected, r, c))
            }
        }
    }
    return tiles
}

private fun generateTile(grid: Tile, selected: MutableSet<Pos>, row: Int, col: Int): Tile {
    val squares = mutableMapOf<Pos, Color>()
    val queue: Queue<Pos> = ArrayDeque<Pos>()
    queue.add(row to col)
    val visited = mutableSetOf<Pos>()

    val random = Random()

    while (queue.isNotEmpty()) {
        val (r, c) = queue.remove()
        if (r to c in selected || r to c in visited) {
            continue
        }
        visited.add(r to c)

        val color = grid[r, c]
        val rate =
            if (squares.size > 1)
                max(1 - 1.8 * (abs(row - r) + abs(row - c)) / grid.size - 0.05 * squares.size, 0.2)
            else 0.95
        if (color != null && (row to col == r to c || random.nextFloat() < rate)) {
            squares[r to c] = color
            selected.add(r to c)
            queue.addAll(listOf(r-1 to c, r+1 to c, r to c-1, r to c+1))
        }
    }
    return Tile(squares)
}
