package info.skyblond.vovoku.frontend.api.admin

import info.skyblond.vovoku.frontend.api.user.UserApiClient
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import java.io.File
import javax.imageio.ImageIO

class MnistUpload {
    @Test
    fun run() {
        val httpClient = OkHttpClient()
        val userApiClient = UserApiClient(httpClient, "http://127.0.0.1:7000/")

        println(userApiClient.login("user", "user"))
        println(userApiClient.accountApiClient.whoAmI())
        val datasetName = "mnist"

//        val usedForTrain = true
//        val baseDir = File("D:\\Vovoku dataset\\train")
        val usedForTrain = false
        val baseDir = File("D:\\Vovoku dataset\\test")

        val counter = mutableMapOf<Int, Int>()
        (0..9).forEach { counter[it] = 1 }

        // upload pic
        for (label in 0..9) {
            println("Uploading label: $label")
            val dir = File(baseDir, "$label")
            dir.listFiles()!!.forEach {
                val image = ImageIO.read(it)
                val uploaded = userApiClient.pictureApiClient.uploadPic(
                    image, label, usedForTrain, datasetName
                )
                require(uploaded.first) { "Failed upload pic: ${uploaded.second}" }
                counter[label] = counter[label]!! + 1
            }
        }
        println(counter)
    }
}