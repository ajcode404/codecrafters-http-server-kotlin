import java.net.ServerSocket;

fun main() {
    println("Logs from your program will appear here!")
    var serverSocket = ServerSocket(4221)
    serverSocket.reuseAddress = true
    serverSocket.receiveBufferSize

    // wait for clients
    val clientSocket = serverSocket.accept()
    // get output stream
    val outputStream = clientSocket.getOutputStream()
    val inputStream = clientSocket.getInputStream()
    inputStream.bufferedReader().use {
        val line = it.readLine()
        val splitline = line.split(' ')

        val path = splitline[1]
        if (path == "/") {
            val response = "HTTP/1.1 200 OK\r\n\r\n"
            outputStream.write(response.toByteArray())
        } else {
            val response = "HTTP/1.1 404 Not Found\r\n\r\n"
            outputStream.write(response.toByteArray())
        }
        outputStream.flush()
        outputStream.close()
    }
}
