class Solver(tiles: List<Tile>, reflect: Boolean = false) {
    val board = tiles.maxBy { it.size } ?: throw IllegalStateException("need at least one tile")
    val pieces = tiles.filter { it !== board }.sortedByDescending { it.size }
    val transformFunc = if (reflect) Tile::transformations else Tile::rotations
    val transforms = pieces.map(transformFunc)

    val validSizes = allSums(pieces.map { it.size })

    val solutions = mutableSetOf<Map<Pos, Tile>>()

    fun logSolution(placed: Map<Pos, Tile>) {
        if (placed !in solutions) {
            solutions.add(placed)
            println("Solution ${solutions.size}")
            printPlacedTiles(placed)
        }
    }

    fun solve(): Boolean {
        val symmetries: List<(Tile) -> Tile> = listOf<(Tile) -> Tile>(
            Tile::reflectLR,
            Tile::reflectUD,
            Tile::reflectPrim,
            Tile::reflectOff,
            Tile::rotateLeft,
            Tile::rotateAbout,
            Tile::rotateRight
        )

        return placePiece(board, syms = symmetries)
    }

    fun placePiece(board: Tile, n: Int = 0, placed: Map<Pos, Tile> = emptyMap(), syms: List<(Tile) -> Tile> = emptyList()): Boolean {
        if (board.size == 0) {
            logSolution(placed)
            return true
        }
        if (n >= pieces.size || !hasValidFragments(board)) {
            return false
        }

        val visitedBoards: MutableSet<Tile> = HashSet()
        val symmetries = syms.filter { sym -> sym(board) == board }

        var success = false
        for (piece in transforms[n]) {
            for (x in 0 until board.width - piece.width + 1) {
                for (y in 0 until board.height - piece.height + 1) {
                    if (piece.fitAt(board, x, y)) {
                        val newBoard = board.without(piece, x, y)
                        if (newBoard in visitedBoards) {
                            continue
                        }
                        visitedBoards += symmetries.map { sym -> sym(newBoard) }
                        val result = placePiece(
                            newBoard,
                            n = n + 1,
                            placed = placed.plus(x to y to piece),
                            syms = symmetries
                        )
                        success = success || result
                    }
                }
            }
        }

        if (board.size <= pieces.listIterator(n + 1).asSequence().map { it.size }.sum()) {
            success = placePiece(board, n = n + 1, placed = placed, syms = symmetries) || success
        }

        return success
    }

    fun hasValidFragments(board: Tile): Boolean {
        for (fragment in findTiles(board)) {
            if (fragment.size !in validSizes) {
                return false
            }
        }
        return true
    }
}

fun printPlacedTiles(placed: Map<Pos, Tile>) {
    for ((pos, tile) in placed) {
        println(pos)
        println(tile)
    }
}

fun allSums(ints: List<Int>): Set<Int> {
    if (ints.isEmpty()) {
        return setOf(0)
    }
    val subSums = allSums(ints.drop(1))
    return subSums + subSums.map { it + ints[0] }
}
