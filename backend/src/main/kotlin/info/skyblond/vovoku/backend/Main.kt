package info.skyblond.vovoku.backend

import info.skyblond.vovoku.backend.config.ConfigUtil
import info.skyblond.vovoku.backend.handler.admin.AdminCRUDHandler
import info.skyblond.vovoku.backend.handler.admin.AdminModelHandler
import info.skyblond.vovoku.backend.handler.admin.AdminPictureHandler
import info.skyblond.vovoku.backend.handler.admin.AdminUserHandler
import info.skyblond.vovoku.backend.handler.user.UserAccountHandler
import info.skyblond.vovoku.backend.handler.user.UserPublicApiHandler
import info.skyblond.vovoku.backend.redis.RedisUtil
import info.skyblond.vovoku.commons.CryptoUtil
import info.skyblond.vovoku.commons.JacksonJsonUtil
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.core.util.Header
import io.javalin.http.UnauthorizedResponse
import io.javalin.plugin.json.JavalinJackson
import org.slf4j.LoggerFactory
import java.security.spec.RSAPublicKeySpec


fun main() {
    val logger = LoggerFactory.getLogger("Application")

    val apiConfig = ConfigUtil.config.api

    JavalinJackson.configure(JacksonJsonUtil.jsonMapper)
    val app = Javalin.create { config ->
        config.enableCorsForAllOrigins()
    }.start(apiConfig.host, apiConfig.port)


    app.before("/user/*") { ctx ->
        val token = ctx.header(Header.AUTHORIZATION) ?: throw UnauthorizedResponse()
        val userId = RedisUtil.queryToken(token) ?: throw UnauthorizedResponse()
        ctx.attribute(UserPublicApiHandler.USER_ID_ATTR_NAME, userId)
    }

    val pubKey = apiConfig.publicKeySpec.restore<RSAPublicKeySpec>()
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

    // TODO 使用模型推理放到用户客户端来做，下载模型到本地，加载、推断

    app.routes {
        // login(request token)
        path("public") {
            path("token") {
                post(UserPublicApiHandler.userTokenHandler)
            }
        }
        path("user") {
            // 用户接口更注重每个接口实现一个功能
            path("account"){
                path("whoami"){
                    get(UserAccountHandler.whoAmIHandler)
                }
                path("delete"){
                    delete(UserAccountHandler.deleteHandler)
                }
            }
            path("picture"){


            }


            // TODO 上传文件，数据部分小于4KB则写入数据库 base64://${data}
            //      否则写入文件 file://...path///
            //      只写入字节数据，一个字节一个单色亮度值（0-黑，255-亮）
            //      元数据存入TagInfo：宽、高、通道数
        }
        path("admin") {
            // Admin接口约等于数据库操作的封装
            path("users") {
                post(AdminUserHandler)
            }
            path("pictures") {
                post(AdminPictureHandler)
            }
            path("models") {
                post(AdminModelHandler)
            }
        }
    }

    logger.info("Main thread done")
}

