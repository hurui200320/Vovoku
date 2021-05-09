package info.skyblond.vovoku.backend.handler.user

import info.skyblond.vovoku.backend.database.DatabaseUtil
import info.skyblond.vovoku.backend.database.Users
import info.skyblond.vovoku.backend.redis.RedisUtil
import io.javalin.http.BadRequestResponse
import io.javalin.http.Handler
import io.javalin.http.UnauthorizedResponse
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.find
import me.liuwj.ktorm.entity.sequenceOf
import org.slf4j.LoggerFactory
import java.util.*

object UserPublicApiHandler {
    private val logger = LoggerFactory.getLogger(UserPublicApiHandler::class.java)
    private val database = DatabaseUtil.database

    const val USER_ID_ATTR_NAME = "USER_ID"

    // TODO 要不要用Form Param重写Admin接口，恢复数据库对象的Nullability？
    val userTokenHandler = Handler { ctx ->
        val username = ctx.formParam<String>("username").get()
        val password = ctx.formParam<String>("password").get().toLowerCase()

        val entity = database.sequenceOf(Users)
            .find { it.username eq username } ?: throw UnauthorizedResponse()

        if (entity.password.toLowerCase() != password)
            throw UnauthorizedResponse()

        var token: String
        do{
            token = UUID.randomUUID().toString()
        }while (RedisUtil.queryToken(token, false) != null)

        RedisUtil.setUserToken(entity.userId, token)
        ctx.result(token)
    }
}