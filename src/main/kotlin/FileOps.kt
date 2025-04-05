import java.io.File

data class FileMetadata(
    val fileData: String
)

internal fun readFile(fileName: String): FileMetadata? {
    return runCatching {
        FileMetadata(File(fileName).readText(Charsets.UTF_8))
    }.fold(
        onSuccess = {
            it
        },
        onFailure = {
            it.printStackTrace()
            null
        }
    )
}

internal fun writeFile(fileName: String, text: String) {
    runCatching {
        val file = File(fileName)
        file.writeText(text)
    }
}
