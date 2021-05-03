package info.skyblond.vovoku.commons

import org.slf4j.LoggerFactory
import java.io.*

object FilePathUtil {
    private val logger = LoggerFactory.getLogger(FilePathUtil::class.java)

    fun readFromFilePath(path: String, token: String): InputStream {
        when {
            checkPrefix(path, "file://") -> return FileInputStream(File(path.removePrefix("file://")))
            else -> throw IllegalArgumentException("Unknown file path: '$path'")
        }
    }

    fun writeToFilePath(path: String, token: String): OutputStream {
        when {
            checkPrefix(path, "file://") -> return FileOutputStream(File(path.removePrefix("file://")))
            else -> throw IllegalArgumentException("Unknown file path: '$path'")
        }
    }

    private fun checkPrefix(path: String, prefix: String): Boolean {
        return path.substring(0, prefix.length).toLowerCase().startsWith(prefix.toLowerCase())
    }
}