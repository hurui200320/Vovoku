package info.skyblond.vovoku.frontend.api.user

import com.fasterxml.jackson.annotation.JsonProperty
import info.skyblond.vovoku.commons.JacksonJsonUtil
import okhttp3.FormBody
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory

class UserAccountApiClient internal constructor(
    private val apiClient: UserApiClient,
    private val httpClient: OkHttpClient,
    _urlPrefix: String,
) {
    private val logger = LoggerFactory.getLogger(UserAccountApiClient::class.java)
    private val urlPrefix: String = _urlPrefix.removeSuffix("/")

    private val whoAmIEndPoint = "whoami"
    private val deleteEndPoint = "delete"

    data class AccountIdentity(
        @JsonProperty("id")
        val userId: Int,
        @JsonProperty("username")
        val username: String,
        @JsonProperty("passwordHash")
        val hashedPassword: String
    )

    fun whoAmI(): Triple<Boolean, String, AccountIdentity?> {
        val response = apiClient.doGet("$urlPrefix/$whoAmIEndPoint", emptyMap())
        return if (response.first == 200) {
            Triple(true, "OK", JacksonJsonUtil.jsonToObject(response.second))
        } else {
            logger.error("WhoAmI failed: Code: ${response.first}, message: ${response.second}")
            Triple(false, response.second, null)
        }
    }

    fun deleteAccount(username: String, password: String): Pair<Boolean, String> {
        val body = FormBody.Builder()
            .add("username", username)
            .add("password_raw", password)
            .build()
        val response = apiClient.doDelete("$urlPrefix/$deleteEndPoint", body)
        return if (response.first == 204) {
            apiClient.token = ""
            Pair(true, "OK")
        } else {
            logger.error("DeleteAccount failed: Code: ${response.first}, message: ${response.second}")
            Pair(false, response.second)
        }
    }

}