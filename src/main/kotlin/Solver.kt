class Solver(tiles: List<Tile>, reflect: Boolean = false) {
    val board = tiles.maxBy { it.size } ?: throw IllegalStateException("need at least one tile")
    val pieces = tiles.filter { it !== board } .sortedByDescending { it.size }
    val transformFunc = if (reflect) { t: Tile -> t.transformations } else { t: Tile -> t.rotations }
    val transforms = pieces.map(transformFunc)

    val solutions = mutableListOf<Map<Pos, Tile>>()

    fun logSolution(placed: Map<Pos, Tile> = mapOf()) {
        solutions.add(placed)
        println(placed)
    }

    fun solve(): Boolean {
        return placePiece(board)
    }

    fun placePiece(board: Tile, n: Int = 0, placed: Map<Pos, Tile> = mapOf()): Boolean {
        if (board.size == 0) {
            logSolution(placed)
            return true
        }
        if (n >= pieces.size) {
            return false
        }

        var success = false
        val pieceList = transforms[n]
        for (piece in pieceList) {
            for (x in 0 until board.width - piece.width + 1) {
                for (y in 0 until board.height - piece.height + 1) {
                    if (board[x, y] != null && piece.fitAt(board, x, y)) {
                        val result = placePiece(
                            board.without(piece, x, y),
                            n = n + 1,
                            placed = placed.plus(x to y to piece)
                        )
                        success = success || result
                    }
                }
            }
        }

        if (board.size <= pieces.listIterator(n + 1).asSequence().map { it.size } .sum()) {
            success = placePiece(board, n = n + 1, placed = placed) || success
        }

        return success
    }
}
