package info.skyblond.vovoku.commons

import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object CryptoUtil {
    private val logger = LoggerFactory.getLogger(CryptoUtil::class.java)

    fun md5(data: ByteArray): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(data)).toString(16).padStart(32, '0')
    }

    fun md5(str: String): String = md5(str.toByteArray(StandardCharsets.UTF_8))
}