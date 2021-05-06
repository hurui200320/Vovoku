package info.skyblond.vovoku.frontend.api.admin

import info.skyblond.vovoku.commons.CryptoUtil
import info.skyblond.vovoku.commons.JacksonJsonUtil
import info.skyblond.vovoku.commons.models.AdminUserRequest
import info.skyblond.vovoku.commons.models.DatabaseUserPojo
import info.skyblond.vovoku.frontend.api.jsonMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import java.security.spec.RSAPrivateKeySpec

class AdminApiClient(
    private val httpClient: OkHttpClient,
    _urlPrefix: String,
    private val privateKey: RSAPrivateKeySpec
) {
    private val logger = LoggerFactory.getLogger(AdminUserRequest::class.java)
    private val urlPrefix: String = _urlPrefix.removeSuffix("/")

    private fun postJson(url: String, json: String): Pair<Int, String> {
        val requestBody = json.toRequestBody(jsonMediaType)
        logger.info("Posting to $url")
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Authorization", CryptoUtil.signWithPrivateKey(json, privateKey))
            .build()
        httpClient.newCall(request).execute()
            .use { response ->
                return response.code to (response.body?.string()
                    ?.let { CryptoUtil.decryptWithPrivateKey(it, privateKey) } ?: "")
            }
    }

    fun queryUser(requestBody: AdminUserRequest): List<DatabaseUserPojo> {
        val json = JacksonJsonUtil.objectToJson(requestBody)
        val response = postJson("$urlPrefix/users", json)
        require(response.first == 200)
        logger.info("Response: $response")
        return JacksonJsonUtil.jsonToObject(response.second)
    }
}