package info.skyblond.vovoku.commons

import org.slf4j.LoggerFactory
import java.io.*
import java.util.*

object FilePathUtil {
    private val logger = LoggerFactory.getLogger(FilePathUtil::class.java)

    fun readFromFilePath(path: String): InputStream {
        return when {
            checkPrefix(path, "file://") -> FileInputStream(File(path.removePrefix("file://")))
            checkPrefix(path, "base64://") -> ByteArrayInputStream(
                Base64.getDecoder().decode(path.removePrefix("base64://"))
            )
            else -> throw IllegalArgumentException("Unknown file path: '$path'")
        }
    }

    fun writeToFilePath(path: String): OutputStream {
        when {
            checkPrefix(path, "file://") -> return FileOutputStream(File(path.removePrefix("file://")))
            checkPrefix(path, "base64://") -> throw IllegalAccessException("Cannot write into static base64 datasource")
            else -> throw IllegalArgumentException("Unknown file path: '$path'")
        }
    }

    private fun checkPrefix(path: String, prefix: String): Boolean {
        return path.substring(0, prefix.length).toLowerCase().startsWith(prefix.toLowerCase())
    }
}