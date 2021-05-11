package info.skyblond.vovoku.backend

import info.skyblond.vovoku.commons.CryptoUtil
import info.skyblond.vovoku.commons.JacksonJsonUtil
import org.junit.jupiter.api.Test
import java.io.File
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.RSAPublicKeySpec
import javax.crypto.KeyGenerator
import javax.xml.bind.DatatypeConverter


class Crypto {
    private val publicKeyFile = File("./publicKey.json")
    private val privateKeyFile = File("./privateKey.json")

    @Test
    fun genAesKey(){
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        val key = keyGen.generateKey()
        val raw = key.encoded
        println(DatatypeConverter.printHexBinary(raw))

    }

    @Test
    fun genCert() {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(4096)
        val keyPair = keyPairGenerator.generateKeyPair()
        val keyFactory = KeyFactory.getInstance("RSA")

        val rsaPublicKeySpec = keyFactory.getKeySpec(keyPair.public, RSAPublicKeySpec::class.java)
        val rsaPrivateKeySpec = keyFactory.getKeySpec(keyPair.private, RSAPrivateKeySpec::class.java)

        // save to file
        JacksonJsonUtil.writeToFile(publicKeyFile, CryptoUtil.RSAKeySpec.fromRSAPublicKeySpec(rsaPublicKeySpec))
        JacksonJsonUtil.writeToFile(privateKeyFile, CryptoUtil.RSAKeySpec.fromRSAPrivateKeySpec(rsaPrivateKeySpec))
    }
}