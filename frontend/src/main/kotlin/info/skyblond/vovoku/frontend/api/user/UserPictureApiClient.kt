package info.skyblond.vovoku.frontend.api.user

import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory

class UserAccountApiClient(
    private val httpClient: OkHttpClient,
    _urlPrefix: String,
) {
    private val logger = LoggerFactory.getLogger(UserAccountApiClient::class.java)
    private val urlPrefix: String = _urlPrefix.removeSuffix("/")

    private val whoAmIEndPoint = "whoami"
    private val deleteEndPoint = "delete"



}