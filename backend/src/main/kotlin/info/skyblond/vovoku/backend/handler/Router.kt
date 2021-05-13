package info.skyblond.vovoku.backend.handler

import info.skyblond.vovoku.backend.handler.admin.*
import info.skyblond.vovoku.backend.handler.user.*
import info.skyblond.vovoku.backend.redis.RedisUtil
import info.skyblond.vovoku.commons.CryptoUtil
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder
import io.javalin.core.util.Header
import io.javalin.http.UnauthorizedResponse
import java.util.*
import javax.crypto.SecretKey

fun setInterceptor(app: Javalin, aesKey: SecretKey){

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
}

fun setRouter(app: Javalin){
    app.routes {
        // login(request token)
        ApiBuilder.path("public") {
            ApiBuilder.path("token") {
                ApiBuilder.post(UserPublicApiHandler.userTokenHandler)
            }
        }
        ApiBuilder.path("user") {
            // 用户接口更注重每个接口实现一个功能
            ApiBuilder.path("account") {
                ApiBuilder.path("whoami") {
                    ApiBuilder.get(UserAccountHandler.whoAmIHandler)
                }
                ApiBuilder.path("delete") {
                    ApiBuilder.delete(UserAccountHandler.deleteHandler)
                }
            }
            ApiBuilder.path("picture") {
                ApiBuilder.path(":picTagId") {
                    // update tag number
                    ApiBuilder.put(UserPictureHandler.updateTagNumberHandler)
                    // delete pic
                    ApiBuilder.delete(UserPictureHandler.deletePicHandler)
                    // query one pic by id
                    ApiBuilder.get(UserPictureHandler.getOnePicHandler)
                }
                // upload new pic
                ApiBuilder.post(UserPictureHandler.uploadNewPicHandler)
                // list user pic
                ApiBuilder.get(UserPictureHandler.listPicHandler)
            }
            ApiBuilder.path("model") {
                ApiBuilder.path(":modelId") {
                    // delete model
                    ApiBuilder.delete(UserModelHandler.deleteModelHandler)
                    // query one model by id
                    ApiBuilder.get(UserModelHandler.getOneModelHandler)
                }
                // request training new model
                ApiBuilder.post(UserModelHandler.requestNewModelHandler)
                // list user models
                ApiBuilder.get(UserModelHandler.listModelHandler)

            }
            ApiBuilder.path("prototype") {
                ApiBuilder.path(":typeId") {
                    // list current available model prototypes
                    ApiBuilder.get(UserModelHandler.getOnePrototypeHandler)
                }
                // list current available model prototypes
                ApiBuilder.get(UserModelHandler.listPrototypeHandler)
            }
            ApiBuilder.path("file") {
                // 接口需要将 file:// 的路径转换为http可达的路径
                // 该接口提供对应数据的交互功能：仅下载
                ApiBuilder.path("pic") {
                    ApiBuilder.path(":picId") {
                        ApiBuilder.get(UserFileHandler.picResolveHandler)
                    }
                }
                ApiBuilder.path("model") {
                    ApiBuilder.path(":modelId") {
                        ApiBuilder.get(UserFileHandler.modelResolveHandler)
                    }
                }
            }
        }
        ApiBuilder.path("admin") {
            // Admin接口约等于数据库操作的封装
            ApiBuilder.path("users") {
                ApiBuilder.post(AdminUserHandler)
            }
            ApiBuilder.path("pictures") {
                ApiBuilder.post(AdminPictureHandler)
            }
            ApiBuilder.path("models") {
                ApiBuilder.post(AdminModelHandler)
            }
            ApiBuilder.path("files") {
                ApiBuilder.post(AdminFileHandler)
            }
        }
    }
}