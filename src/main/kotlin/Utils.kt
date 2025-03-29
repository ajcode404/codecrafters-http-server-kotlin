import java.io.File

const val CRLF_CONST = "\r\n"

object HttpCodes {
    const val HTTP_200_WITHOUT_CRLF =  "HTTP/1.1 200 OK"
    const val HTTP_200_WITH_CRLF =  "$HTTP_200_WITHOUT_CRLF$CRLF_CONST$CRLF_CONST"
    const val HTTP_201 = "HTTP/1.1 201 Created$CRLF_CONST${CRLF_CONST}"
    const val HTTP_404 =  "HTTP/1.1 404 Not Found$CRLF_CONST$CRLF_CONST"
}

enum class ContentType(val value: String) {
    TEXT_PLAIN("text/plain"),
    OCTET_STREAM("application/octet-stream")
}

object HttpHeader {
    fun contentLength(str: String) = "Content-Length: ${str.length}"
    fun contentType(contentType: ContentType) = "Content-Type: ${contentType.value}"
}

internal fun StringBuilder.requestBodyString(
    body: String,
    contentType: ContentType = ContentType.TEXT_PLAIN
) {
    // Request Line
    append(HttpCodes.HTTP_200_WITHOUT_CRLF).append(CRLF_CONST)
    // Headers
    append(HttpHeader.contentType(contentType)).append(CRLF_CONST)
    append(HttpHeader.contentLength(body)).append(CRLF_CONST).append(CRLF_CONST)
    // Request Body
    append(body)
}

data class FileMetadata(
    val fileData: String
)

internal fun readFile(fileName: String): FileMetadata? {
    return runCatching {
        FileMetadata(File(fileName).readText(Charsets.UTF_8))
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

internal fun writeFile(fileName: String, text: String) {
    runCatching {
        val file = File(fileName)
        file.writeText(text)
    }
}
