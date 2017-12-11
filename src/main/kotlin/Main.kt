import java.io.File

fun main(args: Array<String>) {
    while (true) {
        print("File name: ")
        val fileName = readLine()
        val text = File(fileName).readText()
        val tiles = textInput(text)
        val solver = Solver(tiles)
        solver.solve()
    }
}
