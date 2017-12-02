import org.amshove.kluent.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xit

object SolverSpec : Spek({
    describe("the solving algorithm") {
        // tests of things outside the piece placing algorithm
    }

    describe("the piece placing algorithm") {
        it("should work when the board is empty") {
            placePiece(Tile(), listOf()).shouldBeTrue()
            placePiece(Tile(), listOf(Tile())).shouldBeTrue()
        }

        it("should not work when there are no more tiles") {
            val board = Tile(mapOf(0 to 0 to 'a'))
            placePiece(board, listOf()).shouldBeFalse()
        }

        it("should not work when no more tiles fit on the board") {
            // TODO: add more cases
            val board = Tile(mapOf(0 to 0 to 'a'))
            placePiece(board, listOf(Tile(mapOf(0 to 0 to 'a', 1 to 0 to 'b')))).shouldBeFalse()
            placePiece(board, listOf(Tile(mapOf(0 to 0 to 'b')))).shouldBeFalse()
        }

        xit("should find solutions that require transformations") {
            // TODO: implement transformations, so this test would fail
            val board = Tile(mapOf(0 to 0 to 'a', 1 to 0 to 'b'))
            val piece = board.reflectLR
            placePiece(board, listOf(piece)).shouldBeTrue()
        }

        it("should work when the board is the only piece") {
            val squares = mapOf(
                0 to 0 to 'a',
                0 to 1 to 'b'
            )
            val board = Tile(squares)
            val pieces = listOf(Tile(squares))

            placePiece(board, pieces).shouldBeTrue()
        }
    }
})
