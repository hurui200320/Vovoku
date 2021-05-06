package info.skyblond.vovoku.commons.models

data class DatabaseUserPojo(
    val userId: Int?,
    var username: String?,
    var password: String?,
)

data class DatabasePictureTagPojo(
    val tagId: Int?,
    val filePath: String?,
    val userId: Int?,
    val tagData: MutableList<PictureTagEntry>?
)

data class DatabaseModelInfoPojo(
    val modelId: Int?,
    var filePath: String?,
    val userId: Int?,
    val createInfo: ModelCreateInfo?,
    val trainingInfo: ModelTrainingInfo?,
)
