import java.io.File
import java.io.FileNotFoundException
import java.lang.management.ManagementFactory

fun main(args: Array<String>) {
    while (true) {
        print("File name (q to quit): ")
        val fileName = readLine()
        if (fileName == "q") {
            break
        }
        val text =
            try {
                File(fileName).readText()
            } catch (e: FileNotFoundException) {
                println("Invalid file name.")
                continue
            }
        val tiles = textInput(text)

        print("Use tile reflections: ")
        val reflect = readLine()?.toBoolean() ?: false

        var threshold = 30
        val bean = ManagementFactory.getThreadMXBean()
        var firstTime: Long = -1
        val solver = Solver(tiles, reflect = reflect, log = { placed, n ->
            if (n == 1) {
                firstTime = bean.currentThreadCpuTime
            }
            if (n <= threshold) {
                println("Solution $n")
                printPlacedTiles(placed)
            } else if (n == threshold + 1) {
                println("More than $threshold solutions.")
                threshold = if (threshold % 3 == 0) threshold / 3 * 10 else threshold * 3
            }
        })
        println("Started.")
        val startTime = bean.currentThreadCpuTime
        solver.solve()
        val elapsedTime = bean.currentThreadCpuTime - startTime
        val firstElapsedTime = if (firstTime > 0) firstTime - startTime else -1

        println("Total number of solutions: ${solver.solutions.size}")
        println("Time for first solution: $firstElapsedTime ns")
        println("Time for all solutions:  $elapsedTime ns")
    }
}

fun printTiles(tiles: List<Tile>) {
    for (tile in tiles) {
        println("Tile with dims ${tile.dims} and size ${tile.size}")
        println(tile)
    }
}
