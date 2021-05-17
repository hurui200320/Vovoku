package info.skyblond.vovoku.backend.handler.user

import info.skyblond.vovoku.backend.database.DatabaseUtil
import info.skyblond.vovoku.backend.database.ModelInfo
import info.skyblond.vovoku.backend.database.ModelInfos
import info.skyblond.vovoku.backend.database.PictureTags
import info.skyblond.vovoku.backend.handler.getPage
import info.skyblond.vovoku.backend.handler.getUserId
import info.skyblond.vovoku.commons.dl4j.ModelPrototype
import info.skyblond.vovoku.commons.models.ModelCreateInfo
import info.skyblond.vovoku.commons.models.ModelCreateRequest
import info.skyblond.vovoku.commons.models.ModelTrainingStatus
import io.javalin.http.BadRequestResponse
import io.javalin.http.Handler
import io.javalin.http.NotFoundResponse
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import org.slf4j.LoggerFactory
import java.sql.Timestamp
import java.time.Instant

object UserModelHandler {
    private val logger = LoggerFactory.getLogger(UserModelHandler::class.java)
    private val database = DatabaseUtil.database

    val requestNewModelHandler = Handler { ctx ->
        val userId = ctx.getUserId()
        // note we have to discard time and make our new time
        val body = ctx.bodyAsClass(ModelCreateRequest::class.java)

        // gathering pic id
        val trainingPicsId = mutableListOf<Int>()
        val testingPicsId = mutableListOf<Int>()
        database.sequenceOf(PictureTags)
            .filter { (it.userId eq userId) and (it.folderName eq body.datasetName) }
            .forEach {
                if (it.usedForTrain)
                    trainingPicsId.add(it.tagId)
                else
                    testingPicsId.add(it.tagId)
            }

        if (trainingPicsId.isEmpty())
            throw BadRequestResponse("Training set is empty")
        if (testingPicsId.isEmpty())
            throw BadRequestResponse("Testing set is empty")

        val createInfo = ModelCreateInfo(
            Timestamp.from(Instant.now()),
            body.trainingParameter,
            trainingPicsId,
            testingPicsId,
            ModelPrototype.getPrototype(body.trainingParameter.modelIdentifier)?.descriptor
                ?: throw BadRequestResponse("Prototype not found")
        )
        val emptyTrainingInfo = arrayOf(
            Triple(
                ModelTrainingStatus.INITIALIZING,
                Timestamp.from(Instant.now()),
                "Task created by user"
            )
        )

        val entity = ModelInfo {
            this.userId = userId
            this.createInfo = createInfo
            this.trainingInfo = emptyTrainingInfo
            this.lastStatus = ModelTrainingStatus.INITIALIZING.name
        }
        database.sequenceOf(ModelInfos).add(entity)
        ctx.json(entity.toPojo())
    }

    val deleteModelHandler = Handler { ctx ->
        val userId = ctx.getUserId()
        val modelId = ctx.pathParam<Int>("modelId").get()

        database.sequenceOf(ModelInfos)
            .find { (it.userId eq userId) and (it.modelId eq modelId) }
            .let { it ?: throw  NotFoundResponse("Model not found") }
            .delete()

        ctx.status(204)
    }
    val getOneModelHandler = Handler { ctx ->
        val userId = ctx.getUserId()
        val modelId = ctx.pathParam<Int>("modelId").get()

        ctx.json(
            database.sequenceOf(ModelInfos)
                .find { (it.userId eq userId) and (it.modelId eq modelId) }
                .let { it ?: throw  NotFoundResponse("Model not found") }
                .toPojo(ctx)
        )
    }

    val listModelHandler = Handler { ctx ->
        val userId = ctx.getUserId()
        val page = ctx.getPage()

        val lastStatus = ctx.queryParam<String>("lastStatus").getOrNull()

        ctx.json(
            database.sequenceOf(ModelInfos)
                .filter { it.userId eq userId }
                .let { sequence ->
                    if (lastStatus != null) {
                        sequence.filter { it.lastStatus eq lastStatus }
                    } else {
                        sequence
                    }
                }
                .sortedByDescending { it.modelId }
                .drop(page.offset)
                .take(page.limit)
                .map {
                    it.toPojo(ctx)
                }
        )
    }

    val getOnePrototypeHandler = Handler { ctx ->
        val typeId = ctx.pathParam<String>("typeId").get()
        val prototype = ModelPrototype.getPrototype(typeId) ?: throw NotFoundResponse("Prototype not found")
        ctx.json(prototype.descriptor)
    }
    val listPrototypeHandler = Handler { ctx ->
        val page = ctx.getPage()

        ctx.json(
            ModelPrototype.nameToPrototype.values
                .map { it.descriptor }
                .drop(page.offset)
                .take(page.limit)
        )
    }
}