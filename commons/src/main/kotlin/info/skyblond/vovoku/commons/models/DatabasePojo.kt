package info.skyblond.vovoku.commons.models

import java.sql.Timestamp

data class DatabaseUserPojo(
    val userId: Int,
    var username: String,
    var password: String,
)

data class DatabasePictureTagPojo(
    val tagId: Int,
    val filePath: String,
    val userId: Int,
    val tagData: PictureTagEntry,
    val usedForTrain: Boolean,
    val folderName: String
)

data class DatabaseModelInfoPojo(
    val modelId: Int,
    var filePath: String?,
    val userId: Int,
    val createInfo: ModelCreateInfo,
    val trainingInfo: Array<Triple<ModelTrainingStatus, Timestamp, String>>,
    val lastStatus: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DatabaseModelInfoPojo

        if (modelId != other.modelId) return false
        if (filePath != other.filePath) return false
        if (userId != other.userId) return false
        if (createInfo != other.createInfo) return false
        if (!trainingInfo.contentEquals(other.trainingInfo)) return false
        if (lastStatus != other.lastStatus) return false

        return true
    }

    override fun hashCode(): Int {
        var result = modelId
        result = 31 * result + (filePath?.hashCode() ?: 0)
        result = 31 * result + userId
        result = 31 * result + createInfo.hashCode()
        result = 31 * result + trainingInfo.contentHashCode()
        result = 31 * result + lastStatus.hashCode()
        return result
    }
}
