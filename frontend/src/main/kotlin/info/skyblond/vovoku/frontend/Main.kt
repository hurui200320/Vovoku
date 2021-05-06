package info.skyblond.vovoku.frontend

import info.skyblond.vovoku.commons.CryptoUtil
import info.skyblond.vovoku.commons.JacksonJsonUtil
import info.skyblond.vovoku.commons.models.AdminUserRequest
import info.skyblond.vovoku.commons.models.CRUD
import info.skyblond.vovoku.commons.models.DatabaseUserPojo
import info.skyblond.vovoku.frontend.api.admin.AdminApiClient
import okhttp3.OkHttpClient
import java.io.File

fun main() {
    val httpClient = OkHttpClient()
    val privateKey =
        JacksonJsonUtil.readFromFile<CryptoUtil.RSAKeySpec>(File("D:\\Git\\github\\Vovoku\\backend\\privateKey.json"))
    val adminApiClient = AdminApiClient(httpClient, "http://127.0.0.1:7000/admin", privateKey.restore())

    println(
        adminApiClient.queryUser(
            AdminUserRequest(
                DatabaseUserPojo(null, "%hu%", null),
                CRUD.READ,
                null
            )
        )
    )
}