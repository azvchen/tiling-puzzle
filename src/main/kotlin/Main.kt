import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import server.SolveSettings
import java.io.File
import java.io.FileNotFoundException
import java.lang.management.ManagementFactory
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

fun main(args: Array<String>) {
    print("Do benchmark [yN]: ")
    if (readLine()?.startsWith('y', ignoreCase = true) == true) {
        return benchmark()
    }
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

        print("Use tile reflections: ")
        val reflect = readLine()?.toBoolean() ?: false

        var threshold = 30
        val bean = ManagementFactory.getThreadMXBean()
        var firstTime: Long = -1
        val solver = Solver(SolveSettings(puzzle = text, reflections = reflect))
        val count = AtomicInteger(0)
        solver.onSolution.subscribe { solution ->
            val n = count.incrementAndGet()
            if (n == 1) {
                firstTime = bean.currentThreadCpuTime
            }
            if (n <= threshold) {
                println("Solution $n")
                printPlacedTiles(solution)
            } else if (n == threshold + 1) {
                println("More than $threshold solutions.")
                threshold = if (threshold % 3 == 0) threshold / 3 * 10 else threshold * 3
            }
        }
        println("Started.")
        val startTime = bean.currentThreadCpuTime
        solver.solve()
        val elapsedTime = bean.currentThreadCpuTime - startTime
        val firstElapsedTime = if (firstTime > 0) firstTime - startTime else -1

        println("Total number of solutions: ${count.get()}")
        println("Time for first solution: $firstElapsedTime ns")
        println("Time for all solutions:  $elapsedTime ns")
    }
}

fun benchmark() {
    val directory = File(".")
    val files = directory.list().filter { it.endsWith(".txt") }
    fun compute(file: String) {
        val text =
            try {
                File(file).readText()
            } catch (e: FileNotFoundException) {
                return
            }

        val bean = ManagementFactory.getThreadMXBean()
        var firstTime: Long = -1
        val solver = Solver(SolveSettings(puzzle = text, serial = true))
        val found = AtomicBoolean(false)
        solver.onSolution.subscribe { _ ->
            if (found.compareAndSet(false, true)) {
                firstTime = bean.currentThreadCpuTime
            }
        }

        val startTime = bean.currentThreadCpuTime
        solver.solve()
        val elapsedTime = bean.currentThreadCpuTime - startTime
        val firstElapsedTime = if (firstTime > 0) firstTime - startTime else -1

        println("$file,${solver.solutions.size},$firstElapsedTime,$elapsedTime")
    }

    val jobs = files.map { file -> async(CommonPool) { compute(file) } }
    runBlocking {
        jobs.forEach { it.await() }
    }
}

fun printTiles(tiles: List<Tile>) {
    for (tile in tiles) {
        println("Tile with dims ${tile.dims} and size ${tile.size}")
        println(tile)
    }
}
