import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import server.SolveSettings
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.atomic.AtomicBoolean

typealias Solution = Map<Pos, Tile>
fun List<Tile>.board(): Tile = this.maxBy { it.size } ?: throw IllegalStateException("need at least one tile")

class Solver(private val settings: SolveSettings) {
    private val tiles = textInput(settings.puzzle)
    private val board = tiles.board()
    private val pieces = tiles.filter { it !== board }.sortedByDescending { it.size }
    private val transformFunc = if(settings.reflections && settings.rotations) Tile::transformations
        else if (settings.rotations) Tile::rotations else if (settings.reflections) Tile::reflections else Tile::noTransform
    private val transforms = pieces.map(transformFunc)

    private val validSizes = (0 until pieces.size).map { n -> allSums(pieces.drop(n).map { it.size }) }

    val solutions = ConcurrentSkipListSet<Int>()
    val onSolution = PublishSubject.create<Solution>()

    fun logSolution(placed: Solution) {
        // mercy
        val code = placed.hashCode()
        val added = solutions.add(code)
        if (added) {
            onSolution.onNext(placed)
        }
    }

    fun solve(): Boolean {
        val symmetries: MutableList<(Tile) -> Tile> = mutableListOf()
        if (settings.reflections) {
            symmetries += listOf(
                Tile::reflectLR,
                Tile::reflectUD,
                Tile::reflectPrim,
                Tile::reflectOff
            )
        }
        if (settings.rotations) {
            symmetries += listOf(
                Tile::rotateLeft,
                Tile::rotateAbout,
                Tile::rotateRight
            )
        }

        val solvable = placePiece(board, syms = symmetries.toList())
        onSolution.onComplete()
        return solvable
    }

    fun placePiece(board: Tile, n: Int = 0, placed: Solution = emptyMap(), syms: List<(Tile) -> Tile> = emptyList()): Boolean {
        if (board.size == 0) {
            logSolution(placed)
            return true
        }
        if (n >= pieces.size || !hasValidFragments(board, n)) {
            return false
        }

        val visitedBoards: ConcurrentSkipListSet<Tile> = ConcurrentSkipListSet()
        val symmetries = syms.filter { sym -> sym(board) == board }

        val success = AtomicBoolean()
        val compute = { piece: Tile ->
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
                        success.compareAndSet(false, result)
                    }
                }
            }
        }
        if (placed.isEmpty()) {
            // split computations into threads on first level
            val jobs = transforms[n].map { piece ->
                async(CommonPool) { compute(piece) }
            }
            runBlocking {
                // collect all of the coroutines
                jobs.forEach { it.await() }
            }
        } else {
            transforms[n].forEach(compute)
        }

        if (board.size <= pieces.listIterator(n + 1).asSequence().map { it.size }.sum()) {
            success.compareAndSet(false, placePiece(board, n = n + 1, placed = placed, syms = symmetries))
        }

        return success.get()
    }

    private fun hasValidFragments(board: Tile, n: Int): Boolean =
        findTiles(board).all { it.size in validSizes[n] }
}

fun printPlacedTiles(placed: Solution) {
    for ((pos, tile) in placed) {
        println(pos)
        println(tile)
    }
}

private fun allSums(ints: List<Int>): Set<Int> {
    val sums = mutableSetOf(0)
    for (i in ints) {
        sums += sums.map { it + i }
    }
    return sums
}
