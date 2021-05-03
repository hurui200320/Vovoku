package info.skyblond.vovoku.backend.database

import info.skyblond.vovoku.backend.ModelCreateInfo
import info.skyblond.vovoku.backend.ModelTrainingInfo
import info.skyblond.vovoku.backend.PictureTagEntry
import me.liuwj.ktorm.entity.Entity

interface User : Entity<User> {
    companion object : Entity.Factory<User>()

    val userId: Int
    var username: String
    var password: String
}

interface PictureTag : Entity<PictureTag> {
    companion object : Entity.Factory<PictureTag>()

    val tagId: Int
    val filePath: String
    val userId: Int
    val tagData: MutableList<PictureTagEntry>
}


interface ModelInfo : Entity<ModelInfo> {
    companion object : Entity.Factory<ModelInfo>()

    val modelId: Int
    var filePath: String?
    val userId: Int
    val createInfo: ModelCreateInfo
    val trainingInfo: ModelTrainingInfo
}