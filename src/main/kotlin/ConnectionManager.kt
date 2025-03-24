import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
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
                val handler = RequestHandler(clientSocket.inputStream, clientSocket.outputStream)
                handler.handle()
            }
        }
    }
}

data class Request(
    val lines: List<String>
) {
    fun getPath(): String {
        println(lines[0])
        return lines[0].split(Regex(" "))[1]
    }

    fun isEmpty() = lines.isEmpty()

    fun getUserAgent(): String {
        if (lines.size < 3) throw IllegalStateException("User Agent not found")
        return lines[2]
    }
}

class RequestHandler(
    private val inputStream: InputStream,
    private val outputStream: OutputStream
) {

    fun handle() {
        val br = inputStream.bufferedReader()
        var line: String? = br.readLine()
        val list = mutableListOf<String>()
        while (!line.isNullOrEmpty()) {
            list.add(line)
            line = br.readLine()
        }
        val request = Request(list)
        handleResponse(request)
    }

    private fun handleResponse(request: Request) {
        println("Fifth log $request")
        if (request.isEmpty()) {
            return
        }
        val path = request.getPath()
        println("path = $path")
        val resp = when {
            path == "/"  -> {
                 println("Hit here")
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
        outputStream.write(resp.toByteArray())
        outputStream.flush()
        outputStream.close()
    }
}
