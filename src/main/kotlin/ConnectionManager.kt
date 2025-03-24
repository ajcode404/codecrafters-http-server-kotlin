import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class ConnectionManager {
    private var serverSocket = ServerSocket(4221)
    init {
        serverSocket.reuseAddress = true
        serverSocket.receiveBufferSize

    }

    fun run() {
        while (true) {
            val clientSocket = serverSocket.accept()
            thread {
                val handler = RequestHandler(clientSocket)
                val response = handler.handle()
                val os = handler.clientSocket.outputStream
                os.write(response.toByteArray())
                os.flush()
                os.close()
                println(response)
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
    val clientSocket: Socket
) {

    fun handle(): String {
        clientSocket.inputStream.bufferedReader().use {
            var line: String? = it.readLine()
            val list = mutableListOf<String>()
            while (line != null && list.size < 4) {
                list.add(line)
                line = it.readLine()
            }
            val request = Request(list)
            return handleResponse(request)
        }
    }

    private fun handleResponse(request: Request): String {
        val path = request.getPath()
        // println("path = $path")
        val resp = when {
            path == "/"  -> {
                // println("Hit here")
                "HTTP/1.1 200 OK\r\n\r\n"
            }
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
        return resp
    }
}
