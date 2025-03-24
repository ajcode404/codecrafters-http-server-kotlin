import java.net.ServerSocket

fun main() {
    println("Logs from your program will appear here!")
    var serverSocket = ServerSocket(4221)
    serverSocket.reuseAddress = true
    serverSocket.receiveBufferSize
    // wait for clients
    val clientSocket = serverSocket.accept()
    RequestHandler(clientSocket).handle()
    // get output stream
//    val buffer = clientSocket.getOutputStream()
//    val response = "HTTP/1.1 200 OK\r\n\r\n"
//    buffer.write(response.toByteArray())
//    buffer.flush()
//    buffer.close()
//    println(response)
}