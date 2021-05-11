package info.skyblond.vovoku.frontend.api.user

import info.skyblond.vovoku.commons.JacksonJsonUtil
import info.skyblond.vovoku.commons.UBytePicUtil
import info.skyblond.vovoku.commons.models.DatabaseModelInfoPojo
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage

class UserModelApiClient internal constructor(
    private val apiClient: UserApiClient,
    private val httpClient: OkHttpClient,
    _urlPrefix: String,
) {
    private val logger = LoggerFactory.getLogger(UserModelApiClient::class.java)
    private val urlPrefix: String = _urlPrefix.removeSuffix("/")


    // TODO
    // path("model") {
    //                // request training new model
    //                post(UserModelHandler.requestNewModelHandler)
    //            }

    fun trainingNewModel(){
        TODO()
    }

    fun listModel(page: Int, size: Int): Triple<Boolean, String, Array<DatabaseModelInfoPojo>>{
        val response = apiClient.doGet(
            urlPrefix, mapOf(
                "page" to page,
                "size" to size
            )
        )
        return if (response.first == 200) {
            Triple(true, "OK", JacksonJsonUtil.jsonToObject(response.second))
        } else {
            logger.error("ListModel failed: Code: ${response.first}, message: ${response.second}")
            Triple(false, response.second, emptyArray())
        }
    }

    fun getOneModel(modelId: Int): Triple<Boolean, String, DatabaseModelInfoPojo?>{
        val response = apiClient.doGet("$urlPrefix/$modelId", emptyMap())

        return if (response.first == 200) {
            Triple(true, "OK", JacksonJsonUtil.jsonToObject(response.second))
        } else {
            logger.error("GetOneModel failed: Code: ${response.first}, message: ${response.second}")
            Triple(false, response.second, null)
        }
    }

    fun deleteOneModel(modelId: Int): Pair<Boolean, String> {
        val response = apiClient.doDelete("$urlPrefix/$modelId")

        return if (response.first == 204) {
            Pair(true, "OK")
        } else {
            logger.error("DeleteOneModel failed: Code: ${response.first}, message: ${response.second}")
            Pair(false, response.second)
        }
    }

    fun fetchModel(fileUrl: String): Triple<Boolean, String, ByteArray?> {
        val request = Request.Builder()
            .url(fileUrl)
            .header("Authorization", apiClient.token)
            .build()

        httpClient.newCall(request).execute()
            .use { response ->
                return if (response.code == 200) {
                    Triple(true, "OK", response.body?.bytes()!!)
                } else {
                    val respStr = response.body?.string() ?: ""
                    logger.error("FetchModel failed: Code: ${response.code}, message: $respStr")
                    Triple(false, respStr, null)
                }
            }
    }
}