package info.skyblond.vovoku.commons

import org.junit.jupiter.api.Test
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.RSAPublicKeySpec

internal class CryptoUtilTest {
    private val rsaPublicKeySpec: RSAPublicKeySpec
    private val rsaPrivateKeySpec: RSAPrivateKeySpec
    init {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(4096)
        val keyPair = keyPairGenerator.generateKeyPair()
        val keyFactory = KeyFactory.getInstance("RSA")

        rsaPublicKeySpec = keyFactory.getKeySpec(keyPair.public, RSAPublicKeySpec::class.java)
        rsaPrivateKeySpec = keyFactory.getKeySpec(keyPair.private, RSAPrivateKeySpec::class.java)
    }

    @Test
    fun signAndVerify() {
        val data = "This is the data to be signed"
        val sign = CryptoUtil.signWithPrivateKey(data, rsaPrivateKeySpec)

        println("Data: $data")
        println("Sign: $sign")
        require(CryptoUtil.verifyWithPublicKey(data, sign, rsaPublicKeySpec))
    }

    @Test
    fun encryptAndDecrypt(){
        val data = "This is the data to be encrypted"
        val crypto = CryptoUtil.encryptWithPublicKey(data, rsaPublicKeySpec)

        println("Data: $data")
        println("Crypto: $crypto")
        require(CryptoUtil.decryptWithPrivateKey(crypto, rsaPrivateKeySpec) == data)
    }
}