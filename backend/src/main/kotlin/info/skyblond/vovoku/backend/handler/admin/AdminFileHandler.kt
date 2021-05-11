package info.skyblond.vovoku.backend.handler.admin

import info.skyblond.vovoku.backend.database.DatabaseUtil
import info.skyblond.vovoku.backend.database.ModelInfos
import info.skyblond.vovoku.backend.database.PictureTags
import info.skyblond.vovoku.commons.FilePathUtil
import info.skyblond.vovoku.commons.models.AdminRequest
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.slf4j.LoggerFactory

object AdminFileHandler : AdminCRUDHandler<AdminRequest>(AdminRequest::class.java) {
    private val logger = LoggerFactory.getLogger(AdminFileHandler::class.java)
    private val database = DatabaseUtil.database
    override fun handleCreate(ctx: Context, request: AdminRequest) {
        throw NotImplementedError("Create file from file path is not supported")
    }

    override fun handleRead(ctx: Context, request: AdminRequest) {
        val fileType = request.typedParameter<String>(AdminRequest.FILE_TYPE_KEY)
            ?: throw BadRequestResponse("File type required")
        val fileId = request.typedParameter<Int>(AdminRequest.FILE_ID_KEY)
            ?: throw BadRequestResponse("File id required")

        when (fileType) {
            AdminRequest.FILE_TYPE_VALUE_MODEL -> {
                val entity = database.sequenceOf(ModelInfos)
                    .find { it.modelId eq fileId } ?: throw NotFoundResponse("Pic not found")
                if (entity.filePath == null)
                    throw NotFoundResponse("Model file not found")
                ctx.contentType("application/octet-stream")
                ctx.result(FilePathUtil.readFromFilePath(entity.filePath!!))
            }
            AdminRequest.FILE_TYPE_VALUE_PICTURE -> {
                val entity = database.sequenceOf(PictureTags)
                    .find { it.tagId eq fileId } ?: throw NotFoundResponse("Pic not found")
                ctx.header("pic_width", entity.tagData.width.toString())
                ctx.header("pic_height", entity.tagData.height.toString())
                ctx.header("pic_channel", entity.tagData.channelCount.toString())
                ctx.contentType("application/octet-stream")
                ctx.result(FilePathUtil.readFromFilePath(entity.filePath))
            }
            else -> throw BadRequestResponse("Unsupported file type")
        }
    }

    override fun handleUpdate(ctx: Context, request: AdminRequest) {
        throw NotImplementedError("Update file from file path is not supported")
    }

    override fun handleDelete(ctx: Context, request: AdminRequest) {
        throw NotImplementedError("Delete file from file path is not supported")
    }

}