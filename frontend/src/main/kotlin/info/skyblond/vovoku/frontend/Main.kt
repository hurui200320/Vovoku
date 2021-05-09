package info.skyblond.vovoku.frontend

import info.skyblond.vovoku.commons.CryptoUtil
import info.skyblond.vovoku.commons.JacksonJsonUtil
import info.skyblond.vovoku.commons.models.DatabaseUserPojo
import info.skyblond.vovoku.commons.models.Page
import info.skyblond.vovoku.frontend.api.admin.AdminApiClient
import info.skyblond.vovoku.frontend.api.user.UserApiClient
import okhttp3.OkHttpClient
import java.io.File

fun main() {
    val httpClient = OkHttpClient()
    // TODO change default file path to config.yaml
    val privateKey =
        JacksonJsonUtil.readFromFile<CryptoUtil.RSAKeySpec>(File("D:\\Git\\github\\Vovoku\\backend\\privateKey.json"))
    val adminApiClient = AdminApiClient(httpClient, "http://127.0.0.1:7000/", privateKey.restore())
    val userApiClient = UserApiClient(httpClient, "http://127.0.0.1:7000/")
    println(adminApiClient.queryUser(DatabaseUserPojo(null, null), Page(1, 20)))

    println(userApiClient.login("hurui", "passw0rd"))
    println(userApiClient.accountApiClient.whoAmI())


}