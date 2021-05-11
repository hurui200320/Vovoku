package info.skyblond.vovoku.backend.handler.user

import info.skyblond.vovoku.backend.database.DatabaseUtil
import info.skyblond.vovoku.backend.database.ModelInfos
import info.skyblond.vovoku.backend.database.PictureTags
import info.skyblond.vovoku.backend.handler.getUserId
import info.skyblond.vovoku.commons.FilePathUtil
import io.javalin.http.Handler
import io.javalin.http.NotFoundResponse
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.slf4j.LoggerFactory

object UserFileHandler {
    private val logger = LoggerFactory.getLogger(UserFileHandler::class.java)
    private val database = DatabaseUtil.database

    const val PICTURE_HANDLER_PATH = "/user/file/pic/"
    const val MODEL_HANDLER_PATH = "/user/file/model/"

    val picResolveHandler = Handler { ctx ->
        val userId = ctx.getUserId()
        val tagId = ctx.pathParam<Int>("picId").get()

        val entity = database.sequenceOf(PictureTags)
            .find { (it.userId eq userId) and (it.tagId eq tagId) } ?: throw NotFoundResponse("Pic not found")
        ctx.header("pic_width", entity.tagData.width.toString())
        ctx.header("pic_height", entity.tagData.height.toString())
        ctx.header("pic_channel", entity.tagData.channelCount.toString())
        ctx.contentType("application/octet-stream")
        ctx.result(FilePathUtil.readFromFilePath(entity.filePath))
    }

    val modelResolveHandler = Handler { ctx ->
        val userId = ctx.getUserId()
        val modelId = ctx.pathParam<Int>("modelId").get()

        val entity = database.sequenceOf(ModelInfos)
            .find { (it.userId eq userId) and (it.modelId eq modelId) } ?: throw NotFoundResponse("Model not found")
        if (entity.filePath == null)
            throw NotFoundResponse("Model file not found")
        ctx.contentType("application/octet-stream")
        ctx.result(FilePathUtil.readFromFilePath(entity.filePath!!))
    }
}