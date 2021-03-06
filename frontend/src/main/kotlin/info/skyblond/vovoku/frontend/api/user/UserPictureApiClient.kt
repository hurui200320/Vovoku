package info.skyblond.vovoku.frontend.api.user

import info.skyblond.vovoku.commons.JacksonJsonUtil
import info.skyblond.vovoku.commons.UBytePicUtil
import info.skyblond.vovoku.commons.models.DatabasePictureTagPojo
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage

class UserPictureApiClient internal constructor(
    private val apiClient: UserApiClient,
    private val httpClient: OkHttpClient,
    _urlPrefix: String,
) {
    private val logger = LoggerFactory.getLogger(UserPictureApiClient::class.java)
    private val urlPrefix: String = _urlPrefix.removeSuffix("/")

    fun listPic(
        usedForTrain: Boolean?,
        datasetName: String?,
        page: Int,
        size: Int
    ): Triple<Boolean, String, Array<DatabasePictureTagPojo>> {
        val response = apiClient.doGet(
            urlPrefix, mutableMapOf<String, Any>(
                "page" to page,
                "size" to size
            ).apply {
                usedForTrain?.let { put("train", usedForTrain) }
                datasetName?.let { put("folder", it) }
            }
        )

        return if (response.first == 200) {
            Triple(true, "OK", JacksonJsonUtil.jsonToObject(response.second))
        } else {
            logger.error("ListPic failed: Code: ${response.first}, message: ${response.second}")
            Triple(false, response.second, emptyArray())
        }
    }

    fun getOnePic(tagId: Int): Triple<Boolean, String, Array<DatabasePictureTagPojo>> {
        val response = apiClient.doGet("$urlPrefix/$tagId", emptyMap())

        return if (response.first == 200) {
            Triple(true, "OK", arrayOf(JacksonJsonUtil.jsonToObject(response.second)))
        } else {
            logger.error("GetOnePic failed: Code: ${response.first}, message: ${response.second}")
            Triple(false, response.second, arrayOf())
        }
    }

    fun deleteOnePic(tagId: Int): Pair<Boolean, String> {
        val response = apiClient.doDelete("$urlPrefix/$tagId")

        return if (response.first == 204) {
            Pair(true, "OK")
        } else {
            logger.error("DeleteOnePic failed: Code: ${response.first}, message: ${response.second}")
            Pair(false, response.second)
        }
    }

    fun updateOnePicTag(
        tagId: Int,
        newTag: Int?,
        usedForTrain: Boolean?,
        datasetName: String?
    ): Triple<Boolean, String, DatabasePictureTagPojo?> {
        val body = FormBody.Builder()
            .apply {
                newTag?.let {
                    add("newTag", it.toString())
                }
                usedForTrain?.let {
                    add("train", it.toString())
                }
                datasetName?.let {
                    add("folder", it)
                }
            }
            .build()
        val response = apiClient.doPut("$urlPrefix/$tagId", body)

        return if (response.first == 200) {
            Triple(true, "OK", JacksonJsonUtil.jsonToObject(response.second))
        } else {
            logger.error("GetOnePic failed: Code: ${response.first}, message: ${response.second}")
            Triple(false, response.second, null)
        }
    }

    fun uploadPic(
        image: BufferedImage,
        tag: Int,
        usedForTrain: Boolean,
        datasetName: String
    ): Triple<Boolean, String, DatabasePictureTagPojo?> {
        val data = UBytePicUtil.picToUByteArray(image)
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("width", data.first.first.toString())
            .addFormDataPart("height", data.first.second.toString())
            .addFormDataPart("channel", data.second.toString())
            .addFormDataPart("tag", tag.toString())
            .addFormDataPart("train", usedForTrain.toString())
            .addFormDataPart("folder", datasetName)
            .addFormDataPart("data", "data", data.third.toRequestBody("application/octet-stream".toMediaType()))
            .build()

        val response = apiClient.doPost(urlPrefix, body)

        return if (response.first == 200) {
            Triple(true, "OK", JacksonJsonUtil.jsonToObject(response.second))
        } else {
            logger.error("FetchPic failed: Code: ${response.first}, message: ${response.second}")
            Triple(false, response.second, null)
        }
    }

    fun fetchPic(fileUrl: String): Triple<Boolean, String, BufferedImage?> {
        val request = Request.Builder()
            .url(fileUrl)
            .header("Authorization", apiClient.token)
            .build()

        httpClient.newCall(request).execute()
            .use { response ->
                return if (response.code == 200) {
                    // parse byte array to BufferedImage
                    val width = response.header("pic_width")?.toInt()!!
                    val height = response.header("pic_height")?.toInt()!!
                    val channel = response.header("pic_channel")?.toInt()!!
                    val image = UBytePicUtil.uByteArrayToPic(
                        width, height, channel, response.body?.bytes()!!
                    )
                    Triple(true, "OK", image)
                } else {
                    val respStr = response.body?.string() ?: ""
                    logger.error("UploadPic failed: Code: ${response.code}, message: $respStr")
                    Triple(false, respStr, null)
                }
            }
    }
}