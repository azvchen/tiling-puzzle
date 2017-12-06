fun solve(tiles: List<Tile>): Boolean { // what should the return type be
    val board = tiles.maxBy { it.size } ?: throw IllegalStateException("tiles should be nonempty")
    val pieces = tiles.filter { it !== board }

    // preliminary logic
    if (board.size != pieces.map { it.size } .sum()) {
        return false
    }

    return placePiece(board, pieces)
}

fun placePiece(board: Tile, pieces: List<Tile>): Boolean { // what should the return type be
    if (board.size == 0) {
        return true
    }
    for (piece in pieces) {
        for (x in 0 until board.width) {
            for (y in 0 until board.height) {
                if (piece.fitAt(board, x, y)) {
                    val result = placePiece(board.without(piece, x, y), pieces.filter { it != piece })
                    if (result) {
                        return result
                    }
                }
            }
        }
    }
    return false
}
