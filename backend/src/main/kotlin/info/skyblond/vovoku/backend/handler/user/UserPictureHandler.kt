package info.skyblond.vovoku.backend.handler.user

import info.skyblond.vovoku.backend.config.ConfigUtil
import info.skyblond.vovoku.backend.database.DatabaseUtil
import info.skyblond.vovoku.backend.database.PictureTag
import info.skyblond.vovoku.backend.database.PictureTags
import info.skyblond.vovoku.backend.handler.getPage
import info.skyblond.vovoku.backend.handler.getUserId
import info.skyblond.vovoku.commons.FilePathUtil
import info.skyblond.vovoku.commons.models.PictureTagEntry
import io.javalin.http.BadRequestResponse
import io.javalin.http.Handler
import io.javalin.http.NotFoundResponse
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

object UserPictureHandler {
    private val logger = LoggerFactory.getLogger(UserPictureHandler::class.java)
    private val database = DatabaseUtil.database

    val updateTagNumberHandler = Handler { ctx ->
        val userId = ctx.getUserId()
        val tagId = ctx.pathParam<Int>("picTagId").get()
        val newTag = ctx.formParam<Int>("newTag").check({ it in 0..9 }).getOrNull()
        val usedForTrain = ctx.formParam<Boolean>("train").getOrNull()
        val datasetName = ctx.formParam<String>("folder").getOrNull()

        val entity = database.sequenceOf(PictureTags)
            .find { (it.userId eq userId) and (it.tagId eq tagId) }
            .let { it ?: throw  NotFoundResponse("Pic not found") }

        entity.update(newTag, usedForTrain, datasetName)
        ctx.json(entity.toPojo(ctx))
    }

    val deletePicHandler = Handler { ctx ->
        val userId = ctx.getUserId()
        val tagId = ctx.pathParam<Int>("picTagId").get()

        database.sequenceOf(PictureTags)
            .find { (it.userId eq userId) and (it.tagId eq tagId) }
            .let { it ?: throw  NotFoundResponse("Pic not found") }
            .delete()

        ctx.status(204)
    }


    val getOnePicHandler = Handler { ctx ->
        val userId = ctx.getUserId()
        val tagId = ctx.pathParam<Int>("picTagId").get()

        ctx.json(
            database.sequenceOf(PictureTags)
                .find { (it.userId eq userId) and (it.tagId eq tagId) }
                .let { it ?: throw  NotFoundResponse("Pic not found") }
                .toPojo(ctx)
        )
    }

    val uploadNewPicHandler = Handler { ctx ->
        val userId = ctx.getUserId()

        val width = ctx.formParam<Int>("width").check({ it > 0 }).get()
        val height = ctx.formParam<Int>("height").check({ it > 0 }).get()
        val channelCount = ctx.formParam<Int>("channel").check({ it > 0 }).get()
        val tag = ctx.formParam<Int>("tag").check({ it in 0..9 }).get()
        val usedForTrain = ctx.formParam<Boolean>("train").get()
        val datasetName = ctx.formParam<String>("folder").get()
        val file = try {
            ctx.uploadedFile("data") ?: throw BadRequestResponse("Upload data is required")
        } catch (e: Exception) {
            throw BadRequestResponse("Error when handling uploaded file: ${e.localizedMessage}")
        }
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
        val entity = PictureTag {
            this.filePath = filePath
            this.userId = userId
            this.tagData = tagEntry
            this.usedForTrain = usedForTrain
            this.folderName = datasetName
        }
        database.sequenceOf(PictureTags)
            .add(entity)
        ctx.json(entity.toPojo(ctx))
    }

    val listPicHandler = Handler { ctx ->
        val userId = ctx.getUserId()
        val page = ctx.getPage()

        val usedForTrain = ctx.queryParam<Boolean>("train").getOrNull()
        val datasetName = ctx.queryParam<String>("folder").getOrNull()

        ctx.json(database.sequenceOf(PictureTags)
            .filter { it.userId eq userId }
            .let { sequence ->
                if (usedForTrain != null) {
                    sequence.filter { it.usedForTrain eq usedForTrain }
                } else {
                    sequence
                }
            }
            .let { sequence ->
                if (datasetName != null) {
                    sequence.filter { it.folderName eq datasetName }
                } else {
                    sequence
                }
            }
            .sortedBy { it.tagId }
            .drop(page.offset)
            .take(page.limit)
            .map {
                it.toPojo(ctx)
            }
        )
    }
}