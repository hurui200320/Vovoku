package info.skyblond.vovoku.frontend

import info.skyblond.vovoku.commons.models.DatabaseUserPojo
import info.skyblond.vovoku.commons.models.Page
import info.skyblond.vovoku.frontend.api.admin.AdminApiClient
import info.skyblond.vovoku.frontend.api.user.UserApiClient
import okhttp3.OkHttpClient
import java.io.File
import javax.crypto.spec.SecretKeySpec
import javax.imageio.ImageIO
import javax.xml.bind.DatatypeConverter


fun main() {
    val httpClient = OkHttpClient()
    // TODO change default file path to config.yaml
    val key = SecretKeySpec(
        DatatypeConverter.parseHexBinary("0497F52352F21586AC1EA3DFFBF76FCD73B4850F93DE01449738B80A064C8038"),
        "AES"
    )
    val adminApiClient = AdminApiClient(httpClient, "http://127.0.0.1:7000/", key)
    val userApiClient = UserApiClient(httpClient, "http://127.0.0.1:7000/")
    println(adminApiClient.queryUser(null, null, Page(1, 20)))

    println(userApiClient.login("hurui", "passw0rd"))
    println(userApiClient.accountApiClient.whoAmI())

    println(adminApiClient.queryPicture(null, null, null, Page(1, 20)))
    // upload one pic
    val image = ImageIO.read(File("C:\\Users\\hurui\\Desktop\\imag0173.jpg"))
    println(userApiClient.pictureApiClient.uploadPic(image, 1))
    println(adminApiClient.queryPicture(null, null, null, Page(1, 20)))
    val fetched = userApiClient.pictureApiClient.fetchPic(
        userApiClient.pictureApiClient.listPic(1, 20).third.first().filePath!!
    )
    ImageIO.write(fetched.third, "png", File("output.png"))
}