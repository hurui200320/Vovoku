package info.skyblond.vovoku.backend.database

import info.skyblond.vovoku.commons.models.*
import me.liuwj.ktorm.entity.Entity

interface User : Entity<User> {
    companion object : Entity.Factory<User>()

    val userId: Int
    var username: String
    var password: String

    fun toPojo(): DatabaseUserPojo = DatabaseUserPojo(userId, username, password)
    fun update(pojo: DatabaseUserPojo) {
        require(userId == pojo.userId) { "Different user id" }
        pojo.username?.let { username = it }
        pojo.password?.let { password = it }
        flushChanges()
    }
}

interface PictureTag : Entity<PictureTag> {
    companion object : Entity.Factory<PictureTag>()

    val tagId: Int
    val filePath: String
    val userId: Int
    val tagData: MutableList<PictureTagEntry>

    fun toPojo(): DatabasePictureTagPojo = DatabasePictureTagPojo(
        tagId, filePath, userId, tagData
    )

    fun update(pojo: DatabasePictureTagPojo) {
        require(tagId == pojo.tagId) { "Different tag id" }
        pojo.tagData?.let {
            tagData.clear()
            tagData.addAll(it)
        }
        flushChanges()
    }
}


interface ModelInfo : Entity<ModelInfo> {
    companion object : Entity.Factory<ModelInfo>()

    val modelId: Int
    var filePath: String?
    val userId: Int
    val createInfo: ModelCreateInfo
    val trainingInfo: ModelTrainingInfo

    fun toPojo(): DatabaseModelInfoPojo = DatabaseModelInfoPojo(
        modelId, filePath, userId, createInfo, trainingInfo
    )

    fun update(pojo: DatabaseModelInfoPojo) {
        require(modelId == pojo.modelId) { "Different model id" }
        pojo.filePath?.let { filePath = it }
        pojo.trainingInfo?.let {
            trainingInfo.statusList.clear()
            trainingInfo.statusList.addAll(it.statusList)
        }
        flushChanges()
    }
}