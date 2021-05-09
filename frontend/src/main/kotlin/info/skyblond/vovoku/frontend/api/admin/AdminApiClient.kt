package info.skyblond.vovoku.frontend.api.admin

import info.skyblond.vovoku.commons.CryptoUtil
import info.skyblond.vovoku.commons.JacksonJsonUtil
import info.skyblond.vovoku.commons.models.*
import info.skyblond.vovoku.frontend.api.jsonMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import java.security.spec.RSAPrivateKeySpec

class AdminApiClient(
    private val httpClient: OkHttpClient,
    _host: String,
    private val privateKey: RSAPrivateKeySpec
) {
    private val logger = LoggerFactory.getLogger(AdminUserRequest::class.java)
    private val urlPrefix: String = _host.removeSuffix("/") + "/admin"

    private val usersEndPoint = "users"
    private val picturesEndPoint = "pictures"
    private val modelsEndPoint = "models"

    private fun postJson(url: String, json: String): Pair<Int, String> {
        val requestBody = json.toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Authorization", CryptoUtil.signWithPrivateKey(json, privateKey))
            .build()
        httpClient.newCall(request).execute()
            .use { response ->
                return response.code to (response.body?.string()
                    ?.let {
                        if (response.code == 200)
                            CryptoUtil.decryptWithPrivateKey(it, privateKey)
                        else
                            it
                    } ?: "")
            }
    }

    /**
     * Make query and parse the result
     * */
    private fun <T> query(body: Any, endPoint: String): List<T> {
        val json = JacksonJsonUtil.objectToJson(body)
        val response = postJson("$urlPrefix/$endPoint", json)
        if (response.first == 200) {
            return JacksonJsonUtil.jsonToObject(response.second)
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


    fun queryUser(pojo: DatabaseUserPojo, page: Page): List<DatabaseUserPojo> {
        val requestBody = AdminUserRequest(pojo, CRUD.READ, page)
        return query(requestBody, usersEndPoint)
    }

    fun addUser(pojo: DatabaseUserPojo): Boolean {
        val requestBody = AdminUserRequest(pojo, CRUD.CREATE, null)
        return doRequest(requestBody, usersEndPoint)
    }

    fun updateUser(pojo: DatabaseUserPojo): Boolean {
        val requestBody = AdminUserRequest(pojo, CRUD.UPDATE, null)
        return doRequest(requestBody, usersEndPoint)
    }

    fun deleteUser(pojo: DatabaseUserPojo): Boolean {
        val requestBody = AdminUserRequest(pojo, CRUD.DELETE, null)
        return doRequest(requestBody, usersEndPoint)
    }


    fun queryPicture(pojo: DatabasePictureTagPojo, page: Page): List<DatabasePictureTagPojo> {
        val requestBody = AdminPictureTagRequest(pojo, CRUD.READ, page)
        return query(requestBody, picturesEndPoint)
    }

    fun updatePicture(pojo: DatabaseUserPojo): Boolean {
        val requestBody = AdminUserRequest(pojo, CRUD.UPDATE, null)
        return doRequest(requestBody, picturesEndPoint)
    }

    fun deletePicture(pojo: DatabaseUserPojo): Boolean {
        val requestBody = AdminUserRequest(pojo, CRUD.DELETE, null)
        return doRequest(requestBody, picturesEndPoint)
    }


    fun queryModel(pojo: DatabaseModelInfoPojo, page: Page): List<DatabaseModelInfoPojo> {
        val requestBody = AdminModelRequest(pojo, CRUD.READ, page)
        return query(requestBody, modelsEndPoint)
    }

    fun updateModel(pojo: DatabaseModelInfoPojo): Boolean {
        val requestBody = AdminModelRequest(pojo, CRUD.UPDATE, null)
        return doRequest(requestBody, modelsEndPoint)
    }

    fun deleteModel(pojo: DatabaseModelInfoPojo): Boolean {
        val requestBody = AdminModelRequest(pojo, CRUD.DELETE, null)
        return doRequest(requestBody, modelsEndPoint)
    }


}