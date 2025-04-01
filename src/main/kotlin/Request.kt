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
        encodings.split(", ").forEach {
            if (it in supportedEncoding) {
                return true
            }
        }
        return false
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

data class Path(
    val verb: String,
    val route: String,
    val httpVersion: String
) {
    companion object {
        fun create(path: String): Path {
            val (verb, route, httpVersion) = path.split(" ")
            return Path(verb, route, httpVersion)
        }
    }

    fun isVerbGet(): Boolean = "GET" == verb

    fun isVerbPost(): Boolean = "POST" == verb

    override fun toString()
            ="$verb $route $httpVersion"
}
