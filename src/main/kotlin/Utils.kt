import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

const val CRLF_CONST = "\r\n"

internal fun compress(data: String?): ByteArray? {
    data ?: return null
    val os = ByteArrayOutputStream()
    val gzip = GZIPOutputStream(os)
    gzip.write(data.toByteArray(Charsets.UTF_8))
    gzip.close()
    return os.toByteArray()
}
