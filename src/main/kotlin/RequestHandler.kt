import java.io.BufferedReader
import java.io.File
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
        val resp: RequestBodyString = when {

            path.isVerbGet() && path.route == "/" -> RequestBodyString()

            path.isVerbGet() && path.route.startsWith("/echo/") -> {
                val str = path.route.substringAfter("/echo/")
                RequestBodyString(
                    body = str
                ).also {
                    println("echo_body: $it")
                }
            }

            path.isVerbGet() && path.route.startsWith("/user-agent") -> {
                val str = request.getUserAgent()
                RequestBodyString(body = str).also {
                    println("user-agent_body: $it")
                }
            }

            path.isVerbGet() && path.route.startsWith("/files/") -> {
                val cmd = commands[0] as Command.Directory
                val fileName = path.route.substringAfter("/files/")
                val file = readFile("${cmd.directory}/$fileName")
                if (file != null) {
                    RequestBodyString(
                        body = file,
                        headers = Header.createWithDefault(file).also {
                            it.setHeader(Header.HeaderConst.CONTENT_TYPE, ContentType.OCTET_STREAM.value)
                        }
                    ).also {
                        println("files-body: $it")
                    }
                } else {
                    RequestBodyString(
                        httpCode = HttpCode.HTTP_404
                    )
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
                RequestBodyString(
                    httpCode = HttpCode.HTTP_201
                )
            }

            else -> RequestBodyString(httpCode = HttpCode.HTTP_404)
        }
        if (request.isSupportedEncoding()) {
            resp.addHeader(Header.HeaderConst.CONTENT_ENCODING, "gzip")
            outputStream.write(resp.toByteArray())
        } else {
            outputStream.write(resp.toString().toByteArray())
        }
        outputStream.flush()
        outputStream.close()
    }

    private fun writeFile(fileName: String, text: String) {
        runCatching {
            val file = File(fileName)
            file.writeText(text)
        }
    }

    private fun readFile(fileName: String): String? {
        return runCatching {
            File(fileName).readText(Charsets.UTF_8)
        }.fold(
            onSuccess = {
                it
            },
            onFailure = {
                it.printStackTrace()
                null
            }
        )
    }
}

