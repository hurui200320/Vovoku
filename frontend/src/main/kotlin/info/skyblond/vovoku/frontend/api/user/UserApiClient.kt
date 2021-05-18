package info.skyblond.vovoku.frontend.api.user

import info.skyblond.vovoku.commons.CryptoUtil
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.LoggerFactory

class UserApiClient(
    private val httpClient: OkHttpClient,
    _host: String,
) {
    private val logger = LoggerFactory.getLogger(UserApiClient::class.java)
    private val urlPrefix: String = _host.removeSuffix("/")

    private val accountPath = "user/account"
    private val picturePath = "user/picture"
    private val modelPath = "user/model"
    private val prototypePath = "user/prototype"

    var token: String = ""

    // sub api clients
    val accountApiClient = UserAccountApiClient(this, httpClient, "$urlPrefix/$accountPath")
    val pictureApiClient = UserPictureApiClient(this, httpClient, "$urlPrefix/$picturePath")
    val modelApiClient = UserModelApiClient(this, httpClient, "$urlPrefix/$modelPath")
    val prototypeApiClient = UserPrototypeApiClient(this, httpClient, "$urlPrefix/$prototypePath")

    // request with tokens, for sub api clients or raw request
    internal fun doGet(url: String, param: Map<String, Any>): Pair<Int, String> {
        val httpUrl = url.toHttpUrl().newBuilder()
            .apply {
                param.forEach { (k, v) ->
                    addQueryParameter(k, v.toString())
                }
            }
            .build()

        val request = Request.Builder()
            .url(httpUrl)
            .header("Authorization", token)
            .build()

        httpClient.newCall(request).execute()
            .use { response ->
                return response.code to (response.body?.string() ?: "")
            }
    }

    internal fun doPost(url: String, body: RequestBody): Pair<Int, String> {
        val request = Request.Builder()
            .url(url)
            .post(body)
            .header("Authorization", token)
            .build()

        httpClient.newCall(request).execute()
            .use { response ->
                return response.code to (response.body?.string() ?: "")
            }
    }

    internal fun doPut(url: String, body: RequestBody): Pair<Int, String> {
        val request = Request.Builder()
            .url(url)
            .put(body)
            .header("Authorization", token)
            .build()

        httpClient.newCall(request).execute()
            .use { response ->
                return response.code to (response.body?.string() ?: "")
            }
    }

    internal fun doDelete(url: String, body: RequestBody? = null): Pair<Int, String> {
        val request = Request.Builder()
            .url(url)
            .delete(body)
            .header("Authorization", token)
            .build()

        httpClient.newCall(request).execute()
            .use { response ->
                return response.code to (response.body?.string() ?: "")
            }
    }

    // public api implementation, for request token
    private val publicPath = "public"
    private val tokenEndPoint = "token"

    fun login(username: String, password: String): Pair<Boolean, String> {
        val body = FormBody.Builder()
            .add("username", username)
            .add("password", CryptoUtil.md5(password))
            .build()
        val request = Request.Builder()
            .url("$urlPrefix/$publicPath/$tokenEndPoint")
            .post(body)
            .build()

        val response = httpClient.newCall(request).execute()
            .use { response ->
                response.code to (response.body?.string() ?: "")
            }

        return if (response.first == 200) {
            logger.info("Login OK")
            token = response.second
            Pair(true, "OK")
        } else {
            logger.error("Failed login: Code: ${response.first}, message: ${response.second}")
            Pair(false, response.second)
        }
    }
}