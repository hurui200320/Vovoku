package info.skyblond.vovoku.frontend.api.admin

import info.skyblond.vovoku.commons.CryptoUtil
import info.skyblond.vovoku.commons.JacksonJsonUtil
import info.skyblond.vovoku.commons.UBytePicUtil
import info.skyblond.vovoku.commons.models.*
import info.skyblond.vovoku.frontend.api.jsonMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.util.*
import javax.crypto.SecretKey

class AdminApiClient(
    private val httpClient: OkHttpClient,
    _host: String,
    private val aesKey: SecretKey
) {
    private val logger = LoggerFactory.getLogger(AdminApiClient::class.java)
    private val urlPrefix: String = _host.removeSuffix("/") + "/admin"

    private val usersEndPoint = "users"
    private val picturesEndPoint = "pictures"
    private val modelsEndPoint = "models"
    private val filesEndPoint = "files"

    private fun postJson(url: String, json: String): Pair<Int, String> {
        val requestBody = json.toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header(
                "Authorization",
                CryptoUtil.aesEncrypt(CryptoUtil.md5(json), aesKey)
            )
            .build()
        httpClient.newCall(request).execute()
            .use { response ->
                return response.code to (response.body?.string()
                    ?.let {
                        if (response.code == 200) {
                            CryptoUtil.aesDecrypt(it, aesKey)
                        } else
                            it
                    } ?: "")
            }
    }

    /**
     * Make query and parse the result
     * */
    private fun <T> query(body: Any, endPoint: String, clazz: Class<Array<T>>): Array<T> {
        val json = JacksonJsonUtil.objectToJson(body)
        val response = postJson("$urlPrefix/$endPoint", json)
        if (response.first == 200) {
            return JacksonJsonUtil.jsonToObject(response.second, clazz)
        } else {
            logger.warn("Server response '${response.first}': ${response.second}")
            throw IllegalStateException(response.second)
        }
    }

    /**
     * Send request and expect to be HTTP 200
     * */
    private fun doRequest(body: Any, endPoint: String): Boolean {
        val json = JacksonJsonUtil.objectToJson(body)
        val response = postJson("$urlPrefix/$endPoint", json)
        if (response.first != 200) {
            logger.error("Server response '${response.first}': ${response.second}")
        }
        return response.first == 200
    }


    fun queryUser(userId: Int?, username: String?, page: Page): Array<DatabaseUserPojo> {
        val requestBody = AdminRequest(
            CRUD.READ,
            mutableMapOf<String, Any>()
                .also { map ->
                    userId?.let { map[AdminRequest.USER_ID_KEY] = it }
                    username?.let { map[AdminRequest.USERNAME_KEY] = it }
                },
            page
        )
        return query(requestBody, usersEndPoint, Array<DatabaseUserPojo>::class.java)
    }

    fun addUser(username: String, hashedPassword: String): Boolean {
        val requestBody = AdminRequest(
            CRUD.CREATE,
            mutableMapOf<String, Any>()
                .also { map ->
                    map[AdminRequest.USERNAME_KEY] = username
                    map[AdminRequest.USER_PASSWORD_KEY] = hashedPassword
                },
            Page(null, null)
        )
        return doRequest(requestBody, usersEndPoint)
    }

    fun updateUser(userId: Int?, username: String?, hashedPassword: String?): Boolean {
        val requestBody = AdminRequest(
            CRUD.UPDATE,
            mutableMapOf<String, Any>()
                .also { map ->
                    userId?.let { map[AdminRequest.USER_ID_KEY] = it }
                    username?.let { map[AdminRequest.USERNAME_KEY] = it }
                    hashedPassword?.let { map[AdminRequest.USER_PASSWORD_KEY] = it }
                },
            Page(null, null)
        )
        return doRequest(requestBody, usersEndPoint)
    }

    fun deleteUser(userId: Int): Boolean {
        val requestBody = AdminRequest(
            CRUD.DELETE,
            mutableMapOf<String, Any>()
                .also { map ->
                    userId.let { map[AdminRequest.USER_ID_KEY] = it }
                },
            Page(null, null)
        )
        return doRequest(requestBody, usersEndPoint)
    }


    fun queryPicture(
        tagId: Int?,
        userId: Int?,
        forTrain: Boolean?,
        folderName: String?,
        page: Page
    ): Array<DatabasePictureTagPojo> {
        val requestBody = AdminRequest(
            CRUD.READ,
            mutableMapOf<String, Any>()
                .also { map ->
                    tagId?.let { map[AdminRequest.TAG_ID_KEY] = it }
                    userId?.let { map[AdminRequest.USER_ID_KEY] = it }
                    forTrain?.let { map[AdminRequest.TAG_FOR_TRAIN_KEY] = it }
                    folderName?.let { map[AdminRequest.TAG_FOLDER_NAME_KEY] = it }
                },
            page
        )
        return query(requestBody, picturesEndPoint, Array<DatabasePictureTagPojo>::class.java)
    }

    fun deletePicture(tagId: Int?): Boolean {
        val requestBody = AdminRequest(
            CRUD.DELETE,
            mutableMapOf<String, Any>()
                .also { map ->
                    tagId?.let { map[AdminRequest.TAG_ID_KEY] = it }
                },
            Page(null, null)
        )
        return doRequest(requestBody, picturesEndPoint)
    }


    fun queryModel(
        modelId: Int?,
        userId: Int?,
        lastStatus: String?,
        page: Page
    ): Array<DatabaseModelInfoPojo> {
        val requestBody = AdminRequest(
            CRUD.READ,
            mutableMapOf<String, Any>()
                .also { map ->
                    modelId?.let { map[AdminRequest.MODEL_ID_KEY] = it }
                    userId?.let { map[AdminRequest.USER_ID_KEY] = it }
                    lastStatus?.let { map[AdminRequest.MODEL_LAST_STATUS_KEY] = it }
                },
            page
        )
        return query(requestBody, modelsEndPoint, Array<DatabaseModelInfoPojo>::class.java)
    }

    fun deleteModel(modelId: Int?): Boolean {
        val requestBody = AdminRequest(
            CRUD.DELETE,
            mutableMapOf<String, Any>()
                .also { map ->
                    modelId?.let { map[AdminRequest.MODEL_ID_KEY] = it }
                },
            Page(null, null)
        )
        return doRequest(requestBody, modelsEndPoint)
    }

    fun fetchPic(picId: Int): BufferedImage? {
        val json = JacksonJsonUtil.objectToJson(
            AdminRequest(
                CRUD.READ,
                mapOf(
                    AdminRequest.FILE_TYPE_KEY to AdminRequest.FILE_TYPE_VALUE_PICTURE,
                    AdminRequest.FILE_ID_KEY to picId
                ),
                Page(null, null)
            )
        )

        val requestBody = json.toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url("$urlPrefix/$filesEndPoint")
            .post(requestBody)
            .header(
                "Authorization",
                CryptoUtil.aesEncrypt(CryptoUtil.md5(json), aesKey)
            )
            .build()
        httpClient.newCall(request).execute()
            .use { response ->
                if (response.code != 200)
                    return null
                val bytes = CryptoUtil.aesDecrypt(
                    Base64.getUrlDecoder().decode(response.body!!.string()),
                    aesKey, CryptoUtil.defaultIv
                )
                val width = response.header("pic_width")?.toInt()!!
                val height = response.header("pic_height")?.toInt()!!
                val channel = response.header("pic_channel")?.toInt()!!
                return UBytePicUtil.uByteArrayToPic(
                    width, height, channel, bytes
                )
            }
    }

    fun fetchModel(modelId: Int): ByteArray? {
        val json = JacksonJsonUtil.objectToJson(
            AdminRequest(
                CRUD.READ,
                mapOf(
                    AdminRequest.FILE_TYPE_KEY to AdminRequest.FILE_TYPE_VALUE_MODEL,
                    AdminRequest.FILE_ID_KEY to modelId
                ),
                Page(null, null)
            )
        )

        val requestBody = json.toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url("$urlPrefix/$filesEndPoint")
            .post(requestBody)
            .header(
                "Authorization",
                CryptoUtil.aesEncrypt(CryptoUtil.md5(json), aesKey)
            )
            .build()
        httpClient.newCall(request).execute()
            .use { response ->
                if (response.code != 200)
                    return null
                return CryptoUtil.aesDecrypt(
                    Base64.getUrlDecoder().decode(response.body!!.string()),
                    aesKey, CryptoUtil.defaultIv
                )
            }
    }

}