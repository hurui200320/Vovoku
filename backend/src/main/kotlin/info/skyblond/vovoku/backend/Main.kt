package info.skyblond.vovoku.backend

import com.fasterxml.jackson.databind.ObjectMapper
import info.skyblond.vovoku.backend.config.ConfigUtil
import info.skyblond.vovoku.backend.handler.admin.AdminCRUDHandler
import info.skyblond.vovoku.backend.handler.admin.AdminUserHandler
import info.skyblond.vovoku.commons.*
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post
import io.javalin.core.util.Header
import io.javalin.http.UnauthorizedResponse
import io.javalin.plugin.json.JavalinJackson
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPubSub
import java.security.spec.RSAPublicKeySpec
import kotlin.random.Random


fun main() {
    val logger = LoggerFactory.getLogger("Application")

    val apiConfig = ConfigUtil.config.api

    JavalinJackson.configure(JacksonJsonUtil.jsonMapper)
    val app = Javalin.create { config ->
        config.enableCorsForAllOrigins()
    }.start(apiConfig.host, apiConfig.port)


    app.before("/user/*") { ctx ->
        TODO()
        // Verify token, refresh if validate
    }

    val pubKey = apiConfig.publicKeySpec.restore<RSAPublicKeySpec>()
    val encryptionIdentifierKey = "result_encrypt"
    app.before("/admin/*") { ctx ->
        val signHeader = ctx.header(Header.AUTHORIZATION) ?: throw UnauthorizedResponse()
        if (!CryptoUtil.verifyWithPublicKey(ctx.body(), signHeader, pubKey))
            throw UnauthorizedResponse()
    }

    app.after("/admin/*") { ctx ->
        if (ctx.attribute<Boolean>(AdminCRUDHandler.ENCRYPTION_IDENTIFIER_KEY) == true) {
            val result = ctx.resultString() ?: return@after
            logger.info("Processing result string: $result")
            ctx.result(CryptoUtil.encryptWithPublicKey(result, pubKey))
        }
    }

    app.routes {
        // login(request token)
        path("public") {
            path("token") {


            }
        }
        path("user") {

        }
        path("admin") {
            path("users") {
                post(AdminUserHandler)
            }
            path("pictures") {
                post { ctx ->
                }
            }
            path("models") {
                post { ctx ->
                }
            }

        }
    }

    app.get("/") { ctx ->
        ctx.json(object {
            val x = 10
        })
    }

    logger.info("????")


    val jedisSub = object : JedisPubSub() {
        override fun onMessage(channel: String?, message: String?) {
            logger.info("Get message from channel $channel: $message")
            // TODO with message
        }
    }

    Thread {
        Jedis("127.0.0.1").also {
            it.subscribe(jedisSub, RedisTaskReportChannel)
            it.quit()
        }
    }.start()


    val jedis = Jedis("127.0.0.1")
    jedis.publish(
        RedisTaskDistributionChannel,
        JacksonJsonUtil.objectToJson(
            TrainingTaskDistro(
                Random.nextInt(),
                ModelTrainingParameter(
                    128,
                    3,
                    28,
                    28,
                    1000,
                    10,
                    ModelTrainingParameter.Updater.Nesterovs,
                    listOf(0.1, 0.9),
                    1e-4,
                    123L
                ),
                "file://D:\\Git\\github\\Vovoku\\worker\\train_image_byte.bin",
                "file://D:\\Git\\github\\Vovoku\\worker\\train_label_byte.bin",
                60000,
                "file://D:\\Git\\github\\Vovoku\\worker\\test_image_byte.bin",
                "file://D:\\Git\\github\\Vovoku\\worker\\test_label_byte.bin",
                10000,
                "",
                "file://D:\\Git\\github\\Vovoku\\test.model",
                ""
            )
        )
    )


//
//    database.sequenceOf(Users)
//        .add(User {
//            username = "hurui"
//            password = CryptoUtil.md5("some password")
//        })
//
//    println(database.sequenceOf(Users)
//        .find { it.username eq "hurui" })


}

