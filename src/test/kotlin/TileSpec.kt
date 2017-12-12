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

        it("should copy itself properly") {
            val copy = twoTile.copy()
            copy shouldEqual twoTile
            copy shouldNotBe twoTile
        }

        it("should rotate correctly") {
            val square = Tile(mapOf(0 to 0 to 'a', 1 to 0 to 'b', 0 to 1 to 'c', 1 to 1 to 'd'))
            square.rotateLeft shouldEqual Tile(mapOf(0 to 0 to 'b', 1 to 0 to 'd', 0 to 1 to 'a', 1 to 1 to 'c'))
            square.rotateAbout shouldEqual Tile(mapOf(0 to 0 to 'd', 1 to 0 to 'c', 0 to 1 to 'b', 1 to 1 to 'a'))
            square.rotateRight shouldEqual Tile(mapOf(0 to 0 to 'c', 1 to 0 to 'a', 0 to 1 to 'd', 1 to 1 to 'b'))
        }

        it("should reflect along the vertical axis") {
            twoTile.dims shouldEqual flipTile.dims
            twoTile[1, 0] shouldEqual flipTile[0, 0]
            twoTile[0, 0] shouldEqual flipTile[1, 0]
        }

        it("should have 8 transformations in the general case") {
            val tile = Tile(mapOf(0 to 0 to 'a', 1 to 0 to 'b', 0 to 1 to 'c', 1 to 1 to 'd'))
            tile.transformations.size shouldEqual 8
        }

        it("should have fewer than 8 transformations when symmetric") {
            val tile = Tile(mapOf(0 to 0 to 'a', 1 to 0 to 'a', 0 to 1 to 'b', 1 to 1 to 'b'))
            tile.transformations.size shouldEqual 4
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

        it("should not change dimensions when removing a piece") {
            val board = Tile(mapOf(
                0 to 0 to 'a',
                0 to 1 to 'a',
                1 to 0 to 'a',
                1 to 1 to 'a'
            ))
            val piece = Tile(mapOf(
                0 to 0 to 'a',
                0 to 1 to 'a',
                1 to 0 to 'a'
            ))
            val removed = board.without(piece, 0, 0)

            removed shouldEqual Tile(mapOf(1 to 1 to 'a'), pad = true)
            removed.size shouldEqual 1
        }
    }

    describe("the normalize algorithm") {
        it("should do nothing if the squares are already normalize") {
            val squares = mapOf(0 to 0 to 'a', 1 to 0 to 'b')
            normalize(squares) shouldEqual squares
        }

        it("should translate the tile such that the min row and col are 0") {
            val squares = mapOf(1 to 0 to 'b')
            normalize(squares) shouldEqual mapOf(0 to 0 to 'b')
        }
    }
})
