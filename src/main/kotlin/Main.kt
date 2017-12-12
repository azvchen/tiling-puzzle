import java.io.File
import java.io.FileNotFoundException

fun main(args: Array<String>) {
    while (true) {
        print("File name (q to quit): ")
        val fileName = readLine()
        if (fileName == "q") {
            break
        }
        val text: String
        try {
            text = File(fileName).readText()
        } catch (e: FileNotFoundException) {
            println("Invalid file name.")
            continue
        }
        val tiles = textInput(text)
        val solver = Solver(tiles, reflect = true)
        println(solver.validSizes)
        solver.solve()
        println(solver.solutions.size)
    }
}

fun printTiles(tiles: List<Tile>) {
    for (tile in tiles) {
        println("Tile with dims ${tile.dims} and size ${tile.size}")
        println(tile)
    }
}
