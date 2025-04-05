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
