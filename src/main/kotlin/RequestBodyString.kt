data class RequestBodyString(
    private val httpCode: HttpCode = HttpCode.HTTP_200,
    val body: String? = null,
    private val headers: Header = Header.createWithDefault(body),
) {

    override fun toString(): String {
        return buildString {
            // Request Line
            append(httpCode.toString())

            // Headers
            append(headers.toString())

            // Request Body
            append(CRLF_CONST)
            if (body != null) append(body)
        }
    }

    fun toByteArray(): ByteArray {
        val compressedBody = compress(body)
        headers.setHeader(Header.HeaderConst.CONTENT_LENGTH, compressedBody?.size?.toString()
            ?: headers.getValue(Header.HeaderConst.CONTENT_LENGTH))
        var data = buildString {
            // Request Line
            append(httpCode.value).append(CRLF_CONST)

            // Headers
            append(headers.toString())

            // Request Body
            append(CRLF_CONST)
        }.toByteArray()
        if (compressedBody != null) {
            data += compressedBody
            println("compressed body = ${compressedBody.size}")
        }
        return data
    }

    fun addHeader(key: Header.HeaderConst, value: String) {
        headers.setHeader(key, value)
    }

    private fun format(key: String, value: String) = "$key: $value"
}

