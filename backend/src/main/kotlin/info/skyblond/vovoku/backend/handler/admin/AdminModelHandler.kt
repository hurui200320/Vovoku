package info.skyblond.vovoku.backend.handler.admin

import info.skyblond.vovoku.backend.database.DatabaseUtil
import info.skyblond.vovoku.backend.database.ModelInfo
import info.skyblond.vovoku.backend.database.ModelInfos
import info.skyblond.vovoku.commons.models.AdminModelRequest
import info.skyblond.vovoku.commons.models.ModelTrainingStatus
import info.skyblond.vovoku.commons.models.Page
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import org.slf4j.LoggerFactory
import java.sql.Timestamp

object AdminModelHandler : AdminCRUDHandler<AdminModelRequest>(AdminModelRequest::class.java) {
    private val logger = LoggerFactory.getLogger(AdminModelHandler::class.java)
    private val database = DatabaseUtil.database

    private fun query(
        request: AdminModelRequest,
        paging: Boolean = true
    ): EntitySequence<ModelInfo, ModelInfos> {
        return database.sequenceOf(ModelInfos)
            .let { sequence ->
                if (request.pojo.modelId != null) {
                    sequence.filter { it.modelId eq request.pojo.modelId!! }
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
            .sortedBy { it.modelId }
            .let {
                if (paging) {
                    it.drop(request.page?.offset ?: Page(null, null).offset)
                        .take(request.page?.limit ?: Page(null, null).limit)
                } else {
                    it
                }
            }
    }

    override fun handleCreate(ctx: Context, request: AdminModelRequest) {
        throw NotImplementedError("Admin cannot create new models")
    }

    override fun handleRead(ctx: Context, request: AdminModelRequest) {
        val result = query(request).map { it.toPojo() }
        ctx.json(result)
    }

    override fun handleUpdate(ctx: Context, request: AdminModelRequest) {
        val query = query(request, false)
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

    override fun handleDelete(ctx: Context, request: AdminModelRequest) {
        val result = query(request, false)
            .map {
                it.delete()
                it.toPojo()
            }
        ctx.json(result)
    }
}