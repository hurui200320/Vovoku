package info.skyblond.vovoku.backend.handler.user

import info.skyblond.vovoku.backend.database.DatabaseUtil
import info.skyblond.vovoku.backend.database.ModelInfos
import info.skyblond.vovoku.backend.database.PictureTags
import info.skyblond.vovoku.backend.handler.getPage
import info.skyblond.vovoku.backend.handler.getUserId
import info.skyblond.vovoku.commons.dl4j.ModelPrototype
import info.skyblond.vovoku.commons.models.Page
import io.javalin.http.Handler
import io.javalin.http.InternalServerErrorResponse
import io.javalin.http.NotFoundResponse
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import org.slf4j.LoggerFactory

object UserModelHandler {
    private val logger = LoggerFactory.getLogger(UserModelHandler::class.java)
    private val database = DatabaseUtil.database

    val deleteModelHandler = Handler { ctx -> TODO() }
    val getOneModelHandler = Handler { ctx -> TODO() }
    val requestNewModelHandler = Handler { ctx -> TODO() }

    val listModelHandler = Handler { ctx ->
        val userId = ctx.getUserId()
        val page = ctx.getPage()

        ctx.json(
            database.sequenceOf(ModelInfos)
            .filter { it.userId eq userId }
            .sortedBy { it.modelId }
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