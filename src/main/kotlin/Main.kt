import java.io.InputStream
import java.net.ServerSocket
import java.net.SocketTimeoutException


const val CRLF = "\r\n"

object HttpCodes {
    const val HTTP_200 =  "HTTP/1.1 200 OK"
    const val HTTP_404 =  "HTTP/1.1 404 Not Found"
}

enum class ContentType(val value: String) {
    TEXT_PLAIN("text/plain")
}

object HttpHeader {
    fun contentLength(str: String) = "Content-Length: ${str.length}"
    fun contentType(contentType: ContentType) = "Content-Type: ${contentType.value}"
}


class Request(
    inputStream: InputStream
) {

    private val buf = StringBuilder()

    private var requestLine: String? = null

    private var body = ""

    private val headers = mutableMapOf<String, String>()

    init {
        inputStream.bufferedReader().lineSequence().iterator().run {
            kotlin.runCatching {
                requestLine = next()
                while (hasNext()) {
                    val line = next()
                    if (line.isEmpty()) {
                        break
                    }
                    val name = line.substringBefore(":").lowercase()
                    val value = line.substringAfter(":").trim()
                    headers[name] = value
                }
            }.onFailure {
                if (it is SocketTimeoutException) {
                    body = buf.toString()
                }
            }
        }
    }
}

fun main() {
    println("Logs from your program will appear here!")
    val serverSocket = ServerSocket(4221)
    serverSocket.reuseAddress = true
    serverSocket.receiveBufferSize

    // wait for clients
    val clientSocket = serverSocket.accept()
    // get output stream
    val outputStream = clientSocket.getOutputStream()
    val inputStream = clientSocket.getInputStream()

    inputStream.bufferedReader().use {
        val line = it.readLine()
        val splitline = line.split(Regex("\\s+"))
        val path = splitline[1]
        it.readLine()
        val userAgent = it.readLine()
        val response = when {
            path == "/" -> "${HttpCodes.HTTP_200}$CRLF$CRLF"
            path.startsWith("/echo/") -> {
                val str = path.substringAfter("/echo/")
                buildString {
                    requestBody(str)
                }
            }
            path.startsWith("/user-agent") -> {
                val str = userAgent.split(": ")[1]
                buildString {
                    requestBody(str)
                }
            }
            else -> "${HttpCodes.HTTP_404}$CRLF$CRLF"
        }
        outputStream.write(response.toByteArray())
        outputStream.flush()
        outputStream.close()
    }
}

private fun StringBuilder.requestBody(str: String) {
    // Request Line
    append(HttpCodes.HTTP_200).append(CRLF)
    // Headers
    append(HttpHeader.contentType(ContentType.TEXT_PLAIN)).append(CRLF)
    append(HttpHeader.contentLength(str)).append(CRLF).append(CRLF)
    // Request Body
    append(str)
}
