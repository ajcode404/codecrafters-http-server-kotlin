private val supportedEncoding: Set<String>  = setOf("gzip")

data class Request(
    val path: Path,
    val headers: Map<String, String?>
) {
    constructor(lines: List<String>) :
            this(Path.create(lines.first()), generateHeaders(lines))

    fun getPath(): String {
        return path.toString()
    }

    fun getUserAgent(): String {
        return getValue("User-Agent")
    }

    fun contentLength(): Int {
        return getValue("Content-Length").toInt()
    }

    fun isSupportedEncoding(): Boolean {
        val encodings = headers["Accept-Encoding"] ?: return false
        return encodings.split(", ").any {
            it in supportedEncoding
        }
    }

    private fun getValue(key: String): String {
        return headers[key] ?: throw IllegalStateException("$key not found")
    }

    companion object {
        private fun generateHeaders(lines: List<String>): Map<String, String?> {
            return lines.drop(1).mapNotNull { line ->
                val arr = line.split(": ", limit = 2)
                if (arr.size == 2) arr[0] to arr[1] else null
            }.associate { it }
        }
    }
}
