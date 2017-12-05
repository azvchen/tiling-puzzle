import org.amshove.kluent.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

object TileSpec : Spek({
    describe("a tile") {
        val twoTile = Tile(mapOf(
            0 to 0 to 'a',
            1 to 0 to 'b'
        ))
        val flipTile = twoTile.reflectLR

        it("should be empty by default") {
            val emptyTile = Tile()
            emptyTile.size shouldEqual 0
            emptyTile.dims shouldEqual (0 to 0)
        }

        it("should have consistent dimensions") {
            twoTile.size shouldEqual 2
            twoTile.width shouldEqual 2
            twoTile.height shouldEqual 1
            twoTile.dims shouldEqual (2 to 1)
        }

        it("should have structural equality") {
            twoTile shouldEqual twoTile
            twoTile shouldEqual twoTile.reflectLR.reflectLR
        }

        it("should print itself neatly") {
            twoTile.toString() shouldEqual "ab"
        }

        it("should reflect along the vertical axis") {
            twoTile.dims shouldEqual flipTile.dims
            twoTile[1, 0] shouldEqual flipTile[0, 0]
            twoTile[0, 0] shouldEqual flipTile[1, 0]
        }

        it("should fit on a board") {
            val board = Tile(mapOf(
                0 to 0 to 'a',
                0 to 1 to 'b',
                1 to 0 to 'b',
                1 to 1 to 'a'
            ))

            twoTile.fitAt(board, 0, 0).shouldBeTrue()
            twoTile.fitAt(board, 0, 1).shouldBeFalse()

            flipTile.fitAt(board, 0, 0).shouldBeFalse()
            flipTile.fitAt(board, 0, 1).shouldBeTrue()
        }
    }

    describe("the flush algorithm") {
        it("should do nothing if the squares are already flush") {
            val squares = mapOf(0 to 0 to 'a', 1 to 0 to 'b')
            flush(squares) shouldEqual squares
        }

        it("should translate the tile such that the min row and col are 0") {
            val squares = mapOf(1 to 0 to 'b')
            flush(squares) shouldEqual mapOf(0 to 0 to 'b')
        }
    }
})
