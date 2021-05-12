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
    fun update(username: String?, password: String?) {
        username?.let { this.username = it }
        password?.let { this.password = it }
        flushChanges()
    }
}

interface PictureTag : Entity<PictureTag> {
    companion object : Entity.Factory<PictureTag>()

    val tagId: Int
    var filePath: String
    var userId: Int
    var tagData: PictureTagEntry
    var usedForTrain: Boolean
    var folderName: String

    fun toPojo(ctx: Context): DatabasePictureTagPojo = DatabasePictureTagPojo(
        tagId,
        ctx.url().removeSuffix(ctx.path()) + UserFileHandler.PICTURE_HANDLER_PATH + tagId,
        userId,
        tagData,
        usedForTrain,
        folderName
    )

    fun toPojo(): DatabasePictureTagPojo = DatabasePictureTagPojo(
        tagId, filePath, userId, tagData, usedForTrain, folderName
    )

    fun update(tag: Int?, usedForTrain: Boolean?, folderName: String?) {
        tag?.let {
            val newPojo = PictureTagEntry(
                tagData.width, tagData.height,
                tagData.channelCount, it
            )
            tagData = newPojo
        }
        usedForTrain?.let {
            this.usedForTrain = it
        }
        folderName?.let {
            this.folderName = it
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

    // TODO Admin api?
    fun toPojo(): DatabaseModelInfoPojo = DatabaseModelInfoPojo(
        modelId, filePath, userId, createInfo, trainingInfo
    )

    fun toPojo(ctx: Context): DatabaseModelInfoPojo = DatabaseModelInfoPojo(
        modelId,
        ctx.url().removeSuffix(ctx.path()) + UserFileHandler.MODEL_HANDLER_PATH + modelId,
        userId,
        createInfo,
        trainingInfo
    )
}