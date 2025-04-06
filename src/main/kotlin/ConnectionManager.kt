import java.net.ServerSocket
import java.util.concurrent.Executors

class ConnectionManager(
    port: Int,
    private val commands: List<Command>
) {
    private var serverSocket = ServerSocket(port)
    private val executorService = Executors.newFixedThreadPool(10)

    init {
        serverSocket.reuseAddress = true
        serverSocket.receiveBufferSize
    }

    fun run() {
        while (true) {
            val clientSocket = serverSocket.accept()
            executorService.submit {
                val handler = RequestHandler(clientSocket, commands)
                handler.handle()
            }
        }
    }
}
