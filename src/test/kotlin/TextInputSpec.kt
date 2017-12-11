import org.amshove.kluent.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xit

object TextInputSpec : Spek({
    describe("the text to tile-grid converter") {
        it("should work in a simple case") {
            val text = "ab\nc"
            val tile = Tile(mapOf(0 to 0 to 'a', 1 to 0 to 'b', 0 to 1 to 'c'))
            textToTile(text) shouldEqual tile
        }

        it("should work with spaces") {
            val text = " ab\nc d"
            val tile = Tile(mapOf(1 to 0 to 'a', 2 to 0 to 'b', 0 to 1 to 'c', 2 to 1 to 'd'))
            textToTile(text) shouldEqual tile
        }
    }

    describe("the tile finding algorithm") {
        val grid = textToTile(listOf(
            "aa ",
            "a c",
            "b c"
        ).joinToString("\n"))

        it("should work in a simple case") {
            val visited = mutableSetOf<Pos>()
            val tile = findTile(grid, visited, 0, 0)
            tile shouldEqual Tile(mapOf(0 to 0 to 'a', 1 to 0 to 'a', 0 to 1 to 'a', 0 to 2 to 'b'))
        }

        it("should skip a tile that was visited before") {
            val visited = mutableSetOf(0 to 0, 1 to 0, 0 to 1, 0 to 2)
            val tile = findTile(grid, visited, 2, 1)
            tile shouldEqual Tile(mapOf(0 to 0 to 'c', 0 to 1 to 'c'))
        }
    }

    describe("the text input algorithm") {
        it("should work in a trivial case") {
            val text = "ab"
            textInput(text) shouldEqual listOf(Tile(mapOf(0 to 0 to 'a', 1 to 0 to 'b')))
        }

        it("should work in a simple case") {
            val text = listOf(
                " aa    ",
                " a   c   ",
                " b   c   "
            ).joinToString("\n")
            val tiles = textInput(text)
            tiles.size shouldEqual 2
            tiles shouldEqual listOf(
                Tile(mapOf(0 to 0 to 'a', 1 to 0 to 'a', 0 to 1 to 'a', 0 to 2 to 'b')),
                Tile(mapOf(0 to 0 to 'c', 0 to 1 to 'c'))
            )
        }
    }
})
