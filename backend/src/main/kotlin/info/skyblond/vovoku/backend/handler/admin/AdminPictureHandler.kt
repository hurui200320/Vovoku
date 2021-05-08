package info.skyblond.vovoku.backend.handler.admin

import info.skyblond.vovoku.backend.database.DatabaseUtil
import info.skyblond.vovoku.backend.database.PictureTag
import info.skyblond.vovoku.backend.database.PictureTags
import info.skyblond.vovoku.commons.models.AdminPictureTagRequest
import info.skyblond.vovoku.commons.models.Page
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.*
import org.slf4j.LoggerFactory

object AdminPictureHandler : AdminCRUDHandler<AdminPictureTagRequest>(AdminPictureTagRequest::class.java) {
    private val logger = LoggerFactory.getLogger(AdminPictureHandler::class.java)
    private val database = DatabaseUtil.database

    private fun query(
        request: AdminPictureTagRequest,
        paging: Boolean = true
    ): EntitySequence<PictureTag, PictureTags> {
        return database.sequenceOf(PictureTags)
            .let { sequence ->
                if (request.pojo.tagId != null) {
                    sequence.filter { it.tagId eq request.pojo.tagId!! }
                } else {
                    sequence
                }
            }
            .let { sequence ->
                if (request.pojo.userId != null) {
                    sequence.filter { it.userId eq request.pojo.userId!! }
                } else {
                    sequence
                }
            }
            .let { sequence ->
                if (request.pojo.filePath != null) {
                    sequence.filter { it.filePath eq "${request.pojo.filePath!!}%" }
                } else {
                    sequence
                }
            }
            .sortedBy { it.tagId }
            .let {
                if (paging) {
                    it.drop(request.page?.offset ?: Page(null, null).offset)
                        .take(request.page?.limit ?: Page(null, null).limit)
                } else {
                    it
                }
            }
    }

    override fun handleCreate(ctx: Context, request: AdminPictureTagRequest) {
        throw NotImplementedError("Admin cannot upload pictures")
    }

    override fun handleRead(ctx: Context, request: AdminPictureTagRequest) {
        val result = query(request).map { it.toPojo() }
        ctx.json(result)
    }

    override fun handleUpdate(ctx: Context, request: AdminPictureTagRequest) {
        val query = query(request, false)
        if (query.totalRecords != 1) {
            throw BadRequestResponse("Cannot update multiple entity at once")
        }
        val entity = query.first()
        entity.update(request.pojo)
        ctx.json(entity.toPojo())
    }

    override fun handleDelete(ctx: Context, request: AdminPictureTagRequest) {
        val result = query(request, false)
            .map {
                it.delete()
                it.toPojo()
            }
        ctx.json(result)
    }

}