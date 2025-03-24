
const val CRLF_CONST = "\r\n"

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

internal fun StringBuilder.requestBodyString(str: String) {
    // Request Line
    append(HttpCodes.HTTP_200).append(CRLF_CONST)
    // Headers
    append(HttpHeader.contentType(ContentType.TEXT_PLAIN)).append(CRLF_CONST)
    append(HttpHeader.contentLength(str)).append(CRLF_CONST).append(CRLF_CONST)
    // Request Body
    append(str)
}
