package info.skyblond.vovoku.backend.handler.user

import info.skyblond.vovoku.backend.database.DatabaseUtil
import info.skyblond.vovoku.commons.dl4j.ModelPrototype
import info.skyblond.vovoku.commons.models.Page
import io.javalin.http.Handler
import io.javalin.http.NotFoundResponse
import org.slf4j.LoggerFactory

object UserModelHandler {
    private val logger = LoggerFactory.getLogger(UserModelHandler::class.java)
    private val database = DatabaseUtil.database

    val deleteModelHandler = Handler { ctx -> TODO() }
    val getOneModelHandler = Handler { ctx -> TODO() }
    val requestNewModelHandler = Handler { ctx -> TODO() }
    
    val listModelHandler = Handler { ctx -> TODO() }

    val getOnePrototypeHandler = Handler { ctx ->
        val typeId = ctx.pathParam<String>("typeId").get()
        val prototype = ModelPrototype.getPrototype(typeId) ?: throw NotFoundResponse("Prototype not found")
        ctx.json(prototype.descriptor)
    }
    val listPrototypeHandler = Handler { ctx ->
        val page = Page(
            ctx.queryParam<Int>("page").check({ it > 0 }).getOrNull(),
            ctx.queryParam<Int>("size").check({ it > 0 }).getOrNull()
        )

        ctx.json(
            ModelPrototype.nameToPrototype.values
                .map { it.descriptor }
                .drop(page.offset)
                .take(page.limit)
        )
    }
}