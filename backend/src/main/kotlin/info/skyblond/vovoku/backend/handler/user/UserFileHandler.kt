package info.skyblond.vovoku.backend.handler.user

import info.skyblond.vovoku.backend.database.DatabaseUtil
import info.skyblond.vovoku.backend.database.PictureTags
import info.skyblond.vovoku.commons.FilePathUtil
import io.javalin.http.Handler
import io.javalin.http.InternalServerErrorResponse
import io.javalin.http.NotFoundResponse
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.slf4j.LoggerFactory

object UserFileHandler {
    private val logger = LoggerFactory.getLogger(UserFileHandler::class.java)
    private val database = DatabaseUtil.database

    const val HANDLER_PATH = "/user/file/"

    val fileResolveHandler = Handler { ctx ->
        val userId = ctx.attribute<Int>(UserPublicApiHandler.USER_ID_ATTR_NAME)
            ?: throw InternalServerErrorResponse("Cannot parse token for user id")

        val tagId = ctx.pathParam<Int>("tagId").get()

        val entity = database.sequenceOf(PictureTags)
            .find { (it.userId eq userId) and (it.tagId eq tagId) } ?: throw NotFoundResponse("Pic not found")
        ctx.header("pic_width", entity.tagData.width.toString())
        ctx.header("pic_height", entity.tagData.height.toString())
        ctx.header("pic_channel", entity.tagData.channelCount.toString())
        ctx.contentType("application/octet-stream")
        ctx.result(FilePathUtil.readFromFilePath(entity.filePath))
    }
}