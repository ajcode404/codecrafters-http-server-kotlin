import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class ConnectionManager(
    port: Int,
    val commands: List<Command>
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


enum class RequestConstant(val index: Int) {
    PATH(0),
    USER_AGENT(2)
}

data class Request(
    val lines: List<String>
) {

    fun getPath(): String {
        return getValue(RequestConstant.PATH)
    }

    fun getUserAgent(): String {
        return getValue(RequestConstant.USER_AGENT)
    }

    private fun getValue(requestConstant: RequestConstant): String {
        if (lines.size > requestConstant.index) {
            return lines[requestConstant.index]
        }
        throw IllegalStateException("${requestConstant.name} not found")
    }
}

class RequestHandler(
    clientSocket: Socket,
    private val commands: List<Command>
) {
    private val inputStream: InputStream = clientSocket.getInputStream()
    private val outputStream: OutputStream = clientSocket.getOutputStream()

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
        val path = request.getPath()
        val resp: ByteArray = when {
            path == "/"  -> {
                HttpCodes.HTTP_200_WITH_CRLF.toByteArray()
            }
            path.startsWith("/echo/") -> {
                val str = path.substringAfter("/echo/")
                buildString {
                    requestBodyString(str)
                }.also {
                    println("echo_body: $it")
                }.toByteArray()
            }
            path.startsWith("/user-agent") -> {
                val str = request.getUserAgent().split(": ")[1]
                buildString {
                    requestBodyString(str)
                }.also {
                    println("user-agent_body: $it")
                }.toByteArray()
            }
            path.startsWith("/files/") -> {
                val cmd = commands[0] as Command.Directory
                val fileName = path.substringAfter("/files/")
                val file = readFile("${cmd.directory}/$fileName")
                if (file != null) {
                    buildString {
                        requestBodyString(
                            body = file.fileData,
                            contentType = ContentType.OCTET_STREAM
                        )
                    }.toByteArray()
                } else {
                    HttpCodes.HTTP_404.toByteArray()
                }
            }
            else -> HttpCodes.HTTP_404.toByteArray()
        }
        outputStream.write(resp)
        outputStream.flush()
        outputStream.close()
    }
}
