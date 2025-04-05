fun main(args: Array<String>) {
    println("Logs from your program will appear here!")
    val commands = createCommands(args)
    ConnectionManager(4221, commands).run()
}