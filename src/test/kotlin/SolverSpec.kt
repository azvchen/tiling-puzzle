import org.amshove.kluent.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object SolverSpec : Spek({
    describe("the solving algorithm") {
        // tests of things outside the piece placing algorithm
        it("should find a solution in a simple case") {
            val tiles = listOf(
                Tile(mapOf(0 to 0 to 'a', 1 to 0 to 'b', 0 to 1 to 'c')),
                Tile(mapOf(0 to 0 to 'd')),
                Tile(mapOf(0 to 0 to 'a', 1 to 0 to 'b', 0 to 1 to 'c', 1 to 1 to 'd'))
            )
            val s = Solver(tiles)
            s.solve().shouldBeTrue()
            s.solutions.size shouldEqual 1
        }

        it("should find a solution in another simple case") {
            val tiles = listOf(
                Tile(mapOf(0 to 0 to 'a')),
                Tile(mapOf(0 to 0 to 'b')),
                Tile(mapOf(0 to 0 to 'a', 1 to 0 to 'b'))
            )
            val s = Solver(tiles)
            s.solve().shouldBeTrue()
            s.solutions.size shouldEqual 1
        }

        it("should find a unique solution when there are isomorphic ones") {
            val tiles = listOf(
                Tile(mapOf(0 to 0 to 'a')),
                Tile(mapOf(0 to 0 to 'a')),
                Tile(mapOf(0 to 0 to 'a', 1 to 0 to 'a'))
            )
            val s = Solver(tiles)
            s.solve().shouldBeTrue()
            s.solutions.size shouldEqual 1  // will be 2 without removing isomorphisms
        }
    }

    describe("the piece placing algorithm") {
        on("an empty board") {
            val s = Solver(listOf(Tile(), Tile()))

            it("should work when the board is empty") {
                s.placePiece(Tile(), 0).shouldBeTrue()
                s.placePiece(Tile(), 0).shouldBeTrue()
            }
        }

        on("a simple board and tiles") {
            val board = Tile(mapOf(0 to 0 to 'a', 1 to 0 to 'b'))
            val s = Solver(listOf(
                Tile(mapOf(0 to 0 to 'a')),
                Tile(mapOf(0 to 0 to 'c')),
                board
            ))

            it("should not work when there are no more tiles") {
                s.placePiece(board, 1).shouldBeFalse()
            }

            it("should not work when no more tiles fit on the board") {
                s.placePiece(board, 1).shouldBeFalse()
            }
        }

        it("should work when the board is the only piece") {
            val board = Tile(mapOf(0 to 0 to 'a', 1 to 0 to 'b'))
            val s = Solver(listOf(board, board.copy()))
            s.placePiece(board, 0).shouldBeTrue()
        }

        it("should find solutions that require transformations") {
            val board = Tile(mapOf(0 to 0 to 'a', 1 to 0 to 'b'))
            val s = Solver(listOf(board, board.rotateAbout))
            s.placePiece(board, 0).shouldBeTrue()
        }
    }
})
