import java.io.BufferedReader
import java.io.OutputStream
import java.net.Socket

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
        val path = request.path
        val resp: ByteArray = when {

            path.isVerbGet() && path.route == "/" -> {
                HttpCodes.HTTP_200_WITH_CRLF.toByteArray()
            }

            path.isVerbGet() && path.route.startsWith("/echo/") -> {
                val str = path.route.substringAfter("/echo/")
                buildString {
                    requestBodyString(str)
                }.also {
                    println("echo_body: $it")
                }.toByteArray()
            }

            path.isVerbGet() && path.route.startsWith("/user-agent") -> {
                val str = request.getUserAgent()
                buildString {
                    requestBodyString(str)
                }.also {
                    println("user-agent_body: $it")
                }.toByteArray()
            }

            path.isVerbGet() && path.route.startsWith("/files/") -> {
                val cmd = commands[0] as Command.Directory
                val fileName = path.route.substringAfter("/files/")
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

            path.isVerbPost() && path.route.startsWith("/files/") -> {
                val cmd = commands[0] as Command.Directory
                val fileName = path.route.substringAfter("/files/")
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
