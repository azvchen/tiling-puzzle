import java.util.*

fun textInput(text: String): List<Tile> {
    val grid = textToTile(text)
    val tiles = mutableListOf<Tile>()
    val visited = mutableSetOf<Pos>()
    for (r in 0 until grid.width) {
        for (c in 0 until grid.height) {
            if (r to c !in visited && grid[r, c] != null && grid[r, c] != Tile.blank) {
                tiles.add(findTile(grid, visited, r, c))
            }
        }
    }
    return tiles
}

fun textToTile(text: String): Tile {
    val grid = mutableMapOf<Pos, Color>()
    for ((y, line) in text.lines().withIndex()) {
        for ((x, c) in line.withIndex()) {
            grid[x to y] = c
        }
    }
    return Tile(grid)
}

fun findTile(grid: Tile, visited: MutableSet<Pos>, row: Int, col: Int): Tile {
    val squares = mutableMapOf<Pos, Color>()
    val queue: Queue<Pos> = ArrayDeque<Pos>()
    queue.add(row to col)

    while (queue.isNotEmpty()) {
        val (r, c) = queue.remove()
        if (r to c in visited) {
            continue
        }
        visited.add(r to c)

        val color = grid[r, c]
        if (color != null) {
            squares[r to c] = color
            queue.addAll(listOf(r-1 to c, r+1 to c, r to c-1, r to c+1))
        }
    }
    return Tile(squares)
}
