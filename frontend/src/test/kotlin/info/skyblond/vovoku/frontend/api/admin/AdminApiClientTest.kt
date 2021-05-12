package info.skyblond.vovoku.frontend.api.admin

import info.skyblond.vovoku.commons.CryptoUtil
import info.skyblond.vovoku.commons.JacksonJsonUtil
import info.skyblond.vovoku.commons.models.DatabaseUserPojo
import info.skyblond.vovoku.commons.models.Page
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import java.io.File

internal class AdminApiClientTest {
    @Test
    fun demo() {

        val httpClient = OkHttpClient()
        val privateKey =
            JacksonJsonUtil.readFromFile<CryptoUtil.RSAKeySpec>(File("D:\\Git\\github\\Vovoku\\backend\\privateKey.json"))
        val adminApiClient = AdminApiClient(httpClient, "http://127.0.0.1:7000/admin", privateKey.restore())

        // print old
        println(
            adminApiClient.queryUser(
                null, "%hu%",
                Page(1, 20)
            )
        )

        // add new, should success
        println(
            adminApiClient.addUser(
                "hurui200320", CryptoUtil.md5("Some password")
            )
        )

        // add new, should failed
        println(
            adminApiClient.addUser(
                 "hurui200320", CryptoUtil.md5("Some1 password")
            )
        )

        // print new
        println(
            adminApiClient.queryUser(
                null, "%hu%",
                Page(1, 20)
            )
        )

        // delete new
        println(
            adminApiClient.deleteUser(
                null, "hurui200%"
            )
        )

        println(
            adminApiClient.queryUser(
                null, "%hu%",
                Page(1, 20)
            )
        )
    }
}