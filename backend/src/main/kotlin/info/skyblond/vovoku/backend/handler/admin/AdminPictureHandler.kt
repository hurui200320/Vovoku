package info.skyblond.vovoku.backend.handler.admin

import info.skyblond.vovoku.backend.database.DatabaseUtil
import info.skyblond.vovoku.backend.database.PictureTag
import info.skyblond.vovoku.backend.database.PictureTags
import info.skyblond.vovoku.commons.models.AdminRequest
import info.skyblond.vovoku.commons.models.Page
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import org.slf4j.LoggerFactory

object AdminPictureHandler : AdminCRUDHandler<AdminRequest>(AdminRequest::class.java) {
    private val logger = LoggerFactory.getLogger(AdminPictureHandler::class.java)
    private val database = DatabaseUtil.database

    private fun query(
        tagId: Int?,
        userId: Int?,
        filePath: String?,
        page: Page?
    ): EntitySequence<PictureTag, PictureTags> {
        return database.sequenceOf(PictureTags)
            .let { sequence ->
                if (tagId != null) {
                    sequence.filter { it.tagId eq tagId }
                } else {
                    sequence
                }
            }
            .let { sequence ->
                if (userId != null) {
                    sequence.filter { it.userId eq userId }
                } else {
                    sequence
                }
            }
            .let { sequence ->
                if (filePath != null) {
                    sequence.filter { it.filePath eq "${filePath}%" }
                } else {
                    sequence
                }
            }
            .sortedBy { it.tagId }
            .let {
                if (page != null) {
                    it.drop(page.offset)
                        .take(page.limit)
                } else {
                    it
                }
            }
    }

    override fun handleCreate(ctx: Context, request: AdminRequest) {
        throw NotImplementedError("Admin cannot upload pictures")
    }

    override fun handleRead(ctx: Context, request: AdminRequest) {
        val result = query(
            request.typedParameter(AdminRequest.TAG_ID_KEY),
            request.typedParameter(AdminRequest.USER_ID_KEY),
            request.typedParameter(AdminRequest.FILE_PATH_KEY),
            request.page
        ).map { it.toPojo() }
        ctx.json(result)
    }

    /**
     * Update pic by id
     * */
    override fun handleUpdate(ctx: Context, request: AdminRequest) {
        val query = query(
            request.typedParameter(AdminRequest.TAG_ID_KEY),
            null, null,
            null
        )
        if (query.totalRecords != 1) {
            throw BadRequestResponse("Cannot update multiple entity at once")
        }
        val entity = query.first()
        entity.update(request.typedParameter(AdminRequest.TAG_DATA_KEY))
        ctx.json(entity.toPojo())
    }

    override fun handleDelete(ctx: Context, request: AdminRequest) {
        val result = query(
            request.typedParameter(AdminRequest.TAG_ID_KEY),
            request.typedParameter(AdminRequest.USER_ID_KEY),
            request.typedParameter(AdminRequest.FILE_PATH_KEY),
            null
        )
            .map {
                it.delete()
                it.toPojo()
            }
        ctx.json(result)
    }

}