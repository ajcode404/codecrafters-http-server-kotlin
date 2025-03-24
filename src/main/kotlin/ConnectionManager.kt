import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class ConnectionManager {

    // register a socket connection
//    private val clientConnections = mutableListOf<Socket>()

    private val serverSocket = ServerSocket(4221).apply {
        reuseAddress = true
        receiveBufferSize
    }

    fun run() {
        while (true) {
            val clientSocket = serverSocket.accept()
            thread {
                RequestHandler(clientSocket).handle()
            }
        }
    }
}

data class Request(
    val lines: List<String>,
    val size: Int = lines.size
) {
    fun getPath(): String {
        if (size < 1) throw IllegalStateException("No path exist")
        return lines[0].split(Regex("\\s+"))[1]
    }

    fun getUserAgent(): String {
        if (size < 3) throw IllegalStateException("User Agent not found")
        return lines[2]
    }
}

class RequestHandler(
    clientSocket: Socket
) {

    private val inputStream = clientSocket.getInputStream()
    private val outputStream = clientSocket.getOutputStream()

    fun handle() {
        inputStream.bufferedReader().use {
            var line: String? = it.readLine()
            val list = mutableListOf<String>()
            while (line != null && list.size < 4) {
                list.add(line)
                line = it.readLine()
            }
            val request = Request(list)
            handleResponse(request)
        }
    }

    private fun handleResponse(request: Request) {
        val path = request.getPath()
        val resp = when {
            path == "/"  -> HttpCodes.HTTP_200
            path.startsWith("/echo/") -> {
                val str = path.substringAfter("/echo/")
                buildString {
                    requestBodyString(str)
                }
            }
            path.startsWith("/user-agent") -> {
                val str = request.getUserAgent().split(": ")[1]
                buildString {
                    requestBodyString(str)
                }
            }
            else -> "${HttpCodes.HTTP_404}$CRLF_CONST$CRLF_CONST"
        }
        outputStream.write(resp.toByteArray())
        outputStream.flush()
        outputStream.close()
    }
}
