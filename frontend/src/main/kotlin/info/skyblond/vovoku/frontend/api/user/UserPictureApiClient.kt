package info.skyblond.vovoku.frontend.api.user

import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory

class UserPictureApiClient internal constructor(
    private val apiClient: UserApiClient,
    private val httpClient: OkHttpClient,
    _urlPrefix: String,
) {
    private val logger = LoggerFactory.getLogger(UserPictureApiClient::class.java)
    private val urlPrefix: String = _urlPrefix.removeSuffix("/")

    private val whoAmIEndPoint = "whoami"
    private val deleteEndPoint = "delete"


}