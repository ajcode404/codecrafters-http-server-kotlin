fun createCommands(args: Array<String>): List<Command> {
    val commands = mutableListOf<Pair<String, String>>()
    if (args.size % 2 == 0) {
        for (i in args.indices step 2) {
            commands.add(args[i] to args[i + 1])
        }
    }
    return commands.mapNotNull {
        Command.getCommand(it)
    }
}

sealed interface Command {

    data class Directory(val directory: String) : Command

    companion object {
        fun getCommand(pair: Pair<String, String>): Command? {
            return when (pair.first) {
                "--directory" -> Directory(pair.second)
                else -> null
            }
        }
    }
}
