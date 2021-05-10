package info.skyblond.vovoku.backend.database

import info.skyblond.vovoku.backend.handler.user.UserFileHandler
import info.skyblond.vovoku.commons.models.*
import io.javalin.http.Context
import org.ktorm.entity.Entity

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
    var filePath: String
    var userId: Int
    var tagData: PictureTagEntry

    fun toPojo(ctx: Context): DatabasePictureTagPojo = DatabasePictureTagPojo(
        tagId, ctx.url().removeSuffix(ctx.path()) + UserFileHandler.HANDLER_PATH + tagId, userId, tagData
    )

    // TODO Admin api?
    fun toPojo(): DatabasePictureTagPojo = DatabasePictureTagPojo(
        tagId, filePath, userId, tagData
    )

    fun update(pojo: DatabasePictureTagPojo) {
        require(tagId == pojo.tagId) { "Different tag id" }
        pojo.tagData?.let {
            // full update, admin take charge of data correctness
            val newPojo = PictureTagEntry(it.width, it.height, it.channelCount, it.tag)
            tagData = newPojo
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
}