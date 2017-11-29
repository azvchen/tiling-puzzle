import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

object TileSpec: Spek({
    describe("a tile") {
        val twoTile = Tile(mapOf(
                0 to 0 to 'a',
                1 to 0 to 'b'
        ))
        val flipTile = twoTile.reflectLR

        it("should have consistent dimensions") {
            assertEquals(2, twoTile.size)
            assertEquals(2, twoTile.width)
            assertEquals(1, twoTile.height)
            assertEquals(2 to 1, twoTile.dims)
        }

        it("should print itself neatly") {
            assertEquals("ab", twoTile.toString())
        }

        it("should reflect along the vertical axis") {
            assertEquals(twoTile.dims, flipTile.dims)
            assertEquals(twoTile[1, 0], flipTile[0, 0])
            assertEquals(twoTile[0, 0], flipTile[1, 0])
        }

        it("should fit on a board") {
            val board = Tile(mapOf(
                    0 to 0 to 'a',
                    0 to 1 to 'b',
                    1 to 0 to 'b',
                    1 to 1 to 'a'
            ))

            assertTrue(twoTile.fitAt(board, 0, 0))
            assertFalse(twoTile.fitAt(board, 0, 1))

            assertFalse(flipTile.fitAt(board, 0, 0))
            assertTrue(flipTile.fitAt(board, 0, 1))
        }
    }
})