package info.skyblond.vovoku.commons.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

data class Page(
    val page: Int?,
    val size: Int?
) {
    init {
        require(page == null || page > 0)
        require(size == null || size > 0)
    }
    @JsonIgnore
    fun limit(): Int = size ?: 20
    @JsonIgnore
    fun offset(): Int = ((page ?: 1) - 1) * limit()
}

enum class CRUD {
    CREATE, READ, UPDATE, DELETE
}

interface AdminCRUDRequest {
    val operation: CRUD
}

data class AdminRequest(
    override val operation: CRUD,
    val parameter: Map<String, Any>,
    @JsonProperty("segment")
    val page: Page,
) : AdminCRUDRequest {
    inline fun <reified T> typedParameter(key: String): T? {
        val obj = parameter[key] ?: return null
        require(obj is T) { "Parameter '$key' is not instance of ${T::class.java.canonicalName}" }
        return obj
    }

    companion object {
        const val USER_ID_KEY = "userId"
        const val USERNAME_KEY = "username"
        const val USER_PASSWORD_KEY = "userPassword"

        const val TAG_ID_KEY = "tagId"
        const val TAG_DATA_KEY = "tagData"
        const val TAG_FOR_TRAIN_KEY = "tagForTrain"
        const val TAG_FOLDER_NAME_KEY = "tagFolderName"

        const val MODEL_ID_KEY = "modelID"
        const val MODEL_LAST_STATUS_KEY = "modelLastStatus"

        const val FILE_TYPE_KEY = "fileType"
        const val FILE_TYPE_VALUE_MODEL = "model"
        const val FILE_TYPE_VALUE_PICTURE = "picture"
        const val FILE_ID_KEY = "fileId"
    }

}
