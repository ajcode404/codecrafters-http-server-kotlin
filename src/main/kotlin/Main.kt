import java.net.ServerSocket

fun main() {
    println("Logs from your program will appear here!")
//    val serverSocket = ServerSocket(4221)
//    serverSocket.reuseAddress = true
//    serverSocket.receiveBufferSize

    // wait for clients
    ConnectionManager().run()
//    val clientSocket = serverSocket.accept()
//
//    // get output stream
//    val requestHandler = RequestHandler(clientSocket)
//    requestHandler.handle()
}