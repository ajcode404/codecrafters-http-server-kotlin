import java.net.ServerSocket
import kotlin.concurrent.thread

class ConnectionManager(
    port: Int,
    private val commands: List<Command>
) {
    private var serverSocket = ServerSocket(port)

    init {
        serverSocket.reuseAddress = true
        serverSocket.receiveBufferSize

    }

    fun run() {
        while (true) {
            val clientSocket = serverSocket.accept()
            thread {
                val handler = RequestHandler(clientSocket, commands)
                handler.handle()
            }
        }
    }
}

