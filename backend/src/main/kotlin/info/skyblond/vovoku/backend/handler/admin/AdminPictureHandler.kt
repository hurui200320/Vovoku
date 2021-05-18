package info.skyblond.vovoku.backend.handler.admin

import info.skyblond.vovoku.backend.database.DatabaseUtil
import info.skyblond.vovoku.backend.database.PictureTag
import info.skyblond.vovoku.backend.database.PictureTags
import info.skyblond.vovoku.commons.models.AdminRequest
import info.skyblond.vovoku.commons.models.Page
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
        forTrain: Boolean?,
        folderName: String?,
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
                if (forTrain != null) {
                    sequence.filter { it.usedForTrain eq forTrain }
                } else {
                    sequence
                }
            }
            .let { sequence ->
                if (folderName != null) {
                    sequence.filter { it.folderName eq folderName }
                } else {
                    sequence
                }
            }
            .sortedBy { it.tagId }
            .let {
                if (page != null) {
                    it.drop(page.offset())
                        .take(page.limit())
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
            request.typedParameter(AdminRequest.TAG_FOR_TRAIN_KEY),
            request.typedParameter(AdminRequest.TAG_FOLDER_NAME_KEY),
            request.page
        ).map { it.toPojo() }
        ctx.json(result)
    }

    override fun handleUpdate(ctx: Context, request: AdminRequest) {
        throw NotImplementedError("Admin cannot update pictures")
    }

    override fun handleDelete(ctx: Context, request: AdminRequest) {
        val result = query(
            request.typedParameter(AdminRequest.TAG_ID_KEY),
            null, null, null, null
        )
            .map {
                it.delete()
                it.toPojo()
            }
        ctx.json(result)
    }

}