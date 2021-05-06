package info.skyblond.vovoku.commons

import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.Signature
import java.security.spec.KeySpec
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.RSAPublicKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.xml.bind.DatatypeConverter


object CryptoUtil {
    private val logger = LoggerFactory.getLogger(CryptoUtil::class.java)
    private val rsaKeyFactory = KeyFactory.getInstance("RSA")
    private const val rsaSignAlgorithm = "SHA512withRSA"

    fun md5(data: ByteArray): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(data)).toString(16).padStart(32, '0')
    }

    fun md5(str: String): String = md5(str.toByteArray(StandardCharsets.UTF_8))

    data class RSAKeySpec(
        val modulus: String,
        val exponent: String
    ) {
        companion object {
            fun fromRSAPublicKeySpec(publicKeySpec: RSAPublicKeySpec): RSAKeySpec {
                return RSAKeySpec(
                    publicKeySpec.modulus.toString(16),
                    publicKeySpec.publicExponent.toString(16),
                )
            }

            fun fromRSAPrivateKeySpec(privateKeySpec: RSAPrivateKeySpec): RSAKeySpec {
                return RSAKeySpec(
                    privateKeySpec.modulus.toString(16),
                    privateKeySpec.privateExponent.toString(16),
                )
            }
        }

        inline fun <reified T : KeySpec> restore(): T {
            val mod = BigInteger(modulus, 16)
            val exp = BigInteger(exponent, 16)
            val type = T::class.java
            require(type.canonicalName.startsWith("java.security.spec.RSA")) { "Unsupported type: ${type.canonicalName}" }
            val constructor = type.getConstructor(BigInteger::class.java, BigInteger::class.java)
            return constructor.newInstance(mod, exp)
        }
    }

    fun signWithPrivateKey(data: ByteArray, privateKeySpec: RSAPrivateKeySpec): ByteArray {
        val privateKey = synchronized(rsaKeyFactory) {
            rsaKeyFactory.generatePrivate(privateKeySpec)
        }
        val signature = Signature.getInstance(rsaSignAlgorithm)
        signature.initSign(privateKey)
        signature.update(data)
        return signature.sign()
    }

    fun verifyWithPublicKey(data: ByteArray, sign: ByteArray, publicKeySpec: RSAPublicKeySpec): Boolean {
        val publicKey = synchronized(rsaKeyFactory) {
            rsaKeyFactory.generatePublic(publicKeySpec)
        }
        val signature = Signature.getInstance(rsaSignAlgorithm)
        signature.initVerify(publicKey)
        signature.update(data)
        return signature.verify(sign)
    }


    fun signWithPrivateKey(data: String, privateKeySpec: RSAPrivateKeySpec): String {
        val sign = signWithPrivateKey(
            data.toByteArray(StandardCharsets.UTF_8),
            privateKeySpec
        )
        return DatatypeConverter.printHexBinary(sign)
    }

    fun verifyWithPublicKey(data: String, sign: String, publicKeySpec: RSAPublicKeySpec): Boolean {
        val signByte = DatatypeConverter.parseHexBinary(sign)
        return verifyWithPublicKey(
            data.toByteArray(StandardCharsets.UTF_8),
            signByte, publicKeySpec
        )
    }

    fun encryptWithPublicKey(data: ByteArray, publicKeySpec: RSAPublicKeySpec): ByteArray {
        val publicKey = synchronized(rsaKeyFactory) {
            rsaKeyFactory.generatePublic(publicKeySpec)
        }
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(data)
    }

    fun decryptWithPrivateKey(data: ByteArray, privateKeySpec: RSAPrivateKeySpec): ByteArray {
        val privateKey = synchronized(rsaKeyFactory) {
            rsaKeyFactory.generatePrivate(privateKeySpec)
        }
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        return cipher.doFinal(data)
    }

    fun encryptWithPublicKey(data: String, publicKeySpec: RSAPublicKeySpec): String {
        return String(
            Base64.getUrlEncoder().encode(
                encryptWithPublicKey(data.toByteArray(StandardCharsets.UTF_8), publicKeySpec)
            )
        )
    }

    fun decryptWithPrivateKey(data: String, privateKeySpec: RSAPrivateKeySpec): String {
        val dataByte = Base64.getUrlDecoder().decode(data)
        return String(
            decryptWithPrivateKey(dataByte, privateKeySpec), StandardCharsets.UTF_8
        )
    }
}