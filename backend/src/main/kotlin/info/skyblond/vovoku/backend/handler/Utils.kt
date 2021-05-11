package info.skyblond.vovoku.backend.handler

import info.skyblond.vovoku.backend.handler.user.UserPublicApiHandler
import info.skyblond.vovoku.commons.models.Page
import io.javalin.http.Context
import io.javalin.http.InternalServerErrorResponse

fun Context.getUserId(): Int{
    return this.attribute<Int>(UserPublicApiHandler.USER_ID_ATTR_NAME)
        ?: throw InternalServerErrorResponse("Cannot parse token for user id")
}

fun Context.getPage(): Page {
    return Page(
        this.queryParam<Int>("page").check({ it > 0 }).getOrNull(),
        this.queryParam<Int>("size").check({ it > 0 }).getOrNull()
    )
}