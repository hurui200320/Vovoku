package info.skyblond.vovoku.backend.handler.user

import info.skyblond.vovoku.backend.database.DatabaseUtil
import info.skyblond.vovoku.backend.database.Users
import info.skyblond.vovoku.backend.handler.getUserId
import info.skyblond.vovoku.commons.CryptoUtil
import io.javalin.http.BadRequestResponse
import io.javalin.http.Handler
import io.javalin.http.InternalServerErrorResponse
import io.javalin.http.UnauthorizedResponse
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.slf4j.LoggerFactory

object UserAccountHandler {
    private val logger = LoggerFactory.getLogger(UserAccountHandler::class.java)
    private val database = DatabaseUtil.database

    val whoAmIHandler = Handler { ctx ->
        val userId = ctx.getUserId()
        val entity = database.sequenceOf(Users)
            .find { it.userId eq userId } ?: throw InternalServerErrorResponse("User id not found")

        ctx.json(
            mapOf(
                "id" to entity.userId,
                "username" to entity.username,
                "passwordHash" to entity.password
            )
        )
    }


    val deleteHandler = Handler { ctx ->
        val userId = ctx.getUserId()
        val username = ctx.formParam<String>("username").get()
        val passwordRaw = ctx.formParam<String>("password_raw").get()
        val entity = database.sequenceOf(Users)
            .find { it.userId eq userId } ?: throw InternalServerErrorResponse("User id not found")

        if (entity.username != username) {
            throw BadRequestResponse("Username must be confirmed before delete account")
        }

        if (entity.password.toLowerCase() != CryptoUtil.md5(passwordRaw).toLowerCase()) {
            throw UnauthorizedResponse("Password must be confirmed before delete account")
        }

        entity.delete()

        ctx.status(204)
    }

}