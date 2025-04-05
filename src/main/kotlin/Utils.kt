import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.GZIPOutputStream

const val CRLF_CONST = "\r\n"

enum class HttpCodes(val value: String) {
    HTTP_200("HTTP/1.1 200 OK"),
    HTTP_201("HTTP/1.1 201 Created"),
    HTTP_404("HTTP/1.1 404 Not Found")
}

enum class ContentType(val value: String) {
    TEXT_PLAIN("text/plain"),
    OCTET_STREAM("application/octet-stream")
}

internal fun defaultHeaders(body: String?): MutableMap<String, String> {
    return mutableMapOf(
        "Content-Type" to ContentType.TEXT_PLAIN.value
    ).also {
        if (body != null) {
            it["Content-Length"] = body.length.toString()
        }
    }
}

internal fun compress(data: String?): ByteArray? {
    data ?: return null
    val os = ByteArrayOutputStream()
    val gzip = GZIPOutputStream(os)
    gzip.write(data.toByteArray(Charsets.UTF_8))
    gzip.close()
    return os.toByteArray()
}
