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

object HttpHeader {
    fun format(key: String, value: String) = "$key: $value"
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

data class RequestBodyString(
    private val httpCode: HttpCodes = HttpCodes.HTTP_200,
    val body: String? = null,
    private val headers: MutableMap<String, String> = defaultHeaders(body)
) {

    override fun toString(): String {
        return buildString {
            // Request Line
            append(httpCode.value).append(CRLF_CONST)

            // Headers
            headers.forEach {
                append(HttpHeader.format(it.key, it.value)).append(CRLF_CONST)
            }

            // Request Body
            append(CRLF_CONST)
            if (body != null) append(body)
        }
    }

    fun toByteArray(): ByteArray {
        val compressedBody = compress(body)
        headers["Content-Length"] = compressedBody?.size?.toString() ?: headers["Content-Length"]!!
        var data = buildString {
            // Request Line
            append(httpCode.value).append(CRLF_CONST)

            // Headers
            headers.forEach {
                append(HttpHeader.format(it.key, it.value)).append(CRLF_CONST)
            }

            // Request Body
            append(CRLF_CONST)
        }.toByteArray()
        if (compressedBody != null) {
            data += compressedBody
            println("compressed body = ${compressedBody.size}")
        }
        return data
    }

    fun addHeader(key: String, value: String) {
        headers[key] = value
    }
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

internal fun compress(data: String?): ByteArray? {
    data ?: return null
    val os = ByteArrayOutputStream()
    val gzip = GZIPOutputStream(os)
    gzip.write(data.toByteArray(Charsets.UTF_8))
    gzip.close()
    return os.toByteArray()
}
