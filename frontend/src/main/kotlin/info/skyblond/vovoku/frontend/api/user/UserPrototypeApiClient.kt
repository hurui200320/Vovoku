package info.skyblond.vovoku.frontend.api.user

import info.skyblond.vovoku.commons.JacksonJsonUtil
import info.skyblond.vovoku.commons.dl4j.PrototypeDescriptor
import info.skyblond.vovoku.commons.models.DatabaseModelInfoPojo
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory

class UserPrototypeApiClient internal constructor(
    private val apiClient: UserApiClient,
    private val httpClient: OkHttpClient,
    _urlPrefix: String,
) {
    private val logger = LoggerFactory.getLogger(UserPrototypeApiClient::class.java)
    private val urlPrefix: String = _urlPrefix.removeSuffix("/")

    //            path("prototype") {
    //                path(":typeId") {
    //                    // list current available model prototypes
    //                    get(UserModelHandler.getOnePrototypeHandler)
    //                }
    //                // list current available model prototypes
    //                get(UserModelHandler.listPrototypeHandler)
    //            }

    fun listPrototype(page: Int, size: Int): Triple<Boolean, String, Array<PrototypeDescriptor>>{
        val response = apiClient.doGet(
            urlPrefix, mapOf(
                "page" to page,
                "size" to size
            )
        )
        return if (response.first == 200) {
            Triple(true, "OK", JacksonJsonUtil.jsonToObject(response.second))
        } else {
            logger.error("ListPrototype failed: Code: ${response.first}, message: ${response.second}")
            Triple(false, response.second, emptyArray())
        }
    }

    fun getOnePrototype(prototypeIdentifier: String): Triple<Boolean, String, PrototypeDescriptor?>{
        val response = apiClient.doGet("$urlPrefix/$prototypeIdentifier", emptyMap())

        return if (response.first == 200) {
            Triple(true, "OK", JacksonJsonUtil.jsonToObject(response.second))
        } else {
            logger.error("GetOnePrototype failed: Code: ${response.first}, message: ${response.second}")
            Triple(false, response.second, null)
        }
    }
}