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
                append(format(it.key, it.value)).append(CRLF_CONST)
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
                append(format(it.key, it.value)).append(CRLF_CONST)
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

    private fun format(key: String, value: String) = "$key: $value"
}