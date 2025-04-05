enum class HttpCode(val value: String) {
    HTTP_200("HTTP/1.1 200 OK"),
    HTTP_201("HTTP/1.1 201 Created"),
    HTTP_404("HTTP/1.1 404 Not Found");

    override fun toString(): String {
        return buildString {
            append(value).append(CRLF_CONST)
        }
    }
}
