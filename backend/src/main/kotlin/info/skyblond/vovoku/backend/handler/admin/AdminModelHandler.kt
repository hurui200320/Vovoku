package info.skyblond.vovoku.backend.handler.admin

import info.skyblond.vovoku.backend.database.DatabaseUtil
import info.skyblond.vovoku.backend.database.ModelInfo
import info.skyblond.vovoku.backend.database.ModelInfos
import info.skyblond.vovoku.commons.models.AdminRequest
import info.skyblond.vovoku.commons.models.ModelTrainingStatus
import info.skyblond.vovoku.commons.models.Page
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import org.slf4j.LoggerFactory
import java.sql.Timestamp

object AdminModelHandler : AdminCRUDHandler<AdminRequest>(AdminRequest::class.java) {
    private val logger = LoggerFactory.getLogger(AdminModelHandler::class.java)
    private val database = DatabaseUtil.database

    private fun query(
        modelId: Int?,
        userId: Int?,
        filePath: String?,
        page: Page?
    ): EntitySequence<ModelInfo, ModelInfos> {
        return database.sequenceOf(ModelInfos)
            .let { sequence ->
                if (modelId != null) {
                    sequence.filter { it.modelId eq modelId }
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
            .sortedBy { it.modelId }
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
        throw NotImplementedError("Admin cannot create new models")
    }

    override fun handleRead(ctx: Context, request: AdminRequest) {
        val result = query(
            request.typedParameter(AdminRequest.MODEL_ID_KEY),
            request.typedParameter(AdminRequest.USER_ID_KEY),
            request.typedParameter(AdminRequest.FILE_PATH_KEY),
            request.page
        ).map { it.toPojo() }
        ctx.json(result)
    }

    /**
     * Terminate training by id
     * */
    override fun handleUpdate(ctx: Context, request: AdminRequest) {
        val query = query(
            request.typedParameter(AdminRequest.MODEL_ID_KEY),
            null, null,
            null
        )
        if (query.totalRecords != 1) {
            throw BadRequestResponse("Cannot update multiple entity at once")
        }
        // only terminate training
        val entity = query.first()
        entity.trainingInfo.statusList.let {
            if (it.lastOrNull()?.first == ModelTrainingStatus.TERMINATED)
                throw BadRequestResponse("Cannot terminate a terminated task")
            it.add(
                Triple(ModelTrainingStatus.TERMINATED, Timestamp(System.currentTimeMillis()), "Terminated by admin")
            )
        }
        entity.flushChanges()
        ctx.json(entity.toPojo())
    }

    override fun handleDelete(ctx: Context, request: AdminRequest) {
        val result = query(
            request.typedParameter(AdminRequest.MODEL_ID_KEY),
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