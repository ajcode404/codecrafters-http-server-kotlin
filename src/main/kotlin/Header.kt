data class Header(
    private val headers: MutableMap<String, String> = mutableMapOf()
) {
    enum class HeaderConst(val key: String) {
        USER_AGENT("User-Agent"),
        CONTENT_TYPE("Content-Type"),
        CONTENT_LENGTH("Content-Length"),
        ACCEPT_ENCODING("Accept-Encoding"),
        CONTENT_ENCODING("Content-Encoding")
    }

    fun setHeader(header: HeaderConst, value: String) {
        headers[header.key] = value
    }

    fun getValue(header: HeaderConst): String {
        return headers[header.key] ?: throw IllegalStateException("${header.key} not found")
    }

    override fun toString(): String {
        return buildString {
            headers.forEach {
                append(format(it.key, it.value)).append(CRLF_CONST)
            }
        }
    }

    private fun format(key: String, value: String) = "$key: $value"

    companion object {

        fun createWithDefault(body: String?): Header {
            val header = Header()
            header.setHeader(HeaderConst.CONTENT_TYPE, ContentType.TEXT_PLAIN.value)
            if (body != null) {
                header.setHeader(HeaderConst.CONTENT_LENGTH, body.length.toString())
            }
            return header
        }
    }

}

enum class ContentType(val value: String) {
    TEXT_PLAIN("text/plain"),
    OCTET_STREAM("application/octet-stream")
}