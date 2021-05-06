package info.skyblond.vovoku.backend

import info.skyblond.vovoku.commons.CryptoUtil
import info.skyblond.vovoku.commons.JacksonJsonUtil
import org.junit.jupiter.api.Test
import java.io.File
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.RSAPublicKeySpec


class Crypto {
    private val publicKeyFile = File("./publicKey.json")
    private val privateKeyFile = File("./privateKey.json")

    @Test
    fun genSign() {
        val data = "PAYLOAD"
        val privateKey = JacksonJsonUtil.readFromFile<CryptoUtil.RSAKeySpec>(privateKeyFile)
            .restore<RSAPrivateKeySpec>()
        val sign = CryptoUtil.signWithPrivateKey(data, privateKey)
        println(sign)
    }

    @Test
    fun decrypt(){
        val crypto = "RYp5Kc_3dPuh3m0HT421hV8ttV2_dlzD3_y3Lhl48JEtSALiYHVJmfDi3-pQKMKXq28SVoaUIlXer59SleEn6KZtUy-ErFXdURP_tszuVHIEiLa8jdoI7VRYdKFrMazPu10PJ6NMvH_CJ1aKoydnXLdv0niPJeQa_6VVYWn5KxrhO8-rjdEnWUOYbptiK6JMzq9hb9lJlGutNbUSqFU7OitsRPVTrPr00Qng9qq9gSEAxEVDbLzd225UaIv3ZHH_eRbFDtKfRPadEkW2qPnvhdx11wki1dxhOnx2TkivruZrF0nYWVmZaJiHDEAIGVp6CmFGXxxiN2Hqv2T2hXWhprxjTZjlnYGljG3qT94FCm8mPG-lYeOqFkc2UG9FhNsr7lOtBoMxio_NRnhMHhgdSZXrHI8OfIeMHRX9vYxCQJksMz5HrIm586kd5gweMx9nDIpi5_rYdVh1iuYpImWWHkwkytIUlYqFykvxAVkOaXGlUnLlH5vf3fnMvlq6nd0qtH9-oHDkjfWgLCUPymsNv_AhYB5OBM4j0ov9IkO9u5tecyRzVUs--JknlHnNQJFgEVt-YYkWALTRXu2V5kMi4Ulj-C-rsITYcTe0FXqEEF6pVM4jQGB2-B_G_MJ1EVV7D6RqMDl37eeXM5a5r-hS54S0u0v24UsZRRmMy4VBpf0="
        val privateKey = JacksonJsonUtil.readFromFile<CryptoUtil.RSAKeySpec>(privateKeyFile)
            .restore<RSAPrivateKeySpec>()
        val data = CryptoUtil.decryptWithPrivateKey(crypto, privateKey)
        println(data)
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