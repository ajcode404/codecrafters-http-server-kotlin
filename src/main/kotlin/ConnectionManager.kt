import java.io.BufferedReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
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


enum class RequestConstant(val index: Int) {
    PATH(0),
    USER_AGENT(2),
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

    fun contentLength(): Int {
        for (line in lines) {
            if (line.startsWith("Content-Length: ")) {
                return line.substringAfter("Content-Length: ").toInt()
            }
        }
        throw IllegalStateException("Content-Length not found")
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
    private val br: BufferedReader = clientSocket.getInputStream().bufferedReader()
    private val outputStream: OutputStream = clientSocket.getOutputStream()

    fun handle() {
        var line: String? = br.readLine()
        val list = mutableListOf<String>()
        while (!line.isNullOrEmpty()) {
            list.add(line)
            line = br.readLine()
        }
        val request = Request(list)
        println("request = $request")
        handleResponse(request)
    }

    private fun handleResponse(request: Request) {
        val path = request.getPath()
        val resp: ByteArray = when {
            path.startsWith("GET / ")  -> {
                HttpCodes.HTTP_200_WITH_CRLF.toByteArray()
            }
            path.startsWith("GET /echo/") -> {
                val str = path.substringAfter("GET /echo/").substringBefore(" HTTP/1.1")
                buildString {
                    requestBodyString(str)
                }.also {
                    println("echo_body: $it")
                }.toByteArray()
            }
            path.startsWith("GET /user-agent") -> {
                val str = request.getUserAgent().split(": ")[1]
                buildString {
                    requestBodyString(str)
                }.also {
                    println("user-agent_body: $it")
                }.toByteArray()
            }
            path.startsWith("GET /files/") -> {
                val cmd = commands[0] as Command.Directory
                val fileName = path.substringAfter("GET /files/").substringBefore(" HTTP/1.1")
                val file = readFile("${cmd.directory}/$fileName")
                if (file != null) {
                    buildString {
                        requestBodyString(
                            body = file.fileData,
                            contentType = ContentType.OCTET_STREAM
                        )
                    }.also {
                        println("files-body: $it")
                    }.toByteArray()
                } else {
                    HttpCodes.HTTP_404.toByteArray()
                }
            }
            path.startsWith("POST /files/") -> {
                val cmd = commands[0] as Command.Directory
                val fileName = path.substringAfter("POST /files/").substringBefore(" HTTP/1.1")
                val contentLength = request.contentLength()
                val body = CharArray(contentLength)

                if (contentLength > 0) {
                    br.read(body, 0, contentLength)
                }
                writeFile("${cmd.directory}/$fileName", buildString { append(body) })
                HttpCodes.HTTP_201.toByteArray()
            }
            else -> HttpCodes.HTTP_404.toByteArray()
        }
        outputStream.write(resp)
        outputStream.flush()
        outputStream.close()
    }
}
