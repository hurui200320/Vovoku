package info.skyblond.vovoku.backend

import info.skyblond.vovoku.backend.config.ConfigUtil
import info.skyblond.vovoku.backend.handler.admin.*
import info.skyblond.vovoku.backend.handler.user.*
import info.skyblond.vovoku.backend.redis.RedisUtil
import info.skyblond.vovoku.commons.CryptoUtil
import info.skyblond.vovoku.commons.JacksonJsonUtil
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.core.util.Header
import io.javalin.http.UnauthorizedResponse
import io.javalin.plugin.json.JavalinJackson
import org.slf4j.LoggerFactory
import java.util.*
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter


fun main() {
    val logger = LoggerFactory.getLogger("Application")

    val apiConfig = ConfigUtil.config.api
    val aesKey = SecretKeySpec(DatatypeConverter.parseHexBinary(apiConfig.adminAesKey), "AES")

    JavalinJackson.configure(JacksonJsonUtil.jsonMapper)
    val app = Javalin.create { config ->
        config.enableCorsForAllOrigins()
    }.start(apiConfig.host, apiConfig.port)

    app.before("/user/*") { ctx ->
        val token = ctx.header(Header.AUTHORIZATION) ?: throw UnauthorizedResponse("Token required")
        val userId = RedisUtil.queryToken(token) ?: throw UnauthorizedResponse("Invalidate token")
        ctx.attribute(UserPublicApiHandler.USER_ID_ATTR_NAME, userId)
    }

    app.before("/admin/*") { ctx ->
        val signHeader = ctx.header(Header.AUTHORIZATION) ?: throw UnauthorizedResponse("Signature required")
        val md5 = CryptoUtil.md5(ctx.body()).toLowerCase()
        if (CryptoUtil.aesDecrypt(signHeader, aesKey).toLowerCase() != md5)
            throw UnauthorizedResponse("Invalidate signature")
    }

    app.after("/admin/*") { ctx ->
        if (ctx.attribute<Boolean>(AdminCRUDHandler.ENCRYPTION_IDENTIFIER_KEY) == true) {
            val bytes = ctx.resultStream()?.readBytes() ?: return@after
            ctx.result(
                Base64.getUrlEncoder().encodeToString(
                    CryptoUtil.aesEncrypt(
                        bytes,
                        aesKey,
                        CryptoUtil.defaultIv
                    )
                )
            )
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
            path("account") {
                path("whoami") {
                    get(UserAccountHandler.whoAmIHandler)
                }
                path("delete") {
                    delete(UserAccountHandler.deleteHandler)
                }
            }
            path("picture") {
                path(":picTagId") {
                    // update tag number
                    put(UserPictureHandler.updateTagNumberHandler)
                    // delete pic
                    delete(UserPictureHandler.deletePicHandler)
                    // query one pic by id
                    get(UserPictureHandler.getOnePicHandler)
                }
                // upload new pic
                post(UserPictureHandler.uploadNewPicHandler)
                // list user pic
                get(UserPictureHandler.listPicHandler)
            }
            path("model") {
                path(":modelId") {
                    // delete model
                    delete(UserModelHandler.deleteModelHandler)
                    // query one model by id
                    get(UserModelHandler.getOneModelHandler)
                }
                // request training new model
                post(UserModelHandler.requestNewModelHandler)
                // list user models
                get(UserModelHandler.listModelHandler)

            }
            path("prototype") {
                path(":typeId") {
                    // list current available model prototypes
                    get(UserModelHandler.getOnePrototypeHandler)
                }
                // list current available model prototypes
                get(UserModelHandler.listPrototypeHandler)
            }
            path("file") {
                // 接口需要将 file:// 的路径转换为http可达的路径
                // 该接口提供对应数据的交互功能：仅下载
                path("pic") {
                    path(":picId") {
                        get(UserFileHandler.picResolveHandler)
                    }
                }
                path("model") {
                    path(":modelId") {
                        get(UserFileHandler.modelResolveHandler)
                    }
                }
            }
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
            path("files") {
                post(AdminFileHandler)
            }
        }
    }

    logger.info("Main thread done")
}

