package info.skyblond.vovoku.backend.handler.user

import info.skyblond.vovoku.backend.config.ConfigUtil
import info.skyblond.vovoku.backend.database.DatabaseUtil
import info.skyblond.vovoku.backend.database.PictureTag
import info.skyblond.vovoku.backend.database.PictureTags
import info.skyblond.vovoku.commons.FilePathUtil
import info.skyblond.vovoku.commons.models.PictureTagEntry
import io.javalin.http.BadRequestResponse
import io.javalin.http.Handler
import io.javalin.http.InternalServerErrorResponse
import org.ktorm.entity.add
import org.ktorm.entity.sequenceOf
import org.ktorm.jackson.JsonSqlType
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

object UserPictureHandler {
    private val logger = LoggerFactory.getLogger(UserPictureHandler::class.java)
    private val database = DatabaseUtil.database

    val updateTagNumberHandler = Handler { ctx -> TODO() }
    val deletePicHandler = Handler { ctx -> TODO() }
    val getOnePicHandler = Handler { ctx -> TODO() }
    val uploadNewPicHandler = Handler { ctx ->
        val userId = ctx.attribute<Int>(UserPublicApiHandler.USER_ID_ATTR_NAME)
            ?: throw InternalServerErrorResponse("Cannot parse token for user id")

        val width = ctx.formParam<Int>("width").check({ it > 0 }).get()
        val height = ctx.formParam<Int>("height").check({ it > 0 }).get()
        val channelCount = ctx.formParam<Int>("channel").check({ it > 0 }).get()
        val tag = ctx.formParam<Int>("tag").check({ it in 0..9 }).get()

        val file = ctx.uploadedFile("data") ?: throw BadRequestResponse("Upload data is required")
        val filePath = if (file.size > 16 * 1024L) {
            // if file bigger than 16K, store it into file
            val folder = File(ConfigUtil.uploadBaseDir, userId.toString())
            folder.mkdirs()
            val targetFile = File(folder, "${System.currentTimeMillis()}.bin")
            val count = FilePathUtil.writeToFilePath(
                "file://${targetFile.canonicalPath}"
            ).use { outputStream ->
                file.content.use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            if (count != file.size) {
                logger.warn("Upload file with ${file.size} bytes, but copied $count bytes: ${targetFile.canonicalPath}")
            }
            "file://${targetFile.canonicalPath}"
        } else {
            // less than 16K, write it as base64
            "base64://" + Base64.getEncoder().encodeToString(file.content.readBytes())
        }
        val tagEntry = PictureTagEntry(width, height, channelCount, tag)
        val entry = PictureTag {
            this.filePath = filePath
            this.userId = userId
            this.tagData = tagEntry
        }
        database.sequenceOf(PictureTags)
            .add(entry)
        ctx.json(entry.toPojo())
    }
    val listPicHandler = Handler { ctx -> TODO() }
}